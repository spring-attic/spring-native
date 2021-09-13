package org.springframework.context.bootstrap.generator.infrastructure;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.PropertyDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptorFactory;
import org.springframework.context.bootstrap.generator.bean.descriptor.DefaultBeanInstanceDescriptorFactory;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ReflectionConfiguration;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.MethodDescriptor;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;
import org.springframework.util.ClassUtils;

/**
 * Handle reflection and resources metadata necessary at runtime.
 *
 * @author Stephane Nicoll
 */
public class BeanRuntimeResourcesRegistrar {

	private static final List<Class<?>> IGNORED_TYPES = List.of(Indexed.class, Component.class);

	private final BeanInstanceDescriptorFactory beanInstanceDescriptorFactory;

	public BeanRuntimeResourcesRegistrar(ConfigurableListableBeanFactory beanFactory) {
		this.beanInstanceDescriptorFactory = new DefaultBeanInstanceDescriptorFactory(beanFactory);
	}

	/**
	 * Register the reflection and resources information necessary to instantiate the
	 * bean defined by the specified {@link BeanInstanceDescriptor}.
	 * @param registry the registry to use
	 * @param descriptor the descriptor of the bean instance or handle
	 */
	public void register(NativeConfigurationRegistry registry, BeanInstanceDescriptor descriptor) {
		ReflectionConfiguration reflectionConfiguration = registry.reflection();
		addClass(reflectionConfiguration, descriptor.getUserBeanClass());
		MemberDescriptor<Executable> instanceCreator = descriptor.getInstanceCreator();
		if (instanceCreator != null) {
			addMethod(reflectionConfiguration, instanceCreator.getMember());
			findAndRegisterRelevantNativeHints(registry, instanceCreator);
		}
		for (MemberDescriptor<?> injectionPoint : descriptor.getInjectionPoints()) {
			Member member = injectionPoint.getMember();
			if (member instanceof Executable) {
				addMethod(reflectionConfiguration, (Method) member);
			}
			else if (member instanceof Field) {
				addField(reflectionConfiguration, (Field) member);
			}
		}
		for (PropertyDescriptor property : descriptor.getProperties()) {
			Method writeMethod = property.getWriteMethod();
			if (writeMethod != null) {
				addMethod(reflectionConfiguration, writeMethod);
			}
			Object value = property.getPropertyValue().getValue();
			if (value instanceof BeanDefinition) {
				register(registry, this.beanInstanceDescriptorFactory.create((BeanDefinition) value));
			}
		}
	}

	// TODO is this code in the right place - if three beans come from three creators in the same declaring class, work will be duplicated (Should be same outcome, but wasteful)
	// TODO what happens if someone has a hint whose trigger is a configuration that has no beans in it, will that hint be missed? (because no creators are recorded for that configuration)

	/**
	 * Lookup any native hints that have been declared with the declaring class of the instance creator as a trigger, then register
	 * information from those hints with the registry.
	 * @param registry the registry into which hint info should be added
	 * @param instanceCreator the instance creator whose declaring class will be used for lookup
	 */
	private void findAndRegisterRelevantNativeHints(NativeConfigurationRegistry registry, MemberDescriptor<Executable> instanceCreator) {
		ReflectionConfiguration reflectionConfiguration = registry.reflection();
		try {
			Class<?> declaringClass = instanceCreator.getMember().getDeclaringClass();
			List<HintDeclaration> hints = TypeSystem.getClassLoaderBasedTypeSystem().findHints(declaringClass.getName());
			if (hints != null) {
				for (HintDeclaration hint: hints) {
					Map<String, AccessDescriptor> dependantTypes = hint.getDependantTypes();
					for (Map.Entry<String,AccessDescriptor> entry: dependantTypes.entrySet()) {
						Class<?> keyClass = ClassUtils.forName(entry.getKey(), null);
						AccessDescriptor value = entry.getValue();
						Integer accessBits = value.getAccessBits();
						if (accessBits != 0) {
							reflectionConfiguration.forType(keyClass).withFlags(AccessBits.getFlags(accessBits));
						}
						if ((accessBits & AccessBits.RESOURCE)!=0) {
							// TODO ... need to check if types flagged with this flow through and get added to resource-config.json
						}
						for (MethodDescriptor methodDescriptor: value.getMethodDescriptors()) {
							// TODO it is such a shame to convert from the methoddescriptor back to a method that will then go back to a methoddescriptor later
							Executable method = methodDescriptor.findOnClass(keyClass);
							reflectionConfiguration.forType(keyClass).withMethods(method);
						}
						for (FieldDescriptor fieldDescriptor: value.getFieldDescriptors()) {
							Field field = keyClass.getDeclaredField(fieldDescriptor.getName());
							reflectionConfiguration.forType(keyClass).withFields(field);
						}
					}
					// TODO: what about all these from the hints, they need passing back but registry doesn't support these kinds of thing
					// If this code gets moved maybe it is easier, but if it stays here, augment the registry? (more of a configurationregistry than a reflectionregistry)
					// hint.getInitializationDescriptors();
					// hint.getJNITypes();
					// hint.getProxyDescriptors();
					// hint.getSerializationTypes();
					// hint.getOptions();
					// hint.getResourcesDescriptors();
				}
			}
		} catch (Throwable t) {
			// FIXME would like to log this...
		}
	}

	private void addClass(ReflectionConfiguration reflectionConfiguration, Class<?> type) {
		registerAnnotations(reflectionConfiguration,
				MergedAnnotations.from(type, SearchStrategy.INHERITED_ANNOTATIONS));
	}

	private void addMethod(ReflectionConfiguration reflectionConfiguration, Executable executable) {
		reflectionConfiguration.addExecutable(executable);
		registerAnnotations(reflectionConfiguration, MergedAnnotations.from(executable));
	}

	private void addField(ReflectionConfiguration reflectionConfiguration, Field field) {
		reflectionConfiguration.addField(field);
		registerAnnotations(reflectionConfiguration, MergedAnnotations.from(field));
	}

	private void registerAnnotations(ReflectionConfiguration reflectionConfiguration, MergedAnnotations annotations) {
		annotations.stream().filter(this::isRuntimeFrameworkAnnotation).forEach((ann) ->
				reflectionConfiguration.forType(ann.getType()).withFlags(Flag.allDeclaredMethods));
	}

	private boolean isRuntimeFrameworkAnnotation(MergedAnnotation<?> annotation) {
		String name = annotation.getType().getName();
		boolean candidate = name.startsWith("org.springframework.") &&
				!name.startsWith("org.springframework.context.annotation");
		return candidate && !IGNORED_TYPES.contains(annotation.getType());
	}

}

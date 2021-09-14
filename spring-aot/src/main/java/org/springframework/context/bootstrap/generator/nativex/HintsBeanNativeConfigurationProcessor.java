package org.springframework.context.bootstrap.generator.nativex;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.BeanNativeConfigurationProcessor;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ReflectionConfiguration;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.MethodDescriptor;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.util.ClassUtils;

/**
 *
 * @author Andy Clement
 */
class HintsBeanNativeConfigurationProcessor implements BeanNativeConfigurationProcessor {

	@Override
	public void process(BeanInstanceDescriptor descriptor, NativeConfigurationRegistry registry) {
		findAndRegisterRelevantNativeHints(descriptor.getUserBeanClass(), registry);
	}

	// TODO is this code in the right place - if three beans come from three creators in the same declaring class, work will be duplicated (Should be same outcome, but wasteful)
	// TODO what happens if someone has a hint whose trigger is a configuration that has no beans in it, will that hint be missed? (because no creators are recorded for that configuration)

	/**
	 * Lookup any native hints that have been declared with the declaring class of the
	 * instance creator as a trigger, then register information from those hints with the
	 * registry.
	 * @param beanType the bean type
	 * @param registry the registry into which hint info should be added
	 */
	private void findAndRegisterRelevantNativeHints(Class<?> beanType, NativeConfigurationRegistry registry) {
		ReflectionConfiguration reflectionConfiguration = registry.reflection();
		try {
			List<HintDeclaration> hints = TypeSystem.getClassLoaderBasedTypeSystem().findHints(beanType.getName());
			if (hints != null) {
				for (HintDeclaration hint : hints) {
					Map<String, AccessDescriptor> dependantTypes = hint.getDependantTypes();
					for (Map.Entry<String, AccessDescriptor> entry : dependantTypes.entrySet()) {
						Class<?> keyClass = ClassUtils.forName(entry.getKey(), null);
						AccessDescriptor value = entry.getValue();
						Integer accessBits = value.getAccessBits();
						if (accessBits != 0) {
							reflectionConfiguration.forType(keyClass).withFlags(AccessBits.getFlags(accessBits));
						}
						if ((accessBits & AccessBits.RESOURCE) != 0) {
							// TODO ... need to check if types flagged with this flow through and get added to resource-config.json
						}
						for (MethodDescriptor methodDescriptor : value.getMethodDescriptors()) {
							// TODO it is such a shame to convert from the methoddescriptor back to a method that will then go back to a methoddescriptor later
							Executable method = methodDescriptor.findOnClass(keyClass);
							reflectionConfiguration.forType(keyClass).withMethods(method);
						}
						for (FieldDescriptor fieldDescriptor : value.getFieldDescriptors()) {
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
		}
		catch (Throwable t) {
			// FIXME would like to log this...
		}
	}
}

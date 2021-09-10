package org.springframework.context.bootstrap.generator.infrastructure;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.PropertyDescriptor;
import org.springframework.context.bootstrap.generator.infrastructure.reflect.RuntimeReflectionRegistry;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.nativex.hint.Flag;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

/**
 * Handle reflection and resources metadata necessary at runtime.
 *
 * @author Stephane Nicoll
 */
public class BeanRuntimeResourcesRegistrar {

	private static List<Class<?>> IGNORED_TYPES = List.of(Indexed.class, Component.class);

	/**
	 * Register the reflection and resources information necessary to instantiate the
	 * bean defined by the specified {@link BeanInstanceDescriptor}.
	 * @param registry the registry to use
	 * @param descriptor the descriptor of the bean instance ot handle
	 */
	public void register(RuntimeReflectionRegistry registry, BeanInstanceDescriptor descriptor) {
		addClass(registry, descriptor.getUserBeanClass());
		MemberDescriptor<Executable> instanceCreator = descriptor.getInstanceCreator();
		if (instanceCreator != null) {
			addMethod(registry, instanceCreator.getMember());
		}
		for (MemberDescriptor<?> injectionPoint : descriptor.getInjectionPoints()) {
			Member member = injectionPoint.getMember();
			if (member instanceof Executable) {
				addMethod(registry, (Method) member);
			}
			else if (member instanceof Field) {
				addField(registry, (Field) member);
			}
		}
		for (PropertyDescriptor property : descriptor.getProperties()) {
			Method writeMethod = property.getWriteMethod();
			if (writeMethod != null) {
				addMethod(registry, writeMethod);
			}
		}
	}

	private void addClass(RuntimeReflectionRegistry registry, Class<?> type) {
		registerAnnotations(registry, MergedAnnotations.from(type, SearchStrategy.INHERITED_ANNOTATIONS));
	}

	private void addMethod(RuntimeReflectionRegistry registry, Executable executable) {
		registry.addMethod(executable);
		registerAnnotations(registry, MergedAnnotations.from(executable));
	}

	private void addField(RuntimeReflectionRegistry registry, Field field) {
		registry.addField(field);
		registerAnnotations(registry, MergedAnnotations.from(field));
	}

	private void registerAnnotations(RuntimeReflectionRegistry registry, MergedAnnotations annotations) {
		annotations.stream().filter(this::isRuntimeFrameworkAnnotation)
				.forEach((ann) -> registry.add(ann.getType()).withFlags(Flag.allDeclaredMethods));
	}

	private boolean isRuntimeFrameworkAnnotation(MergedAnnotation<?> annotation) {
		String name = annotation.getType().getName();
		boolean candidate = name.startsWith("org.springframework.") &&
				!name.startsWith("org.springframework.context.annotation");
		return candidate && !IGNORED_TYPES.contains(annotation.getType());
	}

}

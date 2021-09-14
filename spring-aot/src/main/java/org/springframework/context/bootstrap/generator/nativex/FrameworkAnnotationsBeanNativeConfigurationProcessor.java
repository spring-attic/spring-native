package org.springframework.context.bootstrap.generator.nativex;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.List;

import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.BeanNativeConfigurationProcessor;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ReflectionConfiguration;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.nativex.hint.Flag;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

/**
 * Register the framework annotations that are discovered on the root class and
 * injection points.
 *
 * @author Stephane Nicoll
 */
class FrameworkAnnotationsBeanNativeConfigurationProcessor implements BeanNativeConfigurationProcessor {

	private static final List<Class<?>> IGNORED_TYPES = List.of(Indexed.class, Component.class);

	@Override
	public void process(BeanInstanceDescriptor descriptor, NativeConfigurationRegistry registry) {
		ReflectionConfiguration reflectionConfiguration = registry.reflection();
		registerAnnotations(reflectionConfiguration, MergedAnnotations.from(
				descriptor.getUserBeanClass(), SearchStrategy.INHERITED_ANNOTATIONS));
		descriptor.getInjectionPoints().forEach((injectionPoint) -> {
			Member member = injectionPoint.getMember();
			if (member instanceof AnnotatedElement) {
				registerAnnotations(reflectionConfiguration, MergedAnnotations.from((AnnotatedElement) member));
			}
		});
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

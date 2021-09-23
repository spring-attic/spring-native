package org.springframework.context.bootstrap.generator.nativex;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.BeanNativeConfigurationProcessor;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.ReflectionUtils;

import static org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ReflectionConfiguration;

/**
 * Register the methods annotated with framework annotations that are discovered on the root class.
 *
 * @author Sébastien Deleuze
 */
public class FrameworkMethodsBeanNativeConfigurationProcessor implements BeanNativeConfigurationProcessor {

	private static final List<Class<?>> IGNORED_TYPES = List.of(Bean.class, EventListener.class);

	@Override
	public void process(BeanInstanceDescriptor descriptor, NativeConfigurationRegistry registry) {
		ReflectionConfiguration reflectionConfiguration = registry.reflection();
		ReflectionUtils.doWithMethods(descriptor.getUserBeanClass(), method -> this.registerMethods(reflectionConfiguration,
				MergedAnnotations.from(method, MergedAnnotations.SearchStrategy.INHERITED_ANNOTATIONS)));
	}

	private void registerMethods(ReflectionConfiguration reflectionConfiguration, MergedAnnotations annotations) {
		annotations.stream()
				.filter(this::isRuntimeFrameworkAnnotation)
				.forEach((ann) -> {
					Method method = (Method) ann.getSource();
					reflectionConfiguration.forType(method.getDeclaringClass()).withMethods(method);
		});
	}

	private boolean isRuntimeFrameworkAnnotation(MergedAnnotation<?> annotation) {
		return annotation.getType().getName().startsWith("org.springframework.")
				&& !IGNORED_TYPES.contains(annotation.getType());
	}
}

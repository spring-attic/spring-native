/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aot.context.bootstrap.generator.nativex;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.ReflectionUtils;

import static org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ReflectionConfiguration;

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
					reflectionConfiguration.forType(method.getDeclaringClass()).withExecutables(method);
		});
	}

	private boolean isRuntimeFrameworkAnnotation(MergedAnnotation<?> annotation) {
		return annotation.getType().getName().startsWith("org.springframework.")
				&& !IGNORED_TYPES.contains(annotation.getType());
	}
}

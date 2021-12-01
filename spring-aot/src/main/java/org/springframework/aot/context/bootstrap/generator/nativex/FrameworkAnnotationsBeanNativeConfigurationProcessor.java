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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ReflectionConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.nativex.hint.TypeAccess;
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
				reflectionConfiguration.forType(ann.getType()).withAccess(TypeAccess.DECLARED_METHODS));
	}

	protected boolean isRuntimeFrameworkAnnotation(MergedAnnotation<?> annotation) {
		Predicate<MergedAnnotation<?>> ignore = isConditionModelAnnotation().or(isConditionAnnotation()).or(isIgnoredType());
		return annotation.getType().getName().startsWith("org.springframework.") && !ignore.test(annotation);
	}

	private Predicate<MergedAnnotation<?>> isConditionModelAnnotation() {
		return (annotation) -> annotation.getType().getName().startsWith("org.springframework.context.annotation");
	}

	private Predicate<MergedAnnotation<?>> isConditionAnnotation() {
		return (annotation) -> MergedAnnotations.from(annotation.getType()).isPresent(Conditional.class);
	}

	private Predicate<MergedAnnotation<?>> isIgnoredType() {
		return (annotation) -> IGNORED_TYPES.contains(annotation.getType());
	}

}

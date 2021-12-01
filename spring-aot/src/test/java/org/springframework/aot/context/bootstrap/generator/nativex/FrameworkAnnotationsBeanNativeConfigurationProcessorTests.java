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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.sample.callback.AsyncConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.injection.InjectionComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FrameworkAnnotationsBeanNativeConfigurationProcessor}.
 *
 * @author Stephane Nicoll
 */
class FrameworkAnnotationsBeanNativeConfigurationProcessorTests {

	@Test
	void registerAnnotationsForClassAnnotations() {
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(AsyncConfiguration.class).build());
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies(annotation(EnableAsync.class));
	}

	@Test
	void registerAnnotationsForMethodInjectionPoint() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		Method injectionPoint = ReflectionUtils.findMethod(InjectionComponent.class, "setCounter", Integer.class);
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).withInjectionPoint(injectionPoint, false).build());
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies(annotation(Autowired.class));
	}

	@ParameterizedTest
	@ValueSource(classes = { GetMapping.class, Async.class, Scheduled.class })
	void isCandidateWithAnnotationToRetain(Class<? extends Annotation> annotationType) {
		assertThat(new FrameworkAnnotationsBeanNativeConfigurationProcessor()
				.isRuntimeFrameworkAnnotation(MergedAnnotation.of(annotationType))).isTrue();
	}

	@ParameterizedTest
	@ValueSource(classes = { Indexed.class, Component.class, Configuration.class, ConditionalOnMissingBean.class, ConditionalOnAvailableEndpoint.class })
	void isCandidateWithAnnotationToIgnore(Class<? extends Annotation> annotationType) {
		assertThat(new FrameworkAnnotationsBeanNativeConfigurationProcessor()
				.isRuntimeFrameworkAnnotation(MergedAnnotation.of(annotationType))).isFalse();
	}

	private Consumer<DefaultNativeReflectionEntry> annotation(Class<? extends Annotation> annotationType) {
		return (entry) -> {
			assertThat(entry.getType()).isEqualTo(annotationType);
			assertThat(entry.getAccess()).containsOnly(TypeAccess.DECLARED_METHODS);
			assertThat(entry.getConstructors()).isEmpty();
			assertThat(entry.getMethods()).isEmpty();
			assertThat(entry.getFields()).isEmpty();
		};
	}

	private NativeConfigurationRegistry register(BeanInstanceDescriptor descriptor) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new FrameworkAnnotationsBeanNativeConfigurationProcessor().process(descriptor, registry);
		return registry;
	}

}

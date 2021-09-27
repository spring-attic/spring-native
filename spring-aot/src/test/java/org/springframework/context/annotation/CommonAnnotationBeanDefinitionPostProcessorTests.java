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

package org.springframework.context.annotation;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link CommonAnnotationBeanDefinitionPostProcessor}.
 *
 * @author Stephane Nicoll
 */
class CommonAnnotationBeanDefinitionPostProcessorTests {

	@Test
	void postProcessWithPostConstructMethod() {
		RootBeanDefinition beanDefinition = rootBeanDefinition(InitMethodSample.class);
		postProcess(beanDefinition);
		assertThat(beanDefinition.isExternallyManagedInitMethod("init")).isTrue();
	}

	@Test
	void postProcessWithPostConstructAndBeanClassName() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClassName(InitMethodSample.class.getName());
		postProcess(beanDefinition);
		assertThat(beanDefinition.isExternallyManagedInitMethod("init")).isTrue();
	}

	@Test
	void postProcessWithPreDestroyMethod() {
		RootBeanDefinition beanDefinition = rootBeanDefinition(PreDestroySample.class);
		postProcess(beanDefinition);
		assertThat(beanDefinition.isExternallyManagedDestroyMethod("shutdown")).isTrue();
	}

	@Test
	void postProcessWithNoInitOrDestroyMethods() {
		RootBeanDefinition beanDefinition = rootBeanDefinition(NonAnnotatedSample.class);
		postProcess(beanDefinition);
		assertThat(beanDefinition.isExternallyManagedInitMethod("init")).isFalse();
		assertThat(beanDefinition.isExternallyManagedDestroyMethod("shutdown")).isFalse();
	}

	@Test
	void postProcessWithNoTargetClass() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		postProcess(beanDefinition);
		assertThat(beanDefinition.isExternallyManagedInitMethod("init")).isFalse();
		assertThat(beanDefinition.isExternallyManagedDestroyMethod("shutdown")).isFalse();
	}

	@Test
	void postProcessWithInvalidClass() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClassName("java.lang.DoesNotExist");
		assertThatIllegalStateException().isThrownBy(() -> postProcess(beanDefinition))
				.withMessageContaining("Bean definition refers to invalid class");
	}

	private RootBeanDefinition rootBeanDefinition(Class<?> beanType) {
		return (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(beanType).getBeanDefinition();
	}

	private void postProcess(RootBeanDefinition beanDefinition) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		CommonAnnotationBeanDefinitionPostProcessor postProcessor = new CommonAnnotationBeanDefinitionPostProcessor();
		postProcessor.setBeanFactory(beanFactory);
		postProcessor.postProcessBeanDefinition("test", beanDefinition);
	}


	static class InitMethodSample {

		@PostConstruct
		public void init() {

		}

	}

	static class PreDestroySample {

		@PreDestroy
		public void shutdown() {

		}

	}

	@SuppressWarnings("unused")
	static class NonAnnotatedSample {

		public void init() {

		}

		public void shutdown() {

		}

	}

}

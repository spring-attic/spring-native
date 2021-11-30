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

package org.springframework.aot.test.boot;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.LazyInitializationBeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link BuildTimeTestSpringApplication}.
 *
 * @author Stephane Nicoll
 */
class BuildTimeTestSpringApplicationTests {

	@Test
	void runDoesNotRefreshContext() {
		BuildTimeTestSpringApplication application = newSpringApplication(TestConfiguration.class);
		GenericApplicationContext context = application.run();
		assertThat(context.isRunning()).isFalse();
		assertThat(context.getBeanDefinitionNames()).contains("buildTimeTestSpringApplicationTests.TestConfiguration")
				.doesNotContain("sampleBean");
	}

	@Test
	void runWithDefaultSettings() {
		BuildTimeTestSpringApplication application = newSpringApplication(TestConfiguration.class);
		GenericApplicationContext context = application.run();
		assertThat(context.getBeanFactory()).isInstanceOfSatisfying(DefaultListableBeanFactory.class,
				(beanFactory) -> assertThat(beanFactory.isAllowBeanDefinitionOverriding()).isFalse());
		assertThat(context.getBeanFactoryPostProcessors()).noneSatisfy(
				(bpp) -> assertThat(bpp).isInstanceOf(LazyInitializationBeanFactoryPostProcessor.class));
	}

	@Test
	void runPreserveCustomEnvironment() {
		BuildTimeTestSpringApplication application = newSpringApplication(TestConfiguration.class);
		MockEnvironment environment = new MockEnvironment();
		application.setEnvironment(environment);
		GenericApplicationContext context = application.run();
		assertThat(context.getEnvironment()).isSameAs(environment);
	}

	@Test
	void runSetAllowBeanDefinitionOverriding() {
		BuildTimeTestSpringApplication application = newSpringApplication(TestConfiguration.class);
		application.setAllowBeanDefinitionOverriding(true);
		GenericApplicationContext context = application.run();
		assertThat(context.getBeanFactory()).isInstanceOfSatisfying(DefaultListableBeanFactory.class,
				(beanFactory) -> assertThat(beanFactory.isAllowBeanDefinitionOverriding()).isTrue());
	}

	@Test
	void runSetLazyInitialization() {
		BuildTimeTestSpringApplication application = newSpringApplication(TestConfiguration.class);
		application.setLazyInitialization(true);
		GenericApplicationContext context = application.run();
		assertThat(context.getBeanFactoryPostProcessors()).anySatisfy(
				(bpp) -> assertThat(bpp).isInstanceOf(LazyInitializationBeanFactoryPostProcessor.class));

	}

	@Test
	void runWithInvalidMainProperty() {
		BuildTimeTestSpringApplication application = newSpringApplication(TestConfiguration.class);
		application.setEnvironment(new MockEnvironment().withProperty("spring.main.web-application-type", "INVALID_TYPE"));
		assertThatIllegalStateException().isThrownBy(application::run)
				.withMessageContaining("Cannot bind to SpringApplication");
	}

	private BuildTimeTestSpringApplication newSpringApplication(Class<?>... classes) {
		BuildTimeTestSpringApplication application = new BuildTimeTestSpringApplication();
		application.addPrimarySources(Arrays.asList(classes));
		return application;
	}


	@Configuration(proxyBeanMethods = false)
	static class TestConfiguration {

		@Bean
		String testBean() {
			return "test";
		}

	}

}

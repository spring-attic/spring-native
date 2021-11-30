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

package org.springframework.aot.test.context.support;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.support.DefaultBootstrapContext;
import org.springframework.test.context.support.DefaultTestContextBootstrapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AotDefaultConfigContextLoader}.
 *
 * @author Stephane Nicoll
 */
class AotDefaultConfigContextLoaderTests {

	@Test
	void loadContextUsesApplicationContextInitializer() {
		run(load(SampleTest.class), (context) -> {
			assertThat(context).hasNotFailed().hasBean("testBean").hasSingleBean(String.class);
			assertThat(context.getBean(String.class)).isEqualTo(TestApplicationContextInitializer.TEST_BEAN);
		});
	}

	@Test
	void loadContextUseTestProperties() {
		run(load(SamplePropertiesTest.class), (context) -> {
			ConfigurableEnvironment environment = context.getEnvironment();
			assertThat(environment.containsProperty("test.property")).isTrue();
			assertThat(environment.getProperty("test.property")).isEqualTo("42");
		});
	}

	@Test
	void loadContextSetActiveProfiles() {
		run(load(SampleProfilesTest.class), (context) ->
				assertThat(context.getEnvironment().getActiveProfiles()).containsOnly("profile1", "profile2"));
	}

	@Test
	void loadContextRemovesAnnotationBasedInfrastructure() {
		run(load(SampleTest.class), (context) -> {
			assertThat(context)
					.doesNotHaveBean(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)
					.doesNotHaveBean(AnnotationConfigUtils.COMMON_ANNOTATION_PROCESSOR_BEAN_NAME)
					.doesNotHaveBean(AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)
					.doesNotHaveBean(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR)
					.doesNotHaveBean(AnnotationConfigUtils.EVENT_LISTENER_FACTORY_BEAN_NAME)
					.doesNotHaveBean(AnnotationConfigUtils.EVENT_LISTENER_PROCESSOR_BEAN_NAME);
		});
	}

	private void run(Supplier<ConfigurableApplicationContext> supplier, Consumer<AssertableApplicationContext> context) {
		try (ConfigurableApplicationContext ctx = supplier.get()) {
			context.accept(AssertableApplicationContext.get(() -> ctx));
		}
	}

	private Supplier<ConfigurableApplicationContext> load(Class<?> testClass) {
		return () -> {
			try {
				AotDefaultConfigContextLoader loader = new AotDefaultConfigContextLoader(TestApplicationContextInitializer.class);
				return loader.loadContext(createMergedContextConfiguration(testClass));
			}
			catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		};
	}

	private MergedContextConfiguration createMergedContextConfiguration(Class<?> testClass) {
		DefaultTestContextBootstrapper bootstrapper = new DefaultTestContextBootstrapper();
		bootstrapper.setBootstrapContext(new DefaultBootstrapContext(testClass, new DefaultCacheAwareContextLoaderDelegate()));
		return bootstrapper.buildMergedContextConfiguration();
	}


	@SpringJUnitConfig
	static class SampleTest {

	}

	@SpringJUnitConfig
	@TestPropertySource(properties = "test.property=42")
	static class SamplePropertiesTest {

	}

	@SpringJUnitConfig
	@ActiveProfiles({ "profile1", "profile2" })
	static class SampleProfilesTest {

	}

	static class TestApplicationContextInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

		static String TEST_BEAN = "test";

		@Override
		public void initialize(GenericApplicationContext applicationContext) {
			applicationContext.registerBeanDefinition("testBean", BeanDefinitionBuilder
					.rootBeanDefinition(ResolvableType.forClass(String.class), () -> TEST_BEAN).getBeanDefinition());
		}

	}

}

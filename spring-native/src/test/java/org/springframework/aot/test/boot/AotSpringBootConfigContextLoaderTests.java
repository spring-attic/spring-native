/*
 * Copyright 2019-2022 the original author or authors.
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

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.support.DefaultBootstrapContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AotSpringBootConfigContextLoader}.
 *
 * @author Stephane Nicoll
 */
class AotSpringBootConfigContextLoaderTests {

	@Test
	void loadContextUsesApplicationContextInitializer() {
		AotSpringBootConfigContextLoader loader = new AotSpringBootConfigContextLoader(TestApplicationContextInitializer.class);
		run(() -> loader.loadContext(createMergedContextConfiguration(SampleTest.class)), (context) -> {
			assertThat(context).hasNotFailed().hasBean("testBean").hasSingleBean(String.class);
			assertThat(context.getBean(String.class)).isEqualTo(TestApplicationContextInitializer.TEST_BEAN);
		});
	}

	@Test
	void loadContextUsesApplicationContextInitializerAndWebSettings() {
		AotSpringBootConfigContextLoader loader = new AotSpringBootConfigContextLoader(TestApplicationContextInitializer.class,
				WebApplicationType.SERVLET, WebEnvironment.MOCK);
		run(() -> loader.loadContext(createMergedContextConfiguration(SampleTest.class)), (context) -> {
			assertThat(context).hasNotFailed().hasBean("testBean").hasSingleBean(String.class);
			assertThat(context.getBean(String.class)).isEqualTo(TestApplicationContextInitializer.TEST_BEAN);
		});
	}

	@Test
	void loadContextUseTestProperties() {
		AotSpringBootConfigContextLoader loader = new AotSpringBootConfigContextLoader(TestApplicationContextInitializer.class);
		run(() -> loader.loadContext(createMergedContextConfiguration(SampleTest.class)), (context) -> {
			ConfigurableEnvironment environment = context.getEnvironment();
			assertThat(environment.containsProperty("test.property")).isTrue();
			assertThat(environment.getProperty("test.property")).isEqualTo("42");
		});
	}

	@Test
	void loadContextSetActiveProfiles() {
		AotSpringBootConfigContextLoader loader = new AotSpringBootConfigContextLoader(TestApplicationContextInitializer.class);
		run(() -> loader.loadContext(createMergedContextConfiguration(SampleProfilesTest.class)), (context) ->
				assertThat(context.getEnvironment().getActiveProfiles()).containsOnly("profile1", "profile2"));
	}

	static Stream<Arguments> applicationArguments() {
		return Stream.of(
				Arguments.of(new String[] {}, Collections.emptyMap()),
				Arguments.of(new String[] { "--app.test=one" }, Map.of("app.test", "one")),
				Arguments.of(new String[] { "--app.test=one", "--app.name=foo" }, Map.of("app.test", "one", "app.name", "foo"))
		);
	}

	@ParameterizedTest
	@MethodSource("applicationArguments")
	void loadContextUsesArguments(String[] arguments, Map<String, String> expectedEntries) {
		AotSpringBootConfigContextLoader loader = new AotSpringBootConfigContextLoader(TestApplicationContextInitializer.class, arguments);
		run(() -> loader.loadContext(createMergedContextConfiguration(SampleTest.class)),
				validateApplicationArguments(expectedEntries));
	}

	@ParameterizedTest
	@MethodSource("applicationArguments")
	void loadContextUsesArgumentsAndWebSettings(String[] arguments, Map<String, String> expectedEntries) {
		AotSpringBootConfigContextLoader loader = new AotSpringBootConfigContextLoader(
				TestApplicationContextInitializer.class, WebApplicationType.SERVLET, WebEnvironment.MOCK, arguments);
		run(() -> loader.loadContext(createMergedContextConfiguration(SampleTest.class)),
				validateApplicationArguments(expectedEntries));
	}

	private Consumer<AssertableApplicationContext> validateApplicationArguments(
			Map<String, String> expectedEntries) {
		return (context) -> {
			ApplicationArguments args = context.getBean(ApplicationArguments.class);
			assertThat(args.getOptionNames()).containsExactlyInAnyOrderElementsOf(expectedEntries.keySet());
			for (String optionName : args.getOptionNames()) {
				assertThat(args.getOptionValues(optionName)).containsOnly(expectedEntries.get(optionName));
			}
		};
	}

	private void run(Supplier<ConfigurableApplicationContext> supplier, Consumer<AssertableApplicationContext> context) {
		try (ConfigurableApplicationContext ctx = supplier.get()) {
			context.accept(AssertableApplicationContext.get(() -> ctx));
		}
	}

	private MergedContextConfiguration createMergedContextConfiguration(Class<?> testClass) {
		SpringBootTestContextBootstrapper bootstrapper = new SpringBootTestContextBootstrapper();
		bootstrapper.setBootstrapContext(new DefaultBootstrapContext(testClass, new DefaultCacheAwareContextLoaderDelegate()));
		return bootstrapper.buildMergedContextConfiguration();
	}


	@SpringBootTest(properties = "test.property=42")
	static class SampleTest {

	}

	@DataJpaTest
	@ActiveProfiles({ "profile1", "profile2" })
	static class SampleProfilesTest {

	}

	@SpringBootConfiguration
	static class SampleConfiguration {

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

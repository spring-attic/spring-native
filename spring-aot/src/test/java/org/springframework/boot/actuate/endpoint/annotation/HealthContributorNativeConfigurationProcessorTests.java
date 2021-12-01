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

package org.springframework.boot.actuate.endpoint.annotation;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.PingHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.BuildTimeBeanDefinitionsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.nativex.hint.TypeAccess;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link HealthContributorNativeConfigurationProcessor}.
 *
 * @author Olivier Boudet
 * @author Stephane Nicoll
 */
class HealthContributorNativeConfigurationProcessorTests {

	@Test
	void registerHealthIndicator() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("healthIndicator", BeanDefinitionBuilder
				.rootBeanDefinition(PingHealthIndicator.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(PingHealthIndicator.class);
			assertThat(entry.getAccess()).contains(TypeAccess.DECLARED_CONSTRUCTORS);
		});
	}

	@Test
	void registerHealthContributorWithInterfaceType() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("healthContributor", BeanDefinitionBuilder
				.rootBeanDefinition(HealthContributor.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).isEmpty();
	}

	@Test
	void registerHealthIndicatorWithInterfaceType() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("healthContributor", BeanDefinitionBuilder
				.rootBeanDefinition(HealthIndicator.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).isEmpty();
	}

	@Test
	void registerHealthContributorWithPartialType() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("configuration", BeanDefinitionBuilder
				.rootBeanDefinition(TestConfiguration.class).getBeanDefinition());
		RootBeanDefinition indicator = new RootBeanDefinition();
		indicator.setFactoryBeanName("configuration");
		indicator.setFactoryMethodName("createIndicator");
		beanFactory.registerBeanDefinition("indicator", indicator);
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(PingHealthIndicator.class);
			assertThat(entry.getAccess()).contains(TypeAccess.DECLARED_CONSTRUCTORS);
		});
	}

	@Test
	void registerHealthContributorWithCompositeHidingType() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBeanDefinition("one", BeanDefinitionBuilder
				.rootBeanDefinition(Integer.class, () -> 1).getBeanDefinition());
		context.registerBeanDefinition("two", BeanDefinitionBuilder
				.rootBeanDefinition(Integer.class, () -> 2).getBeanDefinition());
		context.registerBeanDefinition("configuration", BeanDefinitionBuilder
				.rootBeanDefinition(TestHealthContributorConfiguration.class).getBeanDefinition());
		BuildTimeBeanDefinitionsRegistrar registrar = new BuildTimeBeanDefinitionsRegistrar();
		ConfigurableListableBeanFactory beanFactory = registrar.processBeanDefinitions(context);
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(PingHealthIndicator.class);
			assertThat(entry.getAccess()).contains(TypeAccess.DECLARED_CONSTRUCTORS);
		});
	}

	private NativeConfigurationRegistry process(ConfigurableListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new HealthContributorNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}

	@SuppressWarnings("unused")
	static class TestConfiguration {

		PingHealthIndicator createIndicator() {
			return new PingHealthIndicator();
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class TestHealthContributorConfiguration
			extends CompositeHealthContributorConfiguration<PingHealthIndicator, Integer> {

		public TestHealthContributorConfiguration() {
		}

		@Bean
		public HealthContributor testHealthContributor(Map<String, Integer> numbers) {
			return createContributor(numbers);
		}

		@Override
		protected PingHealthIndicator createIndicator(Integer bean) {
			return new PingHealthIndicator();
		}

	}

}

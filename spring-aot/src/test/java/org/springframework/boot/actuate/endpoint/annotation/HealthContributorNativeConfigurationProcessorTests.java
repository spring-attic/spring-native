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

import org.junit.jupiter.api.Test;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.nativex.hint.Flag;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link HealthContributorNativeConfigurationProcessor}.
 *
 * @author Olivier Boudet
 */
class HealthContributorNativeConfigurationProcessorTests {

	@Test
	void registerHealthIndicator() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("healthIndicator", BeanDefinitionBuilder.rootBeanDefinition(TestHealthIndicator.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<NativeReflectionEntry> entries = registry.reflection().getEntries();
		assertThat(entries).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(TestHealthIndicator.class);
			assertThat(entry.getFlags()).contains(Flag.allDeclaredConstructors);
		});
		assertThat(entries).hasSize(1);
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new HealthContributorNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}

	@SuppressWarnings("unused")
	static class TestHealthIndicator implements HealthIndicator {

		@Override
		public Health health() {
			return Health.up().build();
		}
	}

}

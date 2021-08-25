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

package org.springframework.boot.context.properties;

import org.junit.jupiter.api.Test;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ConstructorBindingValueSupplier}.
 *
 * @author Stephane Nicoll
 */
class ConstructorBindingValueSupplierTests {

	@Test
	void bindConfigurationProperties() {
		MockEnvironment environment = new MockEnvironment().withProperty("test.name", "Hello")
				.withProperty("test.counter", "42");
		try (GenericApplicationContext context = refreshContext(environment)) {
			Object instance = ConstructorBindingValueSupplier.bind(context.getBeanFactory(),
					"test", SampleConfigurationProperties.class);
			assertThat(instance).isInstanceOfSatisfying(SampleConfigurationProperties.class, (sample) -> {
				assertThat(sample.name).isEqualTo("Hello");
				assertThat(sample.counter).isEqualTo(42);
			});
		}
	}

	@Test
	void bindFailureIncludeRelevantException() {
		MockEnvironment environment = new MockEnvironment().withProperty("test.name", "fail")
				.withProperty("test.counter", "42");
		try (GenericApplicationContext context = refreshContext(environment)) {
			assertThatThrownBy(() -> ConstructorBindingValueSupplier
					.bind(context.getBeanFactory(), "test", SampleConfigurationProperties.class)
			).isInstanceOf(ConfigurationPropertiesBindException.class).hasMessageContaining("test")
					.hasMessageContaining(SampleConfigurationProperties.class.getName());
		}
	}


	private GenericApplicationContext refreshContext(ConfigurableEnvironment environment) {
		GenericApplicationContext context = new GenericApplicationContext();
		context.setEnvironment(environment);
		ConfigurationPropertiesBindingPostProcessor.register(context);
		context.refresh();
		return context;
	}

	@ConfigurationProperties("test")
	@ConstructorBinding
	static class SampleConfigurationProperties {

		private final String name;

		private final Integer counter;

		public SampleConfigurationProperties(String name, Integer counter) {
			if ("fail".equals(name)) {
				throw new IllegalArgumentException("fail");
			}
			this.name = name;
			this.counter = counter;
		}

	}

}

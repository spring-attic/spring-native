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

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EndpointNativeConfigurationProcessor}.
 *
 * @author Stephane Nicoll
 */
class EndpointNativeConfigurationProcessorTests {

	@Test
	void registerSimpleEndpoint() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(TestEndpoint.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<DefaultNativeReflectionEntry> entries = registry.reflection().reflectionEntries().collect(Collectors.toList());
		assertThat(entries).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(TestEndpoint.class);
			assertThat(entry.getMethods().stream().map(Method::getName))
					.containsOnly("getAll", "get", "set", "delete");
		});
		assertThat(entries).hasSize(1);
	}

	@Test
	void registerSimpleEndpointExtension() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(TestEndpointWebExtension.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<DefaultNativeReflectionEntry> entries = registry.reflection().reflectionEntries().collect(Collectors.toList());
		assertThat(entries).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(TestEndpointWebExtension.class);
			assertThat(entry.getMethods().stream().map(Method::getName)).containsOnly("get");
		});
		assertThat(entries).hasSize(1);
	}

	@Test
	void registerFilteredEndpointRegistersEndpointFilter() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(TestFilteredEndpoint.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<DefaultNativeReflectionEntry> entries = registry.reflection().reflectionEntries().collect(Collectors.toList());
		assertThat(entries).anySatisfy((entry) ->
				assertThat(entry.getType()).isEqualTo(TestFilteredEndpoint.class));
		assertThat(entries).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(TestEndpointFilter.class);
			assertThat(entry.getConstructors()).containsOnly(TestEndpointFilter.class.getDeclaredConstructors()[0]);
		});
		assertThat(entries).hasSize(2);
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new EndpointNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}

	@Endpoint(id = "test")
	@SuppressWarnings("unused")
	static class TestEndpoint {

		@ReadOperation
		public List<String> getAll() {
			return List.of(get());
		}

		@ReadOperation
		public String get() {
			return "test";
		}

		@WriteOperation
		public void set(String name) {

		}

		@DeleteOperation
		public void delete(String name) {

		}

		public void ignoredMethod(String name) {

		}

	}

	@EndpointWebExtension(endpoint = TestEndpoint.class)
	@SuppressWarnings("unused")
	static class TestEndpointWebExtension {

		@ReadOperation
		public String get() {
			return "web";
		}

		public void noise(String name) {

		}

	}

	@Endpoint
	@FilteredEndpoint(TestEndpointFilter.class)
	static class TestFilteredEndpoint {

	}

	static class TestEndpointFilter implements EndpointFilter<ExposableEndpoint<?>> {

		@Override
		public boolean match(ExposableEndpoint<?> endpoint) {
			return false;
		}
	}

}

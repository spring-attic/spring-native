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

package org.springframework.aot.test.context.bootstrap.generator.nativex;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.test.context.MergedContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link TestNativeConfigurationRegistrar}.
 *
 * @author Stephane Nicoll
 */
class TestNativeConfigurationRegistrarTests {

	@Test
	void processTestConfigurationsInvokeProcessorsInOrder() {
		TestConfigurationNativeConfigurationProcessor first = mock(TestConfigurationNativeConfigurationProcessor.class);
		TestConfigurationNativeConfigurationProcessor second = mock(TestConfigurationNativeConfigurationProcessor.class);
		MergedContextConfiguration testConfiguration = mock(MergedContextConfiguration.class);
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new TestNativeConfigurationRegistrar(List.of(first, second)).processTestConfigurations(registry, List.of(testConfiguration));
		InOrder ordered = inOrder(first, second);
		ordered.verify(first).process(testConfiguration, registry);
		ordered.verify(second).process(testConfiguration, registry);
	}

	@Test
	void processTestConfigurationLoadsFromSpringFactories() {
		CustomSpringFactoriesClassLoader classLoader = new CustomSpringFactoriesClassLoader("test-configuration-processors.factories");
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new TestNativeConfigurationRegistrar(classLoader).processTestConfigurations(registry, List.of(mock(MergedContextConfiguration.class)));
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) ->
				assertThat(entry.getType()).isEqualTo(TestNativeConfigurationRegistrarTests.class));
	}

	static class SimpleTestConfigurationNativeConfigurationProcessor implements TestConfigurationNativeConfigurationProcessor {

		@Override
		public void process(MergedContextConfiguration testConfiguration, NativeConfigurationRegistry registry) {
			registry.reflection().forType(TestNativeConfigurationRegistrarTests.class);
		}
	}

	static class CustomSpringFactoriesClassLoader extends ClassLoader {

		private final String factoriesName;

		CustomSpringFactoriesClassLoader(String factoriesName) {
			super(TestNativeConfigurationRegistrarTests.class.getClassLoader());
			this.factoriesName = factoriesName;
		}

		@Override
		public Enumeration<URL> getResources(String name) throws IOException {
			if ("META-INF/spring.factories".equals(name)) {
				return super.getResources("native-configuration-tests/" + this.factoriesName);
			}
			return super.getResources(name);
		}

	}

}

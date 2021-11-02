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

import java.util.List;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.test.context.MergedContextConfiguration;

/**
 * Register native configuration using processors detected on the classpath.
 *
 * @author Stephane Nicoll
 * @see TestConfigurationNativeConfigurationProcessor
 */
public class TestNativeConfigurationRegistrar {

	private final List<TestConfigurationNativeConfigurationProcessor> testConfigurationProcessors;

	TestNativeConfigurationRegistrar(List<TestConfigurationNativeConfigurationProcessor> testConfigurationProcessors) {
		this.testConfigurationProcessors = testConfigurationProcessors;
	}

	public TestNativeConfigurationRegistrar(ClassLoader classLoader) {
		this(SpringFactoriesLoader.loadFactories(TestConfigurationNativeConfigurationProcessor.class, classLoader));
	}

	/**
	 * Process the {@link MergedContextConfiguration test configurations} against the
	 * specified {@link NativeConfigurationRegistry}.
	 * @param registry the registry to use
	 * @param testConfigurations the test configurations
	 */
	public void processTestConfigurations(NativeConfigurationRegistry registry, Iterable<MergedContextConfiguration> testConfigurations) {
		this.testConfigurationProcessors.forEach((processor) -> testConfigurations.forEach(
				(testConfiguration) -> processor.process(testConfiguration, registry)));
	}

}

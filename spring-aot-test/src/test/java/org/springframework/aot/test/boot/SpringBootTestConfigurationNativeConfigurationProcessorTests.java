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

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.MergedContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link SpringBootTestConfigurationNativeConfigurationProcessor}.
 *
 * @author Stephane Nicoll
 */
class SpringBootTestConfigurationNativeConfigurationProcessorTests {

	private final SpringBootTestConfigurationNativeConfigurationProcessor processor = new SpringBootTestConfigurationNativeConfigurationProcessor();

	@Test
	void registerByteCodeForSpringBootApplication() {
		MergedContextConfiguration contextConfiguration = mock(MergedContextConfiguration.class);
		given(contextConfiguration.getClasses()).willReturn(new Class<?>[] { String.class, Integer.class, SampleApplication.class });
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		this.processor.process(contextConfiguration, registry);
		assertThat(registry.resources().toResourcesDescriptor().getPatterns()).containsOnly(
				"org/springframework/aot/test/boot/SpringBootTestConfigurationNativeConfigurationProcessorTests\\$SampleApplication.class");
	}

	@Test
	void registerByteCodeForSpringBootConfiguration() {
		MergedContextConfiguration contextConfiguration = mock(MergedContextConfiguration.class);
		given(contextConfiguration.getClasses()).willReturn(new Class<?>[] { String.class, Integer.class, SampleConfiguration.class });
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		this.processor.process(contextConfiguration, registry);
		assertThat(registry.resources().toResourcesDescriptor().getPatterns()).containsOnly(
				"org/springframework/aot/test/boot/SpringBootTestConfigurationNativeConfigurationProcessorTests\\$SampleConfiguration.class");
	}


	@SpringBootApplication
	static class SampleApplication {

	}

	@SpringBootConfiguration
	static class SampleConfiguration {

	}

}

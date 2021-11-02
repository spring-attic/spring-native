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

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeResourcesEntry;
import org.springframework.aot.test.context.bootstrap.generator.nativex.TestConfigurationNativeConfigurationProcessor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.test.context.MergedContextConfiguration;

/**
 * A {@link TestConfigurationNativeConfigurationProcessor} that registers the
 * {@link SpringBootConfiguration} used by the test.
 *
 * @author Stephane Nicoll
 */
class SpringBootTestConfigurationNativeConfigurationProcessor implements TestConfigurationNativeConfigurationProcessor {

	@Override
	public void process(MergedContextConfiguration testConfiguration, NativeConfigurationRegistry registry) {
		Arrays.stream(testConfiguration.getClasses()).filter(this::isSpringBootConfiguration).forEach((configuration) ->
				registry.resources().add(NativeResourcesEntry.ofClass(configuration)));
	}

	private boolean isSpringBootConfiguration(Class<?> type) {
		return MergedAnnotations.from(type).isPresent(SpringBootConfiguration.class);
	}
}

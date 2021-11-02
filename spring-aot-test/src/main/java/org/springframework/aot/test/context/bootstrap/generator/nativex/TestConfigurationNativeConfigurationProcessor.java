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

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.test.context.MergedContextConfiguration;

/**
 * Process the {@link MergedContextConfiguration test configuration} and register the
 * necessary native configuration.
 *
 * @author Stephane Nicoll
 */
@FunctionalInterface
public interface TestConfigurationNativeConfigurationProcessor {

	/**
	 * Process the specified {@link MergedContextConfiguration} and register the need for
	 * native configuration.
	 * @param testConfiguration the merged context configuration to process
	 * @param registry the registry to use
	 */
	void process(MergedContextConfiguration testConfiguration, NativeConfigurationRegistry registry);

}

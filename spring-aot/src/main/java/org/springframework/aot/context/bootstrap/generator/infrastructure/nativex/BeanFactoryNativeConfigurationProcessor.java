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

package org.springframework.aot.context.bootstrap.generator.infrastructure.nativex;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Process the {@link ConfigurableListableBeanFactory bean factory} and register the
 * necessary native configuration.
 *
 * @author Stephane Nicoll
 * @see BeanNativeConfigurationProcessor to process a specific bean instead
 */
@FunctionalInterface
public interface BeanFactoryNativeConfigurationProcessor {

	/**
	 * Process the specified bean factory and register the need for native configuration.
	 * @param beanFactory the context to process
	 * @param registry the registry to use
	 */
	void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry);

}

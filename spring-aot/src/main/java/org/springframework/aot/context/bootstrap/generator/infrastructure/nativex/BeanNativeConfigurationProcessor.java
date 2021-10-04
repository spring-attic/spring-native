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

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * Process a {@link BeanInstanceDescriptor bean instance} and register the need for
 * native configuration. Implementation of this interface can also implement
 * {@link BeanFactoryAware} if they need to access the underlying bean factory.
 *
 * @author Stephane Nicoll
 */
@FunctionalInterface
public interface BeanNativeConfigurationProcessor {

	/**
	 * Process the specified bean and register the need for native configuration.
	 * @param descriptor the bean instance descriptor
	 * @param registry the registry to use
	 */
	void process(BeanInstanceDescriptor descriptor, NativeConfigurationRegistry registry);

}

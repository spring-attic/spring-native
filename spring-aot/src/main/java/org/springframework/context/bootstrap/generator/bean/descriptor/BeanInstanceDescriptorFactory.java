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

package org.springframework.context.bootstrap.generator.bean.descriptor;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Create a {@link BeanInstanceDescriptor} from its {@link BeanDefinition}.
 *
 * @author Stephane Nicoll
 */
@FunctionalInterface
public interface BeanInstanceDescriptorFactory {

	/**
	 * Create a {@link BeanInstanceDescriptor} for the specified {@link BeanDefinition}.
	 * @param beanDefinition the bean definition to handle
	 * @return a bean instance descriptor for the specified bean definition
	 */
	BeanInstanceDescriptor create(BeanDefinition beanDefinition);

}

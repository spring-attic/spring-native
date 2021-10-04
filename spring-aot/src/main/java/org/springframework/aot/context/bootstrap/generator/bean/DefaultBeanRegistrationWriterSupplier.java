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

package org.springframework.aot.context.bootstrap.generator.bean;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Default {@link BeanRegistrationWriterSupplier} implementation, providing an instance
 * based on the actual class and resolved factory method.
 *
 * @author Stephane Nicoll
 */
@Order(Ordered.LOWEST_PRECEDENCE - 5)
class DefaultBeanRegistrationWriterSupplier extends AbstractBeanRegistrationWriterSupplier {

	@Override
	protected BeanRegistrationWriter createInstance(String beanName, BeanDefinition beanDefinition,
			BeanInstanceDescriptor beanInstanceDescriptor) {
		return new DefaultBeanRegistrationWriter(beanName, beanDefinition, beanInstanceDescriptor, initializeOptions().build());
	}

}

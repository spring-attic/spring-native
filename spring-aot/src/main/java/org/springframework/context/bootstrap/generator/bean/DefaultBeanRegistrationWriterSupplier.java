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

package org.springframework.context.bootstrap.generator.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptorFactory;
import org.springframework.context.bootstrap.generator.bean.descriptor.DefaultBeanInstanceDescriptorFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Default {@link BeanRegistrationWriterSupplier} implementation, providing an instance
 * based on the actual class and resolved factory method.
 *
 * @author Stephane Nicoll
 */
@Order(Ordered.LOWEST_PRECEDENCE - 5)
class DefaultBeanRegistrationWriterSupplier implements BeanRegistrationWriterSupplier, BeanFactoryAware {

	private BeanInstanceDescriptorFactory beanInstanceDescriptorFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanInstanceDescriptorFactory = new DefaultBeanInstanceDescriptorFactory((ConfigurableBeanFactory) beanFactory);
	}

	@Override
	public DefaultBeanRegistrationWriter get(String beanName, BeanDefinition beanDefinition) {
		BeanValueWriter beanValueWriter = createBeanValueWriter(beanDefinition);
		return (beanValueWriter != null)
				? new DefaultBeanRegistrationWriter(beanName, beanDefinition, beanValueWriter, createOptions())
				: null;
	}

	private BeanValueWriter createBeanValueWriter(BeanDefinition beanDefinition) {
		BeanInstanceDescriptor descriptor = this.beanInstanceDescriptorFactory.create(beanDefinition);
		return (descriptor != null) ? new DefaultBeanValueWriter(descriptor, beanDefinition) : null;
	}

	private BeanRegistrationWriterOptions createOptions() {
		return BeanRegistrationWriterOptions.builder().withWriterFactory(this::get).build();
	}

}

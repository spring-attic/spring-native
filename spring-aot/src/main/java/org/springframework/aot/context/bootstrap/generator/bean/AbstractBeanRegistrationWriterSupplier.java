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
import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptorFactory;
import org.springframework.aot.context.bootstrap.generator.bean.descriptor.DefaultBeanInstanceDescriptorFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Base {@link BeanRegistrationWriterSupplier} implementation taking care of creating
 * a suitable {@link BeanInstanceDescriptor}.
 *
 * @author Stephane Nicoll
 */
public abstract class AbstractBeanRegistrationWriterSupplier implements BeanRegistrationWriterSupplier, BeanFactoryAware {

	private BeanInstanceDescriptorFactory beanInstanceDescriptorFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanInstanceDescriptorFactory = new DefaultBeanInstanceDescriptorFactory((ConfigurableBeanFactory) beanFactory);
	}

	@Override
	public BeanRegistrationWriter get(String beanName, BeanDefinition beanDefinition) {
		BeanInstanceDescriptor beanInstanceDescriptor = resolveBeanInstanceDescriptor(beanDefinition);
		return (beanInstanceDescriptor != null) ? createInstance(beanName, beanDefinition, beanInstanceDescriptor) : null;
	}

	/**
	 * Create a {@link BeanRegistrationWriter} based on a {@link BeanInstanceDescriptor}.
	 * @param beanName the name of the bean
	 * @param beanDefinition the bean definition
	 * @param beanInstanceDescriptor the bean instance descriptor (never {@code null})
	 * @return a bean registration writer for the specified bean definition
	 */
	protected abstract BeanRegistrationWriter createInstance(String beanName, BeanDefinition beanDefinition,
			BeanInstanceDescriptor beanInstanceDescriptor);

	/**
	 * Initialize a builder for the {@link BeanRegistrationWriterOptions}.
	 * @return a builder with sensible defaults
	 */
	protected BeanRegistrationWriterOptions.Builder initializeOptions() {
		return BeanRegistrationWriterOptions.builder().withWriterFactory(this::get);
	}

	private BeanInstanceDescriptor resolveBeanInstanceDescriptor(BeanDefinition beanDefinition) {
		return this.beanInstanceDescriptorFactory.create(beanDefinition);
	}
}

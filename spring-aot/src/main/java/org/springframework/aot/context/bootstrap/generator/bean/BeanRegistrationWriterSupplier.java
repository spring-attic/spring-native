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

import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.lang.Nullable;

/**
 * Strategy interface to provide the bean registration writer for a {@link BeanDefinition}.
 * <p/>
 * Can implement any of the following {@link Aware} interfaces: {@link EnvironmentAware},
 * {@link ResourceLoaderAware}, {@link ApplicationEventPublisherAware},
 * {@link ApplicationContextAware}, {@link BeanClassLoaderAware}, and
 * {@link BeanFactoryAware}.
 *
 * @author Stephane Nicoll
 */
@FunctionalInterface
public interface BeanRegistrationWriterSupplier {

	/**
	 * Return the {@link BeanRegistrationWriter} to use for the specified merged
	 * {@link BeanDefinition}.
	 * @param beanName the name of the bean definition to handle
	 * @param beanDefinition the merged bean definition to handle
	 * @return the {@link BeanRegistrationWriter} to use, or {@code null}
	 * @see ConfigurableBeanFactory#getMergedBeanDefinition(String)
	 */
	@Nullable
	BeanRegistrationWriter get(String beanName, BeanDefinition beanDefinition);
}

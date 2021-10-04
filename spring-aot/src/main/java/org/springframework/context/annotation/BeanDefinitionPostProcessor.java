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

package org.springframework.context.annotation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Strategy interface to modify a {@link BeanDefinition} that has been processed at
 * build time by {@link BuildTimeBeanDefinitionsRegistrar}.
 *
 * @author Stephane Nicoll
 */
@FunctionalInterface
public interface BeanDefinitionPostProcessor {

	/**
	 * Post-process the specified merged {@link RootBeanDefinition}.
	 * @param beanName the name of the bean
	 * @param beanDefinition the merged bean definition to post process
	 * @see ConfigurableBeanFactory#getMergedBeanDefinition(String)
	 */
	void postProcessBeanDefinition(String beanName, RootBeanDefinition beanDefinition);

}

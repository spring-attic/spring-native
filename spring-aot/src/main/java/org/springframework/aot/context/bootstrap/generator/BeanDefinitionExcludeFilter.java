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

package org.springframework.aot.context.bootstrap.generator;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.util.ClassUtils;

/**
 * Strategy interface to exclude infrastructure beans that should not be included when the
 * context is initialized with a bootstrap class.
 *
 * @author Stephane Nicoll
 */
@FunctionalInterface
public interface BeanDefinitionExcludeFilter {

	/**
	 * Determine if the specified bean should be added to the generated bootstrap class.
	 * @param beanName the bean name to consider
	 * @param beanDefinition the bean definition to consider
	 * @return {@code true} if it should be selected
	 */
	boolean isExcluded(String beanName, BeanDefinition beanDefinition);


	/**
	 * Create a {@link BeanDefinitionExcludeFilter} excluding the beans matching any of
	 * the specified bean types
	 * @param beanTypes the types of the beans to exclude
	 * @return a filter
	 */
	static BeanDefinitionExcludeFilter forTypes(Class<?>... beanTypes) {
		return (beanName, beanDefinition) -> {
			Class<?> target = ClassUtils.getUserClass(beanDefinition.getResolvableType().toClass());
			for (Class<?> type : beanTypes) {
				if (type.isAssignableFrom(target)) {
					return true;
				}
			}
			return false;
		};
	}

	/**
	 * Create a {@link BeanDefinitionExcludeFilter} excluding the beans matching any of
	 * the specified bean names
	 * @param beanNames the names of the beans to exclude
	 * @return a filter
	 */
	static BeanDefinitionExcludeFilter forBeanNames(String... beanNames) {
		return (beanNameCandidate, beanDefinition) -> {
			for (String beanName : beanNames) {
				if (beanName.equals(beanNameCandidate)) {
					return true;
				}
			}
			return false;
		};
	}

}

/*
 * Copyright 2021 the original author or authors.
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

import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * {@link BeanDefinitionPostProcessor} that makes sure to post process merged {@link org.springframework.beans.factory.config.BeanDefinition}
 * that contains information about {@literal externallyManagedInitMethods}.
 *
 * @author Christoph Strobl
 * @since 0.11
 */
public class CommonAnnotationBeanDefinitionPostProcessor implements BeanDefinitionPostProcessor {

	private final CommonAnnotationBeanPostProcessor postProcessor = new CommonAnnotationBeanPostProcessor();

	@Override
	public void postProcessBeanDefinition(String beanName, RootBeanDefinition beanDefinition) {
		postProcessor.postProcessMergedBeanDefinition(
				beanDefinition, beanDefinition.getResolvableType().toClass(), beanName);
	}
}

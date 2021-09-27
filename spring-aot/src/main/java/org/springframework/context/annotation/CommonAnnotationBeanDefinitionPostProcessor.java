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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

/**
 * {@link BeanDefinitionPostProcessor} that makes sure to identify custom init and destroy
 * methods.
 *
 * @author Christoph Strobl
 * @author Stephane Nicoll
 */
class CommonAnnotationBeanDefinitionPostProcessor implements BeanDefinitionPostProcessor, BeanFactoryAware {

	private final CommonAnnotationBeanPostProcessor postProcessor = new CommonAnnotationBeanPostProcessor();

	private ClassLoader classLoader;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.postProcessor.setBeanFactory(beanFactory);
		this.classLoader = ((ConfigurableBeanFactory) beanFactory).getBeanClassLoader();
	}

	@Override
	public void postProcessBeanDefinition(String beanName, RootBeanDefinition beanDefinition) {
		postProcessor.postProcessMergedBeanDefinition(beanDefinition, getBeanType(beanDefinition), beanName);
	}

	private Class<?> getBeanType(RootBeanDefinition beanDefinition) {
		ResolvableType resolvableType = beanDefinition.getResolvableType();
		if (resolvableType != ResolvableType.NONE) {
			return resolvableType.toClass();
		}
		if (beanDefinition.getBeanClassName() != null) {
			return loadBeanClassName(beanDefinition.getBeanClassName());
		}
		return Object.class;
	}

	private Class<?> loadBeanClassName(String className) {
		try {
			return ClassUtils.forName(className, this.classLoader);
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalStateException(
					"Bean definition refers to invalid class '" + className + "'", ex);
		}
	}

}

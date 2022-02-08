/*
 * Copyright 2019-2022 the original author or authors.
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

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Ensure that {@link AbstractBeanDefinition#hasBeanClass()} can be safely used by bean
 * definition processors.
 *
 * @author Stephane Nicoll
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
class BeanClassBeanDefinitionPostProcessor implements BeanDefinitionPostProcessor, BeanFactoryAware {

	private ClassLoader classLoader;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.classLoader = ((ConfigurableBeanFactory) beanFactory).getBeanClassLoader();
	}

	@Override
	public void postProcessBeanDefinition(String beanName, RootBeanDefinition beanDefinition) {
		safeResolveBeanClas(beanDefinition);
		for (PropertyValue propertyValue : beanDefinition.getPropertyValues().getPropertyValueList()) {
			Object value = propertyValue.getValue();
			if (value instanceof AbstractBeanDefinition) {
				safeResolveBeanClas((AbstractBeanDefinition) value);
			}
		}
		for (ValueHolder valueHolder : beanDefinition.getConstructorArgumentValues().getIndexedArgumentValues().values()) {
			Object value = valueHolder.getValue();
			if (value instanceof AbstractBeanDefinition) {
				safeResolveBeanClas((AbstractBeanDefinition) value);
			}
		}
	}

	private void safeResolveBeanClas(AbstractBeanDefinition beanDefinition) {
		if (!beanDefinition.hasBeanClass()) {
			try {
				beanDefinition.resolveBeanClass(this.classLoader);
			}
			catch (ClassNotFoundException ex) {
				// ignore
			}
		}
	}

}

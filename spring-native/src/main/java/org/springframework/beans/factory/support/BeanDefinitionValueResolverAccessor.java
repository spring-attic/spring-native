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

package org.springframework.beans.factory.support;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Accessor for {@code BeanDefinitionValueResolver} as we require the logic and the
 * class is package private.
 *
 * @author Stephane Nicoll
 */
public class BeanDefinitionValueResolverAccessor {

	public static ValueResolver get(GenericApplicationContext context, String beanName, BeanDefinition beanDefinition) {
		return new ValueResolver((AbstractAutowireCapableBeanFactory) context.getBeanFactory(), beanName, beanDefinition);
	}

	public static class ValueResolver extends BeanDefinitionValueResolver {

		public ValueResolver(AbstractAutowireCapableBeanFactory beanFactory, String beanName, BeanDefinition beanDefinition) {
			super(beanFactory, beanName, beanDefinition, initializeTypeConverter(beanFactory));
		}

		private static TypeConverter initializeTypeConverter(AbstractAutowireCapableBeanFactory beanFactory) {
			BeanWrapperImpl bw = new BeanWrapperImpl();
			beanFactory.initBeanWrapper(bw);
			return bw;
		}

	}

}

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

package org.springframework.plugin;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.BeanDefinitionPostProcessor;
import org.springframework.core.ResolvableType;

/**
 * Temporary {@link BeanDefinitionPostProcessor} for Spring HATEOAS until the related
 * framework issue is resolved.
 * <p/>
 * See https://github.com/spring-projects/spring-framework/issues/27383
 *
 * @author Stephane Nicoll
 */
class PluginRegistryFactoryBeanPostProcessor implements BeanDefinitionPostProcessor {

	private static final String FACTORY_BEAN = "org.springframework.plugin.core.support.PluginRegistryFactoryBean";

	@Override
	public void postProcessBeanDefinition(String beanName, RootBeanDefinition beanDefinition) {
		ResolvableType resolvableType = beanDefinition.getResolvableType();
		if (FACTORY_BEAN.equals(resolvableType.toClass().getName())) {
			ResolvableType targetType = resolvableType.as(FactoryBean.class).getGeneric(0);
			beanDefinition.setTargetType(targetType);
			beanDefinition.setBeanClass(resolvableType.toClass());
		}
	}

}

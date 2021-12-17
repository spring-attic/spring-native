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

package org.springframework.aop.scope;

import com.squareup.javapoet.CodeBlock.Builder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriterSupplier;
import org.springframework.aot.context.bootstrap.generator.bean.DefaultBeanRegistrationWriter;
import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.bean.support.MultiStatement;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

/**
 * {@link BeanRegistrationWriterSupplier} for {@link ScopedProxyFactoryBean}.
 *
 * @author Stephane Nicoll
 */
@Order(0)
class ScopedProxyBeanRegistrationWriterSupplier implements BeanRegistrationWriterSupplier, BeanFactoryAware {

	private static final Log logger = LogFactory.getLog(ScopedProxyBeanRegistrationWriterSupplier.class);

	@Nullable
	private ConfigurableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
	}

	@Override
	public BeanRegistrationWriter get(String beanName, BeanDefinition beanDefinition) {
		Class<?> beanType = beanDefinition.getResolvableType().toClass();
		return (beanType.equals(ScopedProxyFactoryBean.class))
				? createScopedProxyBeanRegistrationWriter(beanName, beanDefinition) : null;
	}

	@Nullable
	private BeanRegistrationWriter createScopedProxyBeanRegistrationWriter(String beanName, BeanDefinition beanDefinition) {
		Object targetBeanName = beanDefinition.getPropertyValues().get("targetBeanName");
		BeanDefinition targetBeanDefinition = getTargetBeanDefinition(targetBeanName);
		if (targetBeanDefinition == null) {
			logger.warn("Could not handle " + ScopedProxyFactoryBean.class.getSimpleName() +
					": no target bean definition found with name " + targetBeanName);
			return null;
		}
		RootBeanDefinition processedBeanDefinition = new RootBeanDefinition((RootBeanDefinition) beanDefinition);
		processedBeanDefinition.setTargetType(targetBeanDefinition.getResolvableType());
		processedBeanDefinition.getPropertyValues().removePropertyValue("targetBeanName");
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(ScopedProxyFactoryBean.class).build();
		return new DefaultBeanRegistrationWriter(beanName, processedBeanDefinition, descriptor) {
			@Override
			protected void writeInstanceSupplier(Builder code) {
				MultiStatement statements = new MultiStatement();
				statements.add("$T factory = new $T()", ScopedProxyFactoryBean.class, ScopedProxyFactoryBean.class);
				statements.add("factory.setTargetBeanName($S)", targetBeanName);
				statements.add("factory.setBeanFactory(beanFactory)");
				statements.add("return factory.getObject()");
				code.add(statements.toCodeBlock("() -> "));
			}
		};
	}

	@Nullable
	private BeanDefinition getTargetBeanDefinition(@Nullable Object targetBeanName) {
		if (targetBeanName instanceof String) {
			String beanName = (String) targetBeanName;
			if (this.beanFactory != null && this.beanFactory.containsBean(beanName)) {
				return this.beanFactory.getMergedBeanDefinition(beanName);
			}
		}
		return null;
	}

}

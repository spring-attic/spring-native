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

package org.springframework.boot.context.properties;

import java.lang.reflect.Constructor;
import java.util.function.Predicate;

import com.squareup.javapoet.CodeBlock.Builder;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean.BindMethod;
import org.springframework.boot.context.properties.bind.BindConstructorProvider;
import org.springframework.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.context.bootstrap.generator.bean.BeanRegistrationWriterSupplier;
import org.springframework.context.bootstrap.generator.bean.DefaultBeanRegistrationWriter;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.core.annotation.Order;

/**
 * A {@link BeanRegistrationWriterSupplier} that handles immutable
 * {@link ConfigurationProperties}-annotated types.
 *
 * @author Stephane Nicoll
 */
@Order(0)
class ConfigurationPropertiesBeanRegistrationWriterSupplier implements BeanRegistrationWriterSupplier {

	private static final BindConstructorProvider bindConstructorProvider = new ConfigurationPropertiesBindConstructorProvider();

	@Override
	public BeanRegistrationWriter get(String beanName, BeanDefinition beanDefinition) {
		if (!isImmutableConfigurationPropertiesBeanDefinition(beanDefinition)) {
			return null;
		}
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(beanDefinition.getResolvableType())
				.withInstanceCreator(getBindConstructor(beanName, beanDefinition)).build();
		return new DefaultBeanRegistrationWriter(beanName, beanDefinition, descriptor) {

			@Override
			protected Predicate<String> getAttributeFilter() {
				return (candidate) -> candidate.equals(BindMethod.class.getName());
			}

			@Override
			protected boolean shouldDeclareCreator(BeanInstanceDescriptor descriptor) {
				return false;
			}

			@Override
			protected void writeInstanceSupplier(Builder code) {
				code.add("() -> $T.bind(context.getBeanFactory(), $S, $T.class)",
						ConstructorBindingValueSupplier.class, beanName, descriptor.getUserBeanClass());
			}
		};
	}

	private Constructor<?> getBindConstructor(String beanName, BeanDefinition beanDefinition) {
		ConfigurationPropertiesBean bean = ConfigurationPropertiesBean.forValueObject(
				beanDefinition.getResolvableType().toClass(), beanName);
		return bindConstructorProvider.getBindConstructor(bean.asBindTarget(), false);
	}

	private boolean isImmutableConfigurationPropertiesBeanDefinition(BeanDefinition beanDefinition) {
		return beanDefinition.hasAttribute(BindMethod.class.getName())
				&& BindMethod.VALUE_OBJECT.equals(beanDefinition.getAttribute(BindMethod.class.getName()));
	}

}

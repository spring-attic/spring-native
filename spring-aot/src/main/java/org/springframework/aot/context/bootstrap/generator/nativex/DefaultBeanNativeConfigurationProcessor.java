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

package org.springframework.aot.context.bootstrap.generator.nativex;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.PropertyDescriptor;
import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptorFactory;
import org.springframework.aot.context.bootstrap.generator.bean.descriptor.DefaultBeanInstanceDescriptorFactory;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ReflectionConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Register the reflection entries for each {@link BeanInstanceDescriptor}.
 *
 * @author Stephane Nicoll
 */
class DefaultBeanNativeConfigurationProcessor implements BeanNativeConfigurationProcessor, BeanFactoryAware {

	private BeanInstanceDescriptorFactory beanInstanceDescriptorFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanInstanceDescriptorFactory = new DefaultBeanInstanceDescriptorFactory(
				(ConfigurableBeanFactory) beanFactory);
	}

	@Override
	public void process(BeanInstanceDescriptor descriptor, NativeConfigurationRegistry registry) {
		ReflectionConfiguration reflectionConfiguration = registry.reflection();
		MemberDescriptor<Executable> instanceCreator = descriptor.getInstanceCreator();
		if (instanceCreator != null) {
			reflectionConfiguration.addExecutable(instanceCreator.getMember());
		}
		for (MemberDescriptor<?> injectionPoint : descriptor.getInjectionPoints()) {
			Member member = injectionPoint.getMember();
			if (member instanceof Executable) {
				reflectionConfiguration.addExecutable((Method) member);
			}
			else if (member instanceof Field) {
				reflectionConfiguration.addField((Field) member);
			}
		}
		for (PropertyDescriptor property : descriptor.getProperties()) {
			Method writeMethod = property.getWriteMethod();
			if (writeMethod != null) {
				reflectionConfiguration.addExecutable(writeMethod);
			}
			Object value = property.getPropertyValue().getValue();
			processValue(value, registry);
		}
	}

	private void processValue(Object value, NativeConfigurationRegistry registry) {
		if (value == null) {
			return;
		}
		else if (value instanceof BeanDefinition) {
			processInnerBeanDefinition((BeanDefinition) value, registry);
		}
		else if (value instanceof Iterable) {
			Iterable<?> iterable = (Iterable<?>) value;
			iterable.forEach((item) -> processValue(item, registry));
		}
		else if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			for (int i = 0; i < length; i++) {
				processValue(Array.get(value, i), registry);
			}
		}
	}

	private void processInnerBeanDefinition(BeanDefinition beanDefinition, NativeConfigurationRegistry registry) {
		if (this.beanInstanceDescriptorFactory != null) {
			process(this.beanInstanceDescriptorFactory.create(beanDefinition), registry);
		}
	}

}
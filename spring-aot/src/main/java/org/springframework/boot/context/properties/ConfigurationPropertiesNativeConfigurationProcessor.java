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

import java.lang.reflect.Field;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.nativex.hint.Flag;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link BeanFactoryNativeConfigurationProcessor} that allows reflection access on
 * all declared methods of {@link ConfigurationProperties @ConfigurationProperties}
 * annotated types, their nested types and any complex types that are exposed as a sub-
 * namespace.
 *
 * @author Stephane Nicoll
 * @author Christoph Strobl
 */
class ConfigurationPropertiesNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		String[] beanNames = beanFactory.getBeanNamesForAnnotation(ConfigurationProperties.class);
		for (String beanName : beanNames) {
			processConfigurationProperties(registry, beanFactory.getMergedBeanDefinition(beanName));
		}
	}

	private void processConfigurationProperties(NativeConfigurationRegistry registry, BeanDefinition beanDefinition) {
		Class<?> type = ClassUtils.getUserClass(beanDefinition.getResolvableType().toClass());
		registerWithNestedMembersIfNecessary(registry, type);
		ReflectionUtils.doWithFields(type, (field) -> registerWithNestedMembersIfNecessary(registry, field.getType()),
				this::isNestedConfigurationProperties);
	}

	private void registerWithNestedMembersIfNecessary(NativeConfigurationRegistry registry, Class<?> type) {
		registry.reflection().forType(type).withFlags(Flag.allDeclaredMethods)
				.withFlags(Flag.allDeclaredConstructors);
		for (Class<?> nested : type.getDeclaredClasses()) {
			registerWithNestedMembersIfNecessary(registry, nested);
		}
	}

	private boolean isNestedConfigurationProperties(Field field) {
		return (field.getAnnotation(NestedConfigurationProperty.class) != null);
	}

}

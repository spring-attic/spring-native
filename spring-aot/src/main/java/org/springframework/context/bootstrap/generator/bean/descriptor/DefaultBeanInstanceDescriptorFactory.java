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

package org.springframework.context.bootstrap.generator.bean.descriptor;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.InstanceCallback;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.PropertyDescriptor;
import org.springframework.util.Assert;

/**
 * A default {@link BeanInstanceDescriptorFactory} implementation using the standard
 * framework contract to detect the members to use to fully instantiate a bean.
 *
 * @author Stephane Nicoll
 * @author Christoph Strobl
 */
public class DefaultBeanInstanceDescriptorFactory implements BeanInstanceDescriptorFactory {

	private final BeanInstanceExecutableSupplier instanceCreatorSupplier;

	private final InstanceCallbacksSupplier instanceCallbacksSupplier;

	private final InjectionPointsSupplier injectionPointsSupplier;

	private final PropertiesSupplier propertiesSupplier;

	private final InitializationMethodSupplier initializationMethodSupplier;

	public DefaultBeanInstanceDescriptorFactory(ConfigurableBeanFactory beanFactory) {
		this.instanceCreatorSupplier = new BeanInstanceExecutableSupplier(beanFactory);
		this.instanceCallbacksSupplier = new InstanceCallbacksSupplier();
		this.injectionPointsSupplier = new InjectionPointsSupplier(beanFactory.getBeanClassLoader());
		this.propertiesSupplier = new PropertiesSupplier();
		this.initializationMethodSupplier = new InitializationMethodSupplier();
	}

	@Override
	public BeanInstanceDescriptor create(BeanDefinition beanDefinition) {
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		Executable instanceCreator = this.instanceCreatorSupplier.detectBeanInstanceExecutable(beanDefinition);
		if (instanceCreator != null) {
			Class<?> beanType = beanDefinition.getResolvableType().toClass();
			List<InstanceCallback> instanceCallbacks = this.instanceCallbacksSupplier.detectInstanceCallbacks(beanType);
			List<MemberDescriptor<?>> injectionPoints = this.injectionPointsSupplier.detectInjectionPoints(beanType);
			List<PropertyDescriptor> properties = this.propertiesSupplier.detectProperties(beanDefinition);
			List<MemberDescriptor<Method>> initializationCallbacks = this.initializationMethodSupplier.getInstanceCallbacks(beanDefinition);
			return BeanInstanceDescriptor.of(beanDefinition.getResolvableType())
					.withInstanceCreator(instanceCreator).withInstanceCallbacks(instanceCallbacks)
					.withInjectionPoints(injectionPoints).withProperties(properties)
					.withInitMethods(initializationCallbacks)
					.build();
		}
		return null;
	}

}

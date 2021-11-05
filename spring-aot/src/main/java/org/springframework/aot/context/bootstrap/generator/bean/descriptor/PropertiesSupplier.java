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

package org.springframework.aot.context.bootstrap.generator.bean.descriptor;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.PropertyDescriptor;
import org.springframework.beans.BeanInfoFactory;
import org.springframework.beans.ExtendedBeanInfoFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Provide the {@link PropertyDescriptor properties} defined on a {@link BeanDefinition}.
 *
 * @author Stephane Nicoll
 */
class PropertiesSupplier {

	private static final BeanInfoFactory beanInfoFactory = new ExtendedBeanInfoFactory();

	List<PropertyDescriptor> detectProperties(BeanDefinition beanDefinition) {
		List<PropertyDescriptor> properties = new ArrayList<>();
		if (!beanDefinition.hasPropertyValues()) {
			return properties;
		}
		BeanInfo beanInfo = getBeanInfo(beanDefinition.getResolvableType().toClass());
		if (beanInfo == null) {
			return properties;
		}
		for (PropertyValue propertyValue : beanDefinition.getPropertyValues().getPropertyValueList()) {
			Method writeMethod = findWriteMethod(beanInfo, propertyValue.getName());
			properties.add(new PropertyDescriptor(writeMethod, propertyValue));
		}
		return properties;
	}

	private BeanInfo getBeanInfo(Class<?> beanType) {
		try {
			BeanInfo beanInfo = beanInfoFactory.getBeanInfo(beanType);
			if (beanInfo != null) {
				return beanInfo;
			}
			return Introspector.getBeanInfo(beanType, Introspector.IGNORE_ALL_BEANINFO);
		}
		catch (IntrospectionException ex) {
			return null;
		}
	}

	private Method findWriteMethod(BeanInfo beanInfo, String propertyName) {
		return Arrays.stream(beanInfo.getPropertyDescriptors())
				.filter((pd) -> propertyName.equals(pd.getName()))
				.map(java.beans.PropertyDescriptor::getWriteMethod)
				.filter(Objects::nonNull).findFirst().orElse(null);
	}

}

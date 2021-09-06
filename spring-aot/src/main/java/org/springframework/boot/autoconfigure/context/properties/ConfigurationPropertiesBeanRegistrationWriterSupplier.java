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

package org.springframework.boot.autoconfigure.context.properties;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.validation.beanvalidation.MethodValidationExcludeFilter;
import org.springframework.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.context.bootstrap.generator.bean.BeanRegistrationWriterSupplier;
import org.springframework.context.bootstrap.generator.bean.DefaultBeanRegistrationWriter;
import org.springframework.context.bootstrap.generator.bean.SimpleBeanValueWriter;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.core.annotation.Order;
import org.springframework.util.ReflectionUtils;

/**
 * {@link BeanRegistrationWriterSupplier} for {@link ConfigurationProperties} support.
 *
 * @author Stephane Nicoll
 */
@Order(0)
class ConfigurationPropertiesBeanRegistrationWriterSupplier implements BeanRegistrationWriterSupplier {

	@Override
	public BeanRegistrationWriter get(String beanName, BeanDefinition beanDefinition) {
		if (MethodValidationExcludeFilter.class.getName().equals(beanDefinition.getBeanClassName())) {
			BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(MethodValidationExcludeFilter.class)
					.withInstanceCreator(ReflectionUtils.findMethod(MethodValidationExcludeFilter.class, "byAnnotation", Class.class))
					.build();
			return new DefaultBeanRegistrationWriter(beanName, beanDefinition, createBeanValueWriter(descriptor));
		}
		return null;
	}

	private SimpleBeanValueWriter createBeanValueWriter(BeanInstanceDescriptor descriptor) {
		return new SimpleBeanValueWriter(descriptor, (code) -> code.add("() -> $T.byAnnotation($T.class)",
				MethodValidationExcludeFilter.class, ConfigurationProperties.class));
	}

}

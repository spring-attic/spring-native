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

package org.springframework.boot.autoconfigure;

import java.util.function.Supplier;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages.BasePackages;
import org.springframework.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.context.bootstrap.generator.bean.BeanRegistrationWriterSupplier;
import org.springframework.context.bootstrap.generator.bean.BeanValueWriter;
import org.springframework.context.bootstrap.generator.bean.DefaultBeanRegistrationWriter;
import org.springframework.context.bootstrap.generator.bean.SimpleBeanValueWriter;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.support.ParameterWriter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;

/**
 * {@link BeanRegistrationWriterSupplier} for {@link AutoConfigurationPackages}.
 *
 * @author Stephane Nicoll
 */
@Order(0)
class AutoConfigurationPackagesBeanRegistrationWriterSupplier implements BeanRegistrationWriterSupplier {

	private final ParameterWriter parameterWriter = new ParameterWriter();

	@Override
	public BeanRegistrationWriter get(String beanName, BeanDefinition beanDefinition) {
		if (BasePackages.class.getName().equals(beanDefinition.getBeanClassName())) {
			BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(BasePackages.class)
					.withInstanceCreator(BasePackages.class.getDeclaredConstructors()[0]).build();
			BeanValueWriter valueWriter = createBeanValueWriter(beanDefinition, descriptor);
			return new DefaultBeanRegistrationWriter(beanName, beanDefinition, valueWriter);
		}
		return null;
	}

	private BeanValueWriter createBeanValueWriter(BeanDefinition beanDefinition, BeanInstanceDescriptor descriptor) {
		return new SimpleBeanValueWriter(descriptor, (code) -> {
			code.add("() -> new $T(", BasePackages.class);
			code.add(this.parameterWriter.writeParameterValue(getPackageNames(beanDefinition),
					ResolvableType.forArrayComponent(ResolvableType.forClass(String.class))));
			code.add(")"); // End of constructor
		});
	}

	private String[] getPackageNames(BeanDefinition beanDefinition) {
		Supplier<?> instanceSupplier = ((RootBeanDefinition) beanDefinition).getInstanceSupplier();
		BasePackages basePackages = (BasePackages) instanceSupplier.get();
		return basePackages.get().toArray(new String[0]);
	}

}

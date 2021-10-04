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

import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.sample.injection.InjectionConfiguration;
import org.springframework.beans.PropertyValue;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BeanInstanceDescriptor}.
 *
 * @author Stephane Nicoll
 */
class BeanInstanceDescriptorTests {

	@Test
	void descriptorWithSimpleType() {
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(String.class).build();
		assertThat(descriptor.getBeanType().toClass()).isEqualTo(String.class);
		assertThat(descriptor.getUserBeanClass()).isEqualTo(String.class);
	}

	@Test
	void descriptorWithGenericType() {
		ResolvableType beanType = ResolvableType.forClassWithGenerics(Supplier.class, Integer.class);
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(beanType).build();
		assertThat(descriptor.getBeanType()).isSameAs(beanType);
		assertThat(descriptor.getUserBeanClass()).isEqualTo(Supplier.class);
	}

	@Test
	void descriptorWithPropertyValue() {
		Method writeMethod = ReflectionUtils.findMethod(InjectionConfiguration.class, "setName", String.class);
		PropertyValue propertyValue = new PropertyValue("name", "test");
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(InjectionConfiguration.class).withProperty(writeMethod, propertyValue).build();
		assertThat(descriptor.getProperties()).singleElement().satisfies((candidate) -> {
			assertThat(candidate.getWriteMethod()).isEqualTo(writeMethod);
			assertThat(candidate.getPropertyValue()).isEqualTo(propertyValue);
		});
	}
}

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

package org.springframework.context.bootstrap.generator.bean;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultBeanValueWriterSupplier}.
 *
 * @author Stephane Nicoll
 */
class DefaultBeanValueWriterSupplierTests {

	@Test
	void getWithRootBeanDefinitionProvideDefaultWriter() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanValueWriter writer = getBeanValueWriter(beanFactory, BeanDefinitionBuilder.rootBeanDefinition(
				DefaultBeanValueWriterTests.class).getBeanDefinition());
		assertThat(writer).isNotNull().isInstanceOf(DefaultBeanValueWriter.class);
		assertThat(writer.getDescriptor().getUserBeanClass()).isEqualTo(DefaultBeanValueWriterTests.class);
	}

	@Test
	void getWithIncompatibleBeanDefinitionReturnNull() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanValueWriter writer = getBeanValueWriter(beanFactory, BeanDefinitionBuilder
				.genericBeanDefinition("com.example.Test").getBeanDefinition());
		assertThat(writer).isNull();
	}

	private BeanValueWriter getBeanValueWriter(DefaultListableBeanFactory beanFactory, BeanDefinition beanDefinition) {
		DefaultBeanValueWriterSupplier supplier = new DefaultBeanValueWriterSupplier();
		supplier.setBeanFactory(beanFactory);
		return supplier.get(beanDefinition);
	}

}

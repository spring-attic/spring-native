/*
 * Copyright 2019-2022 the original author or authors.
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

package org.springframework.context.annotation;

import org.junit.jupiter.api.Test;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link BeanClassBeanDefinitionPostProcessor}.
 *
 * @author Stephane Nicoll
 */
class BeanClassBeanDefinitionPostProcessorTests {

	@Test
	void postProcessBeanDefinitionLoadBeanClass() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition("java.lang.String");
		assertThat(beanDefinition.hasBeanClass()).isFalse();
		postProcess(beanDefinition);
		assertThat(beanDefinition.hasBeanClass()).isTrue();
		assertThat(beanDefinition.getBeanClass()).isEqualTo(String.class);
	}

	@Test
	void postProcessBeanDefinitionWithBeanClassIsIgnored() {
		RootBeanDefinition beanDefinition = mock(RootBeanDefinition.class);
		given(beanDefinition.hasBeanClass()).willReturn(true);
		given(beanDefinition.getPropertyValues()).willReturn(new MutablePropertyValues());
		given(beanDefinition.getConstructorArgumentValues()).willReturn(new ConstructorArgumentValues());
		postProcess(beanDefinition);
		verify(beanDefinition).hasBeanClass();
		verify(beanDefinition).getPropertyValues();
		verify(beanDefinition).getConstructorArgumentValues();
		verifyNoMoreInteractions(beanDefinition);
	}

	@Test
	void postProcessBeanDefinitionIgnoreInvalidBeanClassName() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition("com.example.DoesNotExist");
		postProcess(beanDefinition);
		assertThat(beanDefinition.hasBeanClass()).isFalse();
	}

	@Test
	void postProcessInnerBeanDefinitionProperty() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(String.class);
		GenericBeanDefinition innerBeanDefinition = new GenericBeanDefinition();
		innerBeanDefinition.setBeanClassName("java.lang.Integer");
		assertThat(innerBeanDefinition.hasBeanClass()).isFalse();
		beanDefinition.getPropertyValues().addPropertyValue("counter", innerBeanDefinition);
		postProcess(beanDefinition);
		assertThat(beanDefinition.getPropertyValues().get("counter")).isEqualTo(innerBeanDefinition);
		assertThat(innerBeanDefinition.hasBeanClass()).isTrue();
		assertThat(innerBeanDefinition.getBeanClass()).isEqualTo(Integer.class);
	}

	@Test
	void postProcessInnerBeanDefinitionArgument() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(String.class);
		GenericBeanDefinition innerBeanDefinition = new GenericBeanDefinition();
		innerBeanDefinition.setBeanClassName("java.lang.Integer");
		assertThat(innerBeanDefinition.hasBeanClass()).isFalse();
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, innerBeanDefinition);
		postProcess(beanDefinition);
		assertThat(beanDefinition.getConstructorArgumentValues().getIndexedArgumentValues().get(0).getValue())
				.isEqualTo(innerBeanDefinition);
		assertThat(innerBeanDefinition.hasBeanClass()).isTrue();
		assertThat(innerBeanDefinition.getBeanClass()).isEqualTo(Integer.class);
	}

	private void postProcess(RootBeanDefinition beanDefinition) {
		BeanClassBeanDefinitionPostProcessor processor = new BeanClassBeanDefinitionPostProcessor();
		processor.setBeanFactory(new DefaultListableBeanFactory());
		processor.postProcessBeanDefinition("test", beanDefinition);
	}

}

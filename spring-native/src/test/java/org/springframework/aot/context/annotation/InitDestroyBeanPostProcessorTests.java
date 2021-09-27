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

package org.springframework.aot.context.annotation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link InitDestroyBeanPostProcessor}.
 *
 * @author Stephane Nicoll
 */
class InitDestroyBeanPostProcessorTests {

	@Test
	void initMethodIsInvoked() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.addBeanPostProcessor(new InitDestroyBeanPostProcessor(Map.of("test", List.of("start")), Collections.emptyMap()));
		beanFactory.registerBeanDefinition("test", mockBeanDefinition(LifecycleSampleBean.class));
		LifecycleSampleBean bean = beanFactory.getBean("test", LifecycleSampleBean.class);
		verify(bean).start();
		verifyNoMoreInteractions(bean);
	}

	@Test
	void initMethodIsInvokedInOrder() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.addBeanPostProcessor(new InitDestroyBeanPostProcessor(Map.of("test", List.of("start", "start2")), Collections.emptyMap()));
		beanFactory.registerBeanDefinition("test", mockBeanDefinition(LifecycleSampleBean.class));
		LifecycleSampleBean bean = beanFactory.getBean("test", LifecycleSampleBean.class);
		InOrder inOrder = inOrder(bean);
		inOrder.verify(bean).start();
		inOrder.verify(bean).start2();
		verifyNoMoreInteractions(bean);
	}

	@Test
	void destroyMethodIsInvoked() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.addBeanPostProcessor(new InitDestroyBeanPostProcessor(Collections.emptyMap(), Map.of("test", List.of("stop"))));
		beanFactory.registerBeanDefinition("test", mockBeanDefinition(LifecycleSampleBean.class));
		LifecycleSampleBean bean = beanFactory.getBean("test", LifecycleSampleBean.class);
		verifyNoInteractions(bean);
		beanFactory.destroySingletons();
		verify(bean).stop();
		verifyNoMoreInteractions(bean);
	}

	@Test
	void destroyMethodIsInvokedInOrder() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.addBeanPostProcessor(new InitDestroyBeanPostProcessor(Collections.emptyMap(),
				Map.of("test", List.of("stop", "stop2"))));
		beanFactory.registerBeanDefinition("test", mockBeanDefinition(LifecycleSampleBean.class));
		LifecycleSampleBean bean = beanFactory.getBean("test", LifecycleSampleBean.class);
		beanFactory.destroySingletons();
		InOrder inOrder = inOrder(bean);
		inOrder.verify(bean).stop();
		inOrder.verify(bean).stop2();
		verifyNoMoreInteractions(bean);
	}

	private RootBeanDefinition mockBeanDefinition(Class<?> type) {
		Object instance = mock(type);
		RootBeanDefinition beanDefinition = new RootBeanDefinition(type);
		beanDefinition.setInstanceSupplier(() -> instance);
		return beanDefinition;
	}

	static class LifecycleSampleBean {

		public void start() {
		}

		void start2() {

		}

		public void stop() {
		}

		void stop2() {

		}

	}

}

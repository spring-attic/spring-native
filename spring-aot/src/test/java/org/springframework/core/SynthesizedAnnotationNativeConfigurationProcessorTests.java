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

package org.springframework.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.SynthesizedAnnotation;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Tests for {@link SynthesizedAnnotationNativeConfigurationProcessor}.
 *
 * @author Andy Clement
 */
class SynthesizedAnnotationNativeConfigurationProcessorTests {

	@Test
	void eventListenerSynthesis() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(Foo.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.proxy().getEntries()).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		JdkProxyDescriptor pd = proxyDescriptors.iterator().next();
		assertThat(pd.getTypes()).containsExactly(EventListener.class.getName(),SynthesizedAnnotation.class.getName());
	}

	@Test
	void responseStatusSynthesis() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(Bar.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.proxy().getEntries()).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		JdkProxyDescriptor pd = proxyDescriptors.iterator().next();
		assertThat(pd.getTypes()).containsExactly(ResponseStatus.class.getName(),SynthesizedAnnotation.class.getName());
	}

	@Test
	void nonLocalAliasTest() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(Boo.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.proxy().getEntries()).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		JdkProxyDescriptor pd = proxyDescriptors.iterator().next();
		assertThat(pd.getTypes()).containsExactly(Component.class.getName(),SynthesizedAnnotation.class.getName());
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new SynthesizedAnnotationNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}

	@Component
	class Foo {
		@EventListener
		void foo() {
		}
	}
	
	@Component
	class Bar {
		@ResponseStatus
		void bar() {
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface AliasTest {
		@AliasFor(annotation = Component.class,attribute="value")
		String value();
	}
	
	@Component
	class Boo {
		@AliasTest(value="abc")
		void boo() {
		}
	}

}

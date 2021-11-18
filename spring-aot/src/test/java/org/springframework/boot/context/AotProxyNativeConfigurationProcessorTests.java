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

package org.springframework.boot.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
import org.springframework.aot.context.bootstrap.generator.sample.factory.TestGenericFactoryBeanConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Tests for {@link AotProxyNativeConfigurationProcessor}.
 *
 * @author Andy Clement
 */
class AotProxyNativeConfigurationProcessorTests {

	@Test
	void componentWithAsyncMarkedMethod() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(Async1.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<NativeProxyEntry> proxyEntries = registry.proxy().getEntries();
		assertThat(proxyEntries).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		assertThat(proxyDescriptors).hasSize(1);
		JdkProxyDescriptor proxyDescriptor = proxyDescriptors.iterator().next();
		assertThat(proxyDescriptor).isInstanceOf(AotProxyDescriptor.class);
		AotProxyDescriptor aotProxyDescriptor = (AotProxyDescriptor)proxyDescriptor;
		assertThat(aotProxyDescriptor.getTargetClassType()).isEqualTo(Async1.class.getName());
		assertThat(aotProxyDescriptor.getInterfaceTypes()).isEmpty();
		assertThat(aotProxyDescriptor.getProxyFeatures()).isEqualTo(ProxyBits.IS_STATIC);
	}

	@Test
	void componentWithNullType() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("genericBeanFactory", BeanDefinitionBuilder.rootBeanDefinition(
				TestGenericFactoryBeanConfiguration.class, "testGenericFactoryBean").getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.proxy().getEntries()).isEmpty();
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new AotProxyNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}

	@Component
	static class Async1 {
		@Async
		public void foo() {
		}
	}
}

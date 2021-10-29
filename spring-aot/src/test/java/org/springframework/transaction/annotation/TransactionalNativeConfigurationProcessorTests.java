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

package org.springframework.transaction.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.DecoratingProxy;
import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.stereotype.Component;

/**
 * Tests for {@link TransactionalNativeConfigurationProcessor}.
 *
 * @author Andy Clement
 */
class TransactionalNativeConfigurationProcessorTests {

	@Test
	void transactionalClass() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(TxClass.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<NativeProxyEntry> proxyEntries = registry.proxy().getEntries();
		assertThat(proxyEntries).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		assertThat(proxyDescriptors).hasSize(1);
		JdkProxyDescriptor proxyDescriptor = proxyDescriptors.iterator().next();
		assertThat(proxyDescriptor).isInstanceOf(AotProxyDescriptor.class);
		AotProxyDescriptor aotProxyDescriptor = (AotProxyDescriptor)proxyDescriptor;
		assertThat(aotProxyDescriptor.getTargetClassType()).isEqualTo(TxClass.class.getName());
		assertThat(aotProxyDescriptor.getInterfaceTypes()).isEmpty();
		assertThat(aotProxyDescriptor.getProxyFeatures()).isEqualTo(ProxyBits.IS_STATIC);
	}

	@Test
	void transactionalMethod() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(TxClass2.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<NativeProxyEntry> proxyEntries = registry.proxy().getEntries();
		assertThat(proxyEntries).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		assertThat(proxyDescriptors).hasSize(1);
		JdkProxyDescriptor proxyDescriptor = proxyDescriptors.iterator().next();
		assertThat(proxyDescriptor).isInstanceOf(AotProxyDescriptor.class);
		AotProxyDescriptor aotProxyDescriptor = (AotProxyDescriptor)proxyDescriptor;
		assertThat(aotProxyDescriptor.getTargetClassType()).isEqualTo(TxClass2.class.getName());
		assertThat(aotProxyDescriptor.getInterfaceTypes()).isEmpty();
		assertThat(aotProxyDescriptor.getProxyFeatures()).isEqualTo(ProxyBits.IS_STATIC);
	}

	@Test
	void transactionalClassWithInterfaces() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(TxClass3.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<NativeProxyEntry> proxyEntries = registry.proxy().getEntries();
		assertThat(proxyEntries).hasSize(2);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		assertThat(proxyDescriptors).hasSize(2);
		Iterator<JdkProxyDescriptor> iterator = proxyDescriptors.iterator();
		JdkProxyDescriptor a = iterator.next();
		AotProxyDescriptor b = null;
		if (a instanceof AotProxyDescriptor) {
			b = (AotProxyDescriptor) a;
			a = iterator.next();
		} else {
			b = (AotProxyDescriptor) iterator.next();
		}
		
		assertThat(a).isNotInstanceOf(AotProxyDescriptor.class);

		assertThat(a.getTypes()).containsOnly(Serializable.class.getName(), 
				SpringProxy.class.getName(), Advised.class.getName(), DecoratingProxy.class.getName());
		
		assertThat(b.getTargetClassType()).isEqualTo(TxClass3.class.getName());
		assertThat(b.getInterfaceTypes()).isEmpty();
		assertThat(b.getProxyFeatures()).isEqualTo(ProxyBits.IS_STATIC);
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new TransactionalNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}

	@Transactional
	@Component
	static class TxClass {
		public void foo() {
		}
	}
	
	@Component
	static class TxClass2 {
		@Transactional
		public void foo() {
		}
	}

	@SuppressWarnings("serial")
	@Transactional
	@Component
	static class TxClass3 implements Serializable {
		public void foo() {
		}
	}
}

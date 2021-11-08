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

package org.springframework.security;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PrePostSecuredNativeConfigurationProcessor}.
 *
 * @author Andy Clement
 */
class PrePostSecuredNativeConfigurationProcessorTests {

	@Test
	void preAuthorizeAnnotationOnClass() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(PreAuthorizeClass.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<NativeProxyEntry> proxyEntries = registry.proxy().getEntries();
		assertThat(proxyEntries).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		assertThat(proxyDescriptors).hasSize(1);
		JdkProxyDescriptor proxyDescriptor = proxyDescriptors.iterator().next();
		assertThat(proxyDescriptor).isInstanceOf(AotProxyDescriptor.class);
		AotProxyDescriptor aotProxyDescriptor = (AotProxyDescriptor) proxyDescriptor;
		assertThat(aotProxyDescriptor.getTargetClassType()).isEqualTo(PreAuthorizeClass.class.getName());
		assertThat(aotProxyDescriptor.getInterfaceTypes()).isEmpty();
		assertThat(aotProxyDescriptor.getProxyFeatures()).isEqualTo(ProxyBits.IS_STATIC);
	}

	@Test
	void preAuthorizeAnnotationOnClassMethod() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(PreAuthorizeClassMethod.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<NativeProxyEntry> proxyEntries = registry.proxy().getEntries();
		assertThat(proxyEntries).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		assertThat(proxyDescriptors).hasSize(1);
		JdkProxyDescriptor proxyDescriptor = proxyDescriptors.iterator().next();
		assertThat(proxyDescriptor).isInstanceOf(AotProxyDescriptor.class);
		AotProxyDescriptor aotProxyDescriptor = (AotProxyDescriptor) proxyDescriptor;
		assertThat(aotProxyDescriptor.getTargetClassType()).isEqualTo(PreAuthorizeClassMethod.class.getName());
		assertThat(aotProxyDescriptor.getInterfaceTypes()).isEmpty();
		assertThat(aotProxyDescriptor.getProxyFeatures()).isEqualTo(ProxyBits.IS_STATIC);
	}

	@Test
	void preAuthorizeAnnotationOnInterface() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(PreAuthorizeExtendsInterface.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<NativeProxyEntry> proxyEntries = registry.proxy().getEntries();
		assertThat(proxyEntries).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		assertThat(proxyDescriptors).hasSize(1);
		Iterator<JdkProxyDescriptor> iterator = proxyDescriptors.iterator();
		JdkProxyDescriptor a = iterator.next();

		assertThat(a).isNotInstanceOf(AotProxyDescriptor.class);
		assertThat(a.getTypes()).containsOnly(PreAuthorizeInterface.class.getName(),
				SpringProxy.class.getName(), Advised.class.getName(), DecoratingProxy.class.getName());
	}

	@Test
	void preAuthorizeAnnotationOnInterfaceMethod() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(PreAuthorizeExtendsInterfaceMethod.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<NativeProxyEntry> proxyEntries = registry.proxy().getEntries();
		assertThat(proxyEntries).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		assertThat(proxyDescriptors).hasSize(1);
		Iterator<JdkProxyDescriptor> iterator = proxyDescriptors.iterator();
		JdkProxyDescriptor a = iterator.next();

		assertThat(a).isNotInstanceOf(AotProxyDescriptor.class);
		assertThat(a.getTypes()).containsOnly(PreAuthorizeInterfaceMethod.class.getName(),
				SpringProxy.class.getName(), Advised.class.getName(), DecoratingProxy.class.getName());
	}

	@Test
	void preFilterAnnotationOnClassMethod() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(PreFilterClassMethod.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<NativeProxyEntry> proxyEntries = registry.proxy().getEntries();
		assertThat(proxyEntries).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		assertThat(proxyDescriptors).hasSize(1);
		JdkProxyDescriptor proxyDescriptor = proxyDescriptors.iterator().next();
		assertThat(proxyDescriptor).isInstanceOf(AotProxyDescriptor.class);
		AotProxyDescriptor aotProxyDescriptor = (AotProxyDescriptor) proxyDescriptor;
		assertThat(aotProxyDescriptor.getTargetClassType()).isEqualTo(PreFilterClassMethod.class.getName());
		assertThat(aotProxyDescriptor.getInterfaceTypes()).isEmpty();
		assertThat(aotProxyDescriptor.getProxyFeatures()).isEqualTo(ProxyBits.IS_STATIC);
	}

	@Test
	void postAuthorizeAnnotationOnClassMethod() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(PostAuthorizeClassMethod.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<NativeProxyEntry> proxyEntries = registry.proxy().getEntries();
		assertThat(proxyEntries).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		assertThat(proxyDescriptors).hasSize(1);
		JdkProxyDescriptor proxyDescriptor = proxyDescriptors.iterator().next();
		assertThat(proxyDescriptor).isInstanceOf(AotProxyDescriptor.class);
		AotProxyDescriptor aotProxyDescriptor = (AotProxyDescriptor) proxyDescriptor;
		assertThat(aotProxyDescriptor.getTargetClassType()).isEqualTo(PostAuthorizeClassMethod.class.getName());
		assertThat(aotProxyDescriptor.getInterfaceTypes()).isEmpty();
		assertThat(aotProxyDescriptor.getProxyFeatures()).isEqualTo(ProxyBits.IS_STATIC);
	}

	@Test
	void postFilterAnnotationOnClassMethod() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(PostFilterClassMethod.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<NativeProxyEntry> proxyEntries = registry.proxy().getEntries();
		assertThat(proxyEntries).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		assertThat(proxyDescriptors).hasSize(1);
		JdkProxyDescriptor proxyDescriptor = proxyDescriptors.iterator().next();
		assertThat(proxyDescriptor).isInstanceOf(AotProxyDescriptor.class);
		AotProxyDescriptor aotProxyDescriptor = (AotProxyDescriptor) proxyDescriptor;
		assertThat(aotProxyDescriptor.getTargetClassType()).isEqualTo(PostFilterClassMethod.class.getName());
		assertThat(aotProxyDescriptor.getInterfaceTypes()).isEmpty();
		assertThat(aotProxyDescriptor.getProxyFeatures()).isEqualTo(ProxyBits.IS_STATIC);
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new PrePostSecuredNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Component
	static class PreAuthorizeClass {
		public void foo() {
		}
	}

	@Component
	static class PreAuthorizeClassMethod {
		@PreAuthorize("hasRole('ADMIN')")
		public void foo() {
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	interface PreAuthorizeInterface {
		void foo();
	}

	@Component
	static class PreAuthorizeExtendsInterface implements PreAuthorizeInterface {
		public void foo() {
		}
	}

	interface PreAuthorizeInterfaceMethod {
		@PreAuthorize("hasRole('ADMIN')")
		void foo();
	}

	@Component
	static class PreAuthorizeExtendsInterfaceMethod implements PreAuthorizeInterfaceMethod {
		public void foo() {
		}
	}

	@Component
	static class PreFilterClassMethod {
		@PreFilter("filterObject.length > 3")
		public void foo(String[] array) {
		}
	}

	@Component
	static class PostAuthorizeClassMethod {
		@PostAuthorize("returnObject.size == 2")
		public List<String> foo() {
			return Collections.emptyList();
		}
	}

	@Component
	static class PostFilterClassMethod {
		@PostFilter("filterObject.length > 5")
		public List<String> foo() {
			return Collections.emptyList();
		}
	}
}
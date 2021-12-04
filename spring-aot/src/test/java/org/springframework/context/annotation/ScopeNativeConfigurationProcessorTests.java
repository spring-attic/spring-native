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
package org.springframework.context.annotation;

import java.io.Serializable;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.samples.scope.RequestScopedComponent;
import org.springframework.context.annotation.samples.scope.ScopedComponent;
import org.springframework.context.annotation.samples.simple.SimpleComponent;
import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ScopeNativeConfigurationProcessor}.
 *
 * @author Sebastien Deleuze
 */
public class ScopeNativeConfigurationProcessorTests {

	@Test
	void typeLevelScope() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("scopedComponent",  BeanDefinitionBuilder.rootBeanDefinition(ScopedComponent.class)
				.getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<JdkProxyDescriptor> proxyDescriptors =  registry.proxy().toProxiesDescriptor().getProxyDescriptors();
		assertThat(proxyDescriptors).singleElement().isInstanceOf(AotProxyDescriptor.class).satisfies((descriptor) -> {
					assertThat(descriptor.isClassProxy()).isEqualTo(true);
					assertThat(((AotProxyDescriptor)descriptor).getInterfaceTypes()).containsExactly(ScopedObject.class.getName(), Serializable.class.getName(), AopInfrastructureBean.class.getName());
				});
	}

	@Test
	void typeLevelRequestScope() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("requestScopedComponent",  BeanDefinitionBuilder.rootBeanDefinition(RequestScopedComponent.class)
				.getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<JdkProxyDescriptor> proxyDescriptors =  registry.proxy().toProxiesDescriptor().getProxyDescriptors();
		assertThat(proxyDescriptors).singleElement().isInstanceOf(AotProxyDescriptor.class).satisfies((descriptor) -> {
			assertThat(descriptor.isClassProxy()).isEqualTo(true);
			assertThat(((AotProxyDescriptor)descriptor).getInterfaceTypes()).containsExactly(ScopedObject.class.getName(), Serializable.class.getName(), AopInfrastructureBean.class.getName());
		});
	}

	@Test
	void noScope() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("simpleComponent",  BeanDefinitionBuilder.rootBeanDefinition(SimpleComponent.class)
				.getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<JdkProxyDescriptor> proxyDescriptors =  registry.proxy().toProxiesDescriptor().getProxyDescriptors();
		assertThat(proxyDescriptors).isEmpty();
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new ScopeNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}
}

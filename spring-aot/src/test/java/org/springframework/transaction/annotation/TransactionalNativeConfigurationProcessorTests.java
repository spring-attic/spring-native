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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
 * @author Petr Hejl
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
		assertThat(proxyEntries).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		JdkProxyDescriptor a = proxyDescriptors.iterator().next();
		assertThat(a).isNotInstanceOf(AotProxyDescriptor.class);
		assertThat(a.getTypes()).containsOnly(Bar.class.getName(),
				SpringProxy.class.getName(), Advised.class.getName(), DecoratingProxy.class.getName());
	}

	@Test
	public void shouldDescribeProxiesForComponentWithTransactionalInterface() {
		checkProxies(ComponentWithTransactionalInterface.class,
				Arrays.asList(
						TransactionalInterface.class.getName(),
						"org.springframework.aop.SpringProxy",
						"org.springframework.aop.framework.Advised",
						"org.springframework.core.DecoratingProxy"),
				null);
	}

	@Test
	public void shouldDescribeProxiesForComponentWithTransactionalInterfaceAndExtraInterface() {
		checkProxies(ComponentWithTransactionalInterfaceAndExtraInterface.class,
				Arrays.asList(
						TransactionalInterface.class.getName(),
						VoidInterface.class.getName(),
						"org.springframework.aop.SpringProxy",
						"org.springframework.aop.framework.Advised",
						"org.springframework.core.DecoratingProxy"),
				null);
	}

	@Test
	public void shouldDescribeProxiesForComponentWithTransactionalInterfaceAndOtherMethod() {
		checkProxies(ComponentWithTransactionalInterfaceAndOtherMethod.class,
				Arrays.asList(
						TransactionalInterface.class.getName(),
						"org.springframework.aop.SpringProxy",
						"org.springframework.aop.framework.Advised",
						"org.springframework.core.DecoratingProxy"),
				null);
	}

	@Test
	public void shouldDescribeProxiesForComponentWithTransactionalInterfaceOverSuperClass() {
		checkProxies(ComponentWithTransactionalInterfaceOverProxyClass.class,
				Arrays.asList(
						TransactionalInterface.class.getName(),
						"org.springframework.aop.SpringProxy",
						"org.springframework.aop.framework.Advised",
						"org.springframework.core.DecoratingProxy"),
				null);
	}

	@Test
	public void shouldDescribeProxiesForComponentWithTransactionalInterfaceOverSuperInterface() {
		checkProxies(ComponentWithTransactionalInterfaceOverProxyInterface.class,
				Arrays.asList(
						ProxyInterface.class.getName(),
						"org.springframework.aop.SpringProxy",
						"org.springframework.aop.framework.Advised",
						"org.springframework.core.DecoratingProxy"),
				null);
	}

	@Test
	public void shouldDescribeProxiesForComponentWithTransactionalInterfaceWithoutMethod() {
		checkProxies(ComponentWithTransactionalInterfaceWithoutMethod.class,
				null,
				ComponentWithTransactionalInterfaceWithoutMethod.class.getName());
	}

	@Test
	public void shouldDescribeProxiesForComponentWithTransactionalInterfaceWithoutMethodAndOtherMethod() {
		checkProxies(ComponentWithTransactionalInterfaceWithoutMethodAndOtherMethod.class,
				null,
				ComponentWithTransactionalInterfaceWithoutMethodAndOtherMethod.class.getName());//asrg.springframework.transactional.components.ComponentWithTransactionalInterfaceWithoutMethodAndOtherMethod");
	}

	@Test
	public void shouldDescribeProxiesForComponentWithTransactionalInterfaceWithDefault() {
		checkProxies(ComponentWithTransactionalInterfaceWithDefault.class,
				Arrays.asList(
						TransactionalInterfaceWithDefault.class.getName(),
						"org.springframework.aop.SpringProxy",
						"org.springframework.aop.framework.Advised",
						"org.springframework.core.DecoratingProxy"),
				null);
	}

	@Test
	public void shouldDescribeProxiesForComponentWithTransactionalMethod() {
		checkProxies(ComponentWithTransactionalMethod.class,
				null,
				ComponentWithTransactionalMethod.class.getName());
	}

	@Test
	public void shouldDescribeProxiesForTransactionalComponent() {
		checkProxies(TransactionalComponent.class,
				null,
				TransactionalComponent.class.getName());
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new TransactionalNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}

	private void checkProxies(Class<?> component, List<String> jdkProxy, String aotProxy) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("testComponent", BeanDefinitionBuilder.rootBeanDefinition(component).getBeanDefinition());

		NativeConfigurationRegistry registry = process(beanFactory);
		System.out.println(registry.proxy().toProxiesDescriptor());
		List<JdkProxyDescriptor> computedClassProxies = registry.proxy().toProxiesDescriptor().getProxyDescriptors().stream().filter(pd -> pd.isClassProxy()).collect(Collectors.toList());
		List<JdkProxyDescriptor> computedJdkProxies = registry.proxy().toProxiesDescriptor().getProxyDescriptors().stream().filter(pd -> !pd.isClassProxy()).collect(Collectors.toList());

		// and has correct proxies
		if (jdkProxy != null) {
			assertThat(computedJdkProxies).isNotEmpty();
			JdkProxyDescriptor jdkProxyDescriptor = computedJdkProxies.get(0);
			assertThat(jdkProxyDescriptor.getTypes()).containsExactlyElementsOf(jdkProxy);
		} else {
			assertThat(computedJdkProxies).isEmpty();
		}
		if (aotProxy == null) {
			assertThat(computedClassProxies).isEmpty();
		} else {
			assertThat(computedClassProxies.size()).isEqualTo(1);
			AotProxyDescriptor aotProxyDescriptor = (AotProxyDescriptor) computedClassProxies.get(0);
			assertThat(aotProxyDescriptor.getTargetClassType()).isEqualTo(aotProxy);
		}
	}

	@Transactional
	public interface TransactionalInterface {

		void foo();

	}

	public interface ProxyInterface extends TransactionalInterface {

	}

	public interface VoidInterface {
	}

	@Transactional
	public interface TransactionalInterfaceWithoutMethod {

	}

	@Transactional
	public interface TransactionalInterfaceWithDefault {

		default void foo() {

		}

	}

	static class ProxyClass implements TransactionalInterface {

		@Override
		public void foo() {

		}

	}

	// needs jdk proxy
	@Component
	static class ComponentWithTransactionalInterface implements TransactionalInterface {

		@Override
		public void foo() {
		}

	}

	// needs jdk proxy
	@Component
	static class ComponentWithTransactionalInterfaceAndExtraInterface implements TransactionalInterface, VoidInterface {

		@Override
		public void foo() {

		}

	}

	// needs jdk proxy
	@Component
	static class ComponentWithTransactionalInterfaceAndOtherMethod implements TransactionalInterface {

		@Override
		public void foo() {

		}

		public void bar() {

		}

	}

	// needs jdk proxy
	@Component
	public class ComponentWithTransactionalInterfaceOverProxyInterface implements ProxyInterface {

		@Override
		public void foo() {

		}

	}

	// needs jdk proxy
	@Component
	static class ComponentWithTransactionalInterfaceOverProxyClass extends ProxyClass {

	}

	// no proxy needed
	@Component
	static class ComponentWithTransactionalInterfaceWithoutMethod implements TransactionalInterfaceWithoutMethod {

	}

	// needs aot proxy
	@Component
	static class ComponentWithTransactionalInterfaceWithoutMethodAndOtherMethod implements TransactionalInterfaceWithoutMethod {

		public void bar() {

		}

	}

	// needs jdk proxy
	@Component
	static class ComponentWithTransactionalInterfaceWithDefault implements TransactionalInterfaceWithDefault {

	}

	// needs aot proxy
	@Component
	static class ComponentWithTransactionalMethod {

		@Transactional
		public void foo() {

		}

	}

	// needs aot proxy
	@Transactional
	@Component
	static class TransactionalComponent {

		public void foo() {

		}

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

	@Transactional
	@Component
	static class TxClass3 implements Bar {
		public void foo() {
		}
		public void test() {
		}
	}

	interface Bar {
		void test();
	}
}

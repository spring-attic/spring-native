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

package org.springframework.validation.annotation;

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

// TODO unlike TransactionalNativeConfigurationProcessor this one does not currently include tests for method
// level Validation annotations. The related native configuration processor doesn't handle that case, if 
// method level @Validation should be handled in a similar way it should be easy to change the ValidationNativeConfigurationProcessor (and Tests)
// to more match the TransactionalNativeConfigurationProcessor - possibly even merge the two into one to handle both annotations.
/**
 * Tests for {@link ValidatedNativeConfigurationProcessor}.
 *
 * @author Andy Clement
 * @author Petr Hejl
 */
class ValidatedNativeConfigurationProcessorTests {

	@Test
	void validatedClass() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(VClass.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		Set<NativeProxyEntry> proxyEntries = registry.proxy().getEntries();
		assertThat(proxyEntries).hasSize(1);
		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
		assertThat(proxyDescriptors).hasSize(1);
		JdkProxyDescriptor proxyDescriptor = proxyDescriptors.iterator().next();
		assertThat(proxyDescriptor).isInstanceOf(AotProxyDescriptor.class);
		AotProxyDescriptor aotProxyDescriptor = (AotProxyDescriptor)proxyDescriptor;
		assertThat(aotProxyDescriptor.getTargetClassType()).isEqualTo(VClass.class.getName());
		assertThat(aotProxyDescriptor.getInterfaceTypes()).isEmpty();
		assertThat(aotProxyDescriptor.getProxyFeatures()).isEqualTo(ProxyBits.IS_STATIC);
	}

	@Test
	void ValidatedClassWithInterfaces() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(VClass3.class).getBeanDefinition());
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
	public void shouldDescribeProxiesForComponentWithValidatedInterface() {
		checkProxies(ComponentWithValidatedInterface.class,
				Arrays.asList(
						ValidatedInterface.class.getName(),
						"org.springframework.aop.SpringProxy",
						"org.springframework.aop.framework.Advised",
						"org.springframework.core.DecoratingProxy"),
				null);
	}

	@Test
	public void shouldDescribeProxiesForComponentWithValidatedInterfaceAndExtraInterface() {
		checkProxies(ComponentWithValidatedInterfaceAndExtraInterface.class,
				Arrays.asList(
						ValidatedInterface.class.getName(),
						VoidInterface.class.getName(),
						"org.springframework.aop.SpringProxy",
						"org.springframework.aop.framework.Advised",
						"org.springframework.core.DecoratingProxy"),
				null);
	}

	@Test
	public void shouldDescribeProxiesForComponentWithValidatedInterfaceAndOtherMethod() {
		checkProxies(ComponentWithValidatedInterfaceAndOtherMethod.class,
				Arrays.asList(
						ValidatedInterface.class.getName(),
						"org.springframework.aop.SpringProxy",
						"org.springframework.aop.framework.Advised",
						"org.springframework.core.DecoratingProxy"),
				null);
	}

	@Test
	public void shouldDescribeProxiesForComponentWithValidatedInterfaceOverSuperClass() {
		checkProxies(ComponentWithValidatedInterfaceOverProxyClass.class,
				Arrays.asList(
						ValidatedInterface.class.getName(),
						"org.springframework.aop.SpringProxy",
						"org.springframework.aop.framework.Advised",
						"org.springframework.core.DecoratingProxy"),
				null);
	}

	@Test
	public void shouldDescribeProxiesForComponentWithValidatedInterfaceOverSuperInterface() {
		checkProxies(ComponentWithValidatedInterfaceOverProxyInterface.class,
				Arrays.asList(
						ProxyInterface.class.getName(),
						"org.springframework.aop.SpringProxy",
						"org.springframework.aop.framework.Advised",
						"org.springframework.core.DecoratingProxy"),
				null);
	}

	@Test
	public void shouldDescribeProxiesForComponentWithValidatedInterfaceWithoutMethod() {
		checkProxies(ComponentWithValidatedInterfaceWithoutMethod.class,
				null,
				ComponentWithValidatedInterfaceWithoutMethod.class.getName());
	}

	@Test
	public void shouldDescribeProxiesForComponentWithValidatedInterfaceWithoutMethodAndOtherMethod() {
		checkProxies(ComponentWithValidatedInterfaceWithoutMethodAndOtherMethod.class,
				null,
				ComponentWithValidatedInterfaceWithoutMethodAndOtherMethod.class.getName());//arg.springframework.Validated.components.ComponentWithValidatedInterfaceWithoutMethodAndOtherMethod");
	}

	@Test
	public void shouldDescribeProxiesForComponentWithValidatedInterfaceWithDefault() {
		checkProxies(ComponentWithValidatedInterfaceWithDefault.class,
				Arrays.asList(
						ValidatedInterfaceWithDefault.class.getName(),
						"org.springframework.aop.SpringProxy",
						"org.springframework.aop.framework.Advised",
						"org.springframework.core.DecoratingProxy"),
				null);
	}

	@Test
	public void shouldDescribeProxiesForValidatedComponent() {
		checkProxies(ValidatedComponent.class,
				null,
				ValidatedComponent.class.getName());
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new ValidatedNativeConfigurationProcessor().process(beanFactory, registry);
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

	@Validated
	public interface ValidatedInterface {

		void foo();

	}

	public interface ProxyInterface extends ValidatedInterface {

	}

	public interface VoidInterface {
	}

	@Validated
	public interface ValidatedInterfaceWithoutMethod {

	}

	@Validated
	public interface ValidatedInterfaceWithDefault {

		default void foo() {

		}

	}

	static class ProxyClass implements ValidatedInterface {

		@Override
		public void foo() {

		}

	}

	// needs jdk proxy
	@Component
	static class ComponentWithValidatedInterface implements ValidatedInterface {

		@Override
		public void foo() {
		}

	}

	// needs jdk proxy
	@Component
	static class ComponentWithValidatedInterfaceAndExtraInterface implements ValidatedInterface, VoidInterface {

		@Override
		public void foo() {

		}

	}

	// needs jdk proxy
	@Component
	static class ComponentWithValidatedInterfaceAndOtherMethod implements ValidatedInterface {

		@Override
		public void foo() {

		}

		public void bar() {

		}

	}

	// needs jdk proxy
	@Component
	public class ComponentWithValidatedInterfaceOverProxyInterface implements ProxyInterface {

		@Override
		public void foo() {

		}

	}

	// needs jdk proxy
	@Component
	static class ComponentWithValidatedInterfaceOverProxyClass extends ProxyClass {

	}

	// no proxy needed
	@Component
	static class ComponentWithValidatedInterfaceWithoutMethod implements ValidatedInterfaceWithoutMethod {

	}

	// needs aot proxy
	@Component
	static class ComponentWithValidatedInterfaceWithoutMethodAndOtherMethod implements ValidatedInterfaceWithoutMethod {

		public void bar() {

		}

	}

	// needs jdk proxy
	@Component
	static class ComponentWithValidatedInterfaceWithDefault implements ValidatedInterfaceWithDefault {

	}

	// needs aot proxy
	@Validated
	@Component
	static class ValidatedComponent {

		public void foo() {

		}

	}

	@Validated
	@Component
	static class VClass {
		public void foo() {
		}
	}

	@Validated
	@Component
	static class VClass3 implements Bar {
		public void foo() {
		}
		public void test() {
		}
	}

	interface Bar {
		void test();
	}
}

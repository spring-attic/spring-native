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

package org.springframework.context.bootstrap.generator.infrastructure;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link InitDestroyMethodsDiscoverer}.
 *
 * @author Stephane Nicoll
 */
class InitDestroyMethodsDiscovererTests {

	private static final Method START_METHOD = ReflectionUtils.findMethod(LifecycleSample.class, "start");

	private static final Method TEST_METHOD = ReflectionUtils.findMethod(LifecycleSample.class, "test");

	private static final Method TEST2_METHOD = ReflectionUtils.findMethod(LifecycleSample.class, "test2");

	private static final Method STOP_METHOD = ReflectionUtils.findMethod(LifecycleSample.class, "stop");

	private final NativeConfigurationRegistry registry = new NativeConfigurationRegistry();

	@Test
	void processWithInitMethodName() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(LifecycleSample.class);
		beanDefinition.setInitMethodName("start");
		Map<String, List<Method>> methods = createInstance("test", beanDefinition).registerInitMethods(registry);
		assertThat(methods).containsOnlyKeys("test");
		assertThat(methods.get("test")).containsExactly(START_METHOD);
		hasSingleNativeReflectionEntry(LifecycleSample.class, START_METHOD);
	}

	@Test
	void processWithExternallyManagedInitMethodName() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(LifecycleSample.class);
		Map<String, List<Method>> methods = createInstance("test", beanDefinition,
				(mbd) -> mbd.registerExternallyManagedInitMethod("start")).registerInitMethods(registry);
		assertThat(methods).containsOnlyKeys("test");
		assertThat(methods.get("test")).containsExactly(START_METHOD);
		hasSingleNativeReflectionEntry(LifecycleSample.class, START_METHOD);
	}

	@Test
	void processWithSeveralInitMethodNames() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(LifecycleSample.class);
		beanDefinition.setInitMethodName("start");
		Map<String, List<Method>> methods = createInstance("test", beanDefinition, (mbd) -> {
			mbd.registerExternallyManagedInitMethod("test");
			mbd.registerExternallyManagedInitMethod("test2");
		}).registerInitMethods(registry);
		assertThat(methods).containsOnlyKeys("test");
		assertThat(methods.get("test")).containsExactlyInAnyOrder(START_METHOD, TEST_METHOD, TEST2_METHOD);
		hasSingleNativeReflectionEntry(LifecycleSample.class, START_METHOD, TEST_METHOD, TEST2_METHOD);
	}

	@Test
	void processWithInitMethodNameOnParentRegisterAppropriateReflectionHint() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(LifecycleExtensionSample.class);
		beanDefinition.setInitMethodName("start");
		Map<String, List<Method>> methods = createInstance("test", beanDefinition).registerInitMethods(registry);
		assertThat(methods).containsOnlyKeys("test");
		assertThat(methods.get("test")).containsExactly(START_METHOD);
		hasSingleNativeReflectionEntry(LifecycleSample.class, START_METHOD);
	}

	@Test
	void processWithNoInitMethodDoesNotRegisterBean() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(LifecycleSample.class);
		Map<String, List<Method>> methods = createInstance("test", beanDefinition).registerInitMethods(registry);
		assertThat(methods).isEmpty();
		assertThat(this.registry.reflection().getEntries()).isEmpty();
	}

	@Test
	void processWithDestroyMethodName() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(LifecycleSample.class);
		beanDefinition.setDestroyMethodName("stop");
		Map<String, List<Method>> methods = createInstance("test", beanDefinition).registerDestroyMethods(registry);
		assertThat(methods).containsOnlyKeys("test");
		assertThat(methods.get("test")).containsExactly(STOP_METHOD);
		hasSingleNativeReflectionEntry(LifecycleSample.class, STOP_METHOD);
	}

	@Test
	void processWithExternallyManagedDestroyMethodName() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(LifecycleSample.class);
		Map<String, List<Method>> methods = createInstance("test", beanDefinition,
				(mbd) -> mbd.registerExternallyManagedDestroyMethod("stop")).registerDestroyMethods(registry);
		assertThat(methods).containsOnlyKeys("test");
		assertThat(methods.get("test")).containsExactly(STOP_METHOD);
		hasSingleNativeReflectionEntry(LifecycleSample.class, STOP_METHOD);
	}

	@Test
	void processWithSeveralDestroyMethodNames() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(LifecycleSample.class);
		beanDefinition.setDestroyMethodName("stop");
		Map<String, List<Method>> methods = createInstance("test", beanDefinition, (mbd) -> {
			mbd.registerExternallyManagedDestroyMethod("test");
			mbd.registerExternallyManagedDestroyMethod("test2");
		}).registerDestroyMethods(registry);
		assertThat(methods).containsOnlyKeys("test");
		assertThat(methods.get("test")).containsExactlyInAnyOrder(STOP_METHOD, TEST_METHOD, TEST2_METHOD);
		hasSingleNativeReflectionEntry(LifecycleSample.class, STOP_METHOD, TEST_METHOD, TEST2_METHOD);
	}

	@Test
	void processWithDestroyMethodNameOnParentRegisterAppropriateReflectionHint() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(LifecycleExtensionSample.class);
		beanDefinition.setDestroyMethodName("stop");
		Map<String, List<Method>> methods = createInstance("test", beanDefinition).registerDestroyMethods(registry);
		assertThat(methods).containsOnlyKeys("test");
		assertThat(methods.get("test")).containsExactly(STOP_METHOD);
		hasSingleNativeReflectionEntry(LifecycleSample.class, STOP_METHOD);
	}

	@Test
	void processWithInferredDestroyMethodNameNoMatch() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(LifecycleSample.class);
		beanDefinition.setDestroyMethodName("(inferred)");
		Map<String, List<Method>> methods = createInstance("test", beanDefinition).registerDestroyMethods(registry);
		assertThat(methods).isEmpty();
		assertThat(this.registry.reflection().getEntries()).isEmpty();
	}

	@Test
	void processWithInferredDestroyMethodNameOnAutoCloseable() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(AutoClosableSample.class);
		beanDefinition.setDestroyMethodName("(inferred)");
		Map<String, List<Method>> methods = createInstance("test", beanDefinition).registerDestroyMethods(registry);
		assertThat(methods).isEmpty();
		Method closeMethod = ReflectionUtils.findMethod(AutoClosableSample.class, "close");
		hasSingleNativeReflectionEntry(AutoClosableSample.class, closeMethod);
	}

	@Test
	void processWithInferredDestroyMethodNameWithCloseMatch() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(CloseSample.class);
		beanDefinition.setDestroyMethodName("(inferred)");
		Map<String, List<Method>> methods = createInstance("test", beanDefinition).registerDestroyMethods(registry);
		Method closeMethod = ReflectionUtils.findMethod(CloseSample.class, "close");
		assertThat(methods.get("test")).containsExactly(closeMethod);
		hasSingleNativeReflectionEntry(CloseSample.class, closeMethod);
	}

	@Test
	void processWithInferredDestroyMethodNameWithShutdownMatch() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(ShutdownSample.class);
		beanDefinition.setDestroyMethodName("(inferred)");
		Map<String, List<Method>> methods = createInstance("test", beanDefinition).registerDestroyMethods(registry);
		Method shutdownMethod = ReflectionUtils.findMethod(ShutdownSample.class, "shutdown");
		assertThat(methods.get("test")).containsExactly(shutdownMethod);
		hasSingleNativeReflectionEntry(ShutdownSample.class, shutdownMethod);
	}

	@Test
	void processWithInferredDestroyMethodNameOnDisposableBean() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(DisposableBeanSample.class);
		beanDefinition.setDestroyMethodName("(inferred)");
		Map<String, List<Method>> methods = createInstance("test", beanDefinition).registerDestroyMethods(registry);
		assertThat(methods).isEmpty();
		assertThat(this.registry.reflection().getEntries()).isEmpty();
	}

	@Test
	void processWithoutInferredDestroyMethodNameWithAutoClosableIsIgnored() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(AutoClosableSample.class);
		Map<String, List<Method>> methods = createInstance("test", beanDefinition).registerDestroyMethods(registry);
		assertThat(methods).isEmpty();
		Method closeMethod = ReflectionUtils.findMethod(AutoClosableSample.class, "close");
		hasSingleNativeReflectionEntry(AutoClosableSample.class, closeMethod);
	}

	@Test
	void processWithNoDestroyMethodDoesNotRegisterBean() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(LifecycleSample.class);
		Map<String, List<Method>> methods = createInstance("test", beanDefinition).registerDestroyMethods(registry);
		assertThat(methods).isEmpty();
		assertThat(this.registry.reflection().getEntries()).isEmpty();
	}

	@Test
	void processWithUnknownLifecycleMethodName() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(LifecycleSample.class);
		beanDefinition.setInitMethodName("doesNotExist");
		InitDestroyMethodsDiscoverer discoverer = createInstance("test", beanDefinition);
		assertThatIllegalStateException().isThrownBy(() -> discoverer.registerInitMethods(registry))
				.withMessageContaining("Lifecycle method annotation 'doesNotExist' not found");
	}

	private void hasSingleNativeReflectionEntry(Consumer<NativeReflectionEntry> assertions) {
		assertThat(this.registry.reflection().getEntries()).singleElement().satisfies(assertions);
	}

	private void hasSingleNativeReflectionEntry(Class<?> type, Method... methods) {
		hasSingleNativeReflectionEntry((entry) -> {
			assertThat(entry.getType()).isEqualTo(type);
			assertThat(entry.getMethods()).containsOnly(methods);
		});
	}

	private InitDestroyMethodsDiscoverer createInstance(String beanName, BeanDefinition beanDefinition) {
		return createInstance(beanName, beanDefinition, null);
	}

	private InitDestroyMethodsDiscoverer createInstance(String beanName, BeanDefinition beanDefinition, Consumer<RootBeanDefinition> mergedBeanDefinitionPostProcessor) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition(beanName, beanDefinition);
		if (mergedBeanDefinitionPostProcessor != null) {
			mergedBeanDefinitionPostProcessor.accept((RootBeanDefinition) beanFactory.getMergedBeanDefinition(beanName));
		}
		return new InitDestroyMethodsDiscoverer(beanFactory);
	}


	@SuppressWarnings("unused")
	static class LifecycleSample {

		public void start() {
		}

		void test() {
		}

		void test2() {
		}

		public void stop() {
		}

	}

	static class LifecycleExtensionSample extends LifecycleSample {

	}

	static class CloseSample {

		public void close() {

		}
	}

	static class ShutdownSample {

		public void shutdown() {

		}
	}

	static class AutoClosableSample implements AutoCloseable {

		@Override
		public void close() throws Exception {
		}

		public void shutdown() {
		}

	}

	static class DisposableBeanSample implements DisposableBean {

		@Override
		public void destroy() throws Exception {
		}

		public void close() {
		}

		public void shutdown() {
		}
	}

}

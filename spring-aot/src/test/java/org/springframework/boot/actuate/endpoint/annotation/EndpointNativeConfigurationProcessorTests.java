package org.springframework.boot.actuate.endpoint.annotation;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EndpointNativeConfigurationProcessor}.
 *
 * @author Stephane Nicoll
 */
class EndpointNativeConfigurationProcessorTests {

	@Test
	void registerSimpleEndpoint() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(TestEndpoint.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<NativeReflectionEntry> entries = registry.reflection().getEntries();
		assertThat(entries).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(TestEndpoint.class);
			assertThat(entry.getMethods().stream().map(Method::getName))
					.containsOnly("getAll", "get", "set", "delete");
		});
		assertThat(entries).hasSize(1);
	}

	@Test
	void registerSimpleEndpointExtension() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(TestEndpointWebExtension.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<NativeReflectionEntry> entries = registry.reflection().getEntries();
		assertThat(entries).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(TestEndpointWebExtension.class);
			assertThat(entry.getMethods().stream().map(Method::getName)).containsOnly("get");
		});
		assertThat(entries).hasSize(1);
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new EndpointNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}

	@Endpoint(id = "test")
	@SuppressWarnings("unused")
	static class TestEndpoint {

		@ReadOperation
		public List<String> getAll() {
			return List.of(get());
		}

		@ReadOperation
		public String get() {
			return "test";
		}

		@WriteOperation
		public void set(String name) {

		}

		@DeleteOperation
		public void delete(String name) {

		}

		public void ignoredMethod(String name) {

		}

	}

	@EndpointWebExtension(endpoint = TestEndpoint.class)
	@SuppressWarnings("unused")
	static class TestEndpointWebExtension {

		@ReadOperation
		public String get() {
			return "web";
		}

		public void noise(String name) {

		}

	}

}

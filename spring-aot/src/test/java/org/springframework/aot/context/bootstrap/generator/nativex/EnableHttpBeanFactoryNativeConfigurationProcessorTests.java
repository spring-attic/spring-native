package org.springframework.aot.context.bootstrap.generator.nativex;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EnableHttpBeanFactoryNativeConfigurationProcessor}.
 *
 * @author Sébastien Deleuze
 */
class EnableHttpBeanFactoryNativeConfigurationProcessorTests {

	@Test
	void registerEnableHttpForWebClient() {
		NativeConfigurationRegistry registry = register(WebClient.class);
		assertThat(registry.options()).containsExactly("--enable-http", "--enable-https");
	}

	@Test
	void registerEnableHttpForWebClientBuilder() {
		NativeConfigurationRegistry registry = register(WebClient.Builder.class);
		assertThat(registry.options()).containsExactly("--enable-http", "--enable-https");
	}

	@Test
	void registerEnableHttpForRestTemplate() {
		NativeConfigurationRegistry registry = register(RestTemplate.class);
		assertThat(registry.options()).containsExactly("--enable-http", "--enable-https");
	}

	@Test
	void registerEnableHttpForRestTemplateBuilder() {
		NativeConfigurationRegistry registry = register(RestTemplateBuilder.class);
		assertThat(registry.options()).containsExactly("--enable-http", "--enable-https");
	}

	@Test
	void registerEnableHttpForOtherBean() {
		NativeConfigurationRegistry registry = register(String.class);
		assertThat(registry.options()).isEmpty();
	}

	@Test
	void registerWithoutRestTemplate() {
		FilteredClassLoader classLoader = new FilteredClassLoader(RestTemplate.class, RestTemplateBuilder.class);
		NativeConfigurationRegistry registry = register(classLoader, String.class);
		assertThat(registry.options()).isEmpty();
	}

	@Test
	void registerWithouWebClient() {
		FilteredClassLoader classLoader = new FilteredClassLoader(WebClient.class);
		NativeConfigurationRegistry registry = register(classLoader, String.class);
		assertThat(registry.options()).isEmpty();
	}

	private NativeConfigurationRegistry register(Class<?>... beanTypes) {
		return register(getClass().getClassLoader(), beanTypes);
	}

	private NativeConfigurationRegistry register(ClassLoader classLoader, Class<?>... beanTypes) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.setBeanClassLoader(classLoader);
		for (Class<?> beanType : beanTypes) {
			beanFactory.registerBeanDefinition(beanType.getName(),
					BeanDefinitionBuilder.rootBeanDefinition(beanType).getBeanDefinition());
		}
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new EnableHttpBeanFactoryNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}
}

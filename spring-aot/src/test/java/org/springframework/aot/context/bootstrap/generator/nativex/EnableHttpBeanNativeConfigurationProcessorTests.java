package org.springframework.aot.context.bootstrap.generator.nativex;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EnableHttpBeanNativeConfigurationProcessor}.
 *
 * @author Sébastien Deleuze
 */
public class EnableHttpBeanNativeConfigurationProcessorTests {

	@Test
	void registerEnableHttpForWebClient() {
		BeanInstanceDescriptor webClientDescriptor = BeanInstanceDescriptor.of(WebClient.class).build();
		NativeConfigurationRegistry registry = register(webClientDescriptor);
		assertThat(registry.options()).containsExactly("--enable-http", "--enable-https");
	}

	@Test
	void registerEnableHttpForWebClientBuilder() {
		BeanInstanceDescriptor webClientDescriptor = BeanInstanceDescriptor.of(WebClient.Builder.class).build();
		NativeConfigurationRegistry registry = register(webClientDescriptor);
		assertThat(registry.options()).containsExactly("--enable-http", "--enable-https");
	}

	@Test
	void registerEnableHttpForRestTemplate() {
		BeanInstanceDescriptor webClientDescriptor = BeanInstanceDescriptor.of(RestTemplate.class).build();
		NativeConfigurationRegistry registry = register(webClientDescriptor);
		assertThat(registry.options()).containsExactly("--enable-http", "--enable-https");
	}

	@Test
	void registerEnableHttpForRestTemplateBuilder() {
		BeanInstanceDescriptor webClientDescriptor = BeanInstanceDescriptor.of(RestTemplateBuilder.class).build();
		NativeConfigurationRegistry registry = register(webClientDescriptor);
		assertThat(registry.options()).containsExactly("--enable-http", "--enable-https");
	}

	@Test
	void registerEnableHttpForOtherBean() {
		BeanInstanceDescriptor webClientDescriptor = BeanInstanceDescriptor.of(String.class).build();
		NativeConfigurationRegistry registry = register(webClientDescriptor);
		assertThat(registry.options()).isEmpty();
	}

	private NativeConfigurationRegistry register(BeanInstanceDescriptor descriptor) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new EnableHttpBeanNativeConfigurationProcessor().process(descriptor, registry);
		return registry;
	}
}

package org.springframework.aot.context.bootstrap.generator.nativex;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EnableHttpsBeanNativeConfigurationProcessor}.
 *
 * @author Sébastien Deleuze
 */
public class EnableHttpsBeanNativeConfigurationProcessorTests {

	@Test
	void registerEnableHttpsForWebClient() {
		BeanInstanceDescriptor webClientDescriptor = BeanInstanceDescriptor.of(WebClient.class).build();
		NativeConfigurationRegistry registry = register(webClientDescriptor);
		assertThat(registry.options()).singleElement().isEqualTo("--enable-https");
	}

	@Test
	void registerEnableHttpsForWebClientBuilder() {
		BeanInstanceDescriptor webClientDescriptor = BeanInstanceDescriptor.of(WebClient.Builder.class).build();
		NativeConfigurationRegistry registry = register(webClientDescriptor);
		assertThat(registry.options()).singleElement().isEqualTo("--enable-https");
	}

	@Test
	void registerEnableHttpsForRestTemplate() {
		BeanInstanceDescriptor webClientDescriptor = BeanInstanceDescriptor.of(RestTemplate.class).build();
		NativeConfigurationRegistry registry = register(webClientDescriptor);
		assertThat(registry.options()).singleElement().isEqualTo("--enable-https");
	}

	@Test
	void registerEnableHttpsForRestTemplateBuilder() {
		BeanInstanceDescriptor webClientDescriptor = BeanInstanceDescriptor.of(RestTemplateBuilder.class).build();
		NativeConfigurationRegistry registry = register(webClientDescriptor);
		assertThat(registry.options()).singleElement().isEqualTo("--enable-https");
	}

	@Test
	void registerEnableHttpsForOtherBean() {
		BeanInstanceDescriptor webClientDescriptor = BeanInstanceDescriptor.of(String.class).build();
		NativeConfigurationRegistry registry = register(webClientDescriptor);
		assertThat(registry.options()).isEmpty();
	}

	private NativeConfigurationRegistry register(BeanInstanceDescriptor descriptor) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new EnableHttpsBeanNativeConfigurationProcessor().process(descriptor, registry);
		return registry;
	}
}

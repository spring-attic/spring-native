package org.springframework.aot.context.bootstrap.generator.nativex;

import java.util.List;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;

/**
 * Enable HTTPS support when that makes sense by default, for example when beans like http clients are detected.
 *
 * @author Sébastien Deleuze
 */
public class EnableHttpsBeanNativeConfigurationProcessor implements BeanNativeConfigurationProcessor {

	private static final List<String> BEAN_TYPE_NAMES = List.of(
			"org.springframework.web.client.RestTemplate",
			"org.springframework.boot.web.client.RestTemplateBuilder",
			"org.springframework.web.reactive.function.client.WebClient",
			"org.springframework.web.reactive.function.client.WebClient$Builder"
	);

	private static final String ENABLE_HTTPS_OPTION = "--enable-https";

	@Override
	public void process(BeanInstanceDescriptor descriptor, NativeConfigurationRegistry registry) {
		for (String beanTypeName : BEAN_TYPE_NAMES) {
			if (descriptor.getUserBeanClass().getName().equals(beanTypeName)) {
				registry.options().add(ENABLE_HTTPS_OPTION);
			}
		}
	}
}

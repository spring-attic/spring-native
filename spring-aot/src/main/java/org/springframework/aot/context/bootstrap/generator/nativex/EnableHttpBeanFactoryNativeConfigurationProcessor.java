package org.springframework.aot.context.bootstrap.generator.nativex;

import java.util.ArrayList;
import java.util.List;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Enable HTTP and HTTPS support for specific beans, for example when http clients are detected.
 *
 * @author Sébastien Deleuze
 * @author Stephane Nicoll
 */
class EnableHttpBeanFactoryNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	private static final List<String> BEAN_TYPE_NAMES = List.of(
			"org.springframework.web.client.RestTemplate",
			"org.springframework.boot.web.client.RestTemplateBuilder",
			"org.springframework.web.reactive.function.client.WebClient",
			"org.springframework.web.reactive.function.client.WebClient$Builder"
	);

	private static final String ENABLE_HTTP_OPTION = "--enable-http";

	private static final String ENABLE_HTTPS_OPTION = "--enable-https";


	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		for (Class<?> candidate : getCandidates(beanFactory.getBeanClassLoader())) {
			if (!ObjectUtils.isEmpty(beanFactory.getBeanNamesForType(candidate))) {
				registry.options().add(ENABLE_HTTP_OPTION);
				registry.options().add(ENABLE_HTTPS_OPTION);
				break;
			}
		}
	}

	private List<Class<?>> getCandidates(ClassLoader classLoader) {
		List<Class<?>> candidates = new ArrayList<>();
		for (String beanTypeName : BEAN_TYPE_NAMES) {
			try {
				candidates.add(ClassUtils.forName(beanTypeName, classLoader));
			}
			catch (ClassNotFoundException ex) {
				// ignore
			}
		}
		return candidates;
	}

}

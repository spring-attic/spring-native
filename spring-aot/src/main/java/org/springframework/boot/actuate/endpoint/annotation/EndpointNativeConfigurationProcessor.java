package org.springframework.boot.actuate.endpoint.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry.Builder;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link BeanFactoryNativeConfigurationProcessor} that register reflection access for
 * actuator endpoints, specifically the methods flagged with one of the supported
 * operation annotations.
 *
 * @author Stephane Nicoll
 */
class EndpointNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	private static final String ENDPOINT_CLASS_NAME = "org.springframework.boot.actuate.endpoint.annotation.Endpoint";

	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		if (ClassUtils.isPresent(ENDPOINT_CLASS_NAME, beanFactory.getBeanClassLoader())) {
			new Processor().process(beanFactory, registry);
		}
	}

	private static class Processor {

		private static final List<Class<? extends Annotation>> ENDPOINT_ANNOTATIONS = List.of(
				Endpoint.class, EndpointExtension.class);

		private static final List<Class<? extends Annotation>> OPERATION_ANNOTATIONS = List.of(
				ReadOperation.class, WriteOperation.class, DeleteOperation.class);

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			findCandidates(beanFactory).forEach((beanName, endpoint) -> registerEndpoint(registry, endpoint));
		}

		private Map<String, Class<?>> findCandidates(ConfigurableListableBeanFactory beanFactory) {
			Map<String, Class<?>> candidates = new LinkedHashMap<>();
			ENDPOINT_ANNOTATIONS.forEach((annotation) -> {
				String[] beanNames = beanFactory.getBeanNamesForAnnotation(annotation);
				for (String beanName : beanNames) {
					candidates.put(beanName, beanFactory.getType(beanName));
				}
			});
			return candidates;
		}

		private void registerEndpoint(NativeConfigurationRegistry registry, Class<?> type) {
			Builder builder = registry.reflection().forType(type);
			ReflectionUtils.doWithMethods(type, builder::withMethods, this::isOperationMethod);
		}

		private boolean isOperationMethod(Method method) {
			MergedAnnotations from = MergedAnnotations.from(method);
			return OPERATION_ANNOTATIONS.stream().anyMatch(from::isPresent);
		}
	}

}

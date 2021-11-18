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

package org.springframework.boot.actuate.endpoint.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntry.Builder;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.support.BeanFactoryProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link BeanFactoryNativeConfigurationProcessor} that register reflection access for
 * actuator endpoints, specifically the methods flagged with one of the supported
 * operation annotations, as well as any endpoint filter.
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
			BeanFactoryProcessor beanFactoryProcessor = new BeanFactoryProcessor(beanFactory);
			findCandidates(beanFactoryProcessor).forEach((beanName, endpoint) -> registerEndpoint(registry, endpoint));
		}

		private Map<String, Class<?>> findCandidates(BeanFactoryProcessor beanFactoryProcessor) {
			Map<String, Class<?>> candidates = new LinkedHashMap<>();
			ENDPOINT_ANNOTATIONS.forEach((annotation) -> beanFactoryProcessor
					.processBeansWithAnnotation(annotation, candidates::put));
			return candidates;
		}

		private void registerEndpoint(NativeConfigurationRegistry registry, Class<?> type) {
			Builder builder = registry.reflection().forType(type);
			ReflectionUtils.doWithMethods(type, builder::withExecutables, this::isOperationMethod);
			Executable endpointFilterConstructor = getEndpointFilterConstructor(type);
			if (endpointFilterConstructor != null) {
				registry.reflection().addExecutable(endpointFilterConstructor);
			}
		}

		private boolean isOperationMethod(Method method) {
			MergedAnnotations from = MergedAnnotations.from(method);
			return OPERATION_ANNOTATIONS.stream().anyMatch(from::isPresent);
		}

		private Executable getEndpointFilterConstructor(Class<?> endpointType) {
			MergedAnnotations annotations = MergedAnnotations.from(endpointType);
			MergedAnnotation<FilteredEndpoint> annotation = annotations.get(FilteredEndpoint.class);
			if (annotation.isPresent()) {
				try {
					Class<?> endpointFilter = annotation.getClass("value");
					return endpointFilter.getDeclaredConstructor();
				}
				catch (Exception ex) {
					// ignore
				}
			}
			return null;
		}
	}

}

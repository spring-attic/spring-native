/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.core.io.support;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aot.StaticSpringFactories;
import org.springframework.core.io.UrlResource;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.nativex.AotModeDetector;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public final class SpringFactoriesLoader {

	private static final Log logger = LogFactory.getLog(SpringFactoriesLoader.class);

	public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";

	static final Map<ClassLoader, Map<String, List<String>>> cache = new ConcurrentReferenceHashMap<>();


	private SpringFactoriesLoader() {
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> loadFactories(Class<T> factoryType, @Nullable ClassLoader classLoader) {
		Assert.notNull(factoryType, "'factoryType' must not be null");
		if (AotModeDetector.isAotModeEnabled() || AotModeDetector.isRunningAotTests()) {
			List<Supplier<Object>> result = StaticSpringFactories.factories.get(factoryType);
			if (result == null) {
				return new ArrayList<>(0);
			}
			List<T> factories = new ArrayList<>(result.size());
			for (Supplier<Object> supplier : result) {
				// TODO: protect against factories that fail during instantiation
				try {
					factories.add((T) supplier.get());
				}
				catch (Throwable throwable) {
					logger.trace("Could not instantiate factory for " + factoryType, throwable);
				}
			}
			return factories;
		}
		else {
			ClassLoader classLoaderToUse = classLoader;
			if (classLoaderToUse == null) {
				classLoaderToUse = SpringFactoriesLoader.class.getClassLoader();
			}
			List<String> factoryImplementationNames = loadFactoryNames(factoryType, classLoaderToUse);
			if (logger.isTraceEnabled()) {
				logger.trace("Loaded [" + factoryType.getName() + "] names: " + factoryImplementationNames);
			}
			List<T> result = new ArrayList<>(factoryImplementationNames.size());
			for (String factoryImplementationName : factoryImplementationNames) {
				result.add(instantiateFactory(factoryImplementationName, factoryType, classLoaderToUse));
			}
			AnnotationAwareOrderComparator.sort(result);
			return result;
		}
	}

	public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
		if (AotModeDetector.isAotModeEnabled() || AotModeDetector.isRunningAotTests()) {
			List<String> result = new ArrayList<>();
			List<String> names = StaticSpringFactories.names.get(factoryType);
			if (names != null) {
				result.addAll(names);
			}
			List<Supplier<Object>> stored = StaticSpringFactories.factories.get(factoryType);
			if (stored != null) {
				for (Supplier<Object> supplier : stored) {
					try {
						result.add(supplier.get().getClass().getName());
					}
					catch (Throwable throwable) {
						logger.trace("Could not get factory name for " + factoryType, throwable);
					}
				}
			}
			return result;
		}
		else {
			ClassLoader classLoaderToUse = classLoader;
			if (classLoaderToUse == null) {
				classLoaderToUse = SpringFactoriesLoader.class.getClassLoader();
			}
			String factoryTypeName = factoryType.getName();
			return loadSpringFactories(classLoaderToUse).getOrDefault(factoryTypeName, Collections.emptyList());
		}
	}

	private static Map<String, List<String>> loadSpringFactories(ClassLoader classLoader) {
		Map<String, List<String>> result = cache.get(classLoader);
		if (result != null) {
			return result;
		}

		result = new HashMap<>();
		try {
			Enumeration<URL> urls = classLoader.getResources(FACTORIES_RESOURCE_LOCATION);
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				UrlResource resource = new UrlResource(url);
				Properties properties = PropertiesLoaderUtils.loadProperties(resource);
				for (Map.Entry<?, ?> entry : properties.entrySet()) {
					String factoryTypeName = ((String) entry.getKey()).trim();
					String[] factoryImplementationNames =
							StringUtils.commaDelimitedListToStringArray((String) entry.getValue());
					for (String factoryImplementationName : factoryImplementationNames) {
						result.computeIfAbsent(factoryTypeName, key -> new ArrayList<>())
								.add(factoryImplementationName.trim());
					}
				}
			}

			// Replace all lists with unmodifiable lists containing unique elements
			result.replaceAll((factoryType, implementations) -> implementations.stream().distinct()
					.collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList)));
			cache.put(classLoader, result);
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load factories from location [" +
					FACTORIES_RESOURCE_LOCATION + "]", ex);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T> T instantiateFactory(String factoryImplementationName, Class<T> factoryType, ClassLoader classLoader) {
		try {
			Class<?> factoryImplementationClass = ClassUtils.forName(factoryImplementationName, classLoader);
			if (!factoryType.isAssignableFrom(factoryImplementationClass)) {
				throw new IllegalArgumentException(
						"Class [" + factoryImplementationName + "] is not assignable to factory type [" + factoryType.getName() + "]");
			}
			return (T) ReflectionUtils.accessibleConstructor(factoryImplementationClass).newInstance();
		}
		catch (Throwable ex) {
			throw new IllegalArgumentException(
					"Unable to instantiate factory class [" + factoryImplementationName + "] for factory type [" + factoryType.getName() + "]",
					ex);
		}
	}

}

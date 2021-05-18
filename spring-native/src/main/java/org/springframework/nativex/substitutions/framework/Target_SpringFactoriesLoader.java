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

package org.springframework.nativex.substitutions.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.commons.logging.Log;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.nativex.AotModeDetector;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.WithAot;
import org.springframework.util.Assert;

@TargetClass(className = "org.springframework.core.io.support.SpringFactoriesLoader", onlyWith = {WithAot.class, OnlyIfPresent.class})
final class Target_SpringFactoriesLoader {

	@Alias
	private static Log logger;


	@SuppressWarnings("unchecked")
	@Substitute
	public static <T> List<T> loadFactories(Class<T> factoryType, @Nullable ClassLoader classLoader) {
		Assert.notNull(factoryType, "'factoryType' must not be null");
		if (AotModeDetector.isAotModeEnabled()) {
			List<Supplier<Object>> result = Target_StaticSpringFactories.factories.get(factoryType);
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
				classLoaderToUse = Target_SpringFactoriesLoader.class.getClassLoader();
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

	@Substitute
	public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
		if (AotModeDetector.isAotModeEnabled()) {
			List<String> result = new ArrayList<>();
			List<String> names = Target_StaticSpringFactories.names.get(factoryType);
			if (names != null) {
				result.addAll(names);
			}
			List<Supplier<Object>> stored = Target_StaticSpringFactories.factories.get(factoryType);
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
				classLoaderToUse = Target_SpringFactoriesLoader.class.getClassLoader();
			}
			String factoryTypeName = factoryType.getName();
			return loadSpringFactories(classLoaderToUse).getOrDefault(factoryTypeName, Collections.emptyList());
		}
	}

	@Alias
	private static Map<String, List<String>> loadSpringFactories(ClassLoader classLoader) {
		return null;
	}

	@Alias
	private static <T> T instantiateFactory(String factoryImplementationName, Class<T> factoryType, ClassLoader classLoader) {
		return null;
	}

}
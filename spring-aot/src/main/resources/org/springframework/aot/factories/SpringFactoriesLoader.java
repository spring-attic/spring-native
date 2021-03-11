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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aot.StaticSpringFactories;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public final class SpringFactoriesLoader {

	private static final Log logger = LogFactory.getLog(SpringFactoriesLoader.class);

	public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";


	private SpringFactoriesLoader() {
	}


	public static <T> List<T> loadFactories(Class<T> factoryType, @Nullable ClassLoader classLoader) {
		Assert.notNull(factoryType, "'factoryType' must not be null");
		List<Supplier<Object>> result = StaticSpringFactories.factories.get(factoryType);
		if (result == null) {
			return new ArrayList<>(0);
		}
		List<T> factories = new ArrayList<>(result.size());
		for (Supplier<Object> supplier: result) {
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

	public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
		List<String> result = new ArrayList<>();
		List<String> names = StaticSpringFactories.names.get(factoryType);
		if (names != null) {
			result.addAll(names);
		}
		List<Supplier<Object>> stored = StaticSpringFactories.factories.get(factoryType);
		if (stored != null) {
			for (Supplier<Object> supplier: stored) {
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

}

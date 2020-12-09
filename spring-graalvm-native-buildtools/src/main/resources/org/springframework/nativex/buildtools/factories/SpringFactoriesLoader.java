/*
 * Copyright 2002-2020 the original author or authors.
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
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.nativex.buildtools.StaticSpringFactories;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public final class SpringFactoriesLoader {

	private static final Log logger = LogFactory.getLog(SpringFactoriesLoader.class);

	public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";


	private SpringFactoriesLoader() {
	}


	public static <T> List<T> loadFactories(Class<T> factoryType, @Nullable ClassLoader classLoader) {
		Assert.notNull(factoryType, "'factoryType' must not be null");
		List<T> result = (List<T>) StaticSpringFactories.factories.get(factoryType);
		if (result == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(result);
	}

	public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
		List<String> names = StaticSpringFactories.names.get(factoryType);
		if (names != null) {
			return names;
		}
		else {
			List<Object> stored = StaticSpringFactories.factories.get(factoryType);
			if (stored == null) {
				return Collections.emptyList();
			}
			return stored.stream().map(factory -> factory.getClass().getName()).collect(Collectors.toList());
		}
	}

}

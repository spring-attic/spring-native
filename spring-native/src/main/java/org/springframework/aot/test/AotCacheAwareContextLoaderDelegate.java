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

package org.springframework.aot.test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.CacheAwareContextLoaderDelegate;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.SmartContextLoader;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link CacheAwareContextLoaderDelegate} that enables the use of generated
 * application contexts for supported test classes.
 *
 * <p>Reflectively accesses {@value #INITIALIZER_NAME} to retrieve the mapping of
 * all known test classes.
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 */
public class AotCacheAwareContextLoaderDelegate extends DefaultCacheAwareContextLoaderDelegate {

	private static final Log logger = LogFactory.getLog(AotCacheAwareContextLoaderDelegate.class);

	private static final String INITIALIZER_NAME = "org.springframework.aot.TestContextBootstrapInitializer";

	private final Map<String, Supplier<SmartContextLoader>> aotContextLoaders;

	public AotCacheAwareContextLoaderDelegate(Map<String, Supplier<SmartContextLoader>> aotContextLoaders) {
		this.aotContextLoaders = aotContextLoaders;
	}

	public AotCacheAwareContextLoaderDelegate() {
		this(loadContextLoadersMapping());
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Supplier<SmartContextLoader>> loadContextLoadersMapping() {
		try {
			Class<?> type = ClassUtils.forName(INITIALIZER_NAME, null);
			Method method = ReflectionUtils.findMethod(type, "getContextLoaders");
			return (Map<String, Supplier<SmartContextLoader>>) ReflectionUtils.invokeMethod(method, null);
		}
		catch (Exception ex) {
			// TODO: exception
			return Collections.emptyMap();
		}
	}

	@Override
	protected ApplicationContext loadContextInternal(MergedContextConfiguration config) throws Exception {
		SmartContextLoader aotContextLoader = getAotContextLoader(config.getTestClass());
		if (aotContextLoader != null) {
			logger.info("Starting test in AOT mode using " + aotContextLoader);
			return aotContextLoader.loadContext(config);
		}
		return super.loadContextInternal(config);
	}

	private SmartContextLoader getAotContextLoader(Class<?> testClass) {
		Supplier<SmartContextLoader> supplier = this.aotContextLoaders.get(testClass.getName());
		return (supplier != null ? supplier.get() : null);
	}

}

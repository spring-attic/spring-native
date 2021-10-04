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

package org.springframework.context.annotation;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.lang.Nullable;

/**
 * Keep track of import origin of {@link ImportAware} beans.
 *
 * @author Stephane Nicoll
 */
public final class ImportOriginRegistry {

	private static final String BEAN_NAME = ImportOriginRegistry.class.getName();

	private final Map<String, Class<?>> importOrigins;

	ImportOriginRegistry(Map<String, Class<?>> importOrigins) {
		this.importOrigins = Collections.unmodifiableMap(importOrigins);
	}

	/**
	 * Register a registry on the specified bean factory using the specified
	 * {@code importOrigins}.
	 * @param beanFactory the bean factory to use to register a registry
	 * @param importOrigins the import origins
	 */
	public static void register(ConfigurableBeanFactory beanFactory, Map<String, Class<?>> importOrigins) {
		beanFactory.registerSingleton(BEAN_NAME, new ImportOriginRegistry(importOrigins));
	}

	/**
	 * Return the registry instance associated with this registry, or {@code null} if none
	 * is associated.
	 * @param beanFactory the bean factory
	 * @return the registry associated with this bean factory, or {@code null}
	 */
	@Nullable
	public static ImportOriginRegistry get(BeanFactory beanFactory) {
		try {
			return beanFactory.getBean(BEAN_NAME, ImportOriginRegistry.class);
		}
		catch (NoSuchBeanDefinitionException ex) {
			return null;
		}
	}

	/**
	 * Return an immutable mapping of the import origins.
	 * @return a mapping between an import aware type and the class that imported it
	 */
	public Map<String, Class<?>> getImportOrigins() {
		return this.importOrigins;
	}

}

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

package org.springframework.aot.factories;

import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ClassUtils;

/**
 * An entry in a {@code spring.factories} file.
 * 
 * @author Brian Clozel
 * @author Sebastien Deleuze
 */
public class SpringFactory {

	private static final Log logger = LogFactory.getLog(SpringFactory.class);

	private final Class<?> factoryType;

	private final Class<?> factory;

	private SpringFactory(Class<?> factoryType, Class<?> factory) {
		this.factoryType = factoryType;
		this.factory = factory;
	}

	public static SpringFactory resolve(String factoryTypeName, String factoryName, ClassLoader classLoader) {
		try {
			Class<?> factoryType = ClassUtils.resolveClassName(factoryTypeName, classLoader);
			Class<?> factory = ClassUtils.resolveClassName(factoryName, classLoader);
			return new SpringFactory(factoryType, factory);
		}
		catch (IllegalArgumentException exc) {
			logger.debug("Could not load SpringFactory: " + exc.getMessage());
			return null;
		}
	}

	public Class<?> getFactoryType() {
		return this.factoryType;
	}

	public Class<?> getFactory() {
		return this.factory;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SpringFactory that = (SpringFactory) o;
		return factoryType.equals(that.factoryType) && factory.equals(that.factory);
	}

	@Override
	public int hashCode() {
		return Objects.hash(factoryType, factory);
	}

	@Override
	public String toString() {
		return "SpringFactory{" +
				"factoryType=" + factoryType.getName() +
				", factory=" + factory.getName() +
				'}';
	}
}

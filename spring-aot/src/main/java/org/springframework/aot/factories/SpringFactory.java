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
import org.springframework.core.type.classreading.ClassDescriptor;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.type.MissingTypeException;

/**
 * An entry in a {@code spring.factories} file.
 * 
 * @author Brian Clozel
 */
public class SpringFactory {

	private static final Log logger = LogFactory.getLog(SpringFactory.class);

	private final ClassDescriptor factoryType;

	private final ClassDescriptor factory;

	private SpringFactory(ClassDescriptor factoryType, ClassDescriptor factory) {
		this.factoryType = factoryType;
		this.factory = factory;
	}

	public static SpringFactory resolve(String factoryTypeName, String factoryName, TypeSystem typeSystem) {
		try {
			ClassDescriptor factoryType = typeSystem.resolveClass(factoryTypeName);
			ClassDescriptor factory = typeSystem.resolveClass(factoryName);
			if (factoryType == null) {
				logger.debug("Could not resolve factory type "+factoryType);
				return null;
			}
			if (factory == null) {
				logger.debug("Could not resolve factory "+factoryName);
				return null;
			}
			return new SpringFactory(factoryType, factory);
		}
		catch (MissingTypeException exc) { // TODO can this still happen with the new type system?
			logger.debug("Could not load SpringFactory: " + exc.getMessage());
			return null;
		}
	}

	public ClassDescriptor getFactoryType() {
		return this.factoryType;
	}

	public ClassDescriptor getFactory() {
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
				"factoryType=" + factoryType.getClassName() +
				", factory=" + factory.getClassName() +
				'}';
	}
}

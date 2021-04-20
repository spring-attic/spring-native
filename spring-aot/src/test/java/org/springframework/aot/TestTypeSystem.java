/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.aot;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.nativex.type.MissingTypeException;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeName;
import org.springframework.nativex.type.TypeSystem;

/**
 * A type {@link TypeSystem} reading {@literal ./target/classes} able to resolve a given {@link Class} to its {@link Type}.
 * Allows to {@link #excludePackages(String...)} based on their name.
 *
 * @author Christoph Strobl
 */
public class TestTypeSystem extends TypeSystem {

	private Set<String> excludedPackages;

	public TestTypeSystem() {
		this(Arrays.asList(new File("./target/classes").toString(), new File("./target/test-classes").toString()));
	}

	public TestTypeSystem(List<String> classpath) {
		super(classpath);
		this.excludedPackages = new LinkedHashSet<>();
	}

	@Override
	public Type resolve(String classname) {
		return super.resolve(classname);
	}

	public Type resolve(Class<?> type) {
		return resolve(TypeName.fromClassName(type.getName()));
	}

	@Override
	public Type resolveSlashed(String slashedTypeName, boolean allowNotFound) {

		Type type = super.resolveSlashed(slashedTypeName, allowNotFound);
		if (!isPartOfExcludedPackage(type)) {
			return type;
		}

		if (allowNotFound) {
			return null;
		}
		throw new MissingTypeException(slashedTypeName);
	}

	private boolean isPartOfExcludedPackage(Type type) {

		return excludedPackages.stream().
				filter(type::isPartOfDomain)
				.findAny().isPresent();
	}

	public TestTypeSystem excludePackages(String... packages) {

		for (String pkg : packages) {
			excludedPackages.add(pkg);
		}
		return this;
	}

	public TestTypeSystem clearExclusions() {

		this.excludedPackages.clear();
		return this;
	}
}

/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.data;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.data.TypeUtils.TypeOps;
import org.springframework.data.TypeUtils.TypeOps.PackageFilter;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.util.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * Entry point to access {@link Field fields}, {@link Method methods} and {@link Constructor constructors} honoring
 * {@link PersistenceConstructor} annotation.
 *
 * @author Christoph Strobl
 */
public class TypeModel { // TODO: implements TypeInformation

	private Class<?> type;
	private Set<Method> methods = new LinkedHashSet<>();
	private Set<Constructor> constructors = new LinkedHashSet<>();
	private Set<Field> fields = new LinkedHashSet<>();
	private Optional<Constructor> persistenceConstructor;
	private TypeOps typeOps;

	public TypeModel(Class<?> type) {

		this.type = type;
		this.typeOps = TypeUtils.type(type);
	}

	TypeModel addMethod(Method method) {
		methods.add(method);
		return this;
	}

	TypeModel addField(Field field) {
		fields.add(field);
		return this;
	}

	public void addConstructor(Constructor<?> constructor) {
		this.constructors.add(constructor);
	}

	public void doWithMethods(Consumer<Method> consumer) {
		methods.forEach(consumer);
	}

	public void doWithFields(Consumer<Field> consumer) {
		fields.forEach(consumer);
	}

	public void doWithConstructors(Consumer<Constructor> consumer) {
		constructors.forEach(consumer);
	}

	public void doWithAnnotatedElements(Consumer<AnnotatedElement> consumer) {

		consumer.accept(getType());
		getConstructors().forEach(consumer);
		getMethods().forEach(consumer);
		getFields().forEach(consumer);
	}

	public Class<?> getType() {
		return type;
	}

	public Set<Method> getMethods() {
		return methods;
	}

	public Set<Field> getFields() {
		return fields;
	}

	boolean isAnnotation() {
		return ClassUtils.isAssignable(Annotation.class, getType());
	}

	public Set<Constructor> getConstructors() {
		return constructors;
	}

	public boolean hasMethods() {
		return !getMethods().isEmpty();
	}

	public boolean hasFields() {
		return !getFields().isEmpty();
	}

	public boolean hasPersistenceConstructor() {
		return getPersistenceConstructor() != null;
	}

	public boolean isPartOf(PackageFilter packageFilter) {
		return typeOps.isPartOf(packageFilter);
	}

	public boolean isPartOf(String... packageNames) {
		return typeOps.isPartOf(packageNames);
	}

	@Nullable
	public Constructor getPersistenceConstructor() {

		if(persistenceConstructor == null) {
			persistenceConstructor = Optional.ofNullable(computePersistenceConstructor());
		}
		return persistenceConstructor.orElse(null);
	}

	private Constructor computePersistenceConstructor() {

		// TODO: unify with PreferredConstructorDiscoverer but must not use reflection to make ctor accessible at build time
		if (TypeUtils.type(getType()).isPartOf("java", "javax", "sun")) {
			return null;
		}

		List<Constructor<?>> candidates = new ArrayList<>();
		Constructor<?> noArg = null;
		for (Constructor<?> candidate : constructors) {

			// Synthetic constructors should not be considered
			if (candidate.isSynthetic()) {
				continue;
			}

			if (MergedAnnotations.from(candidate).isPresent("org.springframework.data.annotation.PersistenceConstructor")) {
				return candidate;
			}

			if (candidate.getParameterCount() == 0) {
				noArg = candidate;
			} else {
				candidates.add(candidate);
			}
		}

		if (noArg != null) {
			return noArg;
		}

		return candidates.size() > 1 || candidates.isEmpty() ? null
				: candidates.iterator().next();
	}

	@Override
	public String toString() {
		return "TypeInspectionResult {" +
				"\n\ttype=" + type + "," +
				"\n\tmethods={\n\t" + methods.stream().map(Method::toString).collect(Collectors.joining(",\n\t")) + "}," +
				"\n\tfields={\n\t" + fields.stream().map(Field::toString).collect(Collectors.joining(",\n\t")) + "},\n" +
				'}';
	}
}

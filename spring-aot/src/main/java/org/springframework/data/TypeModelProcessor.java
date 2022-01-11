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
package org.springframework.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christoph Strobl
 * @author Sebastien Deleuze
 */
public class TypeModelProcessor {

	private static Log logger = LogFactory.getLog(TypeModelProcessor.class);

	static final Set<String> EXCLUDED_DOMAINS = new HashSet<>(Arrays.asList("sun.", "jdk.", "reactor.", "kotlinx.", "kotlin.",
			"org.springframework.core.", "org.springframework.data.", "org.springframework.boot."));

	private Predicate<Class<?>> typeFilter = (type) -> EXCLUDED_DOMAINS.stream().noneMatch(it -> {
		if (type.getPackageName().startsWith("java.")) {
			if (type.getPackageName().startsWith("java.util")) {
				return false;
			}
			if (type.getPackageName().startsWith("java.time")) {
				return false;
			}
			if (type.getPackageName().equals("java.lang") && Modifier.isFinal(type.getModifiers())) {
				return false;
			}
			return true;
		}
		if (type.getPackageName().startsWith(it)) {
			return true;
		}
		return false;
	});

	private final Predicate<Method> methodFilter = (method) -> {
		if (method.getName().startsWith("$$_hibernate")) {
			return false;
		}
		if (method.getDeclaringClass().getPackageName().startsWith("java.") ||
				EXCLUDED_DOMAINS.stream().anyMatch(it ->
						method.getDeclaringClass().getPackageName().startsWith(it))) {
			return false;
		}
		return (!Modifier.isNative(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers()) && !Modifier.isProtected(method.getModifiers()))
				|| !method.getDeclaringClass().equals(Object.class);
	};

	private Predicate<Field> fieldFilter = (field) -> {
		if (field.isSynthetic() | field.getName().startsWith("$$_hibernate")) {
			return false;
		}
		if (field.getDeclaringClass().getPackageName().startsWith("java.")) {
			return false;
		}
		return true;
	};

	public TypeModelProcessor filterFields(Predicate<Field> filter) {
		this.fieldFilter = filter.and(filter);
		return this;
	}

	public TypeModelProcessor filterTypes(Predicate<Class<?>> filter) {
		this.typeFilter = this.typeFilter.and(filter);
		return this;
	}

	/**
	 * Inspect the given type and resolve those reachable via fields, methods, generics, ...
	 * @param type the type to inspect
	 * @return a type model collector for the type
	 */
	public TypeModelCollector inspect(Class<?> type) {
		return new TypeModelCollector(type);
	}

	private void process(Class<?> root, Consumer<TypeModel> consumer) {
		processType(ResolvableType.forType(root), new InspectionCache(), consumer);
	}

	private void processType(ResolvableType type, InspectionCache cache, Consumer<TypeModel> callback) {
		if (ResolvableType.NONE.equals(type) || cache.contains(type.toClass()) || !typeFilter.test(type.toClass())) {
			return;
		}
		TypeModel result = new TypeModel(type.toClass());
		cache.put(result.getType(), result);
		Set<Type> additionalTypes = new LinkedHashSet<>();
		additionalTypes.addAll(TypeUtils.resolveTypesInSignature(type));
		additionalTypes.addAll(visitConstructorsOfType(type, result));
		additionalTypes.addAll(visitMethodsOfType(type, result));
		additionalTypes.addAll(visitFieldsOfType(type, result));
		if(result.hasDeclaredClasses()) {
			additionalTypes.addAll(Arrays.asList(result.getType().getDeclaredClasses()));
		}
		callback.accept(result);
		for (Type discoveredType : additionalTypes) {
			processType(ResolvableType.forType(discoveredType, type), cache, callback);
		}
	}

	Set<Type> visitConstructorsOfType(ResolvableType type, TypeModel result) {
		if (!typeFilter.test(type.toClass())) {
			return Collections.emptySet();
		}
		Set<Type> discoveredTypes = new LinkedHashSet<>();
		for (Constructor<?> constructor : type.toClass().getDeclaredConstructors()) {
			result.addConstructor(constructor);
			for (Class<?> signatureType : TypeUtils.resolveTypesInSignature(type.toClass(), constructor)) {
				if (typeFilter.test(signatureType)) {
					discoveredTypes.add(signatureType);
				}
			}
		}
		return new HashSet<>(discoveredTypes);
	}

	Set<Type> visitMethodsOfType(ResolvableType type, TypeModel result) {
		if (!typeFilter.test(type.toClass())) {
			return Collections.emptySet();
		}
		Set<Type> discoveredTypes = new LinkedHashSet<>();
		try {
			ReflectionUtils.doWithLocalMethods(type.toClass(), method -> {
				if (!methodFilter.test(method)) {
					return;
				}
				result.addMethod(method);
				for (Class<?> signatureType : TypeUtils.resolveTypesInSignature(type.toClass(), method)) {
					if (typeFilter.test(signatureType)) {
						discoveredTypes.add(signatureType);
					}
				}
			});
		} catch (Exception ex) {
			logger.warn(ex);
		}
		return new HashSet<>(discoveredTypes);
	}

	Set<Type> visitFieldsOfType(ResolvableType type, TypeModel result) {
		Set<Type> discoveredTypes = new LinkedHashSet<>();
		ReflectionUtils.doWithLocalFields(type.toClass(), field -> {
			if (!fieldFilter.test(field)) {
				return;
			}
			result.addField(field);
			for (Class<?> signatureType : TypeUtils.resolveTypesInSignature(ResolvableType.forField(field, type))) {
				if (typeFilter.test(signatureType)) {
					discoveredTypes.add(signatureType);
				}
			}
		});
		return discoveredTypes;
	}

	public class TypeModelCollector {

		private final Class<?> root;

		public TypeModelCollector(Class<?> root) {
			this.root = root;
		}

		public void forEach(Consumer<TypeModel> consumer) {
			process(root, consumer);
		}

		public List<TypeModel> list() {
			List<TypeModel> target = new ArrayList<>();
			forEach(target::add);
			return target;
		}
	}

	static class InspectionCache {

		private final Map<Class<?>, TypeModel> mutableCache = new LinkedHashMap<>();

		public boolean contains(Class<?> key) {
			return mutableCache.containsKey(key);
		}

		public TypeModel get(Class<?> key) {
			return mutableCache.get(key);
		}

		public TypeModel put(Class<?> key, TypeModel value) {
			return mutableCache.put(key, value);
		}

		public Collection<TypeModel> values() {
			return mutableCache.values();
		}

		public Collection<TypeModel> types() {
			return values().stream().filter(it -> !it.isAnnotation()).collect(Collectors.toSet());
		}

		public Collection<TypeModel> annotations() {
			return values().stream().filter(TypeModel::isAnnotation).collect(Collectors.toSet());
		}

		public TypeModel putIfAbsent(Class<?> key, TypeModel value) {
			return mutableCache.putIfAbsent(key, value);
		}

		public TypeModel computeIfAbsent(Class<?> key, Function<Class<?>, ? extends TypeModel> mappingFunction) {
			return mutableCache.computeIfAbsent(key, mappingFunction);
		}
	}
}

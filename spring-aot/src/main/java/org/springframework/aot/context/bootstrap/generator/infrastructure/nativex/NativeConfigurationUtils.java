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

package org.springframework.aot.context.bootstrap.generator.infrastructure.nativex;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

// TODO review - does spring core already do some of these things?

/**
 * Common utilities for native configuration processors.
 *
 * @author Andy Clement
 */
public class NativeConfigurationUtils {

	/**
	 * Determine all the reference types used in a field signature. This includes
	 * navigating generic type references.
	 * @param field the field to check
	 * @return a set of the reference types used in the field signature
	 */
	public static Set<Class<?>> collectTypesInSignature(Field field) {
		Set<Class<?>> collector = new TreeSet<>(Comparator.comparing(Class::getName));
		NativeConfigurationUtils.collectReferenceTypesUsed(field.getGenericType(), collector);
		return collector;
	}

	/**
	 * Determine all the reference types used in a method signature. This includes navigating
	 * generic type references. This includes the return type and parameter types.
	 * @param controllerMethod the method to check
	 * @return a set of the reference types used in the method signature
	 */
	public static Set<Class<?>> collectTypesInSignature(Method controllerMethod) {
		Set<Class<?>> collector = new TreeSet<>(Comparator.comparing(Class::getName));
		collectReferenceTypesUsed(controllerMethod.getGenericReturnType(), collector);
		for (Type parameterType : controllerMethod.getGenericParameterTypes()) {
			collectReferenceTypesUsed(parameterType, collector);
		}
		return collector;
	}

	public static Set<Class<?>> collectTypesInSignature(Class<?> clazz) {
		Set<Class<?>> collector = new TreeSet<>((c1,c2) -> c1.getName().compareTo(c2.getName()));
		Type genericSuperclass = clazz.getGenericSuperclass();
		collectReferenceTypesUsed(genericSuperclass, collector);
		Type[] genericInterfaces = clazz.getGenericInterfaces();
		for (Type genericInterface: genericInterfaces) {
			collectReferenceTypesUsed(genericInterface, collector);
		}
		return collector;
	}

	// TODO does this handle all relevant cases?
	public static void collectReferenceTypesUsed(Type type, Set<Class<?>> collector) {
		if (type instanceof GenericArrayType) {
			GenericArrayType gaType = (GenericArrayType) type;
			collectReferenceTypesUsed(gaType.getGenericComponentType(), collector);
		}
		else if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			collectReferenceTypesUsed(pType.getRawType(), collector);
			for (Type typeArg : pType.getActualTypeArguments()) {
				collectReferenceTypesUsed(typeArg, collector);
			}
		}
		else if (type instanceof TypeVariable) {
			// not interesting, although should bound be considered?
		}
		else if (type instanceof WildcardType) {
			WildcardType wType = (WildcardType) type;
			for (Type typeUpper : wType.getUpperBounds()) {
				collectReferenceTypesUsed(typeUpper, collector);
			}
			for (Type typeLower : wType.getLowerBounds()) {
				collectReferenceTypesUsed(typeLower, collector);
			}
		}
		else if (type instanceof Class) {
			Class<?> clazz = (Class<?>) type;
			if (!clazz.isPrimitive()) {
				collector.add((Class<?>) type);
			}
		}
	}

}

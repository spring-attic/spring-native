/*
 * Copyright 2019-2022 the original author or authors.
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.ResolvableType;

/**
 * Map generic types of a base class to a configurable parent.
 * <p>
 * Package-private to the Spring Data use case for now although this may be reused for
 * other use cases in the future.
 *
 * @author Stephane Nicoll
 */
final class ParameterizedTypeMapper {

	private final Class<?> baseClass;

	private final List<ParameterizedType> types;

	private ParameterizedTypeMapper(Class<?> baseClass, Class<?> target) {
		this.baseClass = baseClass;
		this.types = buildHierarchy(baseClass, target);
	}

	private static List<ParameterizedType> buildHierarchy(Class<?> from, Class<?> to) {
		List<ParameterizedType> types = new ArrayList<>();
		ResolvableType current = ResolvableType.forClass(from);
		while (current.toClass() != to) {
			ResolvableType parent = current.getSuperType();
			if (parent.equals(ResolvableType.NONE)) {
				throw new IllegalArgumentException("target parent not found in hierarchy" + to);
			}
			if (!(parent.getType() instanceof ParameterizedType)) {
				throw new IllegalStateException("Parent is not parameterized " + parent);
			}
			types.add((ParameterizedType) parent.getType());
			current = parent;
		}
		return types;
	}

	/**
	 * Create a {@link ParameterizedTypeMapper} for the specified hierarchy. Both the
	 * base class and the parents must have generic attributes.
	 * @param baseClass the base class to resolve
	 * @param targetParent the target parent
	 * @return a new mapper instance
	 */
	public static ParameterizedTypeMapper of(Class<?> baseClass, Class<?> targetParent) {
		if (baseClass.getTypeParameters().length == 0) {
			throw new IllegalArgumentException("Base class must have generics " + baseClass);
		}
		if (targetParent.getTypeParameters().length == 0) {
			throw new IllegalArgumentException("Target parent must have generics " + baseClass);
		}
		if (!targetParent.isAssignableFrom(baseClass)) {
			throw new IllegalArgumentException("Target parent " + targetParent + " must be a parent of " + baseClass);
		}
		return new ParameterizedTypeMapper(baseClass, targetParent);
	}

	/**
	 * Map the generics for the base class using the specified types for the parent.
	 * @param parentGenericTypes the generic types of the parent
	 * @return the generics to use for the base class
	 */
	public ResolvableType[] mapGenericTypes(ResolvableType... parentGenericTypes) {
		int parametersSize = this.baseClass.getTypeParameters().length;
		ResolvableType[] genericTypes = new ResolvableType[parametersSize];
		for (int i = 0; i < parametersSize; i++) {
			Integer targetIndex = getTargetGenericIndexFor(i);
			genericTypes[i] = (targetIndex != null
					? parentGenericTypes[targetIndex] : ResolvableType.NONE);
		}
		return genericTypes;
	}

	public ResolvableType[] mapGenericTypes(Class<?>... parentGenericTypes) {
		return mapGenericTypes(Arrays.stream(parentGenericTypes)
				.map(ResolvableType::forClass).toArray(ResolvableType[]::new));
	}

	private Integer getTargetGenericIndexFor(int baseClassGenericIndex) {
		if (baseClassGenericIndex >= this.baseClass.getTypeParameters().length) {
			throw new IllegalArgumentException(createExceptionMessage(
					"out-of-bound generic index " + baseClassGenericIndex));
		}
		return mapGeneric(this.baseClass, baseClassGenericIndex, 0);
	}

	private Integer mapGeneric(Class<?> currentClass, int genericIndex, int hierarchyIndex) {
		TypeVariable<? extends Class<?>> typeParameter = currentClass.getTypeParameters()[genericIndex];
		ParameterizedType parent = this.types.get(hierarchyIndex);
		Integer parentIndex = findMatchingTypeVariable(parent, typeParameter);
		if (parentIndex == null) {
			return null;
		}
		if (hierarchyIndex == this.types.size() - 1) {
			return parentIndex;
		}
		return mapGeneric((Class<?>) parent.getRawType(), parentIndex, ++hierarchyIndex);
	}

	private Integer findMatchingTypeVariable(ParameterizedType type, TypeVariable<?> typeVariable) {
		Type[] actualTypeArguments = type.getActualTypeArguments();
		for (int i = 0; i < actualTypeArguments.length; i++) {
			Type candidate = actualTypeArguments[i];
			if (candidate.getTypeName().equals(typeVariable.getName())) {
				return i;
			}
		}
		return null;
	}

	private String createExceptionMessage(String cause) {
		return String.format("Failed to match generics for '%s': %s", this.baseClass.getName(), cause);
	}

}

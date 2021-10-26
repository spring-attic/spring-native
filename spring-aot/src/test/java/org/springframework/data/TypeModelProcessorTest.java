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

import static org.assertj.core.api.Assertions.*;
import static org.springframework.data.TypeModelProcessorTest.TypeModelAssert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.sample.data.types.AbstractType;
import org.springframework.sample.data.types.CyclicGenerics;
import org.springframework.sample.data.types.CyclicPropertiesA;
import org.springframework.sample.data.types.CyclicPropertiesB;
import org.springframework.sample.data.types.CyclicPropertiesSelf;
import org.springframework.sample.data.types.FieldsAndMethods;
import org.springframework.sample.data.types.InterfaceType;
import org.springframework.sample.data.types.TypesInMethodSignatures;

/**
 * @author Christoph Strobl
 */
class TypeModelProcessorTest {

	@Test
	void detectsFieldsAndMethodsOfType() {
		assertThatModel(computeModelFor(AbstractType.class))
				.containsOnlyMethods("abstractMethod", "methodDefinedInAbstractType")
				.containsOnlyFields("fieldInAbstractType")
				.hasPersistenceConstructor();
	}

	@Test
	void detectsOnlyLocalFields() {
		assertThatModel(computeModelFor(FieldsAndMethods.class))
				.containsOnlyMethods("abstractMethod", "someDefaultMethod", "privateMethod", "packagePrivateMethod", "protectedMethod", "publicMethod")
				.containsOnlyFields("CONSTANT_FIELD", "privateField", "packagePrivateField", "protectedField", "publicField")
				.hasPersistenceConstructor();
	}

	@Test
	void coversSignatureTypes() {
		assertThat(new TypeModelProcessor().inspect(FieldsAndMethods.class).list())
				.<Class<?>>map(TypeModel::getType)
				.contains(FieldsAndMethods.class, AbstractType.class, InterfaceType.class);
	}

	@Test
	void coversMethodArgs() {
		assertThat(new TypeModelProcessor().inspect(TypesInMethodSignatures.class).list())
				.<Class<?>>map(TypeModel::getType)
				.contains(TypesInMethodSignatures.class, String.class, Long.class, Integer.class, Void.class);
	}

	@Test
	void doesNotOverflowOnCyclicPropertyReferences() {
		assertThat(new TypeModelProcessor().inspect(CyclicPropertiesA.class).list())
				.<Class<?>>map(TypeModel::getType)
				.contains(CyclicPropertiesA.class, CyclicPropertiesB.class);
	}

	@Test
	void doesNotOverflowOnCyclicSelfReferences() {
		assertThat(new TypeModelProcessor().inspect(CyclicPropertiesSelf.class).list())
				.<Class<?>>map(TypeModel::getType)
				.contains(CyclicPropertiesSelf.class);
	}

	@Test
	void doesNotOverflowOnCyclicGenericsReferences() {
		assertThat(new TypeModelProcessor().inspect(CyclicGenerics.class).list())
				.<Class<?>>map(TypeModel::getType)
				.contains(CyclicGenerics.class);
	}


	private TypeModel computeModelFor(Class<?> type) {
		List<TypeModel> modelList = new TypeModelProcessor().inspect(type).list();
		for (TypeModel model : modelList) {
			if (model.getType().equals(type)) {
				return model;
			}
		}
		Assertions.fail("Could not find model for " + type.getSimpleName());
		return null; // why oh why
	}

	static class TypeModelAssert extends AbstractAssert<TypeModelAssert, TypeModel> {

		public TypeModelAssert(TypeModel actual) {
			super(actual, TypeModelAssert.class);
		}

		static TypeModelAssert assertThatModel(TypeModel model) {
			return new TypeModelAssert(model);
		}

		TypeModelAssert matchesType(Class<?> type) {
			isNotNull();
			assertThat(actual.getType()).isEqualTo(type);
			return this;
		}

		TypeModelAssert containsOnlyMethods(String... methods) {
			isNotNull();
			assertThat(actual.getMethods().stream().map(Method::getName).distinct()).containsExactlyInAnyOrder(methods);
			return this;
		}

		TypeModelAssert containsOnlyFields(String... fields) {
			isNotNull();
			assertThat(actual.getFields().stream().map(Field::getName).distinct()).containsExactlyInAnyOrder(fields);
			return this;
		}

		public TypeModelAssert hasPersistenceConstructor() {
			return hasPersistenceConstructor(true);
		}

		public TypeModelAssert hasPersistenceConstructor(boolean available) {
			isNotNull();
			assertThat(actual.hasPersistenceConstructor()).isEqualTo(available);
			return this;
		}
	}
}

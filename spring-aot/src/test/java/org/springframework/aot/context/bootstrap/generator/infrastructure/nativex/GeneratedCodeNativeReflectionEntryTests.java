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

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import org.junit.jupiter.api.Test;

import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.hint.TypeAccess;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GeneratedCodeNativeReflectionEntry}.
 *
 * @author Stephane Nicoll
 */
class GeneratedCodeNativeReflectionEntryTests {

	private static final ClassName TEST_CLASS = ClassName.get("com.example", "Test");

	private static final ClassName VISIBILITY_TEST_CLASS = ClassName.get(VisibilityTestClass.class);

	@Test
	void toClassDescriptorShouldRegisterClassName() {
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(
				ClassName.get(String.class)).build().toClassDescriptor();
		assertThat(descriptor.getName()).isEqualTo("java.lang.String");
	}

	@Test
	void toClassDescriptorShouldRegisterInnerClassWithDollarSeparator() {
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(
				ClassName.get(TestClass.class)).build().toClassDescriptor();
		assertThat(descriptor.getName()).isEqualTo("org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.GeneratedCodeNativeReflectionEntryTests$TestClass");
	}

	@Test
	void toClassDescriptorShouldRegisterMethod() {
		MethodSpec methodSpec = MethodSpec.methodBuilder("test").addParameter(String.class, "first")
				.addParameter(ClassName.get("com.example", "Thing"), "second").build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS).withMethods(methodSpec)
				.build().toClassDescriptor();
		assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("test");
			assertThat(methodDescriptor.getParameterTypes()).containsExactly("java.lang.String", "com.example.Thing");
		});
	}

	@Test
	void toClassDescriptorShouldRegisterQueriedMethod() {
		MethodSpec methodSpec = MethodSpec.methodBuilder("test").addParameter(String.class, "first")
				.addParameter(Integer.class, "second").build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS).withQueriedMethods(methodSpec)
				.build().toClassDescriptor();
		assertThat(descriptor.getQueriedMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("test");
			assertThat(methodDescriptor.getParameterTypes()).containsExactly("java.lang.String", "java.lang.Integer");
		});
	}

	@Test
	void toClassDescriptorShouldRegisterMethodWithDollarSeparatorForParameters() {
		MethodSpec methodSpec = MethodSpec.methodBuilder("test").addParameter(ClassName.get(TestClass.class), "first").build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS).withMethods(methodSpec)
				.build().toClassDescriptor();
		assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("test");
			assertThat(methodDescriptor.getParameterTypes()).containsExactly(
					"org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.GeneratedCodeNativeReflectionEntryTests$TestClass");
		});
	}

	@Test
	void toClassDescriptorShouldRegisterConstructor() {
		MethodSpec methodSpec = MethodSpec.constructorBuilder().build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS).withMethods(methodSpec)
				.build().toClassDescriptor();
		assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("<init>");
			assertThat(methodDescriptor.getParameterTypes()).isEmpty();
		});
		assertThat(descriptor.getQueriedMethods()).isNull();
	}

	@Test
	void toClassDescriptorShouldRegisterQueriedConstructor() {
		MethodSpec methodSpec = MethodSpec.constructorBuilder().build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS).withQueriedMethods(methodSpec)
				.build().toClassDescriptor();
		assertThat(descriptor.getQueriedMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("<init>");
			assertThat(methodDescriptor.getParameterTypes()).isEmpty();
		});
		assertThat(descriptor.getMethods()).isNull();
	}

	@Test
	void toClassDescriptorShouldRegisterConstructorWithDollarSeparatorForParameters() {
		MethodSpec methodSpec = MethodSpec.constructorBuilder().addParameter(ClassName.get(TestClass.class), "first").build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS).withMethods(methodSpec)
				.build().toClassDescriptor();
		assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("<init>");
			assertThat(methodDescriptor.getParameterTypes()).containsExactly(
					"org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.GeneratedCodeNativeReflectionEntryTests$TestClass");
		});
	}

	@Test
	void toClassDescriptorShouldRegisterFields() {
		FieldSpec fieldSpec = FieldSpec.builder(String.class, "field").build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS).withFields(fieldSpec)
				.build().toClassDescriptor();
		assertThat(descriptor.getFields()).singleElement().satisfies((fieldDescriptor) -> {
			assertThat(fieldDescriptor.getName()).isEqualTo("field");
			assertThat(fieldDescriptor.isAllowWrite()).isTrue();
			assertThat(fieldDescriptor.isAllowUnsafeAccess()).isFalse();
		});
	}

	@Test
	void toClassDescriptorShouldRegisterFieldsAccess() {
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS).withAccess(TypeAccess.DECLARED_CONSTRUCTORS)
				.withAccess(TypeAccess.DECLARED_METHODS).build().toClassDescriptor();
		assertThat(descriptor.getAccess()).containsOnly(TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS);
	}

	@Test
	void toClassDescriptorShouldNotRegisterSpecificConstructorIfAllDeclaredConstructorsIsSet() {
		MethodSpec methodSpec = MethodSpec.constructorBuilder().build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS).withMethods(methodSpec)
				.withAccess(TypeAccess.DECLARED_CONSTRUCTORS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).containsOnly(TypeAccess.DECLARED_CONSTRUCTORS);
		assertThat(descriptor.getMethods()).isNull();
	}

	@Test
	void toClassDescriptorShouldNotRegisterSpecificConstructorIfAllPublicConstructorsIsSet() {
		MethodSpec methodSpec = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS).withMethods(methodSpec)
				.withAccess(TypeAccess.PUBLIC_CONSTRUCTORS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).containsOnly(TypeAccess.PUBLIC_CONSTRUCTORS);
		assertThat(descriptor.getMethods()).isNull();
	}

	@Test
	void toClassDescriptorShouldRegisterSpecificConstructorIfNotMatchingAllPublicConstructorsFlag() {
		MethodSpec methodSpec = MethodSpec.constructorBuilder().addParameter(String.class, "name").build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(VISIBILITY_TEST_CLASS).withMethods(methodSpec)
				.withAccess(TypeAccess.PUBLIC_CONSTRUCTORS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).containsOnly(TypeAccess.PUBLIC_CONSTRUCTORS);
		assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("<init>");
			assertThat(methodDescriptor.getParameterTypes()).containsOnly("java.lang.String");
		});
	}

	@Test
	void toClassDescriptorShouldNotRegisterMethodIfAllDeclaredMethodsIsSet() {
		MethodSpec methodSpec = MethodSpec.methodBuilder("test").build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS).withMethods(methodSpec)
				.withAccess(TypeAccess.DECLARED_METHODS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).containsOnly(TypeAccess.DECLARED_METHODS);
		assertThat(descriptor.getMethods()).isNull();
	}

	@Test
	void toClassDescriptorShouldNotRegisterMethodIfAllPublicMethodsIsSet() {
		MethodSpec methodSpec = MethodSpec.methodBuilder("test").addModifiers(Modifier.PUBLIC).build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS).withMethods(methodSpec)
				.withAccess(TypeAccess.PUBLIC_METHODS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).containsOnly(TypeAccess.PUBLIC_METHODS);
		assertThat(descriptor.getMethods()).isNull();
	}

	@Test
	void toClassDescriptorShouldRegisterMethodIfNotMatchingAllPublicMethodsIsSet() {
		MethodSpec methodSpec = MethodSpec.methodBuilder("test").addParameter(String.class, "name").build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(VISIBILITY_TEST_CLASS).withMethods(methodSpec)
				.withAccess(TypeAccess.PUBLIC_METHODS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).containsOnly(TypeAccess.PUBLIC_METHODS);
		assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("test");
			assertThat(methodDescriptor.getParameterTypes()).containsOnly("java.lang.String");
		});
	}

	@Test
	void toClassDescriptorShouldNotRegisterFieldIfAllDeclaredFieldsIsSet() {
		FieldSpec fieldSpec = FieldSpec.builder(String.class, "test").build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS).withFields(fieldSpec)
				.withAccess(TypeAccess.DECLARED_FIELDS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).contains(TypeAccess.DECLARED_FIELDS);
		assertThat(descriptor.getFields()).isNull();
	}

	@Test
	void toClassDescriptorShouldNotRegisterFieldIfAllPublicFieldsIsSet() {
		FieldSpec fieldSpec = FieldSpec.builder(String.class, "test").addModifiers(Modifier.PUBLIC).build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS).withFields(fieldSpec)
				.withAccess(TypeAccess.PUBLIC_FIELDS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).contains(TypeAccess.PUBLIC_FIELDS);
		assertThat(descriptor.getFields()).isNull();
	}

	@Test
	void toClassDescriptorShouldRegisterFieldIfNotMatchingAllPublicFieldsIsSet() {
		FieldSpec fieldSpec = FieldSpec.builder(String.class, "test").build();
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(VISIBILITY_TEST_CLASS).withFields(fieldSpec)
				.withAccess(TypeAccess.PUBLIC_FIELDS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).contains(TypeAccess.PUBLIC_FIELDS);
		assertThat(descriptor.getFields()).singleElement().satisfies((fieldDescriptor) -> {
			assertThat(fieldDescriptor.getName()).isEqualTo("test");
			assertThat(fieldDescriptor.isAllowWrite()).isTrue();
			assertThat(fieldDescriptor.isAllowUnsafeAccess()).isFalse();
		});
	}

	@Test
	void toClassDescriptorShouldRegisterConditionalOnReachableType() {
		ClassDescriptor descriptor = GeneratedCodeNativeReflectionEntry.of(TEST_CLASS)
				.conditionalOnTypeReachable(ClassName.get("com.example", "Another"))
				.build().toClassDescriptor();
		assertThat(descriptor.getName()).isEqualTo("com.example.Test");
		assertThat(descriptor.getCondition().getTypeReachable()).isEqualTo("com.example.Another");
	}

	@SuppressWarnings("unused")
	static class TestClass {

		public String field;

		public TestClass() {
		}

		public TestClass(DefaultNativeReflectionEntryTests.TestClass testClass) {
		}

		public void test(String bean, Integer counter) {

		}

		public void test(String bean, Integer counter, DefaultNativeReflectionEntryTests.TestClass testClass) {

		}

	}

	static class VisibilityTestClass {

		String test;

		VisibilityTestClass(String name) {
		}

		void test(String bean) {

		}

	}

}

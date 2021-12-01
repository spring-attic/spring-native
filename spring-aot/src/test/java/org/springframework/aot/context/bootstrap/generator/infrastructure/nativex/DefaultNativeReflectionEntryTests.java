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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntry.FieldAccess;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultNativeReflectionEntry}.
 *
 * @author Brian Clozel
 * @author Sebastien Deleuze
 * @author Andy Clement
 */
class DefaultNativeReflectionEntryTests {

	@Test
	void toClassDescriptorShouldRegisterClassName() {
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(String.class).build().toClassDescriptor();
		assertThat(descriptor.getName()).isEqualTo("java.lang.String");
	}

	@Test
	void toClassDescriptorShouldRegisterClassNameForArray() {
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(String[].class).build().toClassDescriptor();
		assertThat(descriptor.getName()).isEqualTo("java.lang.String[]");
	}

	@Test
	void toClassDescriptorShouldRegisterClassNameForPrimitiveArray() {
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(int[][].class).build().toClassDescriptor();
		assertThat(descriptor.getName()).isEqualTo("int[][]");
	}

	@Test
	void toClassDescriptorShouldRegisterInnerClassWithDollarSeparator() {
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).build().toClassDescriptor();
		assertThat(descriptor.getName()).isEqualTo("org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntryTests$TestClass");
	}

	@Test
	void toClassDescriptorShouldRegisterMethod() {
		Method method = ReflectionUtils.findMethod(TestClass.class, "test", String.class, Integer.class);
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).withExecutables(method)
				.build().toClassDescriptor();
		assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("test");
			assertThat(methodDescriptor.getParameterTypes()).containsExactly("java.lang.String", "java.lang.Integer");
		});
	}

	@Test
	void toClassDescriptorShouldRegisterQueriedMethod() {
		Method method = ReflectionUtils.findMethod(TestClass.class, "test", String.class, Integer.class);
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).withQueriedExecutables(method)
				.build().toClassDescriptor();
		assertThat(descriptor.getQueriedMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("test");
			assertThat(methodDescriptor.getParameterTypes()).containsExactly("java.lang.String", "java.lang.Integer");
		});
	}

	@Test
	void toClassDescriptorShouldRegisterMethodWithDollarSeparatorForParameters() {
		Method method = ReflectionUtils.findMethod(TestClass.class, "test", String.class, Integer.class, TestClass.class);
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).withExecutables(method)
				.build().toClassDescriptor();
		assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("test");
			assertThat(methodDescriptor.getParameterTypes()).containsExactly("java.lang.String", "java.lang.Integer",
					"org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntryTests$TestClass");
		});
	}

	@Test
	void toClassDescriptorShouldRegisterConstructor() {
		Constructor<?> constructor = TestClass.class.getDeclaredConstructors()[0];
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).withExecutables(constructor)
				.build().toClassDescriptor();
		assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("<init>");
			assertThat(methodDescriptor.getParameterTypes()).isEmpty();
		});
	}

	@Test
	void toClassDescriptorShouldRegisterQueriedConstructor() {
		Constructor<?> constructor = TestClass.class.getDeclaredConstructors()[0];
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).withQueriedExecutables(constructor)
				.build().toClassDescriptor();
		assertThat(descriptor.getQueriedMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("<init>");
			assertThat(methodDescriptor.getParameterTypes()).isEmpty();
		});
	}

	@Test
	void toClassDescriptorShouldRegisterConstructorWithDollarSeparatorForParameters() {
		Constructor<?> constructor = TestClass.class.getDeclaredConstructors()[1];
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).withExecutables(constructor)
				.build().toClassDescriptor();
		assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("<init>");
			assertThat(methodDescriptor.getParameterTypes()).containsExactly(
					"org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntryTests$TestClass");
		});
	}

	@Test
	void toClassDescriptorShouldRegisterFields() {
		Field field = ReflectionUtils.findField(TestClass.class, "field");
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).withFields(field)
				.build().toClassDescriptor();
		assertThat(descriptor.getFields()).singleElement().satisfies((fieldDescriptor) -> {
			assertThat(fieldDescriptor.getName()).isEqualTo("field");
			assertThat(fieldDescriptor.isAllowWrite()).isTrue();
			assertThat(fieldDescriptor.isAllowUnsafeAccess()).isFalse();
		});
	}

	@Test
	void toClassDescriptorShouldRegisterFieldsAccess() {
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).withAccess(TypeAccess.DECLARED_CONSTRUCTORS)
				.withAccess(TypeAccess.DECLARED_METHODS).build().toClassDescriptor();
		assertThat(descriptor.getAccess()).containsOnly(TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS);
	}

	@Test
	void toClassDescriptorShouldNotRegisterSpecificConstructorIfAllDeclaredConstructorsIsSet() {
		Constructor<?> constructor = TestClass.class.getDeclaredConstructors()[0];
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).withExecutables(constructor)
				.withAccess(TypeAccess.DECLARED_CONSTRUCTORS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).containsOnly(TypeAccess.DECLARED_CONSTRUCTORS);
		assertThat(descriptor.getMethods()).isNull();
	}

	@Test
	void toClassDescriptorShouldNotRegisterSpecificConstructorIfAllPublicConstructorsIsSet() {
		Constructor<?> constructor = TestClass.class.getDeclaredConstructors()[0];
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).withExecutables(constructor)
				.withAccess(TypeAccess.PUBLIC_CONSTRUCTORS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).containsOnly(TypeAccess.PUBLIC_CONSTRUCTORS);
		assertThat(descriptor.getMethods()).isNull();
	}

	@Test
	void toClassDescriptorShouldRegisterSpecificConstructorIfNotMatchingAllPublicConstructorsFlag() {
		Constructor<?> constructor = VisibilityTestClass.class.getDeclaredConstructors()[0];
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(VisibilityTestClass.class).withExecutables(constructor)
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
		Method method = ReflectionUtils.findMethod(TestClass.class, "test", String.class, Integer.class);
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).withExecutables(method)
				.withAccess(TypeAccess.DECLARED_METHODS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).containsOnly(TypeAccess.DECLARED_METHODS);
		assertThat(descriptor.getMethods()).isNull();
	}

	@Test
	void toClassDescriptorShouldNotRegisterMethodIfAllPublicMethodsIsSet() {
		Method method = ReflectionUtils.findMethod(TestClass.class, "test", String.class, Integer.class);
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).withExecutables(method)
				.withAccess(TypeAccess.PUBLIC_METHODS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).containsOnly(TypeAccess.PUBLIC_METHODS);
		assertThat(descriptor.getMethods()).isNull();
	}

	@Test
	void toClassDescriptorShouldRegisterMethodIfNotMatchingAllPublicMethodsIsSet() {
		Method method = ReflectionUtils.findMethod(VisibilityTestClass.class, "test", String.class);
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(VisibilityTestClass.class).withExecutables(method)
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
		Field field = ReflectionUtils.findField(TestClass.class, "field");
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).withFields(field)
				.withAccess(TypeAccess.DECLARED_FIELDS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).contains(TypeAccess.DECLARED_FIELDS);
		assertThat(descriptor.getFields()).isNull();
	}

	@Test
	void toClassDescriptorShouldNotRegisterFieldIfAllPublicFieldsIsSet() {
		Field field = ReflectionUtils.findField(TestClass.class, "field");
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class).withFields(field)
				.withAccess(TypeAccess.PUBLIC_FIELDS)
				.build().toClassDescriptor();
		assertThat(descriptor.getAccess()).contains(TypeAccess.PUBLIC_FIELDS);
		assertThat(descriptor.getFields()).isNull();
	}

	@Test
	void toClassDescriptorShouldRegisterFieldIfNotMatchingAllPublicFieldsIsSet() {
		Field field = ReflectionUtils.findField(VisibilityTestClass.class, "test");
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(VisibilityTestClass.class).withFields(field)
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
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(String.class).conditionalOnTypeReachable(Integer.class).build().toClassDescriptor();
		assertThat(descriptor.getName()).isEqualTo("java.lang.String");
		assertThat(descriptor.getCondition().getTypeReachable()).isEqualTo("java.lang.Integer");
	}

	@Test
	void toClassDescriptorShouldRegisterFieldWithSpecificAccess() {
		Field field = ReflectionUtils.findField(TestClass.class, "field");
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class)
				.withField(field, FieldAccess.ALLOW_WRITE, FieldAccess.UNSAFE)
				.build().toClassDescriptor();
		assertThat(descriptor.getFields()).containsExactly(FieldDescriptor.of("field", true, true));
	}

	@Test
	void toClassDescriptorShouldRegisterFieldsWithSpecificAccessOnlyOnce() {
		Field field = ReflectionUtils.findField(TestClass.class, "field");
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class)
				.withField(field, FieldAccess.ALLOW_WRITE, FieldAccess.UNSAFE)
				.withField(field, FieldAccess.ALLOW_WRITE, FieldAccess.UNSAFE)
				.build().toClassDescriptor();
		assertThat(descriptor.getFields()).containsExactly(FieldDescriptor.of("field", true, true));
	}

	@Test
	void toClassDescriptorShouldAppendDifferentFieldAccessForSameField() {
		Field field = ReflectionUtils.findField(TestClass.class, "field");
		ClassDescriptor descriptor = DefaultNativeReflectionEntry.of(TestClass.class)
				.withField(field, FieldAccess.UNSAFE)
				.withField(field, FieldAccess.ALLOW_WRITE)
				.build().toClassDescriptor();
		assertThat(descriptor.getFields()).containsExactly(FieldDescriptor.of("field", true, true));
	}

	@SuppressWarnings("unused")
	static class TestClass {

		public String field;

		public TestClass() {
		}

		public TestClass(TestClass testClass) {
		}

		public void test(String bean, Integer counter) {

		}

		public void test(String bean, Integer counter, TestClass testClass) {

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

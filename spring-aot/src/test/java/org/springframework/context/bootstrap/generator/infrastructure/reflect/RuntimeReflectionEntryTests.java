/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.context.bootstrap.generator.infrastructure.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.hint.Flag;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RuntimeReflectionEntry}.
 *
 * @author Brian Clozel
 */
class RuntimeReflectionEntryTests {

	@Test
	void toClassDescriptorShouldRegisterClassName() {
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(String.class).build().toClassDescriptor();
		assertThat(descriptor.getName()).isEqualTo("java.lang.String");
	}

	@Test
	void toClassDescriptorShouldRegisterInnerClassWithDollarSeparator() {
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(TestClass.class).build().toClassDescriptor();
		assertThat(descriptor.getName()).isEqualTo("org.springframework.context.bootstrap.generator.infrastructure.reflect.RuntimeReflectionEntryTests$TestClass");
	}

	@Test
	void toClassDescriptorShouldRegisterMethod() {
		Method method = ReflectionUtils.findMethod(TestClass.class, "test", String.class, Integer.class);
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(TestClass.class).withMethods(method)
				.build().toClassDescriptor();
		assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("test");
			assertThat(methodDescriptor.getParameterTypes()).containsExactly("java.lang.String", "java.lang.Integer");
		});
	}

	@Test
	void toClassDescriptorShouldRegisterConstructor() {
		Constructor<?> constructor = TestClass.class.getDeclaredConstructors()[0];
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(TestClass.class).withMethods(constructor)
				.build().toClassDescriptor();
		assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("<init>");
			assertThat(methodDescriptor.getParameterTypes()).isEmpty();
		});
	}

	@Test
	void toClassDescriptorShouldRegisterFields() {
		Field field = ReflectionUtils.findField(TestClass.class, "field");
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(TestClass.class).withFields(field)
				.build().toClassDescriptor();
		assertThat(descriptor.getFields()).singleElement().satisfies((fieldDescriptor) -> {
			assertThat(fieldDescriptor.getName()).isEqualTo("field");
			assertThat(fieldDescriptor.isAllowWrite()).isTrue();
			assertThat(fieldDescriptor.isAllowUnsafeAccess()).isFalse();
		});
	}

	@Test
	void toClassDescriptorShouldRegisterFieldsFlags() {
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(TestClass.class).withFlags(Flag.allDeclaredConstructors)
				.withFlags(Flag.allDeclaredMethods).build().toClassDescriptor();
		assertThat(descriptor.getFlags()).containsOnly(Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
	}

	@Test
	void toClassDescriptorShouldNotRegisterSpecificConstructorIfAllDeclaredConstructorsIsSet() {
		Constructor<?> constructor = TestClass.class.getDeclaredConstructors()[0];
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(TestClass.class).withMethods(constructor)
				.withFlags(Flag.allDeclaredConstructors)
				.build().toClassDescriptor();
		assertThat(descriptor.getFlags()).containsOnly(Flag.allDeclaredConstructors);
		assertThat(descriptor.getMethods()).isNull();
	}

	@Test
	void toClassDescriptorShouldNotRegisterSpecificConstructorIfAllPublicConstructorsIsSet() {
		Constructor<?> constructor = TestClass.class.getDeclaredConstructors()[0];
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(TestClass.class).withMethods(constructor)
				.withFlags(Flag.allPublicConstructors)
				.build().toClassDescriptor();
		assertThat(descriptor.getFlags()).containsOnly(Flag.allPublicConstructors);
		assertThat(descriptor.getMethods()).isNull();
	}

	@Test
	void toClassDescriptorShouldRegisterSpecificConstructorIfNotMatchingAllPublicConstructorsFlag() {
		Constructor<?> constructor = VisibilityTestClass.class.getDeclaredConstructors()[0];
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(VisibilityTestClass.class).withMethods(constructor)
				.withFlags(Flag.allPublicConstructors)
				.build().toClassDescriptor();
		assertThat(descriptor.getFlags()).containsOnly(Flag.allPublicConstructors);
		assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("<init>");
			assertThat(methodDescriptor.getParameterTypes()).containsOnly("java.lang.String");
		});
	}

	@Test
	void toClassDescriptorShouldNotRegisterMethodIfAllDeclaredMethodsIsSet() {
		Method method = ReflectionUtils.findMethod(TestClass.class, "test", String.class, Integer.class);
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(TestClass.class).withMethods(method)
				.withFlags(Flag.allDeclaredMethods)
				.build().toClassDescriptor();
		assertThat(descriptor.getFlags()).containsOnly(Flag.allDeclaredMethods);
		assertThat(descriptor.getMethods()).isNull();
	}

	@Test
	void toClassDescriptorShouldNotRegisterMethodIfAllPublicMethodsIsSet() {
		Method method = ReflectionUtils.findMethod(TestClass.class, "test", String.class, Integer.class);
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(TestClass.class).withMethods(method)
				.withFlags(Flag.allPublicMethods)
				.build().toClassDescriptor();
		assertThat(descriptor.getFlags()).containsOnly(Flag.allPublicMethods);
		assertThat(descriptor.getMethods()).isNull();
	}

	@Test
	void toClassDescriptorShouldRegisterMethodIfNotMatchingAllPublicMethodsIsSet() {
		Method method = ReflectionUtils.findMethod(VisibilityTestClass.class, "test", String.class);
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(VisibilityTestClass.class).withMethods(method)
				.withFlags(Flag.allPublicMethods)
				.build().toClassDescriptor();
		assertThat(descriptor.getFlags()).containsOnly(Flag.allPublicMethods);
		assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) -> {
			assertThat(methodDescriptor.getName()).isEqualTo("test");
			assertThat(methodDescriptor.getParameterTypes()).containsOnly("java.lang.String");
		});
	}

	@Test
	void toClassDescriptorShouldNotRegisterFieldIfAllDeclaredFieldsIsSet() {
		Field field = ReflectionUtils.findField(TestClass.class, "field");
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(TestClass.class).withFields(field)
				.withFlags(Flag.allDeclaredFields)
				.build().toClassDescriptor();
		assertThat(descriptor.getFlags()).contains(Flag.allDeclaredFields);
		assertThat(descriptor.getFields()).isNull();
	}

	@Test
	void toClassDescriptorShouldNotRegisterFieldIfAllPublicFieldsIsSet() {
		Field field = ReflectionUtils.findField(TestClass.class, "field");
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(TestClass.class).withFields(field)
				.withFlags(Flag.allPublicFields)
				.build().toClassDescriptor();
		assertThat(descriptor.getFlags()).contains(Flag.allPublicFields);
		assertThat(descriptor.getFields()).isNull();
	}

	@Test
	void toClassDescriptorShouldRegisterFieldIfNotMatchingAllPublicFieldsIsSet() {
		Field field = ReflectionUtils.findField(VisibilityTestClass.class, "test");
		ClassDescriptor descriptor = RuntimeReflectionEntry.of(VisibilityTestClass.class).withFields(field)
				.withFlags(Flag.allPublicFields)
				.build().toClassDescriptor();
		assertThat(descriptor.getFlags()).contains(Flag.allPublicFields);
		assertThat(descriptor.getFields()).singleElement().satisfies((fieldDescriptor) -> {
			assertThat(fieldDescriptor.getName()).isEqualTo("test");
			assertThat(fieldDescriptor.isAllowWrite()).isTrue();
			assertThat(fieldDescriptor.isAllowUnsafeAccess()).isFalse();
		});
	}

	@SuppressWarnings("unused")
	static class TestClass {

		public String field;

		public TestClass() {
		}

		public void test(String bean, Integer counter) {

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
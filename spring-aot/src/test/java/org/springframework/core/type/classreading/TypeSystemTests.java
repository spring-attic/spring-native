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

package org.springframework.core.type.classreading;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.classreading.test.OtherClass;
import org.springframework.core.type.classreading.test.SampleClass;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TypeSystem}
 * @author Brian Clozel
 */
class TypeSystemTests {

	private TypeSystem typeSystem = TypeSystem.getTypeSystem(new DefaultResourceLoader());

	@Test
	void shouldReturnSameTypeSystemInstance() {
		TypeDescriptor stringType = this.typeSystem.resolve(String.class.getName());
		assertThat(stringType).isNotNull();
		assertThat(stringType.isPrimitiveType()).isFalse();
		assertThat(stringType.getTypeSystem()).isEqualTo(this.typeSystem);
	}

	@Test
	void resolveSimpleClassHasNoDimension() {
		TypeDescriptor stringType = this.typeSystem.resolve(String.class.getName());
		assertThat(stringType).isNotNull();
		assertThat(stringType.getArrayDimensions()).isZero();
	}

	@Test
	void resolveStringArrayHasDimensions() {
		TypeDescriptor stringType = this.typeSystem.resolve(String.class.getName() + "[][]");
		assertThat(stringType).isNotNull();
		assertThat(stringType.getArrayDimensions()).isEqualTo(2);
	}

	@Test
	void resolvePrimitiveType() {
		TypeDescriptor byteType = this.typeSystem.resolve("byte");
		assertThat(byteType).isNotNull();
		assertThat(byteType.isPrimitiveType()).isTrue();
		assertThat(byteType.getClassDescriptor()).isNull();
		assertThat(byteType.getArrayDimensions()).isEqualTo(0);
	}

	@Test
	void resolvePrimitiveTypeArray() {
		TypeDescriptor intType = this.typeSystem.resolve("int[]");
		assertThat(intType).isNotNull();
		assertThat(intType.isPrimitiveType()).isTrue();
		assertThat(intType.getClassDescriptor()).isNull();
		assertThat(intType.getArrayDimensions()).isEqualTo(1);
	}

	@Test
	void resolveGenericTypeShouldIgnoreGeneric() {
		TypeDescriptor stringList = this.typeSystem.resolve("java.util.List<java.lang.String>");
		assertThat(stringList).isNotNull();
		assertThat(stringList.isPrimitiveType()).isFalse();
		assertThat(stringList.getClassDescriptor()).isNotNull();
		assertThat(stringList.getTypeName()).isEqualTo("java.util.List");
		assertThat(stringList.getArrayDimensions()).isEqualTo(0);
	}

	@Test
	void scanPackageShouldStreamClasses() {
		Stream<ClassDescriptor> classes = this.typeSystem.scan(SampleClass.class.getPackage().getName());
		assertThat(classes).map(ClassDescriptor::getClassName).hasSize(3)
				.containsExactlyInAnyOrder(SampleClass.class.getName(),
						SampleClass.InnerClass.class.getName(), OtherClass.class.getName());
	}

}
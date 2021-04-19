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

import java.io.Serializable;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.classreading.test.SampleClass;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ClassDescriptor}
 * @author Brian Clozel
 */
public class ClassDescriptorTests {

	private TypeSystem typeSystem = TypeSystem.getTypeSystem(new DefaultResourceLoader());

	@Test
	void resolveStringShouldHaveClassHierarchy() {
		TypeDescriptor stringType = this.typeSystem.resolve(String.class.getName());
		assertThat(stringType).isNotNull();
		ClassDescriptor stringClass = stringType.getClassDescriptor();
		assertThat(stringClass).isNotNull();
		assertThat(stringClass.getClassName()).isEqualTo(String.class.getName());
		assertThat(stringClass.getSuperClass()).isNotNull();
		assertThat(stringClass.getSuperClass().getClassName()).isEqualTo(Object.class.getName());
		assertThat(stringClass.getEnclosingClass()).isNull();
	}

	@Test
	void resolveStringShouldHaveInterfaces() {
		TypeDescriptor stringType = this.typeSystem.resolve(String.class.getName());
		ClassDescriptor stringClass = stringType.getClassDescriptor();
		assertThat(stringClass.getInterfaces()).hasSizeGreaterThanOrEqualTo(3).map(ClassDescriptor::getClassName)
				.contains(Serializable.class.getName(), Comparable.class.getName(), CharSequence.class.getName());
	}

	@Test
	void resolveStringShouldHaveClassModifiers() {
		TypeDescriptor stringType = this.typeSystem.resolve(String.class.getName());
		ClassDescriptor stringClass = stringType.getClassDescriptor();
		assertThat(stringClass.isInterface()).isFalse();
		assertThat(stringClass.isConcrete()).isTrue();
		assertThat(stringClass.isAnnotation()).isFalse();
		assertThat(stringClass.isAbstract()).isFalse();
	}

	@Test
	void resolveInnerClassShouldHaveDescriptor() {
		TypeDescriptor innerType = this.typeSystem.resolve(SampleClass.InnerClass.class.getName());
		ClassDescriptor innerClass = innerType.getClassDescriptor();
		assertThat(innerClass.isInterface()).isTrue();
		assertThat(innerClass.isConcrete()).isFalse();
		assertThat(innerClass.isAnnotation()).isFalse();
		assertThat(innerClass.isAbstract()).isTrue();
		assertThat(innerClass.getClassName())
				.isEqualTo("org.springframework.core.type.classreading.test.SampleClass$InnerClass");
		assertThat(innerClass.getCanonicalClassName())
				.isEqualTo("org.springframework.core.type.classreading.test.SampleClass.InnerClass");
	}

}

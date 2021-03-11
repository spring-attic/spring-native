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

import org.junit.jupiter.api.Test;

import org.springframework.core.annotation.Order;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.classreading.test.SampleClass;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MethodDescriptor}
 * @author Brian Clozel
 */
public class MethodDescriptorTests {

	private TypeSystem typeSystem = TypeSystem.getTypeSystem(new DefaultResourceLoader());

	@Test
	void resolveSampleShouldHaveMethods() {
		TypeDescriptor sampleType = this.typeSystem.resolve(SampleClass.class.getName());
		ClassDescriptor sampleClass = sampleType.getClassDescriptor();
		assertThat(sampleClass.getMethods()).hasSize(4).map(MethodDescriptor::getMethodName)
				.containsExactlyInAnyOrder("hello", "annotated", "SampleClass", "SampleClass");
	}

	@Test
	void resolveSampleShouldHaveManyConstructors() {
		TypeDescriptor sampleType = this.typeSystem.resolve(SampleClass.class.getName());
		ClassDescriptor sampleClass = sampleType.getClassDescriptor();
		assertThat(sampleClass.getConstructors()).hasSize(2);
	}

	@Test
	void resolveSampleShouldHaveDefaultConstructor() {
		TypeDescriptor sampleType = this.typeSystem.resolve(SampleClass.class.getName());
		ClassDescriptor sampleClass = sampleType.getClassDescriptor();
		assertThat(sampleClass.getDefaultConstructor()).isPresent();
	}

	@Test
	void resolveSampleShouldHaveAnnotatedMethods() {
		TypeDescriptor sampleType = this.typeSystem.resolve(SampleClass.class.getName());
		ClassDescriptor sampleClass = sampleType.getClassDescriptor();
		assertThat(sampleClass.getMethods().filter(method -> method.isAnnotated(Order.class.getName()))).hasSize(1)
				.map(method -> method.getAnnotations().get(Order.class.getName()))
				.map(orderAnnotation -> orderAnnotation.getInt("value")).containsOnly(42);
	}

	@Test
	void resolveMethodShouldGiveReturnTypeInformation() {
		TypeDescriptor sampleType = this.typeSystem.resolve(SampleClass.class.getName());
		ClassDescriptor sampleClass = sampleType.getClassDescriptor();
		assertThat(sampleClass.getMethods().filter(method -> method.getMethodName().equals("hello"))).hasSize(1)
				.map(method -> method.getReturnType()).map(type -> type.getTypeName()).containsExactly(String.class.getName());
	}
}

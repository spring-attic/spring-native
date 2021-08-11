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

package org.springframework.context.bootstrap.generator.bean.support;

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.springframework.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.core.ResolvableType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TypeWriter}.
 *
 * @author Stephane Nicoll
 */
class TypeWriterTests {

	@Test
	void generateResolvableTypeForTypeWithGenericParameter() {
		assertThat(generateTypeFor(
				ResolvableType.forClassWithGenerics(Function.class,
						ResolvableType.forClassWithGenerics(Supplier.class, String.class),
						ResolvableType.forClassWithGenerics(Supplier.class, Integer.class))))
				.isEqualTo("ResolvableType.forClassWithGenerics(Function.class, "
						+ "ResolvableType.forClassWithGenerics(Supplier.class, String.class), "
						+ "ResolvableType.forClassWithGenerics(Supplier.class, Integer.class))");
	}

	@Test
	void generateResolvableTypeForTypeWithMixedParameter() {
		assertThat(generateTypeFor(
				ResolvableType.forClassWithGenerics(Function.class,
						ResolvableType.forClassWithGenerics(Supplier.class, String.class),
						ResolvableType.forClass(Integer.class))))
				.isEqualTo("ResolvableType.forClassWithGenerics(Function.class, "
						+ "ResolvableType.forClassWithGenerics(Supplier.class, String.class), "
						+ "ResolvableType.forClass(Integer.class))");
	}

	private CodeSnippet generateTypeFor(ResolvableType type) {
		return CodeSnippet.of(new TypeWriter().generateTypeFor(type));
	}

}

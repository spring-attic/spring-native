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

package org.springframework.nativex.type;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TypeName}
 *
 * @author Brian Clozel
 */
class TypeNameTests {

	@ParameterizedTest
	@MethodSource("provideTypeNames")
	void shouldParseClassName(String className, String slashName, String typeSignature) {
		TypeName typeName = TypeName.fromClassName(className);
		assertThat(typeName.toSlashName()).isEqualTo(slashName);
		assertThat(typeName.toTypeSignature()).isEqualTo(typeSignature);
	}

	@ParameterizedTest
	@MethodSource("provideTypeNames")
	void shouldParseSlashName(String className, String slashName, String typeSignature) {
		TypeName typeName = TypeName.fromSlashName(slashName);
		assertThat(typeName.toClassName()).isEqualTo(className);
		assertThat(typeName.toTypeSignature()).isEqualTo(typeSignature);
	}

	@ParameterizedTest
	@MethodSource("provideTypeNames")
	void shouldParseTypeSignature(String className, String slashName, String typeSignature) {
		TypeName typeName = TypeName.fromTypeSignature(typeSignature);
		assertThat(typeName.toClassName()).isEqualTo(className);
		assertThat(typeName.toSlashName()).isEqualTo(slashName);
	}

	@Test
	void shouldGenerateShortName() {
		TypeName typeName = TypeName.fromClassName("java.lang.String");
		assertThat(typeName.toShortName()).isEqualTo("jl.String");
	}

	@Test
	void shouldExtractSimpleName() {
		TypeName typeName = TypeName.fromClassName("java.lang.String");
		assertThat(typeName.toSimpleName()).isEqualTo("String");
	}

	@Test
	void shouldExtractPackage() {
		TypeName typeName = TypeName.fromClassName("java.lang.String");
		assertThat(typeName.getPackageName()).isEqualTo("java.lang");
	}

	private static Stream<Arguments> provideTypeNames() {
		return Stream.of(
				Arguments.of("java.lang.String", "java/lang/String", "Ljava/lang/String;"),
				Arguments.of("java.lang.String[]", "java/lang/String[]", "[Ljava/lang/String;"),
				Arguments.of("java.lang.String[][]", "java/lang/String[][]", "[[Ljava/lang/String;")
		);
	}

}
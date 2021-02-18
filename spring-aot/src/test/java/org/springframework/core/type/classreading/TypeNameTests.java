package org.springframework.core.type.classreading;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.nativex.type.TypeName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link org.springframework.nativex.type.TypeName}
 *
 * @author Brian Clozel
 */
class TypeNameTests {

	@ParameterizedTest
	@MethodSource("provideTypeNames")
	void shouldParseClassName(String className, String slashName, String typeSignature) {
		org.springframework.nativex.type.TypeName typeName = org.springframework.nativex.type.TypeName.fromClassName(className);
		assertThat(typeName.toSlashName()).isEqualTo(slashName);
		assertThat(typeName.toTypeSignature()).isEqualTo(typeSignature);
	}

	@ParameterizedTest
	@MethodSource("provideTypeNames")
	void shouldParseSlashName(String className, String slashName, String typeSignature) {
		org.springframework.nativex.type.TypeName typeName = org.springframework.nativex.type.TypeName.fromSlashName(slashName);
		assertThat(typeName.toClassName()).isEqualTo(className);
		assertThat(typeName.toTypeSignature()).isEqualTo(typeSignature);
	}

	@ParameterizedTest
	@MethodSource("provideTypeNames")
	void shouldParseTypeSignature(String className, String slashName, String typeSignature) {
		org.springframework.nativex.type.TypeName typeName = org.springframework.nativex.type.TypeName.fromTypeSignature(typeSignature);
		assertThat(typeName.toClassName()).isEqualTo(className);
		assertThat(typeName.toSlashName()).isEqualTo(slashName);
	}

	@Test
	void shouldGenerateShortName() {
		org.springframework.nativex.type.TypeName typeName = org.springframework.nativex.type.TypeName.fromClassName("java.lang.String");
		assertThat(typeName.toShortName()).isEqualTo("jl.String");
	}

	@Test
	void shouldExtractSimpleName() {
		org.springframework.nativex.type.TypeName typeName = org.springframework.nativex.type.TypeName.fromClassName("java.lang.String");
		assertThat(typeName.toSimpleName()).isEqualTo("String");
	}

	@Test
	void shouldExtractPackage() {
		org.springframework.nativex.type.TypeName typeName = TypeName.fromClassName("java.lang.String");
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
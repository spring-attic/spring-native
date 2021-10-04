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

package org.springframework.aot.context.bootstrap.generator.infrastructure;

import java.io.IOException;
import java.io.StringWriter;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BootstrapClass}.
 *
 * @author Stephane Nicoll
 */
class BootstrapClassTests {

	private static final ClassName TEST_CLASS_NAME = ClassName.get("com.acme", "Test");

	@Test
	void className() {
		BootstrapClass bootstrapClass = new BootstrapClass(TEST_CLASS_NAME,
				(type) -> type.addModifiers(Modifier.STATIC));
		assertThat(bootstrapClass.getClassName()).isEqualTo(TEST_CLASS_NAME);
		assertThat(generateCode(bootstrapClass)).contains("static class Test {");
	}

	@Test
	void createWithCustomField() {
		BootstrapClass bootstrapClass = new BootstrapClass(TEST_CLASS_NAME,
				(type) -> type.addField(FieldSpec.builder(TypeName.BOOLEAN, "enabled").build()));
		assertThat(generateCode(bootstrapClass)).contains("boolean enabled;");
	}

	@Test
	void customizeType() {
		BootstrapClass bootstrapClass = createTestBootstrapClass();
		bootstrapClass.customizeType((type) -> type.addJavadoc("Test javadoc."))
				.customizeType((type) -> type.addJavadoc(" Another test javadoc"));
		assertThat(generateCode(bootstrapClass)).containsSequence(
				"/**\n",
				" * Test javadoc. Another test javadoc\n",
				" */");
	}

	@Test
	void addMethod() {
		BootstrapClass bootstrapClass = createTestBootstrapClass();
		bootstrapClass.addMethod(MethodSpec.methodBuilder("test").returns(Integer.class)
				.addCode(CodeBlock.of("return 42;")).build());
		assertThat(generateCode(bootstrapClass)).containsSequence(
				"  Integer test() {\n",
				"    return 42;\n",
				"  }");
	}

	@Test
	void addMultipleMethods() {
		BootstrapClass bootstrapClass = createTestBootstrapClass();
		bootstrapClass.addMethod(MethodSpec.methodBuilder("first").build());
		bootstrapClass.addMethod(MethodSpec.methodBuilder("second").build());
		assertThat(generateCode(bootstrapClass))
				.containsSequence("  void first() {\n", "  }")
				.containsSequence("  void second() {\n", "  }");
	}

	private BootstrapClass createTestBootstrapClass() {
		return BootstrapClass.of(TEST_CLASS_NAME);
	}

	private String generateCode(BootstrapClass bootstrapClass) {
		try {
			StringWriter out = new StringWriter();
			bootstrapClass.toJavaFile().writeTo(out);
			return out.toString();
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}

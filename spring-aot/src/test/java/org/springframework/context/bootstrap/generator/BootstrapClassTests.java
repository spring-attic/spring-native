package org.springframework.context.bootstrap.generator;

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
		BootstrapClass bootstrapClass = BootstrapClass.of(TEST_CLASS_NAME,
				(type) -> type.addModifiers(Modifier.STATIC));
		assertThat(bootstrapClass.getClassName()).isEqualTo(TEST_CLASS_NAME);
		assertThat(generateCode(bootstrapClass)).contains("static class Test {");
	}

	@Test
	void createWithCustomField() {
		BootstrapClass bootstrapClass = BootstrapClass.of(TEST_CLASS_NAME,
				(type) -> type.addField(FieldSpec.builder(TypeName.BOOLEAN, "enabled").build()));
		assertThat(generateCode(bootstrapClass)).contains("boolean enabled;");
	}

	@Test
	void addMethod() {
		BootstrapClass bootstrapClass = BootstrapClass.of(TEST_CLASS_NAME);
		bootstrapClass.addMethod(MethodSpec.methodBuilder("test").returns(Integer.class).addCode(CodeBlock.of("return 42;")).build());
		assertThat(generateCode(bootstrapClass)).containsSequence(
				"  Integer test() {\n",
				"    return 42;\n",
				"  }");
	}

	@Test
	void addMultipleMethods() {
		BootstrapClass bootstrapClass = BootstrapClass.of(TEST_CLASS_NAME);
		bootstrapClass.addMethod(MethodSpec.methodBuilder("first").build());
		bootstrapClass.addMethod(MethodSpec.methodBuilder("second").build());
		assertThat(generateCode(bootstrapClass))
				.containsSequence("  void first() {\n", "  }")
				.containsSequence("  void second() {\n", "  }");
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

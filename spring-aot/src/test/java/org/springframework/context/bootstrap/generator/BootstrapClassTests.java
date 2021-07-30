package org.springframework.context.bootstrap.generator;

import java.io.IOException;
import java.io.StringWriter;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
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

	@Test
	void className() {
		BootstrapClass bootstrapClass = new BootstrapClass("com.acme", "Test",
				(type) -> type.addModifiers(Modifier.STATIC));
		assertThat(bootstrapClass.getClassName()).isEqualTo(ClassName.get("com.acme", "Test"));
	}

	@Test
	void createWithTypeCustomization() throws IOException {
		BootstrapClass bootstrapClass = new BootstrapClass("com.acme", "Test",
				(type) -> type.addField(FieldSpec.builder(TypeName.BOOLEAN, "enabled").build()));
		assertThat(generateCode(bootstrapClass)).contains("boolean enabled;");
	}

	@Test
	void addMethod() {
		BootstrapClass bootstrapClass = new BootstrapClass("com.acme", "Test");
		bootstrapClass.addMethod(MethodSpec.methodBuilder("test").build());
		assertThat(bootstrapClass.hasMethod("test")).isTrue();
	}

	@Test
	void addMethodWithCodeCallback() throws IOException {
		BootstrapClass bootstrapClass = new BootstrapClass("com.acme", "Test");
		bootstrapClass.addMethod(MethodSpec.methodBuilder("test"), (code) -> code.addStatement("// test"));
		assertThat(generateCode(bootstrapClass)).contains("// test");
	}

	private String generateCode(BootstrapClass bootstrapClass) throws IOException {
		StringWriter out = new StringWriter();
		bootstrapClass.build().writeTo(out);
		return out.toString();
	}

}

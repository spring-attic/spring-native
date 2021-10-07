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

package org.springframework.aot.context.bootstrap.generator;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BootstrapWriterContext}.
 *
 * @author Stephane Nicoll
 */
class BootstrapWriterContextTests {

	@Test
	void createDefaultLinkPackageName() {
		BootstrapWriterContext writerContext = createComAcmeWriterContext();
		assertThat(writerContext.getPackageName()).isEqualTo("com.acme");
		assertThat(writerContext.getMainBootstrapClass().getClassName()).isEqualTo(ClassName.get("com.acme", "Test"));
		assertThat(writerContext.getBootstrapClass("com.acme")).isSameAs(writerContext.getMainBootstrapClass());
	}

	@Test
	void createDefaultHasMainClassAnApplicationListener() {
		BootstrapWriterContext context = createComAcmeWriterContext();
		JavaFile javaFile = context.getMainBootstrapClass().toJavaFile();
		assertThat(javaFile.typeSpec.modifiers).containsOnly(Modifier.PUBLIC);
		assertThat(javaFile.typeSpec.superinterfaces).containsOnly(
				ParameterizedTypeName.get(ApplicationContextInitializer.class, GenericApplicationContext.class));
	}

	@Test
	void createDefaultHasProtectedClassPublicFinal() {
		BootstrapWriterContext context = createComAcmeWriterContext();
		JavaFile javaFile = context.getBootstrapClass("com.example.another").toJavaFile();
		assertThat(javaFile.typeSpec.modifiers).containsOnly(Modifier.PUBLIC, Modifier.FINAL);
		assertThat(javaFile.typeSpec.superinterfaces).isEmpty();
	}

	@Test
	void getBootstrapClassRegisterInstance() {
		BootstrapWriterContext writerContext = createComAcmeWriterContext();
		assertThat(writerContext.hasBootstrapClass("com.example")).isFalse();
		BootstrapClass bootstrapClass = writerContext.getBootstrapClass("com.example");
		assertThat(bootstrapClass).isNotNull();
		assertThat(bootstrapClass.getClassName().simpleName()).isEqualTo("Test");
		assertThat(writerContext.hasBootstrapClass("com.example")).isTrue();
	}

	@Test
	void getBootstrapClassReuseInstance() {
		BootstrapWriterContext writerContext = createComAcmeWriterContext();
		BootstrapClass bootstrapClass = writerContext.getBootstrapClass("com.example");
		assertThat(bootstrapClass.getClassName().packageName()).isEqualTo("com.example");
		assertThat(writerContext.getBootstrapClass("com.example")).isSameAs(bootstrapClass);
	}

	@Test
	void getRuntimeReflectionRegistry() {
		BootstrapWriterContext writerContext = createComAcmeWriterContext();
		assertThat(writerContext.getNativeConfigurationRegistry()).isNotNull();
		assertThat(writerContext.getNativeConfigurationRegistry().reflection().getEntries()).isEmpty();
	}

	@Test
	void toJavaFilesWithDefaultClass() {
		BootstrapWriterContext writerContext = createComAcmeWriterContext();
		assertThat(writerContext.toJavaFiles()).hasSize(1);
	}

	@Test
	void toJavaFilesWithDefaultClassAndAdditionalClasses() {
		BootstrapWriterContext writerContext = createComAcmeWriterContext();
		writerContext.getBootstrapClass("com.example");
		writerContext.getBootstrapClass("com.another");
		assertThat(writerContext.toJavaFiles()).hasSize(3);
	}

	private BootstrapWriterContext createComAcmeWriterContext() {
		return new BootstrapWriterContext("com.acme", "Test");
	}

}

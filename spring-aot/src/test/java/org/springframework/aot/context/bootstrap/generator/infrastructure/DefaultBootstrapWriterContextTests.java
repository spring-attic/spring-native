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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link DefaultBootstrapWriterContext}.
 *
 * @author Stephane Nicoll
 */
class DefaultBootstrapWriterContextTests {

	@Test
	void createDefaultLinkPackageName() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		assertThat(writerContext.getMainBootstrapClass().getClassName()).isEqualTo(ClassName.get("com.acme", "Test"));
		assertThat(writerContext.getBootstrapClass("com.acme")).isSameAs(writerContext.getMainBootstrapClass());
	}

	@Test
	void createDefaultHasMainClassAnApplicationListener() {
		DefaultBootstrapWriterContext context = createComAcmeWriterContext();
		JavaFile javaFile = context.getMainBootstrapClass().toJavaFile();
		assertThat(javaFile.typeSpec.modifiers).containsOnly(Modifier.PUBLIC);
		assertThat(javaFile.typeSpec.superinterfaces).containsOnly(
				ParameterizedTypeName.get(ApplicationContextInitializer.class, GenericApplicationContext.class));
	}

	@Test
	void createDefaultHasProtectedClassPublicFinal() {
		DefaultBootstrapWriterContext context = createComAcmeWriterContext();
		JavaFile javaFile = context.getBootstrapClass("com.example.another").toJavaFile();
		assertThat(javaFile.typeSpec.modifiers).containsOnly(Modifier.PUBLIC, Modifier.FINAL);
		assertThat(javaFile.typeSpec.superinterfaces).isEmpty();
	}

	@Test
	void getBootstrapClassRegisterInstance() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		assertThat(writerContext.hasBootstrapClass("com.example")).isFalse();
		BootstrapClass bootstrapClass = writerContext.getBootstrapClass("com.example");
		assertThat(bootstrapClass).isNotNull();
		assertThat(bootstrapClass.getClassName().simpleName()).isEqualTo("Test");
		assertThat(writerContext.hasBootstrapClass("com.example")).isTrue();
	}

	@Test
	void getBootstrapClassReuseInstance() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		BootstrapClass bootstrapClass = writerContext.getBootstrapClass("com.example");
		assertThat(bootstrapClass.getClassName().packageName()).isEqualTo("com.example");
		assertThat(writerContext.getBootstrapClass("com.example")).isSameAs(bootstrapClass);
	}

	@Test
	void getRuntimeReflectionRegistry() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		assertThat(writerContext.getNativeConfigurationRegistry()).isNotNull();
		assertThat(writerContext.getNativeConfigurationRegistry().reflection().getEntries()).isEmpty();
	}

	@Test
	void getProtectedAnalyzer() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		ProtectedAccessAnalyzer protectedAccessAnalyzer = writerContext.getProtectedAccessAnalyzer();
		assertThat(protectedAccessAnalyzer).isNotNull();
		assertThat(protectedAccessAnalyzer.analyze(BeanInstanceDescriptor.of(
				DefaultBootstrapWriterContextTests.class).build()).isAccessible()).isFalse();
	}

	@Test
	void toJavaFilesWithDefaultClass() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		assertThat(writerContext.toJavaFiles()).hasSize(0);
	}

	@Test
	void toJavaFilesWithDefaultClassIsAddedLazily() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		writerContext.getMainBootstrapClass();
		assertThat(writerContext.toJavaFiles()).hasSize(1);
	}

	@Test
	void toJavaFilesWithDefaultClassAndAdditionalClasses() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		writerContext.getBootstrapClass("com.example");
		writerContext.getBootstrapClass("com.another");
		assertThat(writerContext.toJavaFiles()).hasSize(2);
	}

	@Test
	void forkWithClassName() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		BootstrapWriterContext context = writerContext.fork("Test");
		assertThat(context).isNotNull();
		assertThat(context.getMainBootstrapClass().getClassName())
				.isEqualTo(ClassName.get("com.acme", "Test"));
	}

	@Test
	void forkWithClassNameThatIsAlreadyRegistered() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		writerContext.fork("Test");
		assertThatIllegalArgumentException().isThrownBy(() -> writerContext.fork("Test"))
				.withMessageContaining("'Test'");
	}

	@Test
	void forkWithClassNameApplyDefaultFactoryForMainBootstrapClass() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		BootstrapWriterContext context = writerContext.fork("Test");
		assertThat(context).isNotNull();
		JavaFile javaFile = context.getMainBootstrapClass().toJavaFile();
		assertThat(javaFile.typeSpec.modifiers).containsOnly(Modifier.PUBLIC);
		assertThat(javaFile.typeSpec.superinterfaces).containsOnly(
				ParameterizedTypeName.get(ApplicationContextInitializer.class, GenericApplicationContext.class));
	}

	@Test
	void forkWithClassNameApplyDefaultFactoryForBootstrapClass() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		BootstrapWriterContext context = writerContext.fork("Test");
		assertThat(context).isNotNull();
		JavaFile javaFile = context.getBootstrapClass("com.example.another").toJavaFile();
		assertThat(javaFile.typeSpec.modifiers).containsOnly(Modifier.PUBLIC, Modifier.FINAL);
		assertThat(javaFile.typeSpec.superinterfaces).isEmpty();
	}

	@Test
	void forkWithBootstrapClassFactoryForMainBootstrapClass() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		Function<String, BootstrapClass> factory = mockBootstrapClassFactory();
		BootstrapClass mainBootstrapClass = BootstrapClass.of(ClassName.get("com.acme", "Main"));
		given(factory.apply("com.acme")).willReturn(mainBootstrapClass);
		BootstrapWriterContext context = writerContext.fork("test", factory);
		assertThat(context).isNotNull();
		assertThat(context.getMainBootstrapClass()).isSameAs(mainBootstrapClass);
		verify(factory).apply("com.acme");
	}

	@Test
	void forkWithBootstrapClassFactoryForBootstrapClass() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		Function<String, BootstrapClass> factory = mockBootstrapClassFactory();
		BootstrapClass bootstrapClass = BootstrapClass.of(ClassName.get("com.example.another", "Main"));
		given(factory.apply("com.example.another")).willReturn(bootstrapClass);
		BootstrapWriterContext context = writerContext.fork("test", factory);
		assertThat(context).isNotNull();
		assertThat(context.getBootstrapClass("com.example.another")).isSameAs(bootstrapClass);
		verify(factory).apply("com.example.another");
	}

	@Test
	void compositeKeepsTrackOfJavaFiles() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		BootstrapWriterContext test1 = writerContext.fork("Test1");
		test1.getMainBootstrapClass();
		test1.getBootstrapClass("com.acme.test1");
		BootstrapWriterContext test2 = writerContext.fork("Test2");
		test2.getBootstrapClass("com.acme.test2");
		List<JavaFile> javaFiles = writerContext.toJavaFiles();
		assertThat(javaFiles.stream().map((javaFile) -> javaFile.packageName + "." + javaFile.typeSpec.name)).containsOnly(
				"com.acme.Test1", "com.acme.test1.Test1", "com.acme.test2.Test2");
	}

	@Test
	void compositeKeepsTrackOfNativeConfiguration() {
		DefaultBootstrapWriterContext writerContext = createComAcmeWriterContext();
		writerContext.fork("Test1").getNativeConfigurationRegistry()
				.reflection().forType(String.class);
		writerContext.fork("Test2").getNativeConfigurationRegistry()
				.reflection().forType(Integer.class);
		List<Class<?>> reflectionTypes = writerContext.getNativeConfigurationRegistry().reflection().getEntries().stream()
				.map(NativeReflectionEntry::getType).collect(Collectors.toList());
		assertThat(reflectionTypes).containsOnly(String.class, Integer.class);
	}

	@SuppressWarnings("unchecked")
	private Function<String, BootstrapClass> mockBootstrapClassFactory() {
		return mock(Function.class);
	}

	private DefaultBootstrapWriterContext createComAcmeWriterContext() {
		return new DefaultBootstrapWriterContext("com.acme", "Test");
	}

}

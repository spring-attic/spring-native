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

package org.springframework.boot.actuate.autoconfigure.web;

import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Predicate;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.DefaultBootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.aot.context.bootstrap.generator.test.TextAssert;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.web.embedded.tomcat.TomcatReactiveWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ManagementContextBeanRegistrationWriter}.
 *
 * @author Stephane Nicoll
 */
class ManagementContextBeanRegistrationWriterTests {

	@Test
	void writeBeanRegistrationForServletManagementContext() {
		ManagementContextBeanRegistrationWriter writer = new ManagementContextBeanRegistrationWriter(
				new GenericApplicationContext(), "servlet", false);
		assertThat(CodeSnippet.of((code) -> writer.writeBeanRegistration(createWriterContext(), code))).lines().contains(
				"BeanDefinitionRegistrar.of(\"servlet\", AotManagementContextFactory.class)",
				"    .instanceSupplier(() -> new AotManagementContextFactory(() -> new ManagementContextBoostrapInitializer(), false)).register(beanFactory);");
	}

	@Test
	void writeBeanRegistrationForServletManagementContextWithResolvableWebServerFactory() {
		DefaultBootstrapWriterContext writerContext = createWriterContext();
		GenericApplicationContext parent = new GenericApplicationContext();
		parent.registerBeanDefinition("serverFactory", new RootBeanDefinition(TomcatServletWebServerFactory.class));
		ManagementContextBeanRegistrationWriter writer = new ManagementContextBeanRegistrationWriter(parent, "test", false);
		assertBeanRegistrationForServletManagementContext(writer, writerContext);
	}

	@Test
	void writeBeanRegistrationForServletManagementContextWithRawWebServerFactory() {
		DefaultBootstrapWriterContext writerContext = createWriterContext();
		GenericApplicationContext parent = new GenericApplicationContext();
		parent.registerBeanDefinition("serverFactory", new RootBeanDefinition(TomcatServletWebServerFactory.class.getName()));
		ManagementContextBeanRegistrationWriter writer = new ManagementContextBeanRegistrationWriter(parent, "test", false);
		assertBeanRegistrationForServletManagementContext(writer, writerContext);
	}

	private void assertBeanRegistrationForServletManagementContext(ManagementContextBeanRegistrationWriter writer,
			DefaultBootstrapWriterContext writerContext) {
		writer.writeBeanRegistration(writerContext, CodeBlock.builder());
		JavaFile source = findSource(writerContext, matching("com.example.ManagementContextBoostrapInitializer"));
		assertThat(source).isNotNull();
		assertSource(source).doesNotContain("ReactiveWebServerFactory").removeIndent(2).lines().contains(
				"BeanDefinitionRegistrar.of(\"ServletWebServerFactory\", TomcatServletWebServerFactory.class)",
				"    .instanceSupplier(() -> new TomcatServletWebServerFactory()).register(beanFactory);");
	}

	@Test
	void writeBeanRegistrationForReactiveManagementContext() {
		ManagementContextBeanRegistrationWriter writer = new ManagementContextBeanRegistrationWriter(
				new GenericApplicationContext(), "reactive", true);
		assertThat(CodeSnippet.of((code) -> writer.writeBeanRegistration(createWriterContext(), code))).lines().contains(
				"BeanDefinitionRegistrar.of(\"reactive\", AotManagementContextFactory.class)",
				"    .instanceSupplier(() -> new AotManagementContextFactory(() -> new ManagementContextBoostrapInitializer(), true)).register(beanFactory);");
	}

	@Test
	void writeBeanRegistrationForReactiveManagementContextWriteManagementContext() {
		DefaultBootstrapWriterContext writerContext = createWriterContext();
		GenericApplicationContext parent = new GenericApplicationContext();
		parent.registerBeanDefinition("serverFactory", new RootBeanDefinition(TomcatReactiveWebServerFactory.class));
		ManagementContextBeanRegistrationWriter writer = new ManagementContextBeanRegistrationWriter(parent, "test", true);
		writer.writeBeanRegistration(writerContext, CodeBlock.builder());
		JavaFile source = findSource(writerContext, matching("com.example.ManagementContextBoostrapInitializer"));
		assertThat(source).isNotNull();
		assertSource(source).doesNotContain("ServletWebServerFactory").removeIndent(2).lines().contains(
				"BeanDefinitionRegistrar.of(\"ReactiveWebServerFactory\", TomcatReactiveWebServerFactory.class)",
				"    .instanceSupplier(() -> new TomcatReactiveWebServerFactory()).register(beanFactory);");
	}

	private DefaultBootstrapWriterContext createWriterContext() {
		return new DefaultBootstrapWriterContext("com.example", "Test");
	}

	private JavaFile findSource(DefaultBootstrapWriterContext writerContext, Predicate<JavaFile> predicate) {
		return writerContext.toJavaFiles().stream().filter(predicate).findFirst().orElse(null);
	}

	private Predicate<JavaFile> matching(String className) {
		return (candidate) -> className.equals(candidate.packageName + "." + candidate.typeSpec.name);
	}

	private TextAssert assertSource(JavaFile javaFile) {
		try {
			StringWriter writer = new StringWriter();
			javaFile.writeTo(writer);
			return new TextAssert(writer.toString());
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}

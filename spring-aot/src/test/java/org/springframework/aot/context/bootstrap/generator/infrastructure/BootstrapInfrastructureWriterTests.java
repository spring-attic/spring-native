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

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.annotation.ImportAwareBeanPostProcessor;
import org.springframework.aot.context.annotation.InitDestroyBeanPostProcessor;
import org.springframework.aot.context.bootstrap.generator.sample.callback.AsyncConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.callback.ImportConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.callback.InitDestroySampleBean;
import org.springframework.aot.context.bootstrap.generator.sample.callback.NestedImportConfiguration;
import org.springframework.aot.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.BuildTimeBeanDefinitionsRegistrar;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.annotation.samples.simple.ConfigurationTwo;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BootstrapInfrastructureWriter}.
 *
 * @author Stephane Nicoll
 */
class BootstrapInfrastructureWriterTests {

	private final BuildTimeBeanDefinitionsRegistrar registrar = new BuildTimeBeanDefinitionsRegistrar();

	@Test
	void writeInfrastructureAssignBeanFactory() {
		assertThat(writeInfrastructure(createBootstrapContext()))
				.contains("DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();")
				.hasImport(DefaultListableBeanFactory.class);
	}

	@Test
	void writeInfrastructureSetAutowireCandidateResolver() {
		assertThat(writeInfrastructure(createBootstrapContext()))
				.contains("beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());")
				.hasImport(ContextAnnotationAutowireCandidateResolver.class);
	}

	@Test
	void writeInfrastructureWithNoImportAwareCandidateDoesNotRegisterBean() {
		assertThat(writeInfrastructure(createBootstrapContext()))
				.doesNotContain(ImportAwareBeanPostProcessor.class.getSimpleName());
	}

	@Test
	void writeInfrastructureWithNoImportAwareMappingDoesNotRegisterBean() {
		GenericApplicationContext context = new AnnotationConfigApplicationContext();
		context.registerBean(ConfigurationTwo.class);
		assertThat(writeInfrastructure(createBootstrapContext(), context))
				.doesNotContain(ImportAwareBeanPostProcessor.class.getSimpleName());
	}

	@Test
	void writeInfrastructureWithImportAwareRegisterBean() {
		GenericApplicationContext context = new AnnotationConfigApplicationContext();
		context.registerBean(ImportConfiguration.class);
		assertThat(writeInfrastructure(createBootstrapContext(), context))
				.contains("beanFactory.addBeanPostProcessor(createImportAwareBeanPostProcessor());");
	}

	@Test
	void writeInfrastructureWithImportAwareRegisterCreateMethod() {
		GenericApplicationContext context = new AnnotationConfigApplicationContext();
		context.registerBean(ImportConfiguration.class);
		BootstrapWriterContext bootstrapContext = createBootstrapContext();
		writeInfrastructure(bootstrapContext, context);
		assertThat(generateCode(bootstrapContext.getBootstrapClass("com.example")).lines()).contains(
				"  private ImportAwareBeanPostProcessor createImportAwareBeanPostProcessor() {",
				"    Map<String, String> mappings = new LinkedHashMap<>();",
				"    mappings.put(\"org.springframework.aot.context.bootstrap.generator.sample.callback.ImportAwareConfiguration\", \"org.springframework.aot.context.bootstrap.generator.sample.callback.ImportConfiguration\");",
				"    return new ImportAwareBeanPostProcessor(mappings);",
				"  }").contains("import " + ImportAwareBeanPostProcessor.class.getName() + ";");
		assertThat(bootstrapContext.getNativeConfigurationRegistry().resources().toResourcesDescriptor().getPatterns()).containsOnly(
				"org/springframework/aot/context/bootstrap/generator/sample/callback/ImportConfiguration.class");
	}

	@Test
	void writeInfrastructureWithSeveralImportAwareInstances() {
		GenericApplicationContext context = new AnnotationConfigApplicationContext();
		context.registerBean(ImportConfiguration.class);
		context.registerBean(AsyncConfiguration.class);
		BootstrapWriterContext bootstrapContext = createBootstrapContext();
		writeInfrastructure(bootstrapContext, context);
		assertThat(generateCode(bootstrapContext.getBootstrapClass("com.example")).lines()).contains(
				"  private ImportAwareBeanPostProcessor createImportAwareBeanPostProcessor() {",
				"    Map<String, String> mappings = new LinkedHashMap<>();",
				"    mappings.put(\"org.springframework.aot.context.bootstrap.generator.sample.callback.ImportAwareConfiguration\", \"org.springframework.aot.context.bootstrap.generator.sample.callback.ImportConfiguration\");",
				"    mappings.put(\"org.springframework.scheduling.annotation.ProxyAsyncConfiguration\", \"org.springframework.aot.context.bootstrap.generator.sample.callback.AsyncConfiguration\");",
				"    return new ImportAwareBeanPostProcessor(mappings);",
				"  }");
		assertThat(bootstrapContext.getNativeConfigurationRegistry().resources().toResourcesDescriptor().getPatterns()).containsOnly(
				"org/springframework/aot/context/bootstrap/generator/sample/callback/ImportConfiguration.class",
				"org/springframework/aot/context/bootstrap/generator/sample/callback/AsyncConfiguration.class");
	}

	@Test
	void writeInfrastructureWithNestedClass() {
		GenericApplicationContext context = new AnnotationConfigApplicationContext();
		context.registerBean(NestedImportConfiguration.class);
		BootstrapWriterContext bootstrapContext = createBootstrapContext();
		writeInfrastructure(bootstrapContext, context);
		assertThat(generateCode(bootstrapContext.getBootstrapClass("com.example")).lines()).contains(
				"  private ImportAwareBeanPostProcessor createImportAwareBeanPostProcessor() {",
				"    Map<String, String> mappings = new LinkedHashMap<>();",
				"    mappings.put(\"org.springframework.aot.context.bootstrap.generator.sample.callback.ImportAwareConfiguration\", \"org.springframework.aot.context.bootstrap.generator.sample.callback.NestedImportConfiguration$Nested\");",
				"    return new ImportAwareBeanPostProcessor(mappings);",
				"  }");
		assertThat(bootstrapContext.getNativeConfigurationRegistry().resources().toResourcesDescriptor().getPatterns()).containsOnly(
				"org/springframework/aot/context/bootstrap/generator/sample/callback/NestedImportConfiguration\\$Nested.class");
	}

	@Test
	void writeInfrastructureWithNoLifecycleMethodsDoesNotRegisterBean() {
		assertThat(writeInfrastructure(createBootstrapContext()))
				.doesNotContain(InitDestroyBeanPostProcessor.class.getSimpleName())
				.doesNotContain("createInitDestroyBeanPostProcessor");
	}

	@Test
	void writeInfrastructureWithLifecycleMethodsRegisterCreateMethod() {
		GenericApplicationContext context = new AnnotationConfigApplicationContext();
		context.registerBean("testBean", InitDestroySampleBean.class);
		BootstrapWriterContext bootstrapContext = createBootstrapContext();
		writeInfrastructure(bootstrapContext, context);
		assertThat(generateCode(bootstrapContext.getBootstrapClass("com.example")).lines()).contains(
				"  private InitDestroyBeanPostProcessor createInitDestroyBeanPostProcessor(",
				"      ConfigurableBeanFactory beanFactory) {",
				"    Map<String, List<String>> initMethods = new LinkedHashMap<>();",
				"    initMethods.put(\"testBean\", List.of(\"start\"));",
				"    Map<String, List<String>> destroyMethods = new LinkedHashMap<>();",
				"    destroyMethods.put(\"testBean\", List.of(\"stop\"));",
				"    return new InitDestroyBeanPostProcessor(beanFactory, initMethods, destroyMethods);",
				"  }").contains("import " + InitDestroyBeanPostProcessor.class.getName() + ";");
	}

	@Test
	void writeInfrastructureWithLifecycleMethodsRegisterBean() {
		GenericApplicationContext context = new AnnotationConfigApplicationContext();
		context.registerBean("testBean", InitDestroySampleBean.class);
		assertThat(writeInfrastructure(createBootstrapContext(), context)).contains(
				"beanFactory.addBeanPostProcessor(createInitDestroyBeanPostProcessor(beanFactory));");
	}

	private CodeSnippet writeInfrastructure(BootstrapWriterContext writerContext) {
		return writeInfrastructure(writerContext, new AnnotationConfigApplicationContext());
	}

	private CodeSnippet writeInfrastructure(BootstrapWriterContext writerContext, GenericApplicationContext context) {
		ConfigurableListableBeanFactory beanFactory = this.registrar.processBeanDefinitions(context);
		BootstrapInfrastructureWriter writer = new BootstrapInfrastructureWriter(beanFactory, writerContext);
		return CodeSnippet.of(writer::writeInfrastructure);
	}

	private static BootstrapWriterContext createBootstrapContext() {
		return new DefaultBootstrapWriterContext("com.example", "Test");
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

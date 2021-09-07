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

package org.springframework.context.bootstrap.generator;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.squareup.javapoet.ClassName;
import org.junit.jupiter.api.Test;

import org.springframework.aot.context.annotation.ImportAwareInvoker;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.BuildTimeBeanDefinitionsRegistrar;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.bootstrap.generator.BootstrapInfrastructureWriter.ImportAwareInfrastructureBuilder;
import org.springframework.context.bootstrap.generator.reflect.RuntimeReflectionRegistry;
import org.springframework.context.bootstrap.generator.sample.callback.AsyncConfiguration;
import org.springframework.context.bootstrap.generator.sample.callback.ImportAwareConfiguration;
import org.springframework.context.bootstrap.generator.sample.callback.ImportConfiguration;
import org.springframework.context.bootstrap.generator.sample.callback.NestedImportConfiguration;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.context.origin.BeanDefinitionDescriptor;
import org.springframework.context.origin.BeanDefinitionDescriptor.Type;
import org.springframework.context.origin.BeanFactoryStructure;
import org.springframework.context.origin.BeanFactoryStructureAnalyzer;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.entry;

/**
 * Tests for {@link BootstrapInfrastructureWriter}.
 *
 * @author Stephane Nicoll
 */
class BootstrapInfrastructureWriterTests {

	private final BuildTimeBeanDefinitionsRegistrar registrar = new BuildTimeBeanDefinitionsRegistrar();

	@Test
	void writeInfrastructureSetAutowireCandidateResolver() {
		assertThat(writeInfrastructure(new GenericApplicationContext(), createBootstrapContext()))
				.contains("context.getDefaultListableBeanFactory().setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());")
				.hasImport(ContextAnnotationAutowireCandidateResolver.class);
	}

	@Test
	void writeInfrastructureWithNoImportAwareCandidateDoesNotRegisterBean() {
		assertThat(writeInfrastructure(new GenericApplicationContext(), createBootstrapContext()))
				.doesNotContain(ImportAwareInvoker.class.getSimpleName());
	}

	@Test
	void writeInfrastructureWithImportAwareRegisterBean() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ImportConfiguration.class);
		assertThat(writeInfrastructure(context, createBootstrapContext()))
				.contains("ImportAwareInvoker.register(context, this::createImportAwareInvoker);")
				.hasImport(ImportAwareInvoker.class);
	}

	@Test
	void writeInfrastructureWithImportAwareRegisterCreateMethod() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ImportConfiguration.class);
		BootstrapWriterContext bootstrapContext = createBootstrapContext();
		writeInfrastructure(context, bootstrapContext);
		assertThat(generateCode(bootstrapContext.getBootstrapClass("com.example")).lines()).contains(
				"  private ImportAwareInvoker createImportAwareInvoker() {",
				"    Map<String, String> mappings = new LinkedHashMap<>();",
				"    mappings.put(\"org.springframework.context.bootstrap.generator.sample.callback.ImportAwareConfiguration\", \"org.springframework.context.bootstrap.generator.sample.callback.ImportConfiguration\");",
				"    return new ImportAwareInvoker(mappings);",
				"  }");
	}

	@Test
	void writeInfrastructureWithSeveralImportAwareInstances() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ImportConfiguration.class);
		context.registerBean(AsyncConfiguration.class);
		BootstrapWriterContext bootstrapContext = createBootstrapContext();
		writeInfrastructure(context, bootstrapContext);
		assertThat(generateCode(bootstrapContext.getBootstrapClass("com.example")).lines()).contains(
				"  private ImportAwareInvoker createImportAwareInvoker() {",
				"    Map<String, String> mappings = new LinkedHashMap<>();",
				"    mappings.put(\"org.springframework.context.bootstrap.generator.sample.callback.ImportAwareConfiguration\", \"org.springframework.context.bootstrap.generator.sample.callback.ImportConfiguration\");",
				"    mappings.put(\"org.springframework.scheduling.annotation.ProxyAsyncConfiguration\", \"org.springframework.context.bootstrap.generator.sample.callback.AsyncConfiguration\");",
				"    return new ImportAwareInvoker(mappings);",
				"  }");
	}

	@Test
	void buildImportAwareLinksWithInnerClassLinkToActualClass() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(NestedImportConfiguration.class);
		Map<String, Class<?>> importAwareLinks = createImportAwareInfrastructureBuilder(context)
				.buildImportAwareLinks(new RuntimeReflectionRegistry());
		assertThat(importAwareLinks).containsOnly(entry(
				ImportAwareConfiguration.class.getName(), NestedImportConfiguration.Nested.class));
	}

	@Test
	void writeInfrastructureWithNestedClass() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(NestedImportConfiguration.class);
		BootstrapWriterContext bootstrapContext = createBootstrapContext();
		writeInfrastructure(context, bootstrapContext);
		assertThat(generateCode(bootstrapContext.getBootstrapClass("com.example")).lines()).contains(
				"  private ImportAwareInvoker createImportAwareInvoker() {",
				"    Map<String, String> mappings = new LinkedHashMap<>();",
				"    mappings.put(\"org.springframework.context.bootstrap.generator.sample.callback.ImportAwareConfiguration\", \"org.springframework.context.bootstrap.generator.sample.callback.NestedImportConfiguration$Nested\");",
				"    return new ImportAwareInvoker(mappings);",
				"  }");
	}

	@Test
	void buildImportAwareLinksWithInnerClassRegisterClassResource() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(NestedImportConfiguration.class);
		RuntimeReflectionRegistry registry = new RuntimeReflectionRegistry();
		createImportAwareInfrastructureBuilder(context).buildImportAwareLinks(registry);
		assertThat(registry.getResourcesDescriptor().getPatterns()).singleElement().isEqualTo(
				"org/springframework/context/bootstrap/generator/sample/callback/NestedImportConfiguration\\$Nested.class");
	}

	@Test
	void buildImportAwareWithResolvableTypeUseIt() {
		ImportAwareInfrastructureBuilder builder = createForImportingCandidate(BeanDefinitionBuilder
				.rootBeanDefinition(ImportConfiguration.class).getBeanDefinition());
		assertThat(builder.buildImportAwareLinks(new RuntimeReflectionRegistry())).containsOnly(entry(
				ImportAwareConfiguration.class.getName(), ImportConfiguration.class));
	}

	@Test
	void buildImportAwareLinksWithNoImportingBeanDefinitionIgnoreEntry() {
		RootBeanDefinition importingBeanDefinition = new RootBeanDefinition();
		importingBeanDefinition.setBeanClass(ImportConfiguration.class);
		ImportAwareInfrastructureBuilder builder = createForImportingCandidate(null);
		assertThat(builder.buildImportAwareLinks(new RuntimeReflectionRegistry())).isEmpty();
	}

	@Test
	void buildImportAwareLinksWithNoResolvableTypeUseBeanClass() {
		RootBeanDefinition importingBeanDefinition = new RootBeanDefinition();
		importingBeanDefinition.setBeanClass(ImportConfiguration.class);
		ImportAwareInfrastructureBuilder builder = createForImportingCandidate(importingBeanDefinition);
		assertThat(builder.buildImportAwareLinks(new RuntimeReflectionRegistry())).containsOnly(entry(
				ImportAwareConfiguration.class.getName(), ImportConfiguration.class));
	}

	@Test
	void buildImportAwareLinksWithNoResolvableTypeUseClassName() {
		RootBeanDefinition importingBeanDefinition = new RootBeanDefinition();
		importingBeanDefinition.setBeanClassName(ImportConfiguration.class.getName());
		ImportAwareInfrastructureBuilder builder = createForImportingCandidate(importingBeanDefinition);
		assertThat(builder.buildImportAwareLinks(new RuntimeReflectionRegistry())).containsOnly(entry(
				ImportAwareConfiguration.class.getName(), ImportConfiguration.class));
	}

	@Test
	void buildImportAwareLinksWithNoTypeIsIgnored() {
		RootBeanDefinition importingBeanDefinition = new RootBeanDefinition();
		ImportAwareInfrastructureBuilder builder = createForImportingCandidate(importingBeanDefinition);
		assertThat(builder.buildImportAwareLinks(new RuntimeReflectionRegistry())).isEmpty();
	}

	@Test
	void buildImportAwareLinksWithInvalidClassNameThrowsException() {
		RootBeanDefinition importingBeanDefinition = new RootBeanDefinition();
		importingBeanDefinition.setBeanClassName("does-not-exist");
		ImportAwareInfrastructureBuilder builder = createForImportingCandidate(importingBeanDefinition);
		assertThatIllegalStateException().isThrownBy(() -> builder.buildImportAwareLinks(new RuntimeReflectionRegistry()))
				.withMessageContaining("Bean definition refers to invalid class");
	}

	private ImportAwareInfrastructureBuilder createForImportingCandidate(BeanDefinition importingBeanDefinition) {
		Map<String, BeanDefinitionDescriptor> structure = new LinkedHashMap<>();
		if (importingBeanDefinition != null) {
			structure.put("importing", BeanDefinitionDescriptor.unresolved("importing", importingBeanDefinition)
					.resolve(Type.CONFIGURATION, Set.of()));
		}
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(ImportAwareConfiguration.class);
		structure.put("imported", BeanDefinitionDescriptor.unresolved("imported", beanDefinition)
				.resolve(Type.CONFIGURATION, Set.of("importing")));
		BeanFactoryStructure beanFactoryStructure = new BeanFactoryStructure(structure);
		return new ImportAwareInfrastructureBuilder(beanFactoryStructure, getClass().getClassLoader());
	}

	private ImportAwareInfrastructureBuilder createImportAwareInfrastructureBuilder(GenericApplicationContext context) {
		ConfigurableListableBeanFactory beanFactory = this.registrar.processBeanDefinitions(context);
		BeanFactoryStructure structure = new BeanFactoryStructureAnalyzer(context.getClassLoader()).analyze(beanFactory);
		return new ImportAwareInfrastructureBuilder(structure, context.getClassLoader());
	}

	private CodeSnippet writeInfrastructure(GenericApplicationContext context, BootstrapWriterContext writerContext) {
		ConfigurableListableBeanFactory beanFactory = this.registrar.processBeanDefinitions(context);
		BootstrapInfrastructureWriter writer = new BootstrapInfrastructureWriter(beanFactory, writerContext);
		return CodeSnippet.of(writer::writeInfrastructure);
	}

	private static BootstrapWriterContext createBootstrapContext() {
		return new BootstrapWriterContext(BootstrapClass.of(ClassName.get("com.example", "Test")));
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

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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriterSupplier;
import org.springframework.aot.context.bootstrap.generator.bean.DefaultBeanRegistrationWriterSupplier;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.aot.context.bootstrap.generator.infrastructure.DefaultBootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.autoconfigure.AutoConfigurationPackagesConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.factory.TestGenericFactoryBean;
import org.springframework.aot.context.bootstrap.generator.sample.factory.TestGenericFactoryBeanConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.generic.GenericConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.generic.GenericObjectProviderConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.infrastructure.ArgumentValueRegistrarConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.metadata.MetadataConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.visibility.ProtectedConfigurationImport;
import org.springframework.aot.context.bootstrap.generator.sample.visibility.ProtectedConstructorParameterConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.visibility.ProtectedMethodParameterConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.visibility.PublicInnerClassConfigurationImport;
import org.springframework.aot.context.bootstrap.generator.sample.visibility.PublicOuterClassConfiguration;
import org.springframework.aot.context.bootstrap.generator.test.ApplicationContextAotProcessorTester;
import org.springframework.aot.context.bootstrap.generator.test.ContextBootstrapStructure;
import org.springframework.aot.context.bootstrap.generator.test.TextAssert;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.samples.scope.ScopeConfiguration;
import org.springframework.context.annotation.samples.simple.SimpleComponent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link ApplicationContextAotProcessor}.
 *
 * @author Stephane Nicoll
 */
class ApplicationContextAotProcessorTests {

	private ApplicationContextAotProcessorTester tester;

	@BeforeEach
	void setup(@TempDir Path directory) {
		this.tester = new ApplicationContextAotProcessorTester(directory);
	}

	@Test
	void processGeneratesStructure() {
		ContextBootstrapStructure structure = this.tester.process();
		assertThat(structure).contextBootstrapInitializer().lines().containsSubsequence(
				"public class ContextBootstrapInitializer implements ApplicationContextInitializer<GenericApplicationContext> {",
				"  @Override",
				"  public void initialize(GenericApplicationContext context) {",
				"  }",
				"}");
		assertThat(structure).contextBootstrapInitializer().contains("import " + GenericApplicationContext.class.getName() + ";");
	}

	@Test
	void processRegisterInfrastructure() {
		ContextBootstrapStructure structure = this.tester.process();
		assertThat(structure).contextBootstrapInitializer().contains("// infrastructure");
	}

	@Test
	void processRegisterReflectionMetadata() {
		ContextBootstrapStructure structure = this.tester.process(SimpleConfiguration.class);
		assertThat(structure).hasClassDescriptor(SimpleConfiguration.class);
	}

	@Test
	void processWithBeanMethodAndNoParameter() {
		ContextBootstrapStructure structure = this.tester.process(SimpleConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().removeIndent(2).lines().contains(
				"BeanDefinitionRegistrar.of(\"simpleConfiguration\", SimpleConfiguration.class)",
				"    .instanceSupplier(SimpleConfiguration::new).register(beanFactory);",
				"BeanDefinitionRegistrar.of(\"stringBean\", String.class).withFactoryMethod(SimpleConfiguration.class, \"stringBean\")",
				"    .instanceSupplier(() -> beanFactory.getBean(SimpleConfiguration.class).stringBean()).register(beanFactory);",
				"BeanDefinitionRegistrar.of(\"integerBean\", Integer.class).withFactoryMethod(SimpleConfiguration.class, \"integerBean\")",
				"    .instanceSupplier(() -> beanFactory.getBean(SimpleConfiguration.class).integerBean()).register(beanFactory);");
	}

	@Test
	void processWithAutoConfiguration() {
		ContextBootstrapStructure structure = this.tester.process(ProjectInfoAutoConfiguration.class);
		// NOTE: application context runner does not register auto-config as FQNs
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"projectInfoAutoConfiguration\", ProjectInfoAutoConfiguration.class).withConstructor(ProjectInfoProperties.class)",
				".instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new ProjectInfoAutoConfiguration(attributes.get(0)))).register(beanFactory);",
				"BeanDefinitionRegistrar.of(\"spring.info-org.springframework.boot.autoconfigure.info.ProjectInfoProperties\", ProjectInfoProperties.class)",
				".instanceSupplier(ProjectInfoProperties::new).register(beanFactory);");
	}

	@Test
	void processWithAutoConfigurationPackages() {
		ContextBootstrapStructure structure = this.tester.process(AutoConfigurationPackagesConfiguration.class);
		assertThat(structure).contextBootstrapInitializer()
				.contains("ContextBootstrapInitializer.registerAutoConfigurationPackages_BasePackages(beanFactory)");
		assertThat(structure).contextBootstrapInitializer("org.springframework.boot.autoconfigure").contains(
				"BeanDefinitionRegistrar.of(\"org.springframework.boot.autoconfigure.AutoConfigurationPackages\", AutoConfigurationPackages.BasePackages.class)",
				".instanceSupplier(() -> new AutoConfigurationPackages.BasePackages(new String[] { \"org.springframework.aot.context.bootstrap.generator.sample.autoconfigure\" }))"
						+ ".customize((bd) -> bd.setRole(2)).register(beanFactory);");
	}

	@Test
	void processWithConfigurationProperties() {
		ContextBootstrapStructure structure = this.tester.process(ConfigurationPropertiesAutoConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().removeIndent(2).lines().contains(
				"BeanDefinitionRegistrar.of(\"org.springframework.boot.context.properties.EnableConfigurationPropertiesRegistrar.methodValidationExcludeFilter\", MethodValidationExcludeFilter.class)",
				"    .instanceSupplier(() -> MethodValidationExcludeFilter.byAnnotation(ConfigurationProperties.class)).customize((bd) -> bd.setRole(2)).register(beanFactory);");
	}

	@Test
	void processWithPrimaryBean() {
		ContextBootstrapStructure structure = this.tester.process(MetadataConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"primaryBean\", String.class).withFactoryMethod(MetadataConfiguration.class, \"primaryBean\")",
				"    .instanceSupplier(() -> beanFactory.getBean(MetadataConfiguration.class).primaryBean())"
						+ ".customize((bd) -> bd.setPrimary(true)).register(beanFactory);");
	}

	@Test
	void processWithRoleInfrastructureBean() {
		ContextBootstrapStructure structure = this.tester.process(MetadataConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"infrastructureBean\", String.class).withFactoryMethod(MetadataConfiguration.class, \"infrastructureBean\")",
				"    .instanceSupplier(() -> beanFactory.getBean(MetadataConfiguration.class).infrastructureBean()).customize((bd) -> bd.setRole(2)).register(beanFactory);");
	}

	@Test
	void processWithPackageProtectedConfiguration() {
		ContextBootstrapStructure structure = this.tester.process(ProtectedConfigurationImport.class);
		assertThat(structure)
				.contextBootstrapInitializer("org.springframework.aot.context.bootstrap.generator.sample.visibility")
				.removeIndent(1).lines().containsSequence(
						"public static void registerProtectedConfiguration_anotherStringBean(",
						"    DefaultListableBeanFactory beanFactory) {",
						"  BeanDefinitionRegistrar.of(\"anotherStringBean\", String.class).withFactoryMethod(ProtectedConfiguration.class, \"anotherStringBean\")",
						"      .instanceSupplier(() -> beanFactory.getBean(ProtectedConfiguration.class).anotherStringBean()).register(beanFactory);",
						"}");
		assertThat(structure).contextBootstrapInitializer().contains(
				"ContextBootstrapInitializer.registerProtectedConfiguration(beanFactory);",
				"ContextBootstrapInitializer.registerProtectedConfiguration_anotherStringBean(beanFactory);");
	}

	@Test
	void processWithPublicInnerOnPackageProtectedOuterConfiguration() {
		ContextBootstrapStructure structure = this.tester.process(PublicInnerClassConfigurationImport.class);
		assertThat(structure)
				.contextBootstrapInitializer("org.springframework.aot.context.bootstrap.generator.sample.visibility")
				.removeIndent(1).lines().containsSequence(
						"public static void registerPublicInnerClassConfiguration_InnerConfiguration(",
						"    DefaultListableBeanFactory beanFactory) {",
						"  BeanDefinitionRegistrar.of(\"org.springframework.aot.context.bootstrap.generator.sample.visibility.PublicInnerClassConfiguration$InnerConfiguration\", PublicInnerClassConfiguration.InnerConfiguration.class)",
						"      .instanceSupplier(PublicInnerClassConfiguration.InnerConfiguration::new).register(beanFactory);",
						"}");
		assertThat(structure).contextBootstrapInitializer().contains(
				"ContextBootstrapInitializer.registerPublicInnerClassConfiguration(beanFactory);",
				"ContextBootstrapInitializer.registerPublicInnerClassConfiguration_InnerConfiguration(beanFactory);",
				"ContextBootstrapInitializer.registerInnerConfiguration_innerBean(beanFactory);");
	}

	@Test
	void processWithPackageProtectedInnerConfiguration() {
		ContextBootstrapStructure structure = this.tester.process(PublicOuterClassConfiguration.class);
		assertThat(structure)
				.contextBootstrapInitializer("org.springframework.aot.context.bootstrap.generator.sample.visibility")
				.removeIndent(1).lines().containsSequence(
						"public static void registerProtectedInnerConfiguration_anotherInnerBean(",
						"    DefaultListableBeanFactory beanFactory) {",
						"  BeanDefinitionRegistrar.of(\"anotherInnerBean\", String.class).withFactoryMethod(PublicOuterClassConfiguration.ProtectedInnerConfiguration.class, \"anotherInnerBean\")",
						"      .instanceSupplier(() -> beanFactory.getBean(PublicOuterClassConfiguration.ProtectedInnerConfiguration.class).anotherInnerBean()).register(beanFactory);",
						"}");
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"publicOuterClassConfiguration\", PublicOuterClassConfiguration.class)",
				"    .instanceSupplier(PublicOuterClassConfiguration::new).register(beanFactory);",
				"ContextBootstrapInitializer.registerPublicOuterClassConfiguration_ProtectedInnerConfiguration(beanFactory);",
				"ContextBootstrapInitializer.registerProtectedInnerConfiguration_anotherInnerBean(beanFactory);");
	}

	@Test
	void processWithProtectedConstructorParameter() {
		ContextBootstrapStructure structure = this.tester.process(ProtectedConstructorParameterConfiguration.class);
		assertThat(structure)
				.contextBootstrapInitializer("org.springframework.aot.context.bootstrap.generator.sample.visibility")
				.removeIndent(1).lines().containsSequence(
						"public static void registerProtectedType(DefaultListableBeanFactory beanFactory) {",
						"  BeanDefinitionRegistrar.of(\"org.springframework.aot.context.bootstrap.generator.sample.visibility.ProtectedType\", ProtectedType.class)",
						"      .instanceSupplier(ProtectedType::new).register(beanFactory);",
						"}");
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"protectedConstructorParameterConfiguration\", ProtectedConstructorParameterConfiguration.class)",
				"    .instanceSupplier(ProtectedConstructorParameterConfiguration::new).register(beanFactory);",
				"ContextBootstrapInitializer.registerProtectedParameter(beanFactory);");
	}

	@Test
	void processWithProtectedMethodParameter() {
		ContextBootstrapStructure structure = this.tester.process(ProtectedMethodParameterConfiguration.class);
		assertThat(structure)
				.contextBootstrapInitializer("org.springframework.aot.context.bootstrap.generator.sample.visibility")
				.removeIndent(1).lines().containsSequence(
						"public static void registerProtectedMethodParameterConfiguration_protectedParameter(",
						"    DefaultListableBeanFactory beanFactory) {",
						"  BeanDefinitionRegistrar.of(\"protectedParameter\", ProtectedParameter.class).withFactoryMethod(ProtectedMethodParameterConfiguration.class, \"protectedParameter\", ProtectedType.class)",
						"      .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(ProtectedMethodParameterConfiguration.class).protectedParameter(attributes.get(0)))).register(beanFactory);",
						"}");
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"protectedMethodParameterConfiguration\", ProtectedMethodParameterConfiguration.class)",
				"    .instanceSupplier(ProtectedMethodParameterConfiguration::new).register(beanFactory);",
				"ContextBootstrapInitializer.registerProtectedMethodParameterConfiguration_protectedParameter(beanFactory);");
	}

	@Test
	void processWithProtectedMethodGenericParameter() {
		ContextBootstrapStructure structure = this.tester.process(ProtectedMethodParameterConfiguration.class);
		assertThat(structure)
				.contextBootstrapInitializer("org.springframework.aot.context.bootstrap.generator.sample.visibility")
				.removeIndent(1).lines().containsSequence(
						"public static void registerProtectedMethodParameterConfiguration_protectedGenericParameter(",
						"    DefaultListableBeanFactory beanFactory) {",
						"  BeanDefinitionRegistrar.of(\"protectedGenericParameter\", ProtectedParameter.class).withFactoryMethod(ProtectedMethodParameterConfiguration.class, \"protectedGenericParameter\", ObjectProvider.class)",
						"      .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(ProtectedMethodParameterConfiguration.class).protectedGenericParameter(attributes.get(0)))).register(beanFactory);",
						"}");
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"protectedMethodParameterConfiguration\", ProtectedMethodParameterConfiguration.class)",
				"    .instanceSupplier(ProtectedMethodParameterConfiguration::new).register(beanFactory);",
				"ContextBootstrapInitializer.registerProtectedMethodParameterConfiguration_protectedGenericParameter(beanFactory);");
	}

	@Test
	void processWithSimpleGeneric() {
		ContextBootstrapStructure structure = this.tester.process(GenericConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"stringRepository\", ResolvableType.forClassWithGenerics(Repository.class, String.class)).withFactoryMethod(GenericConfiguration.class, \"stringRepository\")",
				"    .instanceSupplier(() -> beanFactory.getBean(GenericConfiguration.class).stringRepository()).register(beanFactory);");
	}

	@Test
	void processWithObjectProviderTargetGeneric() {
		ContextBootstrapStructure structure = this.tester.process(
				GenericConfiguration.class, GenericObjectProviderConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().removeIndent(2).lines().contains(
				"BeanDefinitionRegistrar.of(\"repositoryId\", String.class).withFactoryMethod(GenericObjectProviderConfiguration.class, \"repositoryId\", ObjectProvider.class)",
				"    .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(GenericObjectProviderConfiguration.class).repositoryId(attributes.get(0)))).register(beanFactory);");
	}

	@Test
	void processWithPrototypeScope() {
		ContextBootstrapStructure structure = this.tester.process(ScopeConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().removeIndent(2).lines().contains(
				"BeanDefinitionRegistrar.of(\"counterBean\", ResolvableType.forClassWithGenerics(NumberHolder.class, Integer.class)).withFactoryMethod(ScopeConfiguration.class, \"counterBean\")",
				"    .instanceSupplier(() -> beanFactory.getBean(ScopeConfiguration.class).counterBean()).customize((bd) -> bd.setScope(\"prototype\")).register(beanFactory);");
	}

	@Test
	void processWithPrototypeScopeAndProxy() {
		ContextBootstrapStructure structure = this.tester.process(ScopeConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().removeIndent(2).lines()
				.contains(
						"BeanDefinitionRegistrar.of(\"scopedTarget.timeBean\", StringHolder.class).withFactoryMethod(ScopeConfiguration.class, \"timeBean\")",
						"    .instanceSupplier(() -> beanFactory.getBean(ScopeConfiguration.class).timeBean()).customize((bd) -> {",
						"  bd.setScope(\"prototype\");",
						"  bd.setAutowireCandidate(false);")
				.contains(
						"BeanDefinitionRegistrar.of(\"timeBean\", StringHolder.class)",
						"    .instanceSupplier(() ->  {",
						"      ScopedProxyFactoryBean factory = new ScopedProxyFactoryBean();",
						"      factory.setTargetBeanName(\"scopedTarget.timeBean\");",
						"      factory.setBeanFactory(beanFactory);",
						"      return factory.getObject();",
						"    }).register(beanFactory);");
	}

	@Test
	void processWithArgumentValue() {
		ContextBootstrapStructure structure = this.tester.process(ArgumentValueRegistrarConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().removeIndent(2).lines().contains(
				"BeanDefinitionRegistrar.of(\"argumentValueString\", String.class).withConstructor(char[].class, int.class, int.class)",
				"    .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new String(attributes.get(0, char[].class), attributes.get(1, int.class), attributes.get(2, int.class)))).customize((bd) -> {",
				"  ConstructorArgumentValues argumentValues = bd.getConstructorArgumentValues();",
				"  argumentValues.addIndexedArgumentValue(0, new char[] { 'a', ' ', 't', 'e', 's', 't' });",
				"  argumentValues.addIndexedArgumentValue(1, 2);",
				"  argumentValues.addIndexedArgumentValue(2, 4);",
				"}).register(beanFactory);"
		);
	}

	@Test
	void processWithFactoryBeanAndResolvedGeneric() {
		ContextBootstrapStructure structure = this.tester.process(TestGenericFactoryBeanConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().removeIndent(2).lines().contains(
				"BeanDefinitionRegistrar.of(\"testStringFactoryBean\", ResolvableType.forClassWithGenerics(TestGenericFactoryBean.class, String.class)).withFactoryMethod(TestGenericFactoryBeanConfiguration.class, \"testStringFactoryBean\")",
				"    .instanceSupplier(() -> beanFactory.getBean(TestGenericFactoryBeanConfiguration.class).testStringFactoryBean()).register(beanFactory);");
	}

	@Test
	void processWithFactoryBeanAndUnresolvedGeneric() {
		ContextBootstrapStructure structure = this.tester.process(TestGenericFactoryBeanConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().removeIndent(2).lines().contains(
				"BeanDefinitionRegistrar.of(\"testGenericFactoryBean\", TestGenericFactoryBean.class).withFactoryMethod(TestGenericFactoryBeanConfiguration.class, \"testGenericFactoryBean\")",
				"    .instanceSupplier(() -> beanFactory.getBean(TestGenericFactoryBeanConfiguration.class).testGenericFactoryBean()).register(beanFactory);");
	}

	@Test
	void awareCallbacksOnBeanRegistrationWriterAreHonored() {
		AwareBeanRegistrationWriterSupplier supplier = spy(new AwareBeanRegistrationWriterSupplier());
		ApplicationContextAotProcessor processor = new ApplicationContextAotProcessor(
				List.of(new DefaultBeanRegistrationWriterSupplier(), supplier), Collections.emptyList());
		GenericApplicationContext context = new GenericApplicationContext();
		processor.process(context, new DefaultBootstrapWriterContext("com.acme", "Test"));
		verify(supplier).setEnvironment(context.getEnvironment());
		verify(supplier).setResourceLoader(context);
		verify(supplier).setApplicationEventPublisher(context);
		verify(supplier).setApplicationContext(context);
		verify(supplier).setBeanClassLoader(context.getBeanFactory().getBeanClassLoader());
		verify(supplier).setBeanFactory(context.getBeanFactory());
		verifyNoMoreInteractions(supplier);
	}

	@Test
	void awareCallbacksOnBeanDefinitionExcludeFilterAreHonored() {
		AwareBeanDefinitionExcludeFilter filter = spy(new AwareBeanDefinitionExcludeFilter());
		ApplicationContextAotProcessor processor = new ApplicationContextAotProcessor(
				Collections.emptyList(), List.of(filter));
		GenericApplicationContext context = new GenericApplicationContext();
		processor.process(context, new DefaultBootstrapWriterContext("com.acme", "Test"));
		verify(filter).setEnvironment(context.getEnvironment());
		verify(filter).setResourceLoader(context);
		verify(filter).setApplicationEventPublisher(context);
		verify(filter).setApplicationContext(context);
		verify(filter).setBeanClassLoader(context.getBeanFactory().getBeanClassLoader());
		verify(filter).setBeanFactory(context.getBeanFactory());
		verifyNoMoreInteractions(filter);
	}

	@Test
	void beanDefinitionExcludedIsNotWritten() {
		BeanDefinitionExcludeFilter filter = BeanDefinitionExcludeFilter.forBeanNames("one", "three");
		ApplicationContextAotProcessor processor = new ApplicationContextAotProcessor(
				List.of(new DefaultBeanRegistrationWriterSupplier()), List.of(filter));
		DefaultBootstrapWriterContext writerContext = new DefaultBootstrapWriterContext("com.example", "Test");
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean("one", SimpleComponent.class);
		context.registerBean("two", SimpleComponent.class);
		context.registerBean("three", SimpleComponent.class);
		processor.process(context, writerContext);
		assertGeneratedCode(writerContext.getMainBootstrapClass())
				.doesNotContain("\"one\"").doesNotContain("\"three\"").contains("\"two\"");
	}

	@Test
	void processUnsupportedTypeThrowsMeaningfulException() {
		ApplicationContextAotProcessor processor = new ApplicationContextAotProcessor(getClass().getClassLoader());
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(TestGenericFactoryBean.class)
				.addConstructorArgValue(new StringWriter()).getBeanDefinition());
		DefaultBootstrapWriterContext writerContext = new DefaultBootstrapWriterContext("com.example", "Test");
		assertThatThrownBy(() ->processor.process(context, writerContext))
				.isInstanceOf(BeanDefinitionGenerationException.class)
				.hasMessage("Failed to handle bean with name 'test' and type 'org.springframework.aot.context.bootstrap.generator.sample.factory.TestGenericFactoryBean<?>'")
				.getCause().hasMessageContaining(StringWriter.class.getName());
	}

	private TextAssert assertGeneratedCode(BootstrapClass bootstrapClass) {
		try {
			StringWriter out = new StringWriter();
			bootstrapClass.toJavaFile().writeTo(out);
			return new TextAssert(out.toString());
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	static class TestContextAware implements EnvironmentAware,
			ResourceLoaderAware, ApplicationEventPublisherAware, ApplicationContextAware, BeanClassLoaderAware,
			BeanFactoryAware {

		@Override
		public void setBeanClassLoader(ClassLoader classLoader) {
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		}

		@Override
		public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		}

		@Override
		public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		}

		@Override
		public void setEnvironment(Environment environment) {
		}

		@Override
		public void setResourceLoader(ResourceLoader resourceLoader) {
		}
	}

	static class AwareBeanRegistrationWriterSupplier extends TestContextAware implements BeanRegistrationWriterSupplier {

		@Override
		public BeanRegistrationWriter get(String beanName, BeanDefinition beanDefinition) {
			return null;
		}

	}

	static class AwareBeanDefinitionExcludeFilter extends TestContextAware implements BeanDefinitionExcludeFilter {

		@Override
		public boolean isExcluded(String beanName, BeanDefinition beanDefinition) {
			return false;
		}

	}

}

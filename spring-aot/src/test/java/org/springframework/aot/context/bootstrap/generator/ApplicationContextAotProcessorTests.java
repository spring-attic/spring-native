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

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriterSupplier;
import org.springframework.aot.context.bootstrap.generator.bean.DefaultBeanRegistrationWriterSupplier;
import org.springframework.aot.context.bootstrap.generator.infrastructure.DefaultBootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.autoconfigure.AutoConfigurationPackagesConfiguration;
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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.samples.scope.ScopeConfiguration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;
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
				"    .instanceSupplier(SimpleConfiguration::new).register(context);",
				"BeanDefinitionRegistrar.of(\"stringBean\", String.class).withFactoryMethod(SimpleConfiguration.class, \"stringBean\")",
				"    .instanceSupplier(() -> context.getBean(SimpleConfiguration.class).stringBean()).register(context);",
				"BeanDefinitionRegistrar.of(\"integerBean\", Integer.class).withFactoryMethod(SimpleConfiguration.class, \"integerBean\")",
				"    .instanceSupplier(() -> context.getBean(SimpleConfiguration.class).integerBean()).register(context);");
	}

	@Test
	void processWithAutoConfiguration() {
		ContextBootstrapStructure structure = this.tester.process(ProjectInfoAutoConfiguration.class);
		// NOTE: application context runner does not register auto-config as FQNs
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"projectInfoAutoConfiguration\", ProjectInfoAutoConfiguration.class).withConstructor(ProjectInfoProperties.class)",
				".instanceSupplier((instanceContext) -> instanceContext.create(context, (attributes) -> new ProjectInfoAutoConfiguration(attributes.get(0)))).register(context);",
				"BeanDefinitionRegistrar.of(\"spring.info-org.springframework.boot.autoconfigure.info.ProjectInfoProperties\", ProjectInfoProperties.class)",
				".instanceSupplier(ProjectInfoProperties::new).register(context);");
	}

	@Test
	void processWithAutoConfigurationPackages() {
		ContextBootstrapStructure structure = this.tester.process(AutoConfigurationPackagesConfiguration.class);
		assertThat(structure).contextBootstrapInitializer()
				.contains("ContextBootstrapInitializer.registerAutoConfigurationPackages_BasePackages(context)");
		assertThat(structure).contextBootstrapInitializer("org.springframework.boot.autoconfigure").contains(
				"BeanDefinitionRegistrar.of(\"org.springframework.boot.autoconfigure.AutoConfigurationPackages\", AutoConfigurationPackages.BasePackages.class)",
				".instanceSupplier(() -> new AutoConfigurationPackages.BasePackages(new String[] { \"org.springframework.aot.context.bootstrap.generator.sample.autoconfigure\" }))"
						+ ".customize((bd) -> bd.setRole(2)).register(context);");
	}

	@Test
	void processWithConfigurationProperties() {
		ContextBootstrapStructure structure = this.tester.process(ConfigurationPropertiesAutoConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().removeIndent(2).lines().contains(
				"BeanDefinitionRegistrar.of(\"org.springframework.boot.context.properties.EnableConfigurationPropertiesRegistrar.methodValidationExcludeFilter\", MethodValidationExcludeFilter.class)",
				"    .instanceSupplier(() -> MethodValidationExcludeFilter.byAnnotation(ConfigurationProperties.class)).customize((bd) -> bd.setRole(2)).register(context);");
	}

	@Test
	void processWithPrimaryBean() {
		ContextBootstrapStructure structure = this.tester.process(MetadataConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"primaryBean\", String.class).withFactoryMethod(MetadataConfiguration.class, \"primaryBean\")",
				"    .instanceSupplier(() -> context.getBean(MetadataConfiguration.class).primaryBean())"
						+ ".customize((bd) -> bd.setPrimary(true)).register(context);");
	}

	@Test
	void processWithRoleInfrastructureBean() {
		ContextBootstrapStructure structure = this.tester.process(MetadataConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"infrastructureBean\", String.class).withFactoryMethod(MetadataConfiguration.class, \"infrastructureBean\")",
				"    .instanceSupplier(() -> context.getBean(MetadataConfiguration.class).infrastructureBean()).customize((bd) -> bd.setRole(2)).register(context);");
	}

	@Test
	void processWithPackageProtectedConfiguration() {
		ContextBootstrapStructure structure = this.tester.process(ProtectedConfigurationImport.class);
		assertThat(structure)
				.contextBootstrapInitializer("org.springframework.aot.context.bootstrap.generator.sample.visibility")
				.removeIndent(1).lines().containsSequence(
						"public static void registerProtectedConfiguration_anotherStringBean(",
						"    GenericApplicationContext context) {",
						"  BeanDefinitionRegistrar.of(\"anotherStringBean\", String.class).withFactoryMethod(ProtectedConfiguration.class, \"anotherStringBean\")",
						"      .instanceSupplier(() -> context.getBean(ProtectedConfiguration.class).anotherStringBean()).register(context);",
						"}");
		assertThat(structure).contextBootstrapInitializer().contains(
				"ContextBootstrapInitializer.registerProtectedConfiguration(context);",
				"ContextBootstrapInitializer.registerProtectedConfiguration_anotherStringBean(context);");
	}

	@Test
	void processWithPublicInnerOnPackageProtectedOuterConfiguration() {
		ContextBootstrapStructure structure = this.tester.process(PublicInnerClassConfigurationImport.class);
		assertThat(structure)
				.contextBootstrapInitializer("org.springframework.aot.context.bootstrap.generator.sample.visibility")
				.removeIndent(1).lines().containsSequence(
						"public static void registerPublicInnerClassConfiguration_InnerConfiguration(",
						"    GenericApplicationContext context) {",
						"  BeanDefinitionRegistrar.of(\"org.springframework.aot.context.bootstrap.generator.sample.visibility.PublicInnerClassConfiguration$InnerConfiguration\", PublicInnerClassConfiguration.InnerConfiguration.class)",
						"      .instanceSupplier(PublicInnerClassConfiguration.InnerConfiguration::new).register(context);",
						"}");
		assertThat(structure).contextBootstrapInitializer().contains(
				"ContextBootstrapInitializer.registerPublicInnerClassConfiguration(context);",
				"ContextBootstrapInitializer.registerPublicInnerClassConfiguration_InnerConfiguration(context);",
				"ContextBootstrapInitializer.registerInnerConfiguration_innerBean(context);");
	}

	@Test
	void processWithPackageProtectedInnerConfiguration() {
		ContextBootstrapStructure structure = this.tester.process(PublicOuterClassConfiguration.class);
		assertThat(structure)
				.contextBootstrapInitializer("org.springframework.aot.context.bootstrap.generator.sample.visibility")
				.removeIndent(1).lines().containsSequence(
						"public static void registerProtectedInnerConfiguration_anotherInnerBean(",
						"    GenericApplicationContext context) {",
						"  BeanDefinitionRegistrar.of(\"anotherInnerBean\", String.class).withFactoryMethod(PublicOuterClassConfiguration.ProtectedInnerConfiguration.class, \"anotherInnerBean\")",
						"      .instanceSupplier(() -> context.getBean(PublicOuterClassConfiguration.ProtectedInnerConfiguration.class).anotherInnerBean()).register(context);",
						"}");
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"publicOuterClassConfiguration\", PublicOuterClassConfiguration.class)",
				"    .instanceSupplier(PublicOuterClassConfiguration::new).register(context);",
				"ContextBootstrapInitializer.registerPublicOuterClassConfiguration_ProtectedInnerConfiguration(context);",
				"ContextBootstrapInitializer.registerProtectedInnerConfiguration_anotherInnerBean(context);");
	}

	@Test
	void processWithProtectedConstructorParameter() {
		ContextBootstrapStructure structure = this.tester.process(ProtectedConstructorParameterConfiguration.class);
		assertThat(structure)
				.contextBootstrapInitializer("org.springframework.aot.context.bootstrap.generator.sample.visibility")
				.removeIndent(1).lines().containsSequence(
						"public static void registerProtectedType(GenericApplicationContext context) {",
						"  BeanDefinitionRegistrar.of(\"org.springframework.aot.context.bootstrap.generator.sample.visibility.ProtectedType\", ProtectedType.class)",
						"      .instanceSupplier(ProtectedType::new).register(context);",
						"}");
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"protectedConstructorParameterConfiguration\", ProtectedConstructorParameterConfiguration.class)",
				"    .instanceSupplier(ProtectedConstructorParameterConfiguration::new).register(context);",
				"ContextBootstrapInitializer.registerProtectedParameter(context);");
	}

	@Test
	void processWithProtectedMethodParameter() {
		ContextBootstrapStructure structure = this.tester.process(ProtectedMethodParameterConfiguration.class);
		assertThat(structure)
				.contextBootstrapInitializer("org.springframework.aot.context.bootstrap.generator.sample.visibility")
				.removeIndent(1).lines().containsSequence(
						"public static void registerProtectedMethodParameterConfiguration_protectedParameter(",
						"    GenericApplicationContext context) {",
						"  BeanDefinitionRegistrar.of(\"protectedParameter\", ProtectedParameter.class).withFactoryMethod(ProtectedMethodParameterConfiguration.class, \"protectedParameter\", ProtectedType.class)",
						"      .instanceSupplier((instanceContext) -> instanceContext.create(context, (attributes) -> context.getBean(ProtectedMethodParameterConfiguration.class).protectedParameter(attributes.get(0)))).register(context);",
						"}");
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"protectedMethodParameterConfiguration\", ProtectedMethodParameterConfiguration.class)",
				"    .instanceSupplier(ProtectedMethodParameterConfiguration::new).register(context);",
				"ContextBootstrapInitializer.registerProtectedMethodParameterConfiguration_protectedParameter(context);");
	}

	@Test
	void processWithProtectedMethodGenericParameter() {
		ContextBootstrapStructure structure = this.tester.process(ProtectedMethodParameterConfiguration.class);
		assertThat(structure)
				.contextBootstrapInitializer("org.springframework.aot.context.bootstrap.generator.sample.visibility")
				.removeIndent(1).lines().containsSequence(
						"public static void registerProtectedMethodParameterConfiguration_protectedGenericParameter(",
						"    GenericApplicationContext context) {",
						"  BeanDefinitionRegistrar.of(\"protectedGenericParameter\", ProtectedParameter.class).withFactoryMethod(ProtectedMethodParameterConfiguration.class, \"protectedGenericParameter\", ObjectProvider.class)",
						"      .instanceSupplier((instanceContext) -> instanceContext.create(context, (attributes) -> context.getBean(ProtectedMethodParameterConfiguration.class).protectedGenericParameter(attributes.get(0)))).register(context);",
						"}");
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"protectedMethodParameterConfiguration\", ProtectedMethodParameterConfiguration.class)",
				"    .instanceSupplier(ProtectedMethodParameterConfiguration::new).register(context);",
				"ContextBootstrapInitializer.registerProtectedMethodParameterConfiguration_protectedGenericParameter(context);");
	}

	@Test
	void processWithSimpleGeneric() {
		ContextBootstrapStructure structure = this.tester.process(GenericConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().contains(
				"BeanDefinitionRegistrar.of(\"stringRepository\", ResolvableType.forClassWithGenerics(Repository.class, String.class)).withFactoryMethod(GenericConfiguration.class, \"stringRepository\")",
				"    .instanceSupplier(() -> context.getBean(GenericConfiguration.class).stringRepository()).register(context);");
	}

	@Test
	void processWithObjectProviderTargetGeneric() {
		ContextBootstrapStructure structure = this.tester.process(
				GenericConfiguration.class, GenericObjectProviderConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().removeIndent(2).lines().contains(
				"BeanDefinitionRegistrar.of(\"repositoryId\", String.class).withFactoryMethod(GenericObjectProviderConfiguration.class, \"repositoryId\", ObjectProvider.class)",
				"    .instanceSupplier((instanceContext) -> instanceContext.create(context, (attributes) -> context.getBean(GenericObjectProviderConfiguration.class).repositoryId(attributes.get(0)))).register(context);");
	}

	@Test
	void processWithPrototypeScope() {
		ContextBootstrapStructure structure = this.tester.process(ScopeConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().removeIndent(2).lines().contains(
				"BeanDefinitionRegistrar.of(\"counterBean\", ResolvableType.forClassWithGenerics(NumberHolder.class, Integer.class)).withFactoryMethod(ScopeConfiguration.class, \"counterBean\")",
				"    .instanceSupplier(() -> context.getBean(ScopeConfiguration.class).counterBean()).customize((bd) -> bd.setScope(\"prototype\")).register(context);");
	}

	@Test
	void processWithPrototypeScopeAndProxy() {
		ContextBootstrapStructure structure = this.tester.process(ScopeConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().removeIndent(2).lines()
				.contains(
						"BeanDefinitionRegistrar.of(\"scopedTarget.timeBean\", StringHolder.class).withFactoryMethod(ScopeConfiguration.class, \"timeBean\")",
						"    .instanceSupplier(() -> context.getBean(ScopeConfiguration.class).timeBean()).customize((bd) -> {",
						"  bd.setScope(\"prototype\");",
						"  bd.setAutowireCandidate(false);")
				.contains(
						"BeanDefinitionRegistrar.of(\"timeBean\", StringHolder.class)",
						"    .instanceSupplier(() ->  {",
						"      ScopedProxyFactoryBean factory = new ScopedProxyFactoryBean();",
						"      factory.setTargetBeanName(\"scopedTarget.timeBean\");",
						"      factory.setBeanFactory(context.getBeanFactory());",
						"      return factory.getObject();",
						"    }).register(context);");
	}

	@Test
	void processWithArgumentValue() {
		ContextBootstrapStructure structure = this.tester.process(ArgumentValueRegistrarConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().removeIndent(2).lines().contains(
				"BeanDefinitionRegistrar.of(\"argumentValueString\", String.class).withConstructor(char[].class, int.class, int.class)",
				"    .instanceSupplier((instanceContext) -> instanceContext.create(context, (attributes) -> new String(attributes.get(0, char[].class), attributes.get(1, int.class), attributes.get(2, int.class)))).customize((bd) -> {",
				"  ConstructorArgumentValues argumentValues = bd.getConstructorArgumentValues();",
				"  argumentValues.addIndexedArgumentValue(0, new char[] { 'a', ' ', 't', 'e', 's', 't' });",
				"  argumentValues.addIndexedArgumentValue(1, 2);",
				"  argumentValues.addIndexedArgumentValue(2, 4);",
				"}).register(context);"
		);
	}

	@Test
	void processWithGenericFactoryBean() {
		ContextBootstrapStructure structure = this.tester.process(TestGenericFactoryBeanConfiguration.class);
		assertThat(structure).contextBootstrapInitializer().removeIndent(2).lines().contains(
				"BeanDefinitionRegistrar.of(\"testGenericFactoryBean\", ResolvableType.forClassWithGenerics(TestGenericFactoryBean.class, Object.class)).withFactoryMethod(TestGenericFactoryBeanConfiguration.class, \"testGenericFactoryBean\")",
				"    .instanceSupplier(() -> context.getBean(TestGenericFactoryBeanConfiguration.class).testGenericFactoryBean()).register(context);");
	}

	@Test
	void awareCallbacksAreHonored() {
		AwareBeanRegistrationWriterSupplier supplier = spy(new AwareBeanRegistrationWriterSupplier());
		ApplicationContextAotProcessor processor = new ApplicationContextAotProcessor(List.of(new DefaultBeanRegistrationWriterSupplier(), supplier));
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

	static class AwareBeanRegistrationWriterSupplier implements BeanRegistrationWriterSupplier, EnvironmentAware,
			ResourceLoaderAware, ApplicationEventPublisherAware, ApplicationContextAware, BeanClassLoaderAware,
			BeanFactoryAware {
		@Override
		public BeanRegistrationWriter get(String beanName, BeanDefinition beanDefinition) {
			return null;
		}

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

}

/*
 * Copyright 2019-2022 the original author or authors.
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

package org.springframework.aot.context.bootstrap.generator.bean;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.aot.context.bootstrap.generator.infrastructure.DefaultBootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.sample.InnerComponentConfiguration.EnvironmentAwareComponent;
import org.springframework.aot.context.bootstrap.generator.sample.InnerComponentConfiguration.NoDependencyComponent;
import org.springframework.aot.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.factory.SampleFactory;
import org.springframework.aot.context.bootstrap.generator.sample.factory.TestGenericFactoryBean;
import org.springframework.aot.context.bootstrap.generator.sample.factory.TestGenericFactoryBeanConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.injection.InjectionComponent;
import org.springframework.aot.context.bootstrap.generator.sample.injection.InjectionConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.visibility.ProtectedConstructorComponent;
import org.springframework.aot.context.bootstrap.generator.sample.visibility.ProtectedFactoryMethod;
import org.springframework.aot.context.bootstrap.generator.sample.visibility.PublicFactoryBean;
import org.springframework.aot.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.samples.simple.SimpleComponent;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link DefaultBeanRegistrationWriter}.
 *
 * @author Stephane Nicoll
 */
class DefaultBeanRegistrationWriterTests {

	@Test
	void writeWithConstructorCreator() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InjectionComponent.class).getBeanDefinition();
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(InjectionComponent.class.getDeclaredConstructors()[0]).build();
		assertThat(beanRegistration(beanDefinition, descriptor, (code) -> code.add("() -> test"))).lines().containsOnly(
				"BeanDefinitionRegistrar.of(\"test\", InjectionComponent.class).withConstructor(String.class)",
				"    .instanceSupplier(() -> test).register(beanFactory);");
	}

	@Test
	void writeWithConstructorCreatorWithNoArgument() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleConfiguration.class).getBeanDefinition();
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(SimpleConfiguration.class)
				.withInstanceCreator(SimpleConfiguration.class.getDeclaredConstructors()[0]).build();
		assertThat(beanRegistration(beanDefinition, descriptor, (code) -> code.add("() -> test"))).lines().containsOnly(
				"BeanDefinitionRegistrar.of(\"test\", SimpleConfiguration.class)",
				"    .instanceSupplier(() -> test).register(beanFactory);");
	}

	@Test
	void writeWithConstructorCreatorOnInnerClass() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(EnvironmentAwareComponent.class).getBeanDefinition();
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(EnvironmentAwareComponent.class)
				.withInstanceCreator(EnvironmentAwareComponent.class.getDeclaredConstructors()[0]).build();
		assertThat(beanRegistration(beanDefinition, descriptor, (code) -> code.add("() -> test"))).lines().containsOnly(
				"BeanDefinitionRegistrar.of(\"test\", InnerComponentConfiguration.EnvironmentAwareComponent.class).withConstructor(InnerComponentConfiguration.class, Environment.class)",
				"    .instanceSupplier(() -> test).register(beanFactory);");
	}

	@Test
	void writeWithConstructorCreatorOnInnerClassWithNoExtraArg() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(NoDependencyComponent.class).getBeanDefinition();
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(NoDependencyComponent.class)
				.withInstanceCreator(NoDependencyComponent.class.getDeclaredConstructors()[0]).build();
		assertThat(beanRegistration(beanDefinition, descriptor, (code) -> code.add("() -> test"))).lines().containsOnly(
				"BeanDefinitionRegistrar.of(\"test\", InnerComponentConfiguration.NoDependencyComponent.class)",
				"    .instanceSupplier(() -> test).register(beanFactory);");
	}

	@Test
	void writeWithFactoryMethodCreator() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition();
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(String.class)
				.withInstanceCreator(ReflectionUtils.findMethod(SampleFactory.class, "create", String.class)).build();
		assertThat(beanRegistration(beanDefinition, descriptor, (code) -> code.add("() -> test")))
				.hasImport(SampleFactory.class).lines().containsOnly(
						"BeanDefinitionRegistrar.of(\"test\", String.class).withFactoryMethod(SampleFactory.class, \"create\", String.class)",
						"    .instanceSupplier(() -> test).register(beanFactory);");
	}

	@Test
	void writeWithProtectedConstructorWriteToBlessedPackage() {
		DefaultBootstrapWriterContext context = createBootstrapContext();
		Builder code = CodeBlock.builder();
		createInstance(BeanDefinitionBuilder.rootBeanDefinition(
				ProtectedConstructorComponent.class).getBeanDefinition()).writeBeanRegistration(context, code);
		assertThat(context.hasBootstrapClass(ProtectedConstructorComponent.class.getPackageName())).isTrue();
		BootstrapClass bootstrapClass = context.getBootstrapClass(ProtectedConstructorComponent.class.getPackageName());
		assertThat(beanRegistration(bootstrapClass)).containsSequence(
				"  public static void registerTest(DefaultListableBeanFactory beanFactory) {\n",
				"    BeanDefinitionRegistrar.of(\"test\", ProtectedConstructorComponent.class)\n",
				"        .instanceSupplier(ProtectedConstructorComponent::new).register(beanFactory);\n",
				"  }");
		assertThat(CodeSnippet.of(code.build())).isEqualTo(
				ProtectedConstructorComponent.class.getPackageName() + ".Test.registerTest(beanFactory);\n");
	}

	@Test
	void writeWithProtectedFactoryMethodWriteToBlessedPackage() {
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(String.class)
				.withInstanceCreator(ReflectionUtils.findMethod(ProtectedFactoryMethod.class, "testBean", Integer.class))
				.build();
		DefaultBootstrapWriterContext context = createBootstrapContext();
		Builder code = CodeBlock.builder();
		createInstance(BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition(),
				descriptor, (instance) -> instance.add("() -> factory.testBean(42)")).writeBeanRegistration(context, code);
		assertThat(context.hasBootstrapClass(ProtectedFactoryMethod.class.getPackageName())).isTrue();
		BootstrapClass bootstrapClass = context.getBootstrapClass(ProtectedFactoryMethod.class.getPackageName());
		assertThat(beanRegistration(bootstrapClass)).containsSequence(
				"  public static void registerProtectedFactoryMethod_test(DefaultListableBeanFactory beanFactory) {\n",
				"    BeanDefinitionRegistrar.of(\"test\", String.class).withFactoryMethod(ProtectedFactoryMethod.class, \"testBean\", Integer.class)\n",
				"        .instanceSupplier(() -> factory.testBean(42)).register(beanFactory);\n",
				"  }");
		assertThat(CodeSnippet.of(code.build())).isEqualTo(
				ProtectedConstructorComponent.class.getPackageName() + ".Test.registerProtectedFactoryMethod_test(beanFactory);\n");
	}

	@Test
	void writeWithProtectedGenericTypeWriteToBlessedPackage() {
		DefaultBootstrapWriterContext context = createBootstrapContext();
		Builder code = CodeBlock.builder();
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(
				PublicFactoryBean.class).getBeanDefinition();
		// This resolve the generic parameter to a protected type
		beanDefinition.setTargetType(PublicFactoryBean.resolveToProtectedGenericParameter());
		createInstance(beanDefinition).writeBeanRegistration(context, code);
		assertThat(context.hasBootstrapClass(PublicFactoryBean.class.getPackageName())).isTrue();
		BootstrapClass bootstrapClass = context.getBootstrapClass(PublicFactoryBean.class.getPackageName());
		assertThat(beanRegistration(bootstrapClass)).containsSequence(
				"  public static void registerTest(DefaultListableBeanFactory beanFactory) {\n",
				"    BeanDefinitionRegistrar.of(\"test\", ResolvableType.forClassWithGenerics(PublicFactoryBean.class, ProtectedType.class))\n",
				"        .instanceSupplier(PublicFactoryBean::new).register(beanFactory);\n",
				"  }");
		assertThat(CodeSnippet.of(code.build())).isEqualTo(
				ProtectedConstructorComponent.class.getPackageName() + ".Test.registerTest(beanFactory);\n");
	}

	@Test
	void writeWithUnresolvedGenerics() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(TestGenericFactoryBean.class)
				.getBeanDefinition();
		beanDefinition.setTargetType(ResolvableType.forClass(TestGenericFactoryBean.class));
		assertUnresolvedTargetTypeNotPreserved(beanDefinition);
	}

	@Test
	void writeWithUnresolvedGenericsPreserveTargetTypeToFalse() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(TestGenericFactoryBean.class)
				.getBeanDefinition();
		beanDefinition.setTargetType(ResolvableType.forClass(TestGenericFactoryBean.class));
		beanDefinition.setAttribute(BeanRegistrationWriter.PRESERVE_TARGET_TYPE, false);
		assertUnresolvedTargetTypeNotPreserved(beanDefinition);
	}

	@Test
	void writeWithUnresolvedGenericsPreserveTargetTypeToNonBoolean() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(TestGenericFactoryBean.class)
				.getBeanDefinition();
		beanDefinition.setTargetType(ResolvableType.forClass(TestGenericFactoryBean.class));
		beanDefinition.setAttribute(BeanRegistrationWriter.PRESERVE_TARGET_TYPE, "something");
		assertUnresolvedTargetTypeNotPreserved(beanDefinition);
	}

	private void assertUnresolvedTargetTypeNotPreserved(RootBeanDefinition beanDefinition) {
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(String.class)
				.withInstanceCreator(ReflectionUtils.findMethod(TestGenericFactoryBeanConfiguration.class, "testGenericFactoryBean")).build();
		assertThat(beanRegistration(beanDefinition, descriptor, (code) -> code.add("() -> test")))
				.hasImport(TestGenericFactoryBean.class).lines().containsOnly(
						"BeanDefinitionRegistrar.of(\"test\", TestGenericFactoryBean.class).withFactoryMethod(TestGenericFactoryBeanConfiguration.class, \"testGenericFactoryBean\")",
						"    .instanceSupplier(() -> test).register(beanFactory);");
	}

	@Test
	void writeWithUnresolvedGenericsAndPreserveTargetType() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(TestGenericFactoryBean.class)
				.getBeanDefinition();
		beanDefinition.setTargetType(ResolvableType.forClass(TestGenericFactoryBean.class));
		beanDefinition.setAttribute(BeanRegistrationWriter.PRESERVE_TARGET_TYPE, true);
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(String.class)
				.withInstanceCreator(ReflectionUtils.findMethod(TestGenericFactoryBeanConfiguration.class, "testGenericFactoryBean")).build();
		assertThat(beanRegistration(beanDefinition, descriptor, (code) -> code.add("() -> test")))
				.hasImport(TestGenericFactoryBean.class).lines().containsOnly(
						"BeanDefinitionRegistrar.of(\"test\", ResolvableType.forClassWithGenerics(TestGenericFactoryBean.class, Serializable.class)).withFactoryMethod(TestGenericFactoryBeanConfiguration.class, \"testGenericFactoryBean\")",
						"    .instanceSupplier(() -> test).register(beanFactory);");
	}

	@Test
	void writeWithSyntheticFlag() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class, "create")
				.setSynthetic(true).getBeanDefinition();
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", SampleFactory.class)",
						"    .instanceSupplier(() -> SampleFactory::new).customize((bd) -> bd.setSynthetic(true)).register(beanFactory);");
	}

	@Test
	void writeWithDependsOn() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class, "create")
				.addDependsOn("test").getBeanDefinition();
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", SampleFactory.class)",
						"    .instanceSupplier(() -> SampleFactory::new).customize((bd) -> bd.setDependsOn(new String[] { \"test\" })).register(beanFactory);");
	}

	@Test
	void writeWithMultipleFlags() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class, "create")
				.setSynthetic(true).setPrimary(true).getBeanDefinition();
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly(
						"BeanDefinitionRegistrar.of(\"test\", SampleFactory.class)",
						"    .instanceSupplier(() -> SampleFactory::new).customize((bd) -> {",
						"  bd.setSynthetic(true);",
						"  bd.setPrimary(true);",
						"}).register(beanFactory);");
	}

	@Test
	void writeWithLazyInit() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleComponent.class)
				.setLazyInit(true).getBeanDefinition();
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SimpleComponent::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", SimpleComponent.class)",
						"    .instanceSupplier(() -> SimpleComponent::new).customize((bd) -> bd.setLazyInit(true)).register(beanFactory);");
	}

	@Test
	void writeWithPrototypeScope() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleComponent.class)
				.setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE).getBeanDefinition();
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SimpleComponent::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", SimpleComponent.class)",
						"    .instanceSupplier(() -> SimpleComponent::new).customize((bd) -> bd.setScope(\"prototype\")).register(beanFactory);");
	}

	@Test
	void writeWithDefaultScopeDoesNotCustomizeBeanDefinition() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleComponent.class)
				.setScope(ConfigurableBeanFactory.SCOPE_SINGLETON).getBeanDefinition();
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SimpleComponent::new")))
				.doesNotContain("bd.setScope(");
	}

	@Test
	void writeWithAutowireCandidateDisabled() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleComponent.class)
				.getBeanDefinition();
		beanDefinition.setAutowireCandidate(false);
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SimpleComponent::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", SimpleComponent.class)",
						"    .instanceSupplier(() -> SimpleComponent::new).customize((bd) -> bd.setAutowireCandidate(false)).register(beanFactory);");
	}

	@Test
	void writeWithDefaultAutowireCandidateDoesNotCustomizeBeanDefinition() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleComponent.class)
				.getBeanDefinition();
		beanDefinition.setAutowireCandidate(true);
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SimpleComponent::new")))
				.doesNotContain("bd.setAutowireCandidate(");
	}

	@Test
	void writeWithNoScopeDoesNotCustomizeBeanDefinition() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleComponent.class)
				.setScope("").getBeanDefinition();
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SimpleComponent::new")))
				.doesNotContain("bd.setScope(");
	}

	@Test
	void writeWithSingleConstructorArgument() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class, "create")
				.getBeanDefinition();
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, "test");
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", SampleFactory.class)",
						"    .instanceSupplier(() -> SampleFactory::new).customize((bd) -> bd.getConstructorArgumentValues().addIndexedArgumentValue(0, \"test\")).register(beanFactory);");
	}

	@Test
	void writeWithSeveralConstructorArguments() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class, "create")
				.getBeanDefinition();
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, new RuntimeBeanReference("test"));
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(2, 42);
		CodeSnippet generateCode = beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"));
		assertThat(generateCode).lines()
				.containsOnly(
						"BeanDefinitionRegistrar.of(\"test\", SampleFactory.class)",
						"    .instanceSupplier(() -> SampleFactory::new).customize((bd) -> {",
						"  ConstructorArgumentValues argumentValues = bd.getConstructorArgumentValues();",
						"  argumentValues.addIndexedArgumentValue(0, new RuntimeBeanReference(\"test\"));",
						"  argumentValues.addIndexedArgumentValue(2, 42);",
						"}).register(beanFactory);");
		assertThat(generateCode).hasImport(ConstructorArgumentValues.class);
	}

	@Test
	void writeWithNonIndexedConstructorArgument() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class, "create")
				.getBeanDefinition();
		beanDefinition.getConstructorArgumentValues().addGenericArgumentValue("test");
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", SampleFactory.class)",
						"    .instanceSupplier(() -> SampleFactory::new).register(beanFactory);");
	}

	@Test
	void writeSimpleProperty() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class, "create")
				.addPropertyValue("test", "Hello").getBeanDefinition();
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", SampleFactory.class)",
						"    .instanceSupplier(() -> SampleFactory::new).customize((bd) -> bd.getPropertyValues().addPropertyValue(\"test\", \"Hello\")).register(beanFactory);");
	}

	@Test
	void writeSeveralProperties() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class, "create")
				.addPropertyValue("test", "Hello").addPropertyValue("counter", 42).getBeanDefinition();
		CodeSnippet generateCode = beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"));
		assertThat(generateCode).lines().containsOnly(
				"BeanDefinitionRegistrar.of(\"test\", SampleFactory.class)",
				"    .instanceSupplier(() -> SampleFactory::new).customize((bd) -> {",
				"  MutablePropertyValues propertyValues = bd.getPropertyValues();",
				"  propertyValues.addPropertyValue(\"test\", \"Hello\");",
				"  propertyValues.addPropertyValue(\"counter\", 42);",
				"}).register(beanFactory);");
		assertThat(generateCode).hasImport(MutablePropertyValues.class);
	}

	@Test
	void writePropertyReference() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class, "create")
				.addPropertyReference("myService", "test").getBeanDefinition();
		CodeSnippet generatedCode = beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"));
		assertThat(generatedCode).lines().containsOnly(
				"BeanDefinitionRegistrar.of(\"test\", SampleFactory.class)",
				"    .instanceSupplier(() -> SampleFactory::new).customize((bd) -> bd.getPropertyValues().addPropertyValue(\"myService\", new RuntimeBeanReference(\"test\"))).register(beanFactory);");
		assertThat(generatedCode).hasImport(RuntimeBeanReference.class);
	}

	@Test
	void writePropertyAsBeanDefinition() {
		BeanDefinition innerBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleConfiguration.class, "stringBean")
				.getBeanDefinition();
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InjectionConfiguration.class)
				.addPropertyValue("name", innerBeanDefinition).getBeanDefinition();
		CodeSnippet generatedCode = beanRegistration(beanDefinition, (code) -> code.add("() -> InjectionConfiguration::new"));
		assertThat(generatedCode).lines().containsOnly(
				"BeanDefinitionRegistrar.of(\"test\", InjectionConfiguration.class)",
				"    .instanceSupplier(() -> InjectionConfiguration::new).customize((bd) -> bd.getPropertyValues().addPropertyValue(\"name\", BeanDefinitionRegistrar.inner(SimpleConfiguration.class).withFactoryMethod(SimpleConfiguration.class, \"stringBean\")",
				"    .instanceSupplier(() -> beanFactory.getBean(SimpleConfiguration.class).stringBean()).toBeanDefinition())).register(beanFactory);");
		assertThat(generatedCode).hasImport(SimpleConfiguration.class);
	}

	@Test
	void writePropertyAsListOfBeanDefinitions() {
		BeanDefinition innerBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleConfiguration.class, "stringBean")
				.getBeanDefinition();
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InjectionConfiguration.class)
				.addPropertyValue("names", List.of(innerBeanDefinition, innerBeanDefinition)).getBeanDefinition();
		CodeSnippet generatedCode = beanRegistration(beanDefinition, (code) -> code.add("() -> InjectionConfiguration::new"));
		assertThat(generatedCode).lines().containsOnly(
				"BeanDefinitionRegistrar.of(\"test\", InjectionConfiguration.class)",
				"    .instanceSupplier(() -> InjectionConfiguration::new).customize((bd) -> bd.getPropertyValues().addPropertyValue(\"names\", List.of(BeanDefinitionRegistrar.inner(SimpleConfiguration.class).withFactoryMethod(SimpleConfiguration.class, \"stringBean\")",
				"    .instanceSupplier(() -> beanFactory.getBean(SimpleConfiguration.class).stringBean()).toBeanDefinition(), BeanDefinitionRegistrar.inner(SimpleConfiguration.class).withFactoryMethod(SimpleConfiguration.class, \"stringBean\")",
				"    .instanceSupplier(() -> beanFactory.getBean(SimpleConfiguration.class).stringBean()).toBeanDefinition()))).register(beanFactory);");
		assertThat(generatedCode).hasImport(SimpleConfiguration.class);
	}

	@Test
	void writePropertyAsBeanDefinitionWithNoFactory() {
		BeanDefinition innerBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleConfiguration.class, "stringBean")
				.getBeanDefinition();
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InjectionConfiguration.class)
				.addPropertyValue("name", innerBeanDefinition).getBeanDefinition();
		DefaultBeanRegistrationWriter writer = new DefaultBeanRegistrationWriter("test", beanDefinition,
				simpleBeanInstanceDescriptor(beanDefinition), BeanRegistrationWriterOptions.DEFAULTS) {
			@Override
			protected void writeInstanceSupplier(Builder code) {
				code.add("dummy");
			}
		};
		assertThatIllegalStateException().isThrownBy(() -> writer.writeBeanRegistration(CodeBlock.builder()))
				.withMessageContaining("No bean registration writer available for nested bean definition");
	}

	@Test
	void writeAttributesIsOptIn() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleComponent.class).getBeanDefinition();
		beanDefinition.setAttribute("yes-1", 1);
		beanDefinition.setAttribute("yes-2", 2);
		beanDefinition.setAttribute("no-1", -1);
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SimpleComponent::new"))).doesNotContain("setAttribute");
	}

	@Test
	void writeAttributesUseFilter() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleComponent.class).getBeanDefinition();
		beanDefinition.setAttribute("yes-1", 1);
		beanDefinition.setAttribute("yes-2", 2);
		beanDefinition.setAttribute("no-1", -1);
		DefaultBeanRegistrationWriter writer = new DefaultBeanRegistrationWriter("test", beanDefinition,
				simpleBeanInstanceDescriptor(beanDefinition)) {
			@Override
			protected Predicate<String> getAttributeFilter() {
				return (candidate) -> candidate.startsWith("yes");
			}

			@Override
			protected void writeInstanceSupplier(Builder code) {
				code.add("() -> SimpleComponent::new");
			}
		};
		assertThat(CodeSnippet.of(writer::writeBeanRegistration)).contains("bd.setAttribute(\"yes-1\", 1)")
				.contains("bd.setAttribute(\"yes-2\", 2)").doesNotContain("bd.setAttribute(\"no-1\", -1)");
	}

	@Test
	void writePropertyAsBeanDefinitionUseDedicatedVariableName() {
		BeanDefinition innerBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleConfiguration.class, "stringBean")
				.setRole(2).getBeanDefinition();
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InjectionConfiguration.class)
				.addPropertyValue("name", innerBeanDefinition).getBeanDefinition();
		CodeSnippet generatedCode = beanRegistration(beanDefinition, (code) -> code.add("() -> InjectionConfiguration::new"));
		assertThat(generatedCode).lines().contains(
				"BeanDefinitionRegistrar.of(\"test\", InjectionConfiguration.class)",
				"    .instanceSupplier(() -> InjectionConfiguration::new).customize((bd) -> bd.getPropertyValues().addPropertyValue(\"name\", BeanDefinitionRegistrar.inner(SimpleConfiguration.class).withFactoryMethod(SimpleConfiguration.class, \"stringBean\")",
				"    .instanceSupplier(() -> beanFactory.getBean(SimpleConfiguration.class).stringBean()).customize((bd_) -> bd_.setRole(2)).toBeanDefinition())).register(beanFactory);");
	}

	@Test
	void writeInnerBeanDefinition() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(SimpleComponent.class).getBeanDefinition();
		assertThat(inner(beanDefinition)).lines().containsOnly(
				"BeanDefinitionRegistrar.inner(SimpleComponent.class)",
				"    .instanceSupplier(SimpleComponent::new).toBeanDefinition()");
	}


	private static DefaultBootstrapWriterContext createBootstrapContext() {
		return new DefaultBootstrapWriterContext("com.example", "Test");
	}

	// NEW
	private CodeSnippet beanRegistration(BeanDefinition beanDefinition, BeanInstanceDescriptor descriptor, Consumer<Builder> instanceSupplier) {
		DefaultBeanRegistrationWriter writer = createInstance(beanDefinition, descriptor, instanceSupplier);
		return CodeSnippet.of(writer::writeBeanRegistration);
	}

	private CodeSnippet beanRegistration(BeanDefinition beanDefinition, Consumer<Builder> instanceSupplier) {
		DefaultBeanRegistrationWriter writer = createInstance(beanDefinition,
				simpleBeanInstanceDescriptor(beanDefinition), instanceSupplier);
		return CodeSnippet.of(writer::writeBeanRegistration);
	}

	private DefaultBeanRegistrationWriter createInstance(BeanDefinition beanDefinition, BeanInstanceDescriptor descriptor, Consumer<Builder> instanceSupplier) {
		BeanRegistrationWriterOptions options = BeanRegistrationWriterOptions.builder().withWriterFactory(this::createInstance).build();
		return new DefaultBeanRegistrationWriter("test", beanDefinition, descriptor, options) {
			@Override
			protected void writeInstanceSupplier(Builder code) {
				instanceSupplier.accept(code);
			}
		};
	}

	private DefaultBeanRegistrationWriter createInstance(String beanName, BeanDefinition beanDefinition) {
		DefaultBeanRegistrationWriterSupplier supplier = new DefaultBeanRegistrationWriterSupplier();
		supplier.setBeanFactory(new DefaultListableBeanFactory());
		return (DefaultBeanRegistrationWriter) supplier.get(beanName, beanDefinition);
	}

	private DefaultBeanRegistrationWriter createInstance(BeanDefinition beanDefinition) {
		return createInstance("test", beanDefinition);
	}

	private BeanInstanceDescriptor simpleBeanInstanceDescriptor(BeanDefinition beanDefinition) {
		return BeanInstanceDescriptor.of(beanDefinition.getResolvableType()).build();
	}

	private CodeSnippet inner(BeanDefinition beanDefinition) {
		return CodeSnippet.of((code) -> createInstance(null, beanDefinition).writeBeanDefinition(code));
	}

	private String beanRegistration(BootstrapClass bootstrapClass) {
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

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

package org.springframework.context.bootstrap.generator.bean;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import org.junit.jupiter.api.Test;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.samples.simple.SimpleComponent;
import org.springframework.context.bootstrap.generator.BootstrapClass;
import org.springframework.context.bootstrap.generator.BootstrapWriterContext;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.context.bootstrap.generator.sample.factory.SampleFactory;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionComponent;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionConfiguration;
import org.springframework.context.bootstrap.generator.sample.visibility.ProtectedConstructorComponent;
import org.springframework.context.bootstrap.generator.sample.visibility.ProtectedFactoryMethod;
import org.springframework.context.bootstrap.generator.sample.visibility.PublicFactoryBean;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;
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
	void writeWithProtectedConstructorWriteToBlessedPackage() {
		BootstrapWriterContext context = createBootstrapContext();
		Builder code = CodeBlock.builder();
		createInstance(BeanDefinitionBuilder.rootBeanDefinition(
				ProtectedConstructorComponent.class).getBeanDefinition()).writeBeanRegistration(context, code);
		assertThat(context.hasBootstrapClass(ProtectedConstructorComponent.class.getPackageName())).isTrue();
		BootstrapClass bootstrapClass = context.getBootstrapClass(ProtectedConstructorComponent.class.getPackageName());
		assertThat(beanRegistration(bootstrapClass)).containsSequence(
				"  public static void registerTest(GenericApplicationContext context) {\n",
				"    BeanDefinitionRegistrar.of(\"test\", ProtectedConstructorComponent.class).instanceSupplier(ProtectedConstructorComponent::new).register(context);\n",
				"  }");
		assertThat(CodeSnippet.of(code.build())).isEqualTo("ContextBootstrapInitializer.registerTest(context);\n");
	}

	@Test
	void writeWithProtectedFactoryMethodWriteToBlessedPackage() {
		SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(BeanInstanceDescriptor.of(String.class)
				.withInstanceCreator(ReflectionUtils.findMethod(ProtectedFactoryMethod.class, "testBean", Integer.class))
				.build(), (code) -> code.add("() -> factory.testBean(42)"));
		BootstrapWriterContext context = createBootstrapContext();
		Builder code = CodeBlock.builder();
		createInstance(BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition(),
				beanValueWriter).writeBeanRegistration(context, code);
		assertThat(context.hasBootstrapClass(ProtectedFactoryMethod.class.getPackageName())).isTrue();
		BootstrapClass bootstrapClass = context.getBootstrapClass(ProtectedFactoryMethod.class.getPackageName());
		assertThat(beanRegistration(bootstrapClass)).containsSequence(
				"  public static void registerProtectedFactoryMethod_test(GenericApplicationContext context) {\n",
				"    BeanDefinitionRegistrar.of(\"test\", String.class).instanceSupplier(() -> factory.testBean(42)).register(context);\n",
				"  }");
		assertThat(CodeSnippet.of(code.build())).isEqualTo("ContextBootstrapInitializer.registerProtectedFactoryMethod_test(context);\n");
	}

	@Test
	void writeWithProtectedGenericTypeWriteToBlessedPackage() {
		BootstrapWriterContext context = createBootstrapContext();
		Builder code = CodeBlock.builder();
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(
				PublicFactoryBean.class).getBeanDefinition();
		// This resolve the generic parameter to a protected type
		beanDefinition.setTargetType(PublicFactoryBean.resolveToProtectedGenericParameter());
		createInstance(beanDefinition).writeBeanRegistration(context, code);
		assertThat(context.hasBootstrapClass(PublicFactoryBean.class.getPackageName())).isTrue();
		BootstrapClass bootstrapClass = context.getBootstrapClass(PublicFactoryBean.class.getPackageName());
		assertThat(beanRegistration(bootstrapClass)).containsSequence(
				"  public static void registerTest(GenericApplicationContext context) {\n",
				"    BeanDefinitionRegistrar.of(\"test\", ResolvableType.forClassWithGenerics(PublicFactoryBean.class, ProtectedType.class)).instanceSupplier(PublicFactoryBean::new).register(context);\n",
				"  }");
		assertThat(CodeSnippet.of(code.build())).isEqualTo("ContextBootstrapInitializer.registerTest(context);\n");
	}

	@Test
	void writeWithSyntheticFlag() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(SampleFactory.class.getName(), "create").setSynthetic(true).getBeanDefinition();
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", Object.class).instanceSupplier(() -> SampleFactory::new)"
						+ ".customize((bd) -> bd.setSynthetic(true)).register(context);");
	}

	@Test
	void writeWithMultipleFlags() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(SampleFactory.class.getName(), "create").setSynthetic(true).setPrimary(true).getBeanDefinition();
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", Object.class).instanceSupplier(() -> SampleFactory::new)"
								+ ".customize((bd) -> {",
						"  bd.setSynthetic(true);",
						"  bd.setPrimary(true);",
						"}).register(context);");
	}

	@Test
	void writeWithSingleConstructorArgument() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(SampleFactory.class.getName(), "create").getBeanDefinition();
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, "test");
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", Object.class).instanceSupplier(() -> SampleFactory::new)"
						+ ".customize((bd) -> bd.getConstructorArgumentValues().addIndexedArgumentValue(0, \"test\")).register(context);");
	}

	@Test
	void writeWithSeveralConstructorArguments() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(SampleFactory.class.getName(), "create").getBeanDefinition();
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, new RuntimeBeanReference("test"));
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(2, 42);
		CodeSnippet generateCode = beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"));
		assertThat(generateCode).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", Object.class).instanceSupplier(() -> SampleFactory::new)"
								+ ".customize((bd) -> {",
						"  ConstructorArgumentValues argumentValues = bd.getConstructorArgumentValues();",
						"  argumentValues.addIndexedArgumentValue(0, new RuntimeBeanReference(\"test\"));",
						"  argumentValues.addIndexedArgumentValue(2, 42);",
						"}).register(context);");
		assertThat(generateCode).hasImport(ConstructorArgumentValues.class);
	}

	@Test
	void writeSimpleProperty() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class.getName(), "create")
				.addPropertyValue("test", "Hello").getBeanDefinition();
		assertThat(beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", Object.class).instanceSupplier(() -> SampleFactory::new)"
						+ ".customize((bd) -> bd.getPropertyValues().addPropertyValue(\"test\", \"Hello\")).register(context);");
	}

	@Test
	void writeSeveralProperties() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class.getName(), "create")
				.addPropertyValue("test", "Hello").addPropertyValue("counter", 42).getBeanDefinition();
		CodeSnippet generateCode = beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"));
		assertThat(generateCode).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", Object.class).instanceSupplier(() -> SampleFactory::new)"
								+ ".customize((bd) -> {",
						"  MutablePropertyValues propertyValues = bd.getPropertyValues();",
						"  propertyValues.addPropertyValue(\"test\", \"Hello\");",
						"  propertyValues.addPropertyValue(\"counter\", 42);",
						"}).register(context);");
		assertThat(generateCode).hasImport(MutablePropertyValues.class);
	}

	@Test
	void writePropertyReference() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class.getName(), "create")
				.addPropertyReference("myService", "test").getBeanDefinition();
		CodeSnippet generatedCode = beanRegistration(beanDefinition, (code) -> code.add("() -> SampleFactory::new"));
		assertThat(generatedCode).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", Object.class).instanceSupplier(() -> SampleFactory::new)"
						+ ".customize((bd) -> bd.getPropertyValues().addPropertyValue(\"myService\", new RuntimeBeanReference(\"test\"))).register(context);");
		assertThat(generatedCode).hasImport(RuntimeBeanReference.class);
	}

	@Test
	void writePropertyAsBeanDefinition() {
		BeanDefinition innerBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleConfiguration.class, "stringBean").getBeanDefinition();
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InjectionConfiguration.class)
				.addPropertyValue("name", innerBeanDefinition).getBeanDefinition();
		CodeSnippet generatedCode = beanRegistration(beanDefinition, (code) -> code.add("() -> InjectionConfiguration::new"));
		assertThat(generatedCode).lines().containsOnly(
				"BeanDefinitionRegistrar.of(\"test\", InjectionConfiguration.class).instanceSupplier(() -> InjectionConfiguration::new)"
						+ ".customize((bd) -> bd.getPropertyValues().addPropertyValue(\"name\", BeanDefinitionRegistrar.inner(SimpleConfiguration.class)"
						+ ".instanceSupplier(() -> context.getBean(SimpleConfiguration.class).stringBean()).toBeanDefinition())).register(context);");
		assertThat(generatedCode).hasImport(SimpleConfiguration.class);
	}

	@Test
	void writePropertyAsBeanDefinitionWithNoFactory() {
		BeanDefinition innerBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleConfiguration.class, "stringBean").getBeanDefinition();
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InjectionConfiguration.class)
				.addPropertyValue("name", innerBeanDefinition).getBeanDefinition();
		DefaultBeanRegistrationWriter writer = new DefaultBeanRegistrationWriter("test", beanDefinition,
				new SimpleBeanValueWriter(BeanInstanceDescriptor.of(beanDefinition.getResolvableType()).build(),
						(code) -> code.add("() -> InjectionConfiguration::new")),
				BeanRegistrationWriterOptions.DEFAULTS);
		assertThatIllegalStateException().isThrownBy(() -> writer.writeBeanRegistration(CodeBlock.builder()))
				.withMessageContaining("No bean registration writer available for nested bean definition");
	}

	@Test
	void writePropertyAsBeanDefinitionUseDedicatedVariableName() {
		BeanDefinition innerBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleConfiguration.class, "stringBean").setRole(2).getBeanDefinition();
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InjectionConfiguration.class)
				.addPropertyValue("name", innerBeanDefinition).getBeanDefinition();
		CodeSnippet generatedCode = beanRegistration(beanDefinition, (code) -> code.add("() -> InjectionConfiguration::new"));
		assertThat(generatedCode).contains(
				".addPropertyValue(\"name\", BeanDefinitionRegistrar.inner(SimpleConfiguration.class).instanceSupplier(() -> context.getBean(SimpleConfiguration.class).stringBean())"
						+ ".customize((bd_) -> bd_.setRole(2))");
	}

	@Test
	void writeInnerBeanDefinition() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(SimpleComponent.class).getBeanDefinition();
		assertThat(beanDefinition(beanDefinition)).lines()
				.containsOnly("BeanDefinitionRegistrar.inner(SimpleComponent.class).instanceSupplier(SimpleComponent::new).toBeanDefinition()");
	}

	@Test
	void writeBeanDefinitionRegisterReflectionEntriesForInstanceCreator() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).build(), (code) -> code.add("test"));
		BootstrapWriterContext context = createBootstrapContext();
		createInstance(BeanDefinitionBuilder.rootBeanDefinition(InjectionComponent.class).getBeanDefinition(),
				beanValueWriter).writeBeanRegistration(context, CodeBlock.builder());
		assertThat(context.getRuntimeReflectionRegistry().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionComponent.class);
			assertThat(entry.getMethods()).containsOnly(instanceCreator);
			assertThat(entry.getFields()).isEmpty();
		});
	}

	@Test
	void writeBeanDefinitionRegisterReflectionEntriesForMethodInjectionPoint() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		Method injectionPoint = ReflectionUtils.findMethod(InjectionComponent.class, "setCounter", Integer.class);
		SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).withInjectionPoint(injectionPoint, false).build(),
				(code) -> code.add("test"));
		BootstrapWriterContext context = createBootstrapContext();
		createInstance(BeanDefinitionBuilder.rootBeanDefinition(InjectionComponent.class).getBeanDefinition(),
				beanValueWriter).writeBeanRegistration(context, CodeBlock.builder());
		assertThat(context.getRuntimeReflectionRegistry().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionComponent.class);
			assertThat(entry.getMethods()).containsOnly(instanceCreator, injectionPoint);
			assertThat(entry.getFields()).isEmpty();
		});
	}

	@Test
	void writeBeanDefinitionRegisterReflectionEntriesForFieldInjectionPoint() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		Field injectionPoint = ReflectionUtils.findField(InjectionComponent.class, "counter");
		SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).withInjectionPoint(injectionPoint, false).build(),
				(code) -> code.add("test"));
		BootstrapWriterContext context = createBootstrapContext();
		createInstance(BeanDefinitionBuilder.rootBeanDefinition(InjectionComponent.class).getBeanDefinition(),
				beanValueWriter).writeBeanRegistration(context, CodeBlock.builder());
		assertThat(context.getRuntimeReflectionRegistry().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionComponent.class);
			assertThat(entry.getMethods()).containsOnly(instanceCreator);
			assertThat(entry.getFields()).containsOnly(injectionPoint);
		});
	}

	@Test
	void writeBeanDefinitionRegisterReflectionEntriesForProperties() {
		Constructor<?> instanceCreator = InjectionConfiguration.class.getDeclaredConstructors()[0];
		Method nameWriteMethod = ReflectionUtils.findMethod(InjectionConfiguration.class, "setName", String.class);
		Method counterWriteMethod = ReflectionUtils.findMethod(InjectionConfiguration.class, "setCounter", Integer.class);
		SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).withProperty(nameWriteMethod, new PropertyValue("name", "Hello"))
				.withProperty(counterWriteMethod, new PropertyValue("counter", 42)).build(),
				(code) -> code.add("test"));
		BootstrapWriterContext context = createBootstrapContext();
		createInstance(BeanDefinitionBuilder.rootBeanDefinition(InjectionConfiguration.class).getBeanDefinition(),
				beanValueWriter).writeBeanRegistration(context, CodeBlock.builder());
		assertThat(context.getRuntimeReflectionRegistry().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionConfiguration.class);
			assertThat(entry.getMethods()).containsOnly(instanceCreator, nameWriteMethod, counterWriteMethod);
			assertThat(entry.getFields()).isEmpty();
		});
	}

	private static BootstrapWriterContext createBootstrapContext() {
		return new BootstrapWriterContext(BootstrapClass.of(ClassName.get("com.example", "Test")));
	}

	private CodeSnippet beanRegistration(BeanDefinition beanDefinition, Consumer<Builder> instanceSupplier) {
		return CodeSnippet.of((code) -> {
			SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(BeanInstanceDescriptor
					.of(beanDefinition.getResolvableType()).build(), instanceSupplier);
			createInstance(beanDefinition, beanValueWriter).writeBeanRegistration(code);
		});
	}

	private CodeSnippet beanDefinition(BeanDefinition beanDefinition) {
		return CodeSnippet.of((code) -> createInstance(null, beanDefinition).writeBeanDefinition(code));
	}

	private DefaultBeanRegistrationWriter createInstance(BeanDefinition beanDefinition, BeanValueWriter beanValueWriter) {
		return createInstance("test", beanDefinition, beanValueWriter);
	}

	private DefaultBeanRegistrationWriter createInstance(String beanName,
			BeanDefinition beanDefinition, BeanValueWriter beanValueWriter) {
		return new DefaultBeanRegistrationWriter(beanName, beanDefinition, beanValueWriter,
				BeanRegistrationWriterOptions.builder().withWriterFactory(this::createInstance).build());
	}

	private DefaultBeanRegistrationWriter createInstance(String beanName, BeanDefinition beanDefinition) {
		DefaultBeanRegistrationWriterSupplier supplier = new DefaultBeanRegistrationWriterSupplier();
		supplier.setBeanFactory(new DefaultListableBeanFactory());
		return supplier.get(beanName, beanDefinition);
	}

	private DefaultBeanRegistrationWriter createInstance(BeanDefinition beanDefinition) {
		return createInstance("test", beanDefinition);
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

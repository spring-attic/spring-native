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
import java.util.Collections;
import java.util.function.Consumer;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.bootstrap.generator.BootstrapClass;
import org.springframework.context.bootstrap.generator.BootstrapWriterContext;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.sample.factory.SampleFactory;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionComponent;
import org.springframework.context.bootstrap.generator.sample.visibility.ProtectedConstructorComponent;
import org.springframework.context.bootstrap.generator.sample.visibility.ProtectedFactoryMethod;
import org.springframework.context.bootstrap.generator.sample.visibility.PublicFactoryBean;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultBeanRegistrationGenerator}.
 *
 * @author Stephane Nicoll
 */
class DefaultBeanRegistrationGeneratorTests {

	@Test
	void writeWithProtectedConstructorWriteToBlessedPackage() {
		BootstrapWriterContext context = createBootstrapContext();
		Builder code = CodeBlock.builder();
		createInstance(BeanDefinitionBuilder.rootBeanDefinition(
				ProtectedConstructorComponent.class).getBeanDefinition()).writeBeanRegistration(context, code);
		assertThat(context.hasBootstrapClass(ProtectedConstructorComponent.class.getPackageName())).isTrue();
		BootstrapClass bootstrapClass = context.getBootstrapClass(ProtectedConstructorComponent.class.getPackageName());
		assertThat(generateCode(bootstrapClass)).containsSequence(
				"  public static void registerTest(GenericApplicationContext context) {\n",
				"    BeanDefinitionRegistrar.of(\"test\", ProtectedConstructorComponent.class).instanceSupplier(ProtectedConstructorComponent::new).register(context);\n",
				"  }");
		assertThat(CodeSnippet.of(code.build())).isEqualTo("ContextBootstrapInitializer.registerTest(context);\n");
	}

	@Test
	void writeWithProtectedFactoryMethodWriteToBlessedPackage() {
		Method instanceCreator = ReflectionUtils.findMethod(ProtectedFactoryMethod.class, "testBean", Integer.class);
		SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(new BeanInstanceDescriptor(
				String.class, instanceCreator), (code) -> code.add("() -> factory.testBean(42)"));
		BootstrapWriterContext context = createBootstrapContext();
		Builder code = CodeBlock.builder();
		new DefaultBeanRegistrationGenerator("test", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition(),
				beanValueWriter).writeBeanRegistration(context, code);
		assertThat(context.hasBootstrapClass(ProtectedFactoryMethod.class.getPackageName())).isTrue();
		BootstrapClass bootstrapClass = context.getBootstrapClass(ProtectedFactoryMethod.class.getPackageName());
		assertThat(generateCode(bootstrapClass)).containsSequence(
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
		assertThat(generateCode(bootstrapClass)).containsSequence(
				"  public static void registerTest(GenericApplicationContext context) {\n",
				"    BeanDefinitionRegistrar.of(\"test\", ResolvableType.forClassWithGenerics(PublicFactoryBean.class, ProtectedType.class)).instanceSupplier(PublicFactoryBean::new).register(context);\n",
				"  }");
		assertThat(CodeSnippet.of(code.build())).isEqualTo("ContextBootstrapInitializer.registerTest(context);\n");
	}

	@Test
	void writeWithSyntheticFlag() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(SampleFactory.class.getName(), "create").setSynthetic(true).getBeanDefinition();
		assertThat(generateCode(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", Object.class).instanceSupplier(() -> SampleFactory::new)"
						+ ".customize((bd) -> bd.setSynthetic(true)).register(context);");
	}

	@Test
	void writeWithMultipleFlags() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(SampleFactory.class.getName(), "create").setSynthetic(true).setPrimary(true).getBeanDefinition();
		assertThat(generateCode(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
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
		assertThat(generateCode(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", Object.class).instanceSupplier(() -> SampleFactory::new)"
						+ ".customize((bd) -> bd.getConstructorArgumentValues().addIndexedArgumentValue(0, \"test\")).register(context);");
	}

	@Test
	void writeWithSeveralConstructorArguments() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(SampleFactory.class.getName(), "create").getBeanDefinition();
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, new RuntimeBeanReference("test"));
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(2, 42);
		assertThat(generateCode(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", Object.class).instanceSupplier(() -> SampleFactory::new)"
								+ ".customize((bd) -> {",
						"  ConstructorArgumentValues argumentValues = bd.getConstructorArgumentValues();",
						"  argumentValues.addIndexedArgumentValue(0, new RuntimeBeanReference(\"test\"));",
						"  argumentValues.addIndexedArgumentValue(2, 42);",
						"}).register(context);");
	}

	@Test
	void writeBeanDefinitionRegisterReflectionEntriesForInstanceCreator() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(new BeanInstanceDescriptor(
				InjectionComponent.class, instanceCreator), (code) -> code.add("test"));
		BootstrapWriterContext context = createBootstrapContext();
		new DefaultBeanRegistrationGenerator("test", BeanDefinitionBuilder.rootBeanDefinition(InjectionComponent.class).getBeanDefinition(),
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
		SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(new BeanInstanceDescriptor(
				ResolvableType.forClass(InjectionComponent.class), instanceCreator, Collections.singletonList(
				new MemberDescriptor<>(injectionPoint, false))), (code) -> code.add("test"));
		BootstrapWriterContext context = createBootstrapContext();
		new DefaultBeanRegistrationGenerator("test", BeanDefinitionBuilder.rootBeanDefinition(InjectionComponent.class).getBeanDefinition(),
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
		SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(new BeanInstanceDescriptor(
				ResolvableType.forClass(InjectionComponent.class), instanceCreator, Collections.singletonList(
				new MemberDescriptor<>(injectionPoint, false))), (code) -> code.add("test"));
		BootstrapWriterContext context = createBootstrapContext();
		new DefaultBeanRegistrationGenerator("test", BeanDefinitionBuilder.rootBeanDefinition(InjectionComponent.class).getBeanDefinition(),
				beanValueWriter).writeBeanRegistration(context, CodeBlock.builder());
		assertThat(context.getRuntimeReflectionRegistry().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionComponent.class);
			assertThat(entry.getMethods()).containsOnly(instanceCreator);
			assertThat(entry.getFields()).containsOnly(injectionPoint);
		});
	}

	private static BootstrapWriterContext createBootstrapContext() {
		return new BootstrapWriterContext(BootstrapClass.of(ClassName.get("com.example", "Test")));
	}

	private DefaultBeanRegistrationGenerator createInstance(BeanDefinition beanDefinition) {
		DefaultBeanValueWriterSupplier supplier = new DefaultBeanValueWriterSupplier();
		supplier.setBeanFactory(new DefaultListableBeanFactory());
		return new DefaultBeanRegistrationGenerator("test", beanDefinition, supplier.get(beanDefinition));
	}

	private CodeSnippet generateCode(BeanDefinition beanDefinition, Consumer<Builder> instanceSupplier) {
		return CodeSnippet.of((code) -> {
			SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(new BeanInstanceDescriptor(
					beanDefinition.getResolvableType().toClass(), null), instanceSupplier);
			new DefaultBeanRegistrationGenerator("test", beanDefinition, beanValueWriter).writeBeanRegistration(code);
		});
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

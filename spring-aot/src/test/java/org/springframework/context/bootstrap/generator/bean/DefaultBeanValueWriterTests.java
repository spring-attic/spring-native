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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;

import com.squareup.javapoet.CodeBlock;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.sample.InnerComponentConfiguration.EnvironmentAwareComponent;
import org.springframework.context.bootstrap.generator.sample.InnerComponentConfiguration.NoDependencyComponent;
import org.springframework.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.context.bootstrap.generator.sample.callback.ImportAwareConfiguration;
import org.springframework.context.bootstrap.generator.sample.factory.SampleFactory;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionComponent;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionConfiguration;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link DefaultBeanValueWriter}.
 *
 * @author Stephane Nicoll
 */
class DefaultBeanValueWriterTests {

	@Test
	void writeWithNoInstanceCreator() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(NoDependencyComponent.class.getName()).getBeanDefinition();
		assertThatIllegalStateException().isThrownBy(() -> generateCode(beanDefinition, (Executable) null))
				.withMessageContaining("no instance creator available");
	}

	@Test
	void writeConstructorWithNoParameterUseShortcut() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleConfiguration.class).getBeanDefinition();
		assertThat(generateCode(beanDefinition, SimpleConfiguration.class.getDeclaredConstructors()[0]))
				.isEqualTo("() -> new SimpleConfiguration()");
	}

	@Test
	void writeConstructorWithParameter() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InjectionComponent.class.getName()).getBeanDefinition();
		assertThat(generateCode(beanDefinition, InjectionComponent.class.getDeclaredConstructors()[0])).lines().containsOnly(
				"(instanceContext) -> instanceContext.create(context, (attributes) -> new InjectionComponent(attributes.get(0)))");
	}

	@Test
	void writeConstructorWithInnerClassAndNoExtraArg() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(NoDependencyComponent.class.getName()).getBeanDefinition();
		assertThat(generateCode(beanDefinition, NoDependencyComponent.class.getDeclaredConstructors()[0])).lines().containsOnly(
				"() -> context.getBean(InnerComponentConfiguration.class).new NoDependencyComponent()");
	}

	@Test
	void writeConstructorWithInnerClassAndExtraArg() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(EnvironmentAwareComponent.class.getName()).getBeanDefinition();
		assertThat(generateCode(beanDefinition, EnvironmentAwareComponent.class.getDeclaredConstructors()[0])).lines().containsOnly(
				"(instanceContext) -> instanceContext.create(context, (attributes) -> context.getBean(InnerComponentConfiguration.class).new EnvironmentAwareComponent(attributes.get(1)))");
	}

	@Test
	void writeConstructorWithInstanceCallback() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ImportAwareConfiguration.class).getBeanDefinition();
		assertThat(generateCode(beanDefinition, (type) -> BeanInstanceDescriptor.of(type)
				.withInstanceCreator(ImportAwareConfiguration.class.getDeclaredConstructors()[0])
				.withInstanceCallback((name) -> CodeBlock.of("$L.setImportMetadata(metadata)", name)).build()))
				.lines().containsExactly("(instanceContext) -> {",
				"  ImportAwareConfiguration bean = new ImportAwareConfiguration();",
				"  bean.setImportMetadata(metadata);",
				"  return bean;",
				"}");
	}

	@Test
	void writeConstructorWithInstanceCallbacks() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ImportAwareConfiguration.class).getBeanDefinition();
		assertThat(generateCode(beanDefinition, (type) -> BeanInstanceDescriptor.of(type)
				.withInstanceCreator(ImportAwareConfiguration.class.getDeclaredConstructors()[0])
				.withInstanceCallback((name) -> CodeBlock.of("$L.setImportMetadata(metadata)", name))
				.withInstanceCallback((name) -> CodeBlock.of("$L.setImportMetadata(anotherMetadata)", name)).build()))
				.lines().containsExactly("(instanceContext) -> {",
				"  ImportAwareConfiguration bean = new ImportAwareConfiguration();",
				"  bean.setImportMetadata(metadata);",
				"  bean.setImportMetadata(anotherMetadata);",
				"  return bean;",
				"}");
	}

	@Test
	void writeConstructorWithInjectionPoints() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InjectionConfiguration.class).getBeanDefinition();
		Constructor<?> creator = InjectionConfiguration.class.getDeclaredConstructors()[0];
		assertThat(generateCode(beanDefinition, creator,
				new MemberDescriptor<>(ReflectionUtils.findMethod(InjectionConfiguration.class, "setEnvironment", Environment.class), true),
				new MemberDescriptor<>(ReflectionUtils.findMethod(InjectionConfiguration.class, "setBean", String.class), false))
		).lines().containsOnly(
				"(instanceContext) -> {",
				"  InjectionConfiguration bean = new InjectionConfiguration();",
				"  instanceContext.method(\"setEnvironment\", Environment.class)",
				"      .invoke(context, (attributes) -> bean.setEnvironment(attributes.get(0)));",
				"  instanceContext.method(\"setBean\", String.class)",
				"      .resolve(context, false).ifResolved((attributes) -> bean.setBean(attributes.get(0)));",
				"  return bean;",
				"}");

	}

	@Test
	void writeConstructorWithInstanceCallbackAndInjectionPoint() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ImportAwareConfiguration.class).getBeanDefinition();
		assertThat(generateCode(beanDefinition, (type) -> BeanInstanceDescriptor.of(type)
				.withInstanceCreator(ImportAwareConfiguration.class.getDeclaredConstructors()[0])
				.withInjectionPoint(ReflectionUtils.findMethod(ImportAwareConfiguration.class, "setEnvironment", Environment.class), true)
				.withInstanceCallback((name) -> CodeBlock.of("$L.setImportMetadata(metadata)", name)).build())).lines().containsOnly(
				"(instanceContext) -> {",
				"  ImportAwareConfiguration bean = new ImportAwareConfiguration();",
				"  bean.setImportMetadata(metadata);",
				"  instanceContext.method(\"setEnvironment\", Environment.class)",
				"      .invoke(context, (attributes) -> bean.setEnvironment(attributes.get(0)));",
				"  return bean;",
				"}");
	}

	@Test
	void writeMethodWithNoArgUseShortcut() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition();
		assertThat(generateCode(beanDefinition, ReflectionUtils.findMethod(SimpleConfiguration.class, "stringBean")))
				.isEqualTo("() -> context.getBean(SimpleConfiguration.class).stringBean()");
	}

	@Test
	void writeStaticMethodWithNoArgUseShortcut() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(Integer.class).getBeanDefinition();
		assertThat(generateCode(beanDefinition, ReflectionUtils.findMethod(SampleFactory.class, "integerBean")))
				.isEqualTo("() -> SampleFactory.integerBean()");
	}

	@Test
	void writeMethodWithInjectionPoint() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InjectionComponent.class).getBeanDefinition();
		Method creator = ReflectionUtils.findMethod(InjectionConfiguration.class, "injectionComponent");
		assertThat(generateCode(beanDefinition, creator,
				new MemberDescriptor<>(ReflectionUtils.findMethod(InjectionComponent.class, "setCounter", Integer.class), false))
		).lines().containsOnly(
				"(instanceContext) -> {",
				"  InjectionComponent bean = context.getBean(InjectionConfiguration.class).injectionComponent();",
				"  instanceContext.method(\"setCounter\", Integer.class)",
				"      .resolve(context, false).ifResolved((attributes) -> bean.setCounter(attributes.get(0)));",
				"  return bean;",
				"}");
	}

	@Test
	void writeParameterWithNoDependency() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SimpleConfiguration.class)
				.setFactoryMethod("integerBean").getBeanDefinition();
		Method method = ReflectionUtils.findMethod(SimpleConfiguration.class, "integerBean");
		assertThat(generateCode(beanDefinition, method)).isEqualTo(
				"() -> context.getBean(SimpleConfiguration.class).integerBean()");
	}


	private CodeSnippet generateCode(BeanDefinition beanDefinition, Executable executable, MemberDescriptor<?>... injectionPoints) {
		return generateCode(beanDefinition, (resolvableType) -> BeanInstanceDescriptor.of(resolvableType)
				.withInstanceCreator(executable).withInjectionPoints(Arrays.asList(injectionPoints)).build());
	}

	private CodeSnippet generateCode(BeanDefinition beanDefinition, Function<ResolvableType,
			BeanInstanceDescriptor> descriptorFactory) {
		return CodeSnippet.of((code) -> {
			GenericApplicationContext context = new GenericApplicationContext();
			context.registerBeanDefinition("test", beanDefinition);
			context.getBeanFactory().getType("test");
			BeanDefinition resolvedBeanDefinition = context.getBeanFactory().getMergedBeanDefinition("test");
			BeanInstanceDescriptor descriptor = descriptorFactory.apply(resolvedBeanDefinition.getResolvableType());
			new DefaultBeanValueWriter(descriptor, resolvedBeanDefinition).writeValueSupplier(code);
		});
	}

}

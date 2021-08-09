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
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.function.Consumer;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.bootstrap.generator.BootstrapClass;
import org.springframework.context.bootstrap.generator.BootstrapWriterContext;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.sample.factory.SampleFactory;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionComponent;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultBeanRegistrationGenerator}.
 *
 * @author Stephane Nicoll
 */
class DefaultBeanRegistrationGeneratorTests {

	@Test
	void writeWithSyntheticFlag() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(SampleFactory.class.getName(), "create").setSynthetic(true).getBeanDefinition();
		assertThat(generateCode(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", Object.class).instanceSupplier(() -> SampleFactory::new)"
						+ ".customize((builder) -> builder.setSynthetic(true)).register(context);");
	}

	@Test
	void writeBeanDefinitionRegisterReflectionEntriesForInstanceCreator() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(new BeanInstanceDescriptor(
				InjectionComponent.class, instanceCreator), (code) -> code.add("test"));
		BootstrapWriterContext context = new BootstrapWriterContext(new BootstrapClass("com.example", "Test"));
		new DefaultBeanRegistrationGenerator("test", BeanDefinitionBuilder.rootBeanDefinition(InjectionComponent.class).getBeanDefinition(),
				beanValueWriter).writeBeanRegistration(context, CodeBlock.builder());
		assertThat(context.getRuntimeReflectionRegistry().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionComponent.class);
			assertThat(entry.getMethods()).containsOnly(instanceCreator);
			assertThat(entry.getFields()).isEmpty();
		});
	}

	@Test
	void writeBeanDefinitionRegisterReflectionEntriesForInjectionPoints() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		Method injectionPoint = ReflectionUtils.findMethod(InjectionComponent.class, "setCounter", Integer.class);
		SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(new BeanInstanceDescriptor(
				InjectionComponent.class, instanceCreator, Collections.singletonList(new MemberDescriptor<>(injectionPoint, false))), (code) -> code.add("test"));
		BootstrapWriterContext context = new BootstrapWriterContext(new BootstrapClass("com.example", "Test"));
		new DefaultBeanRegistrationGenerator("test", BeanDefinitionBuilder.rootBeanDefinition(InjectionComponent.class).getBeanDefinition(),
				beanValueWriter).writeBeanRegistration(context, CodeBlock.builder());
		assertThat(context.getRuntimeReflectionRegistry().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionComponent.class);
			assertThat(entry.getMethods()).containsOnly(instanceCreator, injectionPoint);
			assertThat(entry.getFields()).isEmpty();
		});
	}

	private CodeSnippet generateCode(BeanDefinition beanDefinition, Consumer<Builder> instanceSupplier) {
		return CodeSnippet.of((code) -> {
			SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(new BeanInstanceDescriptor(
					beanDefinition.getResolvableType().toClass(), null), instanceSupplier);
			new DefaultBeanRegistrationGenerator("test", beanDefinition, beanValueWriter).writeBeanRegistration(code);
		});
	}

}

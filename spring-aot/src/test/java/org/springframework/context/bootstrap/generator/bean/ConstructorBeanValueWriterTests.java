package org.springframework.context.bootstrap.generator.bean;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.bootstrap.generator.sample.InnerComponentConfiguration.EnvironmentAwareComponent;
import org.springframework.context.bootstrap.generator.sample.InnerComponentConfiguration.NoDependencyComponent;
import org.springframework.context.bootstrap.generator.sample.generic.GenericWildcardComponent;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConstructorBeanValueWriter}.
 *
 * @author Stephane Nicoll
 */
class ConstructorBeanValueWriterTests {

	@Test
	void writeConstructorWithGenericWildcard() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(GenericWildcardComponent.class.getName())
				.getBeanDefinition();
		assertThat(generateCode(beanDefinition, GenericWildcardComponent.class.getDeclaredConstructors()[0])).lines().containsOnly("() -> {",
				"  ObjectProvider<Repository<?>> repositoryProvider = context.getBeanProvider(ResolvableType.forClassWithGenerics(Repository.class, Object.class));",
				"  return new GenericWildcardComponent(repositoryProvider.getObject());",
				"}");
	}

	@Test
	void writeConstructorWithInnerClassAndNoExtraArg() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(NoDependencyComponent.class.getName()).getBeanDefinition();
		assertThat(generateCode(beanDefinition, NoDependencyComponent.class.getDeclaredConstructors()[0])).lines().containsOnly(
				"() -> context.getBean(InnerComponentConfiguration.class).new NoDependencyComponent()");
	}

	@Test
	void writeConstructorWithInnerClassAndxtraArg() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(EnvironmentAwareComponent.class.getName()).getBeanDefinition();
		assertThat(generateCode(beanDefinition, EnvironmentAwareComponent.class.getDeclaredConstructors()[0])).lines().containsOnly(
				"() -> context.getBean(InnerComponentConfiguration.class).new EnvironmentAwareComponent(context.getEnvironment())");
	}

	private CodeSnippet generateCode(BeanDefinition beanDefinition, Constructor constructor) {
		return CodeSnippet.of((code) ->
				new ConstructorBeanValueWriter(beanDefinition, getClass().getClassLoader(), constructor).writeValueSupplier(code));
	}

}

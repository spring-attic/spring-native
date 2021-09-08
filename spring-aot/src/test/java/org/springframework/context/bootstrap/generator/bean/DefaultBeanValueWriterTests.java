package org.springframework.context.bootstrap.generator.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.sample.InnerComponentConfiguration.EnvironmentAwareComponent;
import org.springframework.context.bootstrap.generator.sample.InnerComponentConfiguration.NoDependencyComponent;
import org.springframework.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.context.bootstrap.generator.sample.factory.SampleFactory;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionComponent;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionConfiguration;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.context.support.GenericApplicationContext;
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
		assertThatIllegalStateException().isThrownBy(() -> generateCode(beanDefinition, null))
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
		return CodeSnippet.of((code) -> {
			GenericApplicationContext context = new GenericApplicationContext();
			context.registerBeanDefinition("test", beanDefinition);
			context.getBeanFactory().getType("test");
			BeanDefinition resolvedBeanDefinition = context.getBeanFactory().getMergedBeanDefinition("test");
			BeanInstanceDescriptor descriptor = BeanInstanceDescriptor
					.of(resolvedBeanDefinition.getResolvableType())
					.withInstanceCreator(executable)
					.withInjectionPoints(Arrays.asList(injectionPoints)).build();
			new DefaultBeanValueWriter(descriptor, resolvedBeanDefinition).writeValueSupplier(code);
		});
	}

}

package org.springframework.context.bootstrap.generator.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.sample.InnerComponentConfiguration.EnvironmentAwareComponent;
import org.springframework.context.bootstrap.generator.sample.InnerComponentConfiguration.NoDependencyComponent;
import org.springframework.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.context.bootstrap.generator.sample.dependency.DependencyConfiguration;
import org.springframework.context.bootstrap.generator.sample.dependency.GenericDependencyConfiguration;
import org.springframework.context.bootstrap.generator.sample.factory.SampleFactory;
import org.springframework.context.bootstrap.generator.sample.generic.GenericWildcardComponent;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionComponent;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionConfiguration;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultBeanValueWriter}.
 *
 * @author Stephane Nicoll
 */
class DefaultBeanValueWriterTests {

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
	void writeConstructorWithInnerClassAndExtraArg() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(EnvironmentAwareComponent.class.getName()).getBeanDefinition();
		assertThat(generateCode(beanDefinition, EnvironmentAwareComponent.class.getDeclaredConstructors()[0])).lines().containsOnly(
				"() -> context.getBean(InnerComponentConfiguration.class).new EnvironmentAwareComponent(context.getEnvironment())");
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

	@Test
	void writeParameterWithDependencyOnEnvironment() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(DependencyConfiguration.class)
				.setFactoryMethod("injectEnvironment").getBeanDefinition();
		Method method = ReflectionUtils.findMethod(DependencyConfiguration.class, "injectEnvironment", Environment.class);
		assertThat(generateCode(beanDefinition, method)).isEqualTo(
				"() -> context.getBean(DependencyConfiguration.class).injectEnvironment(context.getEnvironment())");
	}

	@Test
	void writeParameterWithDependencyOnApplicationContext() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(DependencyConfiguration.class)
				.setFactoryMethod("injectContext").getBeanDefinition();
		Method method = ReflectionUtils.findMethod(DependencyConfiguration.class, "injectContext", ConfigurableApplicationContext.class);
		assertThat(generateCode(beanDefinition, method)).isEqualTo(
				"() -> context.getBean(DependencyConfiguration.class).injectContext(context)");
	}

	@Test
	void writeParameterWithDependencyOnBeanFactory() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(DependencyConfiguration.class)
				.setFactoryMethod("injectBeanFactory").getBeanDefinition();
		Method method = ReflectionUtils.findMethod(DependencyConfiguration.class, "injectBeanFactory", AutowireCapableBeanFactory.class);
		assertThat(generateCode(beanDefinition, method)).isEqualTo(
				"() -> context.getBean(DependencyConfiguration.class).injectBeanFactory(context.getBeanFactory())");
	}

	@Test
	void writeParameterWithDependencyOnObjectProvider() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(DependencyConfiguration.class)
				.setFactoryMethod("injectObjectProvider").getBeanDefinition();
		Method method = ReflectionUtils.findMethod(DependencyConfiguration.class, "injectObjectProvider", ObjectProvider.class);
		assertThat(generateCode(beanDefinition, method)).isEqualTo(
				"() -> context.getBean(DependencyConfiguration.class).injectObjectProvider("
						+ "context.getBeanProvider(ResolvableType.forClassWithGenerics(Repository.class, Integer.class)))");
	}

	@Test
	void writeParameterWithList() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(DependencyConfiguration.class.getName()).setFactoryMethod("injectList")
				.getBeanDefinition();
		Method method = ReflectionUtils.findMethod(DependencyConfiguration.class, "injectList", List.class);
		assertThat(generateCode(beanDefinition, method)).containsSequence(
				"() -> context.getBean(DependencyConfiguration.class)",
				".injectList(context.getBeanProvider(String.class).orderedStream().collect(Collectors.toList()))");
	}

	@Test
	void writeParameterWithSet() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(DependencyConfiguration.class.getName()).setFactoryMethod("injectSet")
				.getBeanDefinition();
		Method method = ReflectionUtils.findMethod(DependencyConfiguration.class, "injectSet", Set.class);
		assertThat(generateCode(beanDefinition, method)).containsSequence(
				"() -> context.getBean(DependencyConfiguration.class)",
				".injectSet(context.getBeanProvider(String.class).orderedStream().collect(Collectors.toSet()))");
	}

	@Test
	void writeParameterWithRuntimeBeanReference() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class.getName())
				.setFactoryMethod("create").addConstructorArgReference("testBean").getBeanDefinition();
		Method method = ReflectionUtils.findMethod(SampleFactory.class, "create", String.class);
		assertThat(generateCode(beanDefinition, method)).isEqualTo(
				"() -> SampleFactory.create(context.getBean(\"testBean\", String.class))");
	}

	@Test
	void writeParameterWithCharacterReferenceEscapeSpecialChar() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class.getName())
				.setFactoryMethod("create").addConstructorArgValue('\\').getBeanDefinition();
		Method method = ReflectionUtils.findMethod(SampleFactory.class, "create", char.class);
		assertThat(generateCode(beanDefinition, method)).isEqualTo(
				"() -> SampleFactory.create('\\\\')");
	}

	@Test
	void writeParameterWithClassAsString() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class.getName())
				.setFactoryMethod("create").addConstructorArgValue("java.lang.String").getBeanDefinition();
		Method method = ReflectionUtils.findMethod(SampleFactory.class, "create", Class.class);
		assertThat(generateCode(beanDefinition, method)).isEqualTo(
				"() -> SampleFactory.create(String.class)");
	}

	@Test
	void writeParameterWithWildcard() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(GenericDependencyConfiguration.class.getName())
				.setFactoryMethod("injectWildcard").getBeanDefinition();
		Method method = ReflectionUtils.findMethod(GenericDependencyConfiguration.class, "injectWildcard", Predicate.class);
		assertThat(generateCode(beanDefinition, method)).lines().containsOnly(
				"() -> {",
				"  ObjectProvider<Predicate<?>> predicateProvider = context.getBeanProvider(ResolvableType.forClassWithGenerics(Predicate.class, Object.class));",
				"  return context.getBean(GenericDependencyConfiguration.class).injectWildcard(predicateProvider.getObject());",
				"}");
	}

	@Test
	void writeParameterWithListOfGeneric() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(GenericDependencyConfiguration.class.getName())
				.setFactoryMethod("injectWildcardCollection").getBeanDefinition();
		Method method = ReflectionUtils.findMethod(GenericDependencyConfiguration.class, "injectWildcardCollection", Collection.class);
		assertThat(generateCode(beanDefinition, method)).lines().containsOnly(
				"() -> {",
				"  ObjectProvider<Predicate<?>> collectionPredicateProvider = context.getBeanProvider(ResolvableType.forClassWithGenerics(Predicate.class, Object.class));",
				"  return context.getBean(GenericDependencyConfiguration.class).injectWildcardCollection(collectionPredicateProvider.orderedStream().collect(Collectors.toList()));",
				"}");
	}


	private CodeSnippet generateCode(BeanDefinition beanDefinition, Executable executable, MemberDescriptor<?>... injectionPoints) {
		return CodeSnippet.of((code) -> {
			GenericApplicationContext context = new GenericApplicationContext();
			context.registerBeanDefinition("test", beanDefinition);
			context.getBeanFactory().getType("test");
			BeanDefinition resolvedBeanDefinition = context.getBeanFactory().getMergedBeanDefinition("test");
			BeanInstanceDescriptor descriptor = new BeanInstanceDescriptor(resolvedBeanDefinition.getResolvableType().toClass(),
					executable, Arrays.asList(injectionPoints));
			new DefaultBeanValueWriter(descriptor, resolvedBeanDefinition, getClass().getClassLoader()).writeValueSupplier(code);
		});
	}

}

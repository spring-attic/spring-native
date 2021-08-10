package org.springframework.aot.beans.factory;

import java.lang.reflect.Constructor;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link InjectedConstructorResolver}.
 *
 * @author Stephane Nicoll
 */
class InjectedConstructorResolverTests {

	@Test
	void resolveNoArgConstructor() {
		GenericApplicationContext context = new GenericApplicationContext();
		assertAttributes(context, createResolver(InjectedConstructorResolverTests.class),
				(attributes) -> assertThat(attributes.isResolved()).isTrue());
	}

	@Test
	void resolveSingleArgConstructor() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean("one", String.class, () -> "1");
		assertAttributes(context, createResolver(SingleArgConstructor.class, String.class), (attributes) -> {
			assertThat(attributes.isResolved()).isTrue();
			assertThat((String) attributes.get(0)).isEqualTo("1");
		});
	}

	@Test
	void resolveRequiredDependencyNotPresentThrowsUnsatisfiedDependencyException() {
		Constructor<?> constructor = SingleArgConstructor.class.getDeclaredConstructors()[0];
		GenericApplicationContext context = new GenericApplicationContext();
		assertThatThrownBy(() -> createResolver(SingleArgConstructor.class, String.class).resolve(context))
				.isInstanceOfSatisfying(UnsatisfiedDependencyException.class, (ex) -> {
					assertThat(ex.getBeanName()).isEqualTo("test");
					assertThat(ex.getInjectionPoint()).isNotNull();
					assertThat(ex.getInjectionPoint().getMember()).isEqualTo(constructor);
				});
	}

	@Test
	void resolveMultiArgsConstructor() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean("one", String.class, () -> "1");
		assertAttributes(context, createResolver(MultiArgConstructor.class,
				ResourceLoader.class, Environment.class, ObjectProvider.class), (attributes) -> {
			assertThat(attributes.isResolved()).isTrue();
			assertThat((ResourceLoader) attributes.get(0)).isEqualTo(context);
			assertThat((Environment) attributes.get(1)).isEqualTo(context.getEnvironment());
			ObjectProvider<String> provider = attributes.get(2);
			assertThat(provider.getIfAvailable()).isEqualTo("1");
		});
	}

	@Test
	void resolveMixedArgsConstructorWithUserValue() {
		GenericApplicationContext context = new GenericApplicationContext();
		AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(MixedArgConstructor.class)
				.setAutowireMode(RootBeanDefinition.AUTOWIRE_CONSTRUCTOR).getBeanDefinition();
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(1, "user-value");
		context.registerBeanDefinition("test", beanDefinition);
		assertAttributes(context, createResolver(MixedArgConstructor.class,
				ApplicationContext.class, String.class, Environment.class), (attributes) -> {
			assertThat(attributes.isResolved()).isTrue();
			assertThat((ApplicationContext) attributes.get(0)).isEqualTo(context);
			assertThat((String) attributes.get(1)).isEqualTo("user-value");
			assertThat((Environment) attributes.get(2)).isEqualTo(context.getEnvironment());
		});
	}

	@Test
	void resolveMixedArgsConstructorWithUserBeanReference() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean("one", String.class, "1");
		context.registerBean("two", String.class, "2");
		AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(MixedArgConstructor.class)
				.setAutowireMode(RootBeanDefinition.AUTOWIRE_CONSTRUCTOR).getBeanDefinition();
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(1, new RuntimeBeanReference("two"));
		context.registerBeanDefinition("test", beanDefinition);
		assertAttributes(context, createResolver(MixedArgConstructor.class,
				ApplicationContext.class, String.class, Environment.class), (attributes) -> {
			assertThat(attributes.isResolved()).isTrue();
			assertThat((ApplicationContext) attributes.get(0)).isEqualTo(context);
			assertThat((String) attributes.get(1)).isEqualTo("2");
			assertThat((Environment) attributes.get(2)).isEqualTo(context.getEnvironment());
		});
	}

	@Test
	void resolveQualifiedDependency() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.getDefaultListableBeanFactory().setAutowireCandidateResolver(
				new ContextAnnotationAutowireCandidateResolver());
		context.registerBean("one", String.class, () -> "1");
		context.registerBean("two", String.class, () -> "2");
		assertAttributes(context, createResolver(QualifiedDependency.class, String.class), (attributes) -> {
			assertThat(attributes.isResolved()).isTrue();
			assertThat((String) attributes.get(0)).isEqualTo("2");
		});
	}

	@Test
	void createInvokeFactory() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean("one", String.class, () -> "1");
		String instance = createResolver(SingleArgConstructor.class, String.class)
				.create(context, (attributes) -> attributes.get(0));
		assertThat(instance).isEqualTo("1");
	}

	private InjectedConstructorResolver createResolver(Class<?> beanType, Class<?>... parameterTypes) {
		try {
			Constructor<?> constructor = beanType.getDeclaredConstructor(parameterTypes);
			return new InjectedConstructorResolver("test", beanType, constructor);
		}
		catch (NoSuchMethodException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private void assertAttributes(GenericApplicationContext context, InjectedConstructorResolver resolver,
			Consumer<InjectedElementAttributes> attributes) {
		try (context) {
			if (!context.isRunning()) {
				context.refresh();
			}
			attributes.accept(resolver.resolve(context));
		}
	}

	@SuppressWarnings("unused")
	static class SingleArgConstructor {

		public SingleArgConstructor(String s) {
		}

	}

	@SuppressWarnings("unused")
	static class MultiArgConstructor {

		public MultiArgConstructor(ResourceLoader resourceLoader, Environment environment, ObjectProvider<String> provider) {
		}
	}

	@SuppressWarnings("unused")
	static class MixedArgConstructor {

		public MixedArgConstructor(ApplicationContext context, String test, Environment environment) {

		}

	}

	@SuppressWarnings("unused")
	static class QualifiedDependency {

		QualifiedDependency(@Qualifier("two") String s) {
		}

	}

}

package org.springframework.aot.beans.factory;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link BeanDefinitionRegistrar}.
 *
 * @author Stephane Nicoll
 */
class BeanDefinitionRegistrarTests {

	@Test
	void registerWithSimpleInstanceSupplier() {
		GenericApplicationContext context = new GenericApplicationContext();
		BeanDefinitionRegistrar.of("test", InjectionSample.class)
				.instanceSupplier(InjectionSample::new).register(context);
		assertContext(context, () -> {
			assertThat(context.containsBean("test")).isTrue();
			assertThat(context.getBean(InjectionSample.class)).isNotNull();
		});
	}

	@Test
	void registerWithSimpleInstanceSupplierThatThrowsException() {
		GenericApplicationContext context = new GenericApplicationContext();
		BeanDefinitionRegistrar.of("testBean", InjectionSample.class)
				.instanceSupplier(() -> {
					throw new IOException("test exception");
				}).register(context);
		assertThatThrownBy(context::refresh).isInstanceOf(BeanCreationException.class)
				.hasMessageContaining("testBean").hasMessageContaining("test exception");
		;
	}

	@Test
	@SuppressWarnings("unchecked")
	void registerWithCustomizers() {
		GenericApplicationContext context = new GenericApplicationContext();
		SmartConsumer<RootBeanDefinition> first = mock(SmartConsumer.class);
		SmartConsumer<RootBeanDefinition> second = mock(SmartConsumer.class);
		BeanDefinitionRegistrar.of("test", InjectionSample.class)
				.instanceSupplier(InjectionSample::new).customize(first).customize(second).register(context);
		assertContext(context, () -> {
			assertThat(context.containsBean("test")).isTrue();
			InOrder ordered = inOrder(first, second);
			ordered.verify(first).accept(any(RootBeanDefinition.class));
			ordered.verify(second).accept(any(RootBeanDefinition.class));
		});
	}

	@Test
	void registerWithConstructorInstantiation() {
		GenericApplicationContext context = new GenericApplicationContext();
		BeanDefinitionRegistrar.of("test", ConstructorSample.class)
				.instanceSupplier((instanceContext) -> instanceContext.constructor(ResourceLoader.class)
						.create(context, (attributes) -> new ConstructorSample(attributes.get(0)))).register(context);
		assertContext(context, () -> {
			assertThat(context.containsBean("test")).isTrue();
			assertThat(context.getBean(ConstructorSample.class).resourceLoader).isEqualTo(context);
		});
	}

	@Test
	void registerWithInjectedConstructorAndConstructorArgs() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.getDefaultListableBeanFactory().setAutowireCandidateResolver(
				new ContextAnnotationAutowireCandidateResolver());
		context.registerBean("testBean", String.class, "test");
		context.registerBean("anotherBean", String.class, "another");
		BeanDefinitionRegistrar.of("test", MultiArgConstructorSample.class)
				.instanceSupplier((instanceContext) -> instanceContext.constructor(String.class, Integer.class)
						.create(context, (attributes) -> new MultiArgConstructorSample(attributes.get(0), attributes.get(1))))
				.customize((bd) -> {
					ConstructorArgumentValues constructorArgumentValues = bd.getConstructorArgumentValues();
					constructorArgumentValues.addIndexedArgumentValue(0, new RuntimeBeanReference("anotherBean"));
					constructorArgumentValues.addIndexedArgumentValue(1, 42);
				})
				.register(context);
		assertContext(context, () -> {
			assertThat(context.containsBean("test")).isTrue();
			MultiArgConstructorSample bean = context.getBean(MultiArgConstructorSample.class);
			assertThat(bean.name).isEqualTo("another");
			assertThat(bean.counter).isEqualTo(42);
		});
	}

	@Test
	void registerWithConstructorOnInnerClass() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(InnerClassSample.class);
		BeanDefinitionRegistrar.of("test", InnerClassSample.Inner.class)
				.instanceSupplier((instanceContext) -> instanceContext.constructor(InnerClassSample.class, Environment.class)
						.create(context, (attributes) -> context.getBean(InnerClassSample.class).new Inner(attributes.get(1))))
				.register(context);
		assertContext(context, () -> {
			assertThat(context.containsBean("test")).isTrue();
			InnerClassSample.Inner bean = context.getBean(InnerClassSample.Inner.class);
			assertThat(bean.environment).isEqualTo(context.getEnvironment());
		});
	}

	@Test
	void registerWithInvalidConstructor() {
		GenericApplicationContext context = new GenericApplicationContext();
		assertThatThrownBy(() -> {
					BeanDefinitionRegistrar.of("test", ConstructorSample.class)
							.instanceSupplier((instanceContext) -> instanceContext.constructor(Object.class).resolve(context)).register(context);
					context.refresh();
					context.getBean(ConstructorSample.class);
				}
		).isInstanceOf(BeanCreationException.class)
				.hasMessageContaining("No constructor with type(s) [java.lang.Object] found on")
				.hasMessageContaining(ConstructorSample.class.getName())
				.getCause().hasCauseInstanceOf(NoSuchMethodException.class);
	}

	@Test
	void registerWithMethodInstantiation() {
		GenericApplicationContext context = new GenericApplicationContext();
		BeanDefinitionRegistrar.of("configuration", ConfigurationSample.class).instanceSupplier(ConfigurationSample::new)
				.register(context);
		BeanDefinitionRegistrar.of("test", ConstructorSample.class)
				.instanceSupplier((instanceContext) -> instanceContext.method(ConfigurationSample.class, "sampleBean", ResourceLoader.class)
						.create(context, (attributes) -> context.getBean(ConfigurationSample.class).sampleBean(attributes.get(0)))).register(context);
		assertContext(context, () -> {
			assertThat(context.containsBean("configuration")).isTrue();
			assertThat(context.containsBean("test")).isTrue();
			assertThat(context.getBean(ConstructorSample.class).resourceLoader).isEqualTo(context);
		});
	}

	@Test
	void registerWithInjectedField() {
		GenericApplicationContext context = new GenericApplicationContext();
		BeanDefinitionRegistrar.of("test", InjectionSample.class).instanceSupplier((instanceContext) -> {
			InjectionSample bean = new InjectionSample();
			instanceContext.field("environment", Environment.class).invoke(context,
					(attributes) -> bean.environment = (attributes.get(0)));
			return bean;
		}).register(context);
		assertContext(context, () -> {
			assertThat(context.containsBean("test")).isTrue();
			assertThat(context.getBean(InjectionSample.class).environment).isEqualTo(context.getEnvironment());
		});
	}

	@Test
	void registerWithInvalidField() {
		GenericApplicationContext context = new GenericApplicationContext();
		assertThatThrownBy(() -> {
					BeanDefinitionRegistrar.of("test", InjectionSample.class).instanceSupplier((instanceContext) ->
							instanceContext.field("doesNotExist", Object.class).resolve(context)).register(context);
					context.refresh();
					context.getBean(InjectionSample.class);
				}
		).isInstanceOf(BeanCreationException.class)
				.hasMessageContaining("No field '%s' with type %s found", "doesNotExist", Object.class.getName())
				.hasMessageContaining(InjectionSample.class.getName());
	}

	@Test
	void registerWithInjectedMethod() {
		GenericApplicationContext context = new GenericApplicationContext();
		BeanDefinitionRegistrar.of("test", InjectionSample.class).instanceSupplier((instanceContext) -> {
			InjectionSample bean = new InjectionSample();
			instanceContext.method("setEnvironment", Environment.class).invoke(context,
					(attributes) -> bean.setEnvironment(attributes.get(0)));
			return bean;
		}).register(context);
		assertContext(context, () -> {
			assertThat(context.containsBean("test")).isTrue();
			assertThat(context.getBean(InjectionSample.class).environment).isEqualTo(context.getEnvironment());
		});
	}

	@Test
	void registerWithInvalidMethod() {
		GenericApplicationContext context = new GenericApplicationContext();
		assertThatThrownBy(() -> {
					BeanDefinitionRegistrar.of("test", InjectionSample.class).instanceSupplier((instanceContext) ->
							instanceContext.method("setEnvironment", Object.class).resolve(context)).register(context);
					context.refresh();
					context.getBean(ConstructorSample.class);
				}
		).isInstanceOf(BeanCreationException.class)
				.hasMessageContaining("No method '%s' with type(s) [%s] found", "setEnvironment", Object.class.getName())
				.hasMessageContaining(InjectionSample.class.getName());
	}

	@Test
	void registerWithInjectedMethodHandleAtValue() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.setEnvironment(new MockEnvironment().withProperty("test.counter", "12"));
		BeanDefinitionRegistrar.of("test", InjectionSample.class).instanceSupplier((instanceContext) -> {
			InjectionSample bean = new InjectionSample();
			instanceContext.method("setNameAndCounter", String.class, Integer.class).invoke(context,
					(attributes) -> bean.setNameAndCounter(attributes.get(0), attributes.get(1)));
			return bean;
		}).register(context);
		assertContext(context, () -> {
			assertThat(context.containsBean("test")).isTrue();
			InjectionSample bean = context.getBean(InjectionSample.class);
			assertThat(bean.name).isEqualTo("test");
			assertThat(bean.counter).isEqualTo(12);
		});
	}

	private void assertContext(GenericApplicationContext context, Runnable assertions) {
		context.getDefaultListableBeanFactory().setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
		if (!context.isRunning()) {
			context.refresh();
		}
		try (context) {
			assertions.run();
		}
	}

	static class ConfigurationSample {

		ConstructorSample sampleBean(ResourceLoader resourceLoader) {
			return new ConstructorSample(resourceLoader);
		}

	}

	static class ConstructorSample {
		private final ResourceLoader resourceLoader;

		ConstructorSample(ResourceLoader resourceLoader) {
			this.resourceLoader = resourceLoader;
		}
	}

	static class MultiArgConstructorSample {

		private final String name;

		private final Integer counter;

		public MultiArgConstructorSample(String name, Integer counter) {
			this.name = name;
			this.counter = counter;
		}

	}

	static class InjectionSample {

		private Environment environment;

		private String name;

		private Integer counter;

		void setEnvironment(Environment environment) {
			this.environment = environment;
		}

		void setNameAndCounter(@Value("${test.name:test}") String name, @Value("${test.counter:42}") Integer counter) {
			this.name = name;
			this.counter = counter;
		}

	}

	static class InnerClassSample {

		class Inner {

			private Environment environment;

			Inner(Environment environment) {
				this.environment = environment;
			}

		}

	}

}

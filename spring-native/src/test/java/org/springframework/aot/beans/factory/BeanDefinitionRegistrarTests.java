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

package org.springframework.aot.beans.factory;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import org.springframework.aot.beans.factory.BeanDefinitionRegistrar.BeanInstanceContext;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
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
	void beanDefinitionWithBeanClassDoesNotSetTargetType() {
		RootBeanDefinition beanDefinition = BeanDefinitionRegistrar.of("test", String.class).toBeanDefinition();
		assertThat(beanDefinition.getBeanClass()).isEqualTo(String.class);
		assertThat(beanDefinition.getTargetType()).isNull();
	}

	@Test
	void beanDefinitionWithResolvableTypeSetsTargetType() {
		ResolvableType targetType = ResolvableType.forClassWithGenerics(NumberHolder.class, Integer.class);
		RootBeanDefinition beanDefinition = BeanDefinitionRegistrar.of("test", targetType).toBeanDefinition();
		assertThat(beanDefinition.getTargetType()).isNotNull().isEqualTo(NumberHolder.class);
	}

	@Test
	void registerWithSimpleInstanceSupplier() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanDefinitionRegistrar.of("test", InjectionSample.class)
				.instanceSupplier(InjectionSample::new).register(beanFactory);
		assertBeanFactory(beanFactory, () -> {
			assertThat(beanFactory.containsBean("test")).isTrue();
			assertThat(beanFactory.getBean(InjectionSample.class)).isNotNull();
		});
	}

	@Test
	void registerWithSimpleInstanceSupplierThatThrowsRuntimeException() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		Exception exception = new IllegalArgumentException("test exception");
		BeanDefinitionRegistrar.of("testBean", InjectionSample.class)
				.instanceSupplier(() -> {
					throw exception;
				}).register(beanFactory);
		assertThatThrownBy(() -> beanFactory.getBean("testBean")).isInstanceOf(BeanCreationException.class)
				.getRootCause().isEqualTo(exception);
	}

	@Test
	void registerWithSimpleInstanceSupplierThatThrowsCheckedException() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		Exception exception = new IOException("test exception");
		BeanDefinitionRegistrar.of("testBean", InjectionSample.class)
				.instanceSupplier(() -> {
					throw exception;
				}).register(beanFactory);
		assertThatThrownBy(() -> beanFactory.getBean("testBean")).isInstanceOf(BeanCreationException.class)
				.getRootCause().isEqualTo(exception);
	}

	@Test
	void registerWithoutBeanNameFails() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanDefinitionRegistrar registrar = BeanDefinitionRegistrar.inner(InjectionSample.class)
				.instanceSupplier(InjectionSample::new);
		assertThatIllegalStateException().isThrownBy(() -> registrar.register(beanFactory))
				.withMessageContaining("Bean name not set.");
	}

	@Test
	@SuppressWarnings("unchecked")
	void registerWithCustomizer() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		ThrowableConsumer<RootBeanDefinition> first = mock(ThrowableConsumer.class);
		ThrowableConsumer<RootBeanDefinition> second = mock(ThrowableConsumer.class);
		BeanDefinitionRegistrar.of("test", InjectionSample.class)
				.instanceSupplier(InjectionSample::new).customize(first).customize(second).register(beanFactory);
		assertBeanFactory(beanFactory, () -> {
			assertThat(beanFactory.containsBean("test")).isTrue();
			InOrder ordered = inOrder(first, second);
			ordered.verify(first).accept(any(RootBeanDefinition.class));
			ordered.verify(second).accept(any(RootBeanDefinition.class));
		});
	}

	@Test
	void registerWithCustomizerThatThrowsRuntimeException() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		Exception exception = new RuntimeException("test exception");
		BeanDefinitionRegistrar registrar = BeanDefinitionRegistrar.of("test", InjectionSample.class)
				.instanceSupplier(InjectionSample::new).customize((bd) -> {
					throw exception;
				});
		assertThatThrownBy(() -> registrar.register(beanFactory)).isInstanceOf(FatalBeanException.class)
				.hasMessageContaining("Failed to create bean definition for bean with name 'test'")
				.hasMessageContaining("test exception")
				.hasCause(exception);
	}

	@Test
	void registerWithCustomizerThatThrowsCheckedException() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		Exception exception = new IOException("test exception");
		BeanDefinitionRegistrar registrar = BeanDefinitionRegistrar.of("test", InjectionSample.class)
				.instanceSupplier(InjectionSample::new).customize((bd) -> {
					throw exception;
				});
		assertThatThrownBy(() -> registrar.register(beanFactory)).isInstanceOf(FatalBeanException.class)
				.hasMessageContaining("Failed to create bean definition for bean with name 'test'")
				.hasMessageContaining("test exception");
	}

	@Test
	void registerWithConstructorInstantiation() {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerResolvableDependency(ResourceLoader.class, resourceLoader);
		BeanDefinitionRegistrar.of("test", ConstructorSample.class).withConstructor(ResourceLoader.class)
				.instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) ->
						new ConstructorSample(attributes.get(0)))).register(beanFactory);
		assertBeanFactory(beanFactory, () -> {
			assertThat(beanFactory.containsBean("test")).isTrue();
			assertThat(beanFactory.getBean(ConstructorSample.class).resourceLoader).isEqualTo(resourceLoader);
		});
	}

	@Test
	void registerWithConstructorInstantiationThatThrowsRuntimeException() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		Exception exception = new RuntimeException("test exception");
		BeanDefinitionRegistrar.of("test", ConstructorSample.class).withConstructor(ResourceLoader.class)
				.instanceSupplier((instanceContext) -> {
					throw exception;
				}).register(beanFactory);
		assertThatThrownBy(() -> beanFactory.getBean("test")).isInstanceOf(BeanCreationException.class)
				.getRootCause().isEqualTo(exception);
	}

	@Test
	void registerWithConstructorInstantiationThatThrowsCheckedException() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		Exception exception = new IOException("test exception");
		BeanDefinitionRegistrar.of("test", ConstructorSample.class).withConstructor(ResourceLoader.class)
				.instanceSupplier((instanceContext) -> {
					throw exception;
				}).register(beanFactory);
		assertThatThrownBy(() -> beanFactory.getBean("test")).isInstanceOf(BeanCreationException.class)
				.getRootCause().isEqualTo(exception);
	}

	@Test
	void registerWithInjectedConstructorAndConstructorArgs() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
		beanFactory.registerSingleton("testBean", "test");
		beanFactory.registerSingleton("anotherBean", "another");
		BeanDefinitionRegistrar.of("test", MultiArgConstructorSample.class).withConstructor(String.class, Integer.class)
				.instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) ->
						new MultiArgConstructorSample(attributes.get(0), attributes.get(1))))
				.customize((bd) -> {
					ConstructorArgumentValues constructorArgumentValues = bd.getConstructorArgumentValues();
					constructorArgumentValues.addIndexedArgumentValue(0, new RuntimeBeanReference("anotherBean"));
					constructorArgumentValues.addIndexedArgumentValue(1, 42);
				})
				.register(beanFactory);
		assertBeanFactory(beanFactory, () -> {
			assertThat(beanFactory.containsBean("test")).isTrue();
			MultiArgConstructorSample bean = beanFactory.getBean(MultiArgConstructorSample.class);
			assertThat(bean.name).isEqualTo("another");
			assertThat(bean.counter).isEqualTo(42);
		});
	}

	@Test
	void registerWithConstructorOnInnerClass() {
		MockEnvironment environment = new MockEnvironment();
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("environment", environment);
		beanFactory.registerBeanDefinition("sample", BeanDefinitionBuilder.rootBeanDefinition(InnerClassSample.class).getBeanDefinition());
		BeanDefinitionRegistrar.of("test", InnerClassSample.Inner.class).withConstructor(InnerClassSample.class, Environment.class)
				.instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) ->
						beanFactory.getBean(InnerClassSample.class).new Inner(attributes.get(1))))
				.register(beanFactory);
		assertBeanFactory(beanFactory, () -> {
			assertThat(beanFactory.containsBean("test")).isTrue();
			InnerClassSample.Inner bean = beanFactory.getBean(InnerClassSample.Inner.class);
			assertThat(bean.environment).isEqualTo(environment);
		});
	}

	@Test
	void registerWithInvalidConstructor() {
		assertThatThrownBy(() -> BeanDefinitionRegistrar.of("test", ConstructorSample.class).withConstructor(Object.class))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No constructor with type(s) [java.lang.Object] found on")
				.hasMessageContaining(ConstructorSample.class.getName());
	}

	@Test
	void registerWithFactoryMethod() {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerResolvableDependency(ResourceLoader.class, resourceLoader);
		BeanDefinitionRegistrar.of("configuration", ConfigurationSample.class).instanceSupplier(ConfigurationSample::new)
				.register(beanFactory);
		BeanDefinitionRegistrar.of("test", ConstructorSample.class)
				.withFactoryMethod(ConfigurationSample.class, "sampleBean", ResourceLoader.class)
				.instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes)
						-> beanFactory.getBean(ConfigurationSample.class).sampleBean(attributes.get(0))))
				.register(beanFactory);
		assertBeanFactory(beanFactory, () -> {
			assertThat(beanFactory.containsBean("configuration")).isTrue();
			assertThat(beanFactory.containsBean("test")).isTrue();
			assertThat(beanFactory.getBean(ConstructorSample.class).resourceLoader).isEqualTo(resourceLoader);
			RootBeanDefinition bd = (RootBeanDefinition) beanFactory.getBeanDefinition("test");
			assertThat(bd.getResolvedFactoryMethod()).isNotNull().isEqualTo(
					ReflectionUtils.findMethod(ConfigurationSample.class, "sampleBean", ResourceLoader.class));
		});
	}

	@Test
	void registerWithCreateShortcutWithoutFactoryMethod() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanDefinitionRegistrar.of("configuration", ConfigurationSample.class).instanceSupplier(ConfigurationSample::new)
				.register(beanFactory);
		BeanDefinitionRegistrar.of("test", ConstructorSample.class)
				.instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes)
						-> beanFactory.getBean(ConfigurationSample.class).sampleBean(attributes.get(0))))
				.register(beanFactory);
		assertThatThrownBy(() -> beanFactory.getBean("test")).isInstanceOf(BeanCreationException.class)
				.hasMessageContaining("No factory method or constructor is set");
	}

	@Test
	void registerWithInjectedField() {
		MockEnvironment environment = new MockEnvironment();
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("environment", environment);
		BeanDefinitionRegistrar.of("test", InjectionSample.class).instanceSupplier((instanceContext) -> {
			InjectionSample bean = new InjectionSample();
			instanceContext.field("environment", Environment.class).invoke(beanFactory,
					(attributes) -> bean.environment = (attributes.get(0)));
			return bean;
		}).register(beanFactory);
		assertBeanFactory(beanFactory, () -> {
			assertThat(beanFactory.containsBean("test")).isTrue();
			assertThat(beanFactory.getBean(InjectionSample.class).environment).isEqualTo(environment);
		});
	}

	@Test
	void registerWithInvalidField() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanDefinitionRegistrar.of("test", InjectionSample.class).instanceSupplier((instanceContext) ->
				instanceContext.field("doesNotExist", Object.class).resolve(beanFactory)).register(beanFactory);
		assertThatThrownBy(() -> beanFactory.getBean(InjectionSample.class)
		).isInstanceOf(BeanCreationException.class)
				.hasMessageContaining("No field '%s' with type %s found", "doesNotExist", Object.class.getName())
				.hasMessageContaining(InjectionSample.class.getName());
	}

	@Test
	void registerWithInjectedMethod() {
		MockEnvironment environment = new MockEnvironment();
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("environment", environment);
		BeanDefinitionRegistrar.of("test", InjectionSample.class).instanceSupplier((instanceContext) -> {
			InjectionSample bean = new InjectionSample();
			instanceContext.method("setEnvironment", Environment.class).invoke(beanFactory,
					(attributes) -> bean.setEnvironment(attributes.get(0)));
			return bean;
		}).register(beanFactory);
		assertBeanFactory(beanFactory, () -> {
			assertThat(beanFactory.containsBean("test")).isTrue();
			assertThat(beanFactory.getBean(InjectionSample.class).environment).isEqualTo(environment);
		});
	}

	@Test
	void registerWithInvalidMethod() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		assertThatThrownBy(() -> {
					BeanDefinitionRegistrar.of("test", InjectionSample.class).instanceSupplier((instanceContext) ->
							instanceContext.method("setEnvironment", Object.class).resolve(beanFactory)).register(beanFactory);
					beanFactory.getBean(InjectionSample.class);
				}
		).isInstanceOf(BeanCreationException.class)
				.hasMessageContaining("No method '%s' with type(s) [%s] found", "setEnvironment", Object.class.getName())
				.hasMessageContaining(InjectionSample.class.getName());
	}

	@Test
	void registerWithInjectedMethodHandleAtValue() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.setEnvironment(new MockEnvironment().withProperty("test.counter", "12"));
		DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
		BeanDefinitionRegistrar.of("test", InjectionSample.class).instanceSupplier((instanceContext) -> {
			InjectionSample bean = new InjectionSample();
			instanceContext.method("setNameAndCounter", String.class, Integer.class).invoke(beanFactory,
					(attributes) -> bean.setNameAndCounter(attributes.get(0), attributes.get(1)));
			return bean;
		}).register(beanFactory);
		assertContext(context, () -> {
			assertThat(context.containsBean("test")).isTrue();
			InjectionSample bean = context.getBean(InjectionSample.class);
			assertThat(bean.name).isEqualTo("test");
			assertThat(bean.counter).isEqualTo(12);
		});
	}

	@Test
	void innerBeanDefinitionWithClass() {
		RootBeanDefinition beanDefinition = BeanDefinitionRegistrar.inner(ConfigurationSample.class)
				.customize((bd) -> bd.setSynthetic(true)).toBeanDefinition();
		assertThat(beanDefinition).isNotNull();
		assertThat(beanDefinition.getResolvableType().resolve()).isEqualTo(ConfigurationSample.class);
		assertThat(beanDefinition.isSynthetic()).isTrue();
	}

	@Test
	void innerBeanDefinitionWithResolvableType() {
		RootBeanDefinition beanDefinition = BeanDefinitionRegistrar.inner(ResolvableType.forClass(ConfigurationSample.class))
				.customize((bd) -> bd.setDescription("test")).toBeanDefinition();
		assertThat(beanDefinition).isNotNull();
		assertThat(beanDefinition.getResolvableType().resolve()).isEqualTo(ConfigurationSample.class);
		assertThat(beanDefinition.getDescription()).isEqualTo("test");
	}

	@Test
	void innerBeanDefinitionHasInnerBeanNameInInstanceSupplier() {
		RootBeanDefinition beanDefinition = BeanDefinitionRegistrar.inner(String.class)
				.instanceSupplier((instanceContext) -> {
					Field field = ReflectionUtils.findField(BeanInstanceContext.class, "beanName", String.class);
					ReflectionUtils.makeAccessible(field);
					return ReflectionUtils.getField(field, instanceContext);
				}).toBeanDefinition();
		assertThat(beanDefinition).isNotNull();
		String beanName = (String) beanDefinition.getInstanceSupplier().get();
		assertThat(beanName).isNotNull().startsWith("(inner bean)#");
	}

	@Test
	void beanFactoryWithUnresolvedGenericCanBeInjected() {
		GenericApplicationContext context = new AnnotationConfigApplicationContext();
		// See https://github.com/spring-projects/spring-framework/issues/27727
		context.registerBean(NumberHolderSample.class);
		BeanDefinitionRegistrar.of("factory", FactoryBean.class)
				.withFactoryMethod(GenericFactoryBeanConfiguration.class, "integerHolderFactory")
				.instanceSupplier(() -> new GenericFactoryBeanConfiguration().integerHolderFactory())
				.register(context.getDefaultListableBeanFactory());
		assertContext(context, () -> {
			NumberHolder<Integer> numberHolder = context.getDefaultListableBeanFactory()
					.getBean(NumberHolderSample.class).numberHolder;
			assertThat(numberHolder).isNotNull();
			assertThat(numberHolder.number).isEqualTo(42);
		});
	}

	@Test
	void beanWithUnresolvedGenericCanBeInjected() {
		GenericApplicationContext context = new AnnotationConfigApplicationContext();
		// See https://github.com/spring-projects/spring-framework/issues/27727
		context.registerBean(NumberHolderSample.class);
		BeanDefinitionRegistrar.of("numberHolder", NumberHolder.class)
				.withFactoryMethod(GenericFactoryBeanConfiguration.class, "integerHolder")
				.instanceSupplier(() -> new GenericFactoryBeanConfiguration().integerHolder())
				.register(context.getDefaultListableBeanFactory());
		assertContext(context, () -> {
			NumberHolder<Integer> numberHolder = context.getDefaultListableBeanFactory()
					.getBean(NumberHolderSample.class).numberHolder;
			assertThat(numberHolder).isNotNull();
			assertThat(numberHolder.number).isEqualTo(42);
		});
	}

	private void assertBeanFactory(DefaultListableBeanFactory beanFactory, Runnable assertions) {
		beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
		assertions.run();
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

	static class GenericFactoryBeanConfiguration {

		FactoryBean<NumberHolder<?>> integerHolderFactory() {
			return new GenericFactoryBean<>(integerHolder());
		}

		NumberHolder<?> integerHolder() {
			return new NumberHolder<>(42);
		}

	}

	static class GenericFactoryBean<T> implements FactoryBean<T> {

		private final T value;

		public GenericFactoryBean(T value) {
			this.value = value;
		}

		@Override
		public T getObject() {
			return this.value;
		}

		@Override
		public Class<?> getObjectType() {
			return this.value.getClass();
		}
	}

	static class NumberHolder<N extends Number> {

		private final N number;

		public NumberHolder(N number) {
			this.number = number;
		}

	}

	static class NumberHolderSample {

		@Autowired
		private NumberHolder<Integer> numberHolder;

	}

}

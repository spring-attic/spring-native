package org.springframework.aot.beans.factory;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for {@link InjectedMethodResolver}.
 *
 * @author Stephane Nicoll
 */
class InjectedMethodResolverTests {

	@Test
	void resolveSingleDependency() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean("test", String.class, () -> "testValue");
		assertAttributes(context, createResolver(TestBean.class, "injectString", String.class), true, (attributes) -> {
			assertThat(attributes.isResolved()).isTrue();
			assertThat((String) attributes.get(0)).isEqualTo("testValue");
		});
	}

	@Test
	void resolveRequiredDependencyNotPresentThrowsUnsatisfiedDependencyException() {
		Method method = ReflectionUtils.findMethod(TestBean.class, "injectString", String.class);
		GenericApplicationContext context = new GenericApplicationContext();
		assertThatThrownBy(() -> createResolver(TestBean.class, "injectString", String.class).resolve(context))
				.isInstanceOfSatisfying(UnsatisfiedDependencyException.class, (ex) -> {
					assertThat(ex.getBeanName()).isEqualTo("test");
					assertThat(ex.getInjectionPoint()).isNotNull();
					assertThat(ex.getInjectionPoint().getMember()).isEqualTo(method);
				});
	}

	@Test
	void resolveNonRequiredDependency() {
		GenericApplicationContext context = new GenericApplicationContext();
		assertAttributes(context, createResolver(TestBean.class, "injectString", String.class), false,
				(attributes) -> assertThat(attributes.isResolved()).isFalse());
	}

	@Test
	void resolveDependencyAndEnvironment() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean("test", String.class, () -> "testValue");
		assertAttributes(context, createResolver(TestBean.class, "injectStringAndEnvironment", String.class, Environment.class), true, (attributes) -> {
			assertThat(attributes.isResolved()).isTrue();
			String string = attributes.get(0);
			assertThat(string).isEqualTo("testValue");
			Environment environment = attributes.get(1);
			assertThat(environment).isEqualTo(context.getEnvironment());
		});
	}

	@Test
	void resolveQualifiedDependency() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.getDefaultListableBeanFactory().setAutowireCandidateResolver(
				new ContextAnnotationAutowireCandidateResolver());
		context.registerBean("one", String.class, () -> "1");
		context.registerBean("two", String.class, () -> "2");
		assertAttributes(context, createResolver(TestBean.class, "injectQualifiedString", String.class), true, (attributes) -> {
			assertThat(attributes.isResolved()).isTrue();
			assertThat((String) attributes.get(0)).isEqualTo("2");
		});
	}

	@Test
	@SuppressWarnings("unchecked")
	void createWithUnresolvedAttributesDoesNotInvokeCallback() {
		GenericApplicationContext context = new GenericApplicationContext();
		SmartFunction<InjectedElementAttributes, ?> callback = mock(SmartFunction.class);
		assertThatExceptionOfType(UnsatisfiedDependencyException.class).isThrownBy(() -> createResolver(TestBean.class, "injectString", String.class)
				.create(context, callback));
		verifyNoInteractions(callback);
	}

	@Test
	@SuppressWarnings("unchecked")
	void invokeWithUnresolvedAttributesDoesNotInvokeCallback() {
		GenericApplicationContext context = new GenericApplicationContext();
		SmartConsumer<InjectedElementAttributes> callback = mock(SmartConsumer.class);
		assertThatExceptionOfType(UnsatisfiedDependencyException.class).isThrownBy(() -> createResolver(TestBean.class, "injectString", String.class)
				.invoke(context, callback));
		verifyNoInteractions(callback);
	}

	private InjectedMethodResolver createResolver(Class<?> beanType, String methodName, Class<?>... parameterTypes) {
		return new InjectedMethodResolver("test", beanType, ReflectionUtils.findMethod(beanType, methodName, parameterTypes));
	}

	private void assertAttributes(GenericApplicationContext context, InjectedMethodResolver resolver, boolean required,
			Consumer<InjectedElementAttributes> attributes) {
		try (context) {
			if (!context.isRunning()) {
				context.refresh();
			}
			attributes.accept(resolver.resolve(context, required));
		}
	}


	@SuppressWarnings("unused")
	static class TestBean {

		public void injectString(String string) {

		}

		public void injectStringAndEnvironment(String string, Environment environment) {

		}

		public void injectQualifiedString(@Qualifier("two") String string) {

		}

	}

}

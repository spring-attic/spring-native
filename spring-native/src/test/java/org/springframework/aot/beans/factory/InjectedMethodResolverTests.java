package org.springframework.aot.beans.factory;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
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
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("test", "testValue");
		InjectedElementAttributes attributes = createResolver(TestBean.class, "injectString", String.class).resolve(beanFactory, true);
		assertThat(attributes.isResolved()).isTrue();
		assertThat((String) attributes.get(0)).isEqualTo("testValue");
	}

	@Test
	void resolveRequiredDependencyNotPresentThrowsUnsatisfiedDependencyException() {
		Method method = ReflectionUtils.findMethod(TestBean.class, "injectString", String.class);
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		assertThatThrownBy(() -> createResolver(TestBean.class, "injectString", String.class).resolve(beanFactory))
				.isInstanceOfSatisfying(UnsatisfiedDependencyException.class, (ex) -> {
					assertThat(ex.getBeanName()).isEqualTo("test");
					assertThat(ex.getInjectionPoint()).isNotNull();
					assertThat(ex.getInjectionPoint().getMember()).isEqualTo(method);
				});
	}

	@Test
	void resolveNonRequiredDependency() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		InjectedElementAttributes attributes = createResolver(TestBean.class, "injectString", String.class).resolve(beanFactory, false);
		assertThat(attributes.isResolved()).isFalse();
	}

	@Test
	void resolveDependencyAndEnvironment() {
		MockEnvironment environment = new MockEnvironment();
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("environment", environment);
		beanFactory.registerSingleton("test", "testValue");
		InjectedElementAttributes attributes = createResolver(TestBean.class, "injectStringAndEnvironment",
				String.class, Environment.class).resolve(beanFactory, true);
		assertThat(attributes.isResolved()).isTrue();
		String string = attributes.get(0);
		assertThat(string).isEqualTo("testValue");
		assertThat((Environment) attributes.get(1)).isEqualTo(environment);
	}

	@Test
	void resolveQualifiedDependency() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
		beanFactory.registerSingleton("one", "1");
		beanFactory.registerSingleton("two", "2");
		InjectedElementAttributes attributes = createResolver(TestBean.class, "injectQualifiedString", String.class).resolve(beanFactory, true);
		assertThat(attributes.isResolved()).isTrue();
		assertThat((String) attributes.get(0)).isEqualTo("2");
	}

	@Test
	@SuppressWarnings("unchecked")
	void createWithUnresolvedAttributesDoesNotInvokeCallback() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		ThrowableFunction<InjectedElementAttributes, ?> callback = mock(ThrowableFunction.class);
		assertThatExceptionOfType(UnsatisfiedDependencyException.class).isThrownBy(() -> createResolver(TestBean.class, "injectString", String.class)
				.create(beanFactory, callback));
		verifyNoInteractions(callback);
	}

	@Test
	@SuppressWarnings("unchecked")
	void invokeWithUnresolvedAttributesDoesNotInvokeCallback() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		ThrowableConsumer<InjectedElementAttributes> callback = mock(ThrowableConsumer.class);
		assertThatExceptionOfType(UnsatisfiedDependencyException.class).isThrownBy(() -> createResolver(TestBean.class, "injectString", String.class)
				.invoke(beanFactory, callback));
		verifyNoInteractions(callback);
	}

	private InjectedMethodResolver createResolver(Class<?> beanType, String methodName, Class<?>... parameterTypes) {
		return new InjectedMethodResolver(ReflectionUtils.findMethod(beanType, methodName, parameterTypes), beanType, "test");
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

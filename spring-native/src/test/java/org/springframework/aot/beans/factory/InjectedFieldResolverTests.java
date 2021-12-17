package org.springframework.aot.beans.factory;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link InjectedFieldResolver}.
 *
 * @author Stephane Nicoll
 */
class InjectedFieldResolverTests {

	private static final Field FIELD_STRING = ReflectionUtils.findField(TestBean.class, "string", String.class);

	@Test
	void resolveDependency() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("one", "1");
		InjectedElementAttributes attributes = createResolver(TestBean.class, "string",
				String.class).resolve(beanFactory, true);
		assertThat(attributes.isResolved()).isTrue();
		assertThat((String) attributes.get(0)).isEqualTo("1");
	}

	@Test
	void resolveRequiredDependencyNotPresentThrowsUnsatisfiedDependencyException() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		assertThatThrownBy(() -> createResolver(TestBean.class, "string", String.class).resolve(beanFactory))
				.isInstanceOfSatisfying(UnsatisfiedDependencyException.class, (ex) -> {
					assertThat(ex.getBeanName()).isEqualTo("test");
					assertThat(ex.getInjectionPoint()).isNotNull();
					assertThat(ex.getInjectionPoint().getField()).isEqualTo(FIELD_STRING);
				});
	}

	@Test
	void resolveNonRequiredDependency() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		InjectedElementAttributes attributes = createResolver(TestBean.class, "string", String.class).resolve(beanFactory, false);
		assertThat(attributes.isResolved()).isFalse();
	}

	private InjectedFieldResolver createResolver(Class<?> beanType, String fieldName, Class<?> fieldType) {
		return new InjectedFieldResolver(ReflectionUtils.findField(beanType, fieldName, fieldType), "test");
	}

	static class TestBean {

		private String string;

	}

}

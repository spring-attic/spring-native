package org.springframework.aot.beans.factory;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.context.support.GenericApplicationContext;
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
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean("one", String.class, () -> "1");
		assertAttributes(context, createResolver(TestBean.class, "string", String.class), true, (attributes) -> {
			assertThat(attributes.isResolved()).isTrue();
			assertThat((String) attributes.get(0)).isEqualTo("1");
		});
	}

	@Test
	void resolveRequiredDependencyNotPresentThrowsUnsatisfiedDependencyException() {
		GenericApplicationContext context = new GenericApplicationContext();
		assertThatThrownBy(() -> createResolver(TestBean.class, "string", String.class).resolve(context))
				.isInstanceOfSatisfying(UnsatisfiedDependencyException.class, (ex) -> {
					assertThat(ex.getBeanName()).isEqualTo("test");
					assertThat(ex.getInjectionPoint()).isNotNull();
					assertThat(ex.getInjectionPoint().getField()).isEqualTo(FIELD_STRING);
				});
	}

	@Test
	void resolveNonRequiredDependency() {
		GenericApplicationContext context = new GenericApplicationContext();
		assertAttributes(context, createResolver(TestBean.class, "string", String.class), false,
				(attributes) -> assertThat(attributes.isResolved()).isFalse());
	}

	private InjectedFieldResolver createResolver(Class<?> beanType, String fieldName, Class<?> fieldType) {
		return new InjectedFieldResolver(ReflectionUtils.findField(beanType, fieldName, fieldType), "test");
	}

	private void assertAttributes(GenericApplicationContext context, InjectedFieldResolver resolver, boolean required,
			Consumer<InjectedElementAttributes> attributes) {
		try (context) {
			if (!context.isRunning()) {
				context.refresh();
			}
			attributes.accept(resolver.resolve(context, required));
		}
	}

	static class TestBean {

		private String string;

	}

}

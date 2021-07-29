package org.springframework.context.bootstrap.generator.bean.descriptor;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.context.bootstrap.generator.sample.visibility.ProtectedParameter;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BeanInstanceDescriptor}.
 *
 * @author Stephane Nicoll
 */
class BeanInstanceDescriptorTests {

	@Test
	void accessibleFromWithPublicConstructor() {
		assertThat(forSingleConstructor(SimpleConfiguration.class).getInstanceCreator().
				isAccessibleFrom("org.acme")).isTrue();
	}

	@Test
	void accessibleFromWithPackagePrivateConstructor() {
		assertThat(forSingleConstructor(ProtectedAccess.class).getInstanceCreator()
				.isAccessibleFrom("org.acme")).isFalse();
	}

	@Test
	void accessibleFromWithPackagePrivateConstructorInTargetPackage() {
		assertThat(forSingleConstructor(ProtectedAccess.class).getInstanceCreator()
				.isAccessibleFrom(getClass().getPackageName())).isTrue();
	}

	@Test
	void accessibleFromWithPackagePrivateClass() {
		assertThat(forSingleConstructor(ProtectedClass.class).getInstanceCreator()
				.isAccessibleFrom("org.acme")).isFalse();
	}

	@Test
	void accessibleFromWithPackagePrivateClassInTargetPackage() {
		assertThat(forSingleConstructor(ProtectedClass.class).getInstanceCreator()
				.isAccessibleFrom(getClass().getPackageName())).isTrue();
	}

	@Test
	void accessibleFromWithPackagePrivateDeclaringType() {
		assertThat(forMethod(String.class, ProtectedClass.class, "stringBean").getInstanceCreator()
				.isAccessibleFrom("org.acme")).isFalse();
	}

	@Test
	void accessibleFromWithPackagePrivateDeclaringTypeInTargetPackage() {
		assertThat(forMethod(String.class, ProtectedClass.class, "stringBean").getInstanceCreator()
				.isAccessibleFrom(getClass().getPackageName())).isTrue();
	}

	@Test
	void accessibleFromWithPackagePrivateParameter() {
		assertThat(forSingleConstructor(ProtectedParameter.class).getInstanceCreator()
				.isAccessibleFrom("org.acme")).isFalse();
	}

	@Test
	void accessibleFromWithPackagePrivateParameterInTargetPackage() {
		assertThat(forSingleConstructor(ProtectedParameter.class).getInstanceCreator()
				.isAccessibleFrom(ProtectedParameter.class.getPackageName())).isTrue();
	}

	@Test
	void accessibleFromWithPackagePrivateMethodReturnType() {
		assertThat(forMethod(ProtectedClass.class, ProtectedAccess.class, "methodWithProtectedReturnType")
				.getInstanceCreator().isAccessibleFrom("org.acme")).isFalse();
	}

	@Test
	void accessibleFromWithPackagePrivateMethodReturnTypeInTargetPackage() {
		assertThat(forMethod(ProtectedClass.class, ProtectedAccess.class, "methodWithProtectedReturnType")
				.getInstanceCreator().isAccessibleFrom(ProtectedAccess.class.getPackageName())).isTrue();
	}

	@Test
	void accessibleFromWithPackagePrivateMethodParameter() {
		assertThat(forMethod(String.class, ProtectedAccess.class, "methodWithProtectedParameter", ProtectedClass.class)
				.getInstanceCreator().isAccessibleFrom("org.acme")).isFalse();
	}

	@Test
	void accessibleFromWithPackagePrivateMethodParameterInTargetPackage() {
		assertThat(forMethod(String.class, ProtectedAccess.class, "methodWithProtectedParameter", ProtectedClass.class)
				.getInstanceCreator().isAccessibleFrom(getClass().getPackageName())).isTrue();
	}

	@Test
	void accessibleFromWithPackagePrivateInjectedField() {
		assertThat(forSingleConstructor(PublicClass.class, ReflectionUtils.findField(PublicClass.class, "protectedField"))
				.getInjectionPoints().get(0).isAccessibleFrom("com.example")).isFalse();
	}

	@Test
	void accessibleFromWithPackagePrivateInjectedFieldInTargetPackage() {
		assertThat(forSingleConstructor(PublicClass.class, ReflectionUtils.findField(PublicClass.class, "protectedField"))
				.getInjectionPoints().get(0).isAccessibleFrom(getClass().getPackageName())).isTrue();
	}

	private static BeanInstanceDescriptor forSingleConstructor(Class<?> type, Member... injectionPoints) {
		return new BeanInstanceDescriptor(type, type.getDeclaredConstructors()[0],
				Arrays.stream(injectionPoints).map((member) -> new MemberDescriptor<>(member, true))
						.collect(Collectors.toList()));
	}

	private static BeanInstanceDescriptor forMethod(Class<?> type, Class<?> declaringType, String methodName, Class<?>... parameterTypes) {
		Method method = ReflectionUtils.findMethod(declaringType, methodName, parameterTypes);
		return new BeanInstanceDescriptor(type, method);
	}


	@SuppressWarnings("unused")
	public static class PublicClass {

		String protectedField;

	}

	@SuppressWarnings("unused")
	public static class ProtectedAccess {

		ProtectedAccess() {
		}

		public String methodWithProtectedParameter(ProtectedClass type) {
			return "test";
		}

		public ProtectedClass methodWithProtectedReturnType() {
			return new ProtectedClass();
		}
	}

	@SuppressWarnings("unused")
	static class ProtectedClass {

		ProtectedClass() {
		}

		public String stringBean() {
			return "public";
		}

	}

}

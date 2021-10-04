/*
 * Copyright 2019-2021 the original author or authors.
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

package org.springframework.aot.context.bootstrap.generator.infrastructure;

import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.aot.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.visibility.ProtectedParameter;
import org.springframework.aot.context.bootstrap.generator.sample.visibility.PublicFactoryBean;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ProtectedAccessAnalyzer}.
 *
 * @author Stephane Nicoll
 */
class ProtectedAccessAnalyzerTests {

	@Test
	void analyzeWithPublicConstructor() {
		assertThat(analyze(forSingleConstructor(SimpleConfiguration.class))
				.isAccessible()).isTrue();
	}

	@Test
	void analyzeWithPackagePrivateConstructor() {
		ProtectedAccessAnalysis analysis = analyze(forSingleConstructor(ProtectedAccess.class));
		assertThat(analysis.isAccessible()).isFalse();
		assertThat(analysis.getPrivilegedPackageName()).isEqualTo(ProtectedAccess.class.getPackageName());
	}

	@Test
	void analyzeWithPackagePrivateConstructorInTargetPackage() {
		assertThat(analyze(forSingleConstructor(ProtectedAccess.class), ProtectedAccess.class.getPackageName())
				.isAccessible()).isTrue();
	}

	@Test
	void analyzeWithPackagePrivateClass() {
		ProtectedAccessAnalysis analysis = analyze(forSingleConstructor(ProtectedClass.class));
		assertThat(analysis.isAccessible()).isFalse();
		assertThat(analysis.getPrivilegedPackageName()).isEqualTo(ProtectedClass.class.getPackageName());
	}

	@Test
	void analyzeWithPackagePackagePrivateClassInTargetPackage() {
		assertThat(analyze(forSingleConstructor(ProtectedClass.class), ProtectedClass.class.getPackageName())
				.isAccessible()).isTrue();
	}

	@Test
	void analyzeWithPackagePrivateDeclaringType() {
		ProtectedAccessAnalysis analysis = analyze(forMethod(String.class, ProtectedClass.class, "stringBean"));
		assertThat(analysis.isAccessible()).isFalse();
		assertThat(analysis.getPrivilegedPackageName()).isEqualTo(ProtectedClass.class.getPackageName());
	}

	@Test
	void analyzeWithPackagePrivateDeclaringTypeInTargetPackage() {
		assertThat(analyze(forMethod(String.class, ProtectedClass.class, "stringBean"), ProtectedClass.class.getPackageName())
				.isAccessible()).isTrue();
	}

	@Test
	void analyzeWithPackagePrivateParameter() {
		ProtectedAccessAnalysis analysis = analyze(forSingleConstructor(ProtectedParameter.class));
		assertThat(analysis.isAccessible()).isFalse();
		assertThat(analysis.getPrivilegedPackageName()).isEqualTo(
				"org.springframework.aot.context.bootstrap.generator.sample.visibility");
	}

	@Test
	void analyzeWithPackagePrivateParameterInTargetPackage() {
		assertThat(analyze(forSingleConstructor(ProtectedParameter.class), ProtectedParameter.class.getPackageName())
				.isAccessible()).isTrue();
	}

	@Test
	void analyzeWithPackagePrivateMethodReturnType() {
		ProtectedAccessAnalysis analysis = analyze(forMethod(ProtectedClass.class, ProtectedAccess.class, "methodWithProtectedReturnType"));
		assertThat(analysis.isAccessible()).isFalse();
		assertThat(analysis.getPrivilegedPackageName()).isEqualTo(ProtectedAccess.class.getPackageName());
	}

	@Test
	void analyzeWithPackagePrivateMethodReturnTypeInTargetPackage() {
		assertThat(analyze(forMethod(ProtectedClass.class, ProtectedAccess.class, "methodWithProtectedReturnType"),
				ProtectedAccess.class.getPackageName()).isAccessible()).isTrue();
	}

	@Test
	void analyzeWithPackagePrivateMethodParameter() {
		ProtectedAccessAnalysis analysis = analyze(forMethod(String.class, ProtectedAccess.class, "methodWithProtectedParameter",
				ProtectedClass.class));
		assertThat(analysis.isAccessible()).isFalse();
		assertThat(analysis.getPrivilegedPackageName()).isEqualTo(ProtectedAccess.class.getPackageName());
	}

	@Test
	void analyzeWithPackagePrivateMethodParameterInTargetPackage() {
		assertThat(analyze(forMethod(String.class, ProtectedAccess.class, "methodWithProtectedParameter", ProtectedClass.class),
				ProtectedAccess.class.getPackageName()).isAccessible()).isTrue();
	}

	@Test
	void analyzeWithPackagePrivateInjectedField() {
		ProtectedAccessAnalysis analysis = analyze(forSingleConstructor(PublicClass.class,
				ReflectionUtils.findField(PublicClass.class, "protectedField")));
		assertThat(analysis.isAccessible()).isFalse();
		assertThat(analysis.getPrivilegedPackageName()).isEqualTo(PublicClass.class.getPackageName());
	}

	@Test
	void analyzeWithPackagePrivateInjectedFieldInTargetPackage() {
		assertThat(analyze(forSingleConstructor(PublicClass.class, ReflectionUtils.findField(PublicClass.class, "protectedField")),
				PublicClass.class.getPackageName()).isAccessible()).isTrue();
	}

	@Test
	void analyzeWithPackagePrivateGenericArgument() {
		ProtectedAccessAnalysis analysis = analyze(BeanInstanceDescriptor
				.of(PublicFactoryBean.resolveToProtectedGenericParameter()).build());
		assertThat(analysis.isAccessible()).isFalse();
		assertThat(analysis.getPrivilegedPackageName()).isEqualTo(PublicFactoryBean.class.getPackageName());
	}

	@Test
	void analyzeWithPackagePrivateGenericArgumentInTargetPackage() {
		assertThat(analyze(BeanInstanceDescriptor.of(PublicFactoryBean.resolveToProtectedGenericParameter()).build(),
				PublicFactoryBean.class.getPackageName()).isAccessible()).isTrue();
	}

	private ProtectedAccessAnalysis analyze(BeanInstanceDescriptor descriptor) {
		return analyze(descriptor, "com.example.public");
	}

	private ProtectedAccessAnalysis analyze(BeanInstanceDescriptor descriptor, String targetPackageName) {
		ProtectedAccessAnalyzer analyzer = new ProtectedAccessAnalyzer(targetPackageName);
		return analyzer.analyze(descriptor);
	}

	private static BeanInstanceDescriptor forSingleConstructor(Class<?> type, Member... injectionPoints) {
		return BeanInstanceDescriptor.of(type).withInstanceCreator(type.getDeclaredConstructors()[0])
				.withInjectionPoints(Arrays.stream(injectionPoints).map((member) -> new MemberDescriptor<>(member, true))
						.collect(Collectors.toList())).build();
	}

	private static BeanInstanceDescriptor forMethod(Class<?> type, Class<?> declaringType,
			String methodName, Class<?>... parameterTypes) {
		return BeanInstanceDescriptor.of(type).withInstanceCreator(
				ReflectionUtils.findMethod(declaringType, methodName, parameterTypes)).build();
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

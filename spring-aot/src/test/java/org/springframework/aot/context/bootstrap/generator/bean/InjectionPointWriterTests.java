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

package org.springframework.aot.context.bootstrap.generator.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;

import org.springframework.aot.context.bootstrap.generator.bean.InjectionPointWriterTests.SimpleConstructorBean.InnerClass;
import org.springframework.aot.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.factory.SampleFactory;
import org.springframework.aot.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.condition.JRE.JAVA_11;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link InjectionPointWriter}.
 *
 * @author Stephane Nicoll
 */
class InjectionPointWriterTests {

	@Test
	@EnabledForJreRange(max = JAVA_11) // Executable is a sealed class and can't be mocked on Java 17
	void writeInstantiationForUnsupportedExecutable() {
		assertThatIllegalArgumentException().isThrownBy(() -> writeInstantiation(mock(Executable.class)));
	}

	@Test
	void writeInstantiationForConstructorWithNoArgUseShortcut() {
		Constructor<?> constructor = SimpleConfiguration.class.getDeclaredConstructors()[0];
		assertThat(writeInstantiation(constructor)).lines().containsExactly("new SimpleConfiguration()");
	}

	@Test
	void writeInstantiationForConstructorWithNonGenericParameter() {
		Constructor<?> constructor = SimpleConstructorBean.class.getDeclaredConstructors()[0];
		assertThat(writeInstantiation(constructor)).lines().containsExactly("instanceContext.create(beanFactory, (attributes) -> new InjectionPointWriterTests.SimpleConstructorBean(attributes.get(0), attributes.get(1)))");
	}

	@Test
	void writeInstantiationForConstructorWithGenericParameter() {
		Constructor<?> constructor = GenericConstructorBean.class.getDeclaredConstructors()[0];
		assertThat(writeInstantiation(constructor)).lines().containsExactly(
				"instanceContext.create(beanFactory, (attributes) -> new InjectionPointWriterTests.GenericConstructorBean(attributes.get(0)))");
	}

	@Test
	void writeInstantiationForAmbiguousConstructor() throws Exception {
		Constructor<?> constructor = AmbiguousConstructorBean.class.getDeclaredConstructor(String.class, Number.class);
		assertThat(writeInstantiation(constructor)).lines().containsExactly("instanceContext.create(beanFactory, (attributes) -> new InjectionPointWriterTests.AmbiguousConstructorBean(attributes.get(0, String.class), attributes.get(1, Number.class)))");
	}

	@Test
	void writeInstantiationForConstructorInInnerClass() {
		Constructor<?> constructor = InnerClass.class.getDeclaredConstructors()[0];
		assertThat(writeInstantiation(constructor)).lines().containsExactly("beanFactory.getBean(InjectionPointWriterTests.SimpleConstructorBean.class).new InnerClass()");
	}

	@Test
	void writeInstantiationForMethodWithNoArgUseShortcut() {
		Method method = ReflectionUtils.findMethod(SimpleConfiguration.class, "stringBean");
		assertThat(writeInstantiation(method)).lines().containsExactly("beanFactory.getBean(SimpleConfiguration.class).stringBean()");
	}

	@Test
	void writeInstantiationForStaticMethodWithNoArgUseShortcut() {
		Method method = ReflectionUtils.findMethod(SampleFactory.class, "integerBean");
		assertThat(writeInstantiation(method)).lines().containsExactly("SampleFactory.integerBean()");
	}

	@Test
	void writeInstantiationForMethodWithNonGenericParameter() {
		Method method = ReflectionUtils.findMethod(SampleBean.class, "source", Integer.class);
		assertThat(writeInstantiation(method)).lines().containsExactly(
				"instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(InjectionPointWriterTests.SampleBean.class).source(attributes.get(0)))");
	}

	@Test
	void writeInstantiationForStaticMethodWithNonGenericParameter() {
		Method method = ReflectionUtils.findMethod(SampleBean.class, "staticSource", Integer.class);
		assertThat(writeInstantiation(method)).lines().containsExactly(
				"instanceContext.create(beanFactory, (attributes) -> InjectionPointWriterTests.SampleBean.staticSource(attributes.get(0)))");
	}

	@Test
	void writeInstantiationForMethodWithGenericParameters() {
		Method method = ReflectionUtils.findMethod(SampleBean.class, "sourceWithProvider", ObjectProvider.class);
		assertThat(writeInstantiation(method)).lines().containsExactly(
				"instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(InjectionPointWriterTests.SampleBean.class).sourceWithProvider(attributes.get(0)))");
	}

	@Test
	void writeInstantiationForAmbiguousMethod() {
		Method method = ReflectionUtils.findMethod(SampleFactory.class, "create", String.class);
		assertThat(writeInstantiation(method)).lines().containsExactly("instanceContext.create(beanFactory, (attributes) -> SampleFactory.create(attributes.get(0, String.class)))");
	}

	@Test
	void writeInjectionForUnsupportedMember() {
		assertThatIllegalArgumentException().isThrownBy(() -> writeInjection(mock(Member.class), false));
	}

	@Test
	void writeInjectionForNonRequiredMethodWithNonGenericParameters() {
		Method method = ReflectionUtils.findMethod(SampleBean.class, "sourceAndCounter", String.class, Integer.class);
		assertThat(writeInjection(method, false)).lines().containsExactly(
				"instanceContext.method(\"sourceAndCounter\", String.class, Integer.class)",
				"    .resolve(beanFactory, false).ifResolved((attributes) -> bean.sourceAndCounter(attributes.get(0), attributes.get(1)))");
	}

	@Test
	void writeInjectionForRequiredMethodWithGenericParameter() {
		Method method = ReflectionUtils.findMethod(SampleBean.class, "nameAndCounter", String.class, ObjectProvider.class);
		assertThat(writeInjection(method, true)).lines().containsExactly(
				"instanceContext.method(\"nameAndCounter\", String.class, ObjectProvider.class)",
				"    .invoke(beanFactory, (attributes) -> bean.nameAndCounter(attributes.get(0), attributes.get(1)))");
	}

	@Test
	void writeInjectionForNonRequiredMethodWithGenericParameter() {
		Method method = ReflectionUtils.findMethod(SampleBean.class, "nameAndCounter", String.class, ObjectProvider.class);
		assertThat(writeInjection(method, false)).lines().containsExactly(
				"instanceContext.method(\"nameAndCounter\", String.class, ObjectProvider.class)",
				"    .resolve(beanFactory, false).ifResolved((attributes) -> bean.nameAndCounter(attributes.get(0), attributes.get(1)))");
	}

	@Test
	void writeInjectionForRequiredField() {
		Field field = ReflectionUtils.findField(SampleBean.class, "counter", Integer.class);
		assertThat(writeInjection(field, true)).lines().containsExactly(
				"instanceContext.field(\"counter\", Integer.class)",
				"    .invoke(beanFactory, (attributes) -> bean.counter = attributes.get(0))");
	}

	@Test
	void writeInjectionForNonRequiredField() {
		Field field = ReflectionUtils.findField(SampleBean.class, "counter", Integer.class);
		assertThat(writeInjection(field, false)).lines().containsExactly(
				"instanceContext.field(\"counter\", Integer.class)",
				"    .resolve(beanFactory, false).ifResolved((attributes) -> bean.counter = attributes.get(0))");
	}

	@Test
	void writeInjectionForRequiredPrivateField() {
		Field field = ReflectionUtils.findField(SampleBean.class, "source", String.class);
		assertThat(writeInjection(field, true)).lines().containsExactly(
				"instanceContext.field(\"source\", String.class)",
				"    .invoke(beanFactory, (attributes) -> {",
				"      Field sourceField = ReflectionUtils.findField(InjectionPointWriterTests.SampleBean.class, \"source\", String.class);",
				"      ReflectionUtils.makeAccessible(sourceField);",
				"      ReflectionUtils.setField(sourceField, bean, attributes.get(0));",
				"    })");
	}


	private CodeSnippet writeInstantiation(Executable creator) {
		return CodeSnippet.of((code) -> code.add(new InjectionPointWriter().writeInstantiation(creator)));
	}

	private CodeSnippet writeInjection(Member member, boolean required) {
		return CodeSnippet.of((code) -> code.add(new InjectionPointWriter().writeInjection(member, required)));
	}

	@SuppressWarnings("unused")
	static class SampleBean {

		private String source;

		Integer counter;


		void sourceAndCounter(String source, Integer counter) {

		}

		void nameAndCounter(String name, ObjectProvider<Integer> counter) {

		}

		String source(Integer counter) {
			return "source" + counter;
		}

		String sourceWithProvider(ObjectProvider<Integer> counter) {
			return "source" + counter.getIfAvailable(() -> 0);
		}

		static String staticSource(Integer counter) {
			return counter + "source";
		}

	}

	@SuppressWarnings("unused")
	static class SimpleConstructorBean {

		private final String source;

		private final Integer counter;

		public SimpleConstructorBean(String source, Integer counter) {
			this.source = source;
			this.counter = counter;
		}

		class InnerClass {

		}

	}

	@SuppressWarnings("unused")
	static class GenericConstructorBean {

		private final ObjectProvider<Integer> counter;

		GenericConstructorBean(ObjectProvider<Integer> counter) {
			this.counter = counter;
		}

	}

	static class AmbiguousConstructorBean {

		AmbiguousConstructorBean(String first, String second) {

		}

		AmbiguousConstructorBean(String first, Number second) {

		}

	}

}

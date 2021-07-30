package org.springframework.context.bootstrap.generator.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.bootstrap.generator.bean.InjectionPointWriterTests.SimpleConstructorBean.InnerClass;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link InjectionPointWriter}.
 *
 * @author Stephane Nicoll
 */
class InjectionPointWriterTests {

	@Test
	void writeInstantiationForUnsupportedExecutable() {
		assertThatIllegalArgumentException().isThrownBy(() -> writeInstantiation(mock(Executable.class)));
	}

	@Test
	void writeInstantiationForConstructorWithNonGenericParameter() {
		Constructor<?> constructor = SimpleConstructorBean.class.getDeclaredConstructors()[0];
		assertThat(writeInstantiation(constructor)).lines().containsExactly("instanceContext.constructor(String.class, Integer.class)",
				"    .create(context, (attributes) -> new SimpleConstructorBean(attributes.get(0), attributes.get(1)))");
	}

	@Test
	void writeInstantiationForConstructorWithGenericParameter() {
		Constructor<?> constructor = GenericConstructorBean.class.getDeclaredConstructors()[0];
		assertThat(writeInstantiation(constructor)).lines().containsExactly("instanceContext.constructor(ObjectProvider.class)",
				"    .create(context, (attributes) -> {",
				"      ObjectProvider<Integer> attributes0 = attributes.get(0);",
				"      return new GenericConstructorBean(attributes0);",
				"    })");
	}

	@Test
	void writeInstantiationForConstructorInInnerClass() {
		Constructor<?> constructor = InnerClass.class.getDeclaredConstructors()[0];
		assertThat(writeInstantiation(constructor)).lines().containsExactly("instanceContext.constructor()",
				"    .create(context, (attributes) -> context.getBean(InjectionPointWriterTests.SimpleConstructorBean.class).new InnerClass())");
	}

	@Test
	void writeInstantiationForMethodWithNonGenericParameter() {
		Method method = ReflectionUtils.findMethod(SampleBean.class, "source", Integer.class);
		assertThat(writeInstantiation(method)).lines().containsExactly(
				"instanceContext.method(\"source\", Integer.class)",
				"    .create(context, (attributes) -> context.getBean(InjectionPointWriterTests.SampleBean.class).source(attributes.get(0)))");
	}

	@Test
	void writeInstantiationForStaticMethodWithNonGenericParameter() {
		Method method = ReflectionUtils.findMethod(SampleBean.class, "staticSource", Integer.class);
		assertThat(writeInstantiation(method)).lines().containsExactly(
				"instanceContext.method(\"staticSource\", Integer.class)",
				"    .create(context, (attributes) -> InjectionPointWriterTests.SampleBean.staticSource(attributes.get(0)))");
	}

	@Test
	void writeInstantiationForMethodWithGenericParameters() {
		Method method = ReflectionUtils.findMethod(SampleBean.class, "source", ObjectProvider.class);
		assertThat(writeInstantiation(method)).lines().containsExactly(
				"instanceContext.method(\"source\", ObjectProvider.class)",
				"    .create(context, (attributes) -> {",
				"      ObjectProvider<Integer> attributes0 = attributes.get(0);",
				"      return context.getBean(InjectionPointWriterTests.SampleBean.class).source(attributes0);",
				"    })");
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
				"    .resolve(context, false).ifResolved((attributes) -> bean.sourceAndCounter(attributes.get(0), attributes.get(1)))");
	}

	@Test
	void writeInjectionForRequiredMethodWithGenericParameter() {
		Method method = ReflectionUtils.findMethod(SampleBean.class, "nameAndCounter", String.class, ObjectProvider.class);
		assertThat(writeInjection(method, true)).lines().containsExactly(
				"instanceContext.method(\"nameAndCounter\", String.class, ObjectProvider.class)",
				"    .invoke(context, (attributes) -> {",
				"      ObjectProvider<Integer> attributes1 = attributes.get(1);",
				"      bean.nameAndCounter(attributes.get(0), attributes1);",
				"    })");
	}

	@Test
	void writeInjectionForNonRequiredMethodWithGenericParameter() {
		Method method = ReflectionUtils.findMethod(SampleBean.class, "nameAndCounter", String.class, ObjectProvider.class);
		assertThat(writeInjection(method, false)).lines().containsExactly(
				"instanceContext.method(\"nameAndCounter\", String.class, ObjectProvider.class)",
				"    .resolve(context, false).ifResolved((attributes) -> {",
				"      ObjectProvider<Integer> attributes1 = attributes.get(1);",
				"      bean.nameAndCounter(attributes.get(0), attributes1);",
				"    })");
	}

	@Test
	void writeInjectionForRequiredField() {
		Field field = ReflectionUtils.findField(SampleBean.class, "counter", Integer.class);
		assertThat(writeInjection(field, true)).lines().containsExactly(
				"instanceContext.field(\"counter\", Integer.class)",
				"    .invoke(context, (attributes) -> bean.counter = attributes.get(0))");
	}

	@Test
	void writeInjectionForNonRequiredField() {
		Field field = ReflectionUtils.findField(SampleBean.class, "counter", Integer.class);
		assertThat(writeInjection(field, false)).lines().containsExactly(
				"instanceContext.field(\"counter\", Integer.class)",
				"    .resolve(context, false).ifResolved((attributes) -> bean.counter = attributes.get(0))");
	}

	@Test
	void writeInjectionForRequiredPrivateField() {
		Field field = ReflectionUtils.findField(SampleBean.class, "source", String.class);
		assertThat(writeInjection(field, true)).lines().containsExactly(
				"instanceContext.field(\"source\", String.class)",
				"    .invoke(context, (attributes) -> {",
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

		String source(ObjectProvider<Integer> counter) {
			return "source" + counter.getIfAvailable(() -> 0);
		}

		static String staticSource(Integer counter) {
			return counter + "source";
		}

	}

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

	static class GenericConstructorBean {

		private final ObjectProvider<Integer> counter;

		GenericConstructorBean(ObjectProvider<Integer> counter) {
			this.counter = counter;
		}

	}

}

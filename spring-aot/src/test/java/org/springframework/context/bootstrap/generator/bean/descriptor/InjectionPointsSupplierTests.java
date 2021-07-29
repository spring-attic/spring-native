package org.springframework.context.bootstrap.generator.bean.descriptor;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link InjectionPointsSupplier}.
 *
 * @author Stephane Nicoll
 */
class InjectionPointsSupplierTests {

	private final InjectionPointsSupplier supplier = new InjectionPointsSupplier(getClass().getClassLoader());

	@Test
	void detectOnNonCandidateMethod() {
		List<MemberDescriptor<?>> descriptors = supplier.detectInjectionPoints(InjectionPointsSupplier.class);
		assertThat(descriptors).isEmpty();
	}

	@Test
	void detectAutowiredField() {
		List<MemberDescriptor<?>> descriptors = supplier.detectInjectionPoints(AutowiredField.class);
		assertThat(descriptors).singleElement().satisfies((descriptor) -> {
			assertThat(descriptor.getMember()).isEqualTo(ReflectionUtils.findField(AutowiredField.class, "test"));
			assertThat(descriptor.isRequired()).isTrue();
		});
	}

	@Test
	void detectAtValueField() {
		List<MemberDescriptor<?>> descriptors = supplier.detectInjectionPoints(AtValueField.class);
		assertThat(descriptors).singleElement().satisfies((descriptor) -> {
			assertThat(descriptor.getMember()).isEqualTo(ReflectionUtils.findField(AtValueField.class, "test"));
			assertThat(descriptor.isRequired()).isTrue();
		});
	}

	@Test
	void detectAutowireMethod() {
		List<MemberDescriptor<?>> descriptors = supplier.detectInjectionPoints(AutowiredMethod.class);
		assertThat(descriptors).singleElement().satisfies((descriptor) -> {
			assertThat(descriptor.getMember()).isEqualTo(ReflectionUtils.findMethod(AutowiredMethod.class, "test", String.class));
			assertThat(descriptor.isRequired()).isTrue();
		});
	}

	@Test
	void detectAtValueMethod() {
		List<MemberDescriptor<?>> descriptors = supplier.detectInjectionPoints(AtValueMethod.class);
		assertThat(descriptors).singleElement().satisfies((descriptor) -> {
			assertThat(descriptor.getMember()).isEqualTo(ReflectionUtils.findMethod(AtValueMethod.class, "test", Integer.class));
			assertThat(descriptor.isRequired()).isTrue();
		});
	}

	@Test
	void detectAutowiredFieldAndMethodHasConsistentOrder() {
		List<MemberDescriptor<?>> descriptors = supplier.detectInjectionPoints(AutowiredFieldAndMethod.class);
		assertThat(descriptors).hasSize(2);
		assertThat(descriptors.get(0).getMember()).isEqualTo(
				ReflectionUtils.findField(AutowiredFieldAndMethod.class, "test"));
		assertThat(descriptors.get(1).getMember()).isEqualTo(
				ReflectionUtils.findMethod(AutowiredFieldAndMethod.class, "test", String.class));
	}


	private static class AutowiredField {

		@Autowired
		private String test;

	}

	private static class AtValueField {

		@Value("${test.counter:42}")
		private Integer test;

	}

	private static class AutowiredMethod {

		@Autowired
		void test(String bean) {

		}

	}

	private static class AtValueMethod {

		@Value("${test.counter:42}")
		void test(Integer bean) {

		}

	}

	private static class AutowiredFieldAndMethod {

		@Autowired
		private String test;

		@Autowired
		void test(String bean) {

		}

	}

}

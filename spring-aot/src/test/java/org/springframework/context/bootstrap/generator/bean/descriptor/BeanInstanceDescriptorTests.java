package org.springframework.context.bootstrap.generator.bean.descriptor;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.springframework.core.ResolvableType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BeanInstanceDescriptor}.
 *
 * @author Stephane Nicoll
 */
class BeanInstanceDescriptorTests {

	@Test
	void beanInstanceWithSimpleType() {
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(String.class).build();
		assertThat(descriptor.getBeanType().toClass()).isEqualTo(String.class);
		assertThat(descriptor.getUserBeanClass()).isEqualTo(String.class);
	}

	@Test
	void beanInstanceWithGenericType() {
		ResolvableType beanType = ResolvableType.forClassWithGenerics(Supplier.class, Integer.class);
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(beanType).build();
		assertThat(descriptor.getBeanType()).isSameAs(beanType);
		assertThat(descriptor.getUserBeanClass()).isEqualTo(Supplier.class);
	}
}

package org.springframework.context.bootstrap.generator.bean.descriptor;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.springframework.beans.PropertyValue;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionConfiguration;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BeanInstanceDescriptor}.
 *
 * @author Stephane Nicoll
 */
class BeanInstanceDescriptorTests {

	@Test
	void descriptorWithSimpleType() {
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(String.class).build();
		assertThat(descriptor.getBeanType().toClass()).isEqualTo(String.class);
		assertThat(descriptor.getUserBeanClass()).isEqualTo(String.class);
	}

	@Test
	void descriptorWithGenericType() {
		ResolvableType beanType = ResolvableType.forClassWithGenerics(Supplier.class, Integer.class);
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(beanType).build();
		assertThat(descriptor.getBeanType()).isSameAs(beanType);
		assertThat(descriptor.getUserBeanClass()).isEqualTo(Supplier.class);
	}

	@Test
	void descriptorWithPropertyValue() {
		Method writeMethod = ReflectionUtils.findMethod(InjectionConfiguration.class, "setName", String.class);
		PropertyValue propertyValue = new PropertyValue("name", "test");
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(InjectionConfiguration.class).withProperty(writeMethod, propertyValue).build();
		assertThat(descriptor.getProperties()).singleElement().satisfies((candidate) -> {
			assertThat(candidate.getWriteMethod()).isEqualTo(writeMethod);
			assertThat(candidate.getPropertyValue()).isEqualTo(propertyValue);
		});
	}
}

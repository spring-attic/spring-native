package org.springframework.context.bootstrap.generator.bean.descriptor;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link DefaultBeanInstanceDescriptorFactory}.
 *
 * @author Stephane Nicoll
 */
class DefaultBeanInstanceDescriptorFactoryTests {

	@Test
	void createWithNullBeanDefinition() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		assertThatIllegalArgumentException().isThrownBy(() -> new DefaultBeanInstanceDescriptorFactory(beanFactory).create(null));
	}

	@Test
	void createWithUnsupportedBeanDefinition() {
		BeanDefinition beanDefinition = new GenericBeanDefinition();
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		assertThat(new DefaultBeanInstanceDescriptorFactory(beanFactory).create(beanDefinition)).isNull();
	}

	@Test
	void createWithConstructor() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(SimpleConfiguration.class).getBeanDefinition());
		BeanInstanceDescriptor descriptor = createDescriptor(beanFactory, "test");
		assertThat(descriptor.getUserBeanClass()).isEqualTo(SimpleConfiguration.class);
		assertThat(descriptor.getInstanceCreator()).isNotNull();
		assertThat(descriptor.getInstanceCreator().getMember()).isEqualTo(SimpleConfiguration.class.getDeclaredConstructors()[0]);
		assertThat(descriptor.getInjectionPoints()).isEmpty();
	}

	@Test
	void createWithInjectionPoints() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(InjectionConfiguration.class).getBeanDefinition());
		BeanInstanceDescriptor descriptor = createDescriptor(beanFactory, "test");
		assertThat(descriptor.getUserBeanClass()).isEqualTo(InjectionConfiguration.class);
		assertThat(descriptor.getInstanceCreator()).isNotNull();
		assertThat(descriptor.getInstanceCreator().getMember()).isEqualTo(InjectionConfiguration.class.getDeclaredConstructors()[0]);
		assertThat(descriptor.getInjectionPoints()).hasSize(2);
		assertThat(descriptor.getInjectionPoints()).anySatisfy((injectionPoint) -> {
			assertThat(injectionPoint.getMember()).isEqualTo(ReflectionUtils.findMethod(InjectionConfiguration.class, "setEnvironment", Environment.class));
			assertThat(injectionPoint.isRequired()).isTrue();
		});
		assertThat(descriptor.getInjectionPoints()).anySatisfy((injectionPoint) -> {
			assertThat(injectionPoint.getMember()).isEqualTo(ReflectionUtils.findMethod(InjectionConfiguration.class, "setBean", String.class));
			assertThat(injectionPoint.isRequired()).isFalse();
		});
	}

	private BeanInstanceDescriptor createDescriptor(DefaultListableBeanFactory beanFactory, String beanName) {
		return new DefaultBeanInstanceDescriptorFactory(beanFactory).create(beanFactory.getMergedBeanDefinition(beanName));
	}

}

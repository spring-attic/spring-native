package org.springframework.context.bootstrap.generator.bean.descriptor;

import java.lang.reflect.Executable;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.bootstrap.generator.sample.constructor.SampleBeanWithConstructors;
import org.springframework.context.bootstrap.generator.sample.factory.NumberHolder;
import org.springframework.context.bootstrap.generator.sample.factory.NumberHolderFactoryBean;
import org.springframework.context.bootstrap.generator.sample.factory.SampleFactory;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link BeanInstanceExecutableSupplier}.
 *
 * @author Stephane Nicoll
 */
class BeanInstanceExecutableSupplierTests {

	@Test
	void detectBeanInstanceExecutableWithFactoryMethodName() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("testBean", "test");
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class.getName())
				.setFactoryMethod("create").addConstructorArgReference("testBean").getBeanDefinition();
		Executable executable = detectBeanInstanceExecutable(beanFactory, beanDefinition);
		assertThat(executable).isNotNull().isEqualTo(ReflectionUtils.findMethod(SampleFactory.class, "create", String.class));
	}

	@Test
	void beanDefinitionWithFactoryMethodNameAndAssignableConstructorArg() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("testNumber", 1L);
		beanFactory.registerSingleton("testBean", "test");
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleFactory.class.getName())
				.setFactoryMethod("create").addConstructorArgReference("testNumber")
				.addConstructorArgReference("testBean").getBeanDefinition();
		Executable executable = detectBeanInstanceExecutable(beanFactory, beanDefinition);
		assertThat(executable).isNotNull().isEqualTo(ReflectionUtils.findMethod(SampleFactory.class, "create", Number.class, String.class));
	}

	@Test
	void beanDefinitionWithConstructorArgsForMultipleConstructors() throws Exception {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("testNumber", 1L);
		beanFactory.registerSingleton("testBean", "test");
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SampleBeanWithConstructors.class.getName())
				.addConstructorArgReference("testNumber")
				.addConstructorArgReference("testBean").getBeanDefinition();
		Executable executable = detectBeanInstanceExecutable(beanFactory, beanDefinition);
		assertThat(executable).isNotNull().isEqualTo(SampleBeanWithConstructors.class.getDeclaredConstructor(String.class, Number.class));
	}

	@Test
	void genericBeanDefinitionWithConstructorArgsForMultipleConstructors() throws Exception {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("testNumber", 1L);
		beanFactory.registerSingleton("testBean", "test");
		BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(SampleBeanWithConstructors.class.getName())
				.addConstructorArgReference("testNumber")
				.addConstructorArgReference("testBean").getBeanDefinition();
		Executable executable = detectBeanInstanceExecutable(beanFactory, beanDefinition);
		assertThat(executable).isNotNull().isEqualTo(SampleBeanWithConstructors.class.getDeclaredConstructor(String.class, Number.class));
	}

	@Test
	void detectBeanInstanceExecutableWithFactoryBeanSetInBeanClass() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(NumberHolder.class, Integer.class));
		beanDefinition.setBeanClass(NumberHolderFactoryBean.class);
		Executable executable = detectBeanInstanceExecutable(beanFactory, beanDefinition);
		assertThat(executable).isNotNull().isEqualTo(NumberHolderFactoryBean.class.getDeclaredConstructors()[0]);
	}

	@Test
	void detectBeanInstanceExecutableWithFactoryBeanSetInBeanClassAndNoResolvableType() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(NumberHolderFactoryBean.class);
		Executable executable = detectBeanInstanceExecutable(beanFactory, beanDefinition);
		assertThat(executable).isNotNull().isEqualTo(NumberHolderFactoryBean.class.getDeclaredConstructors()[0]);
	}

	@Test
	void detectBeanInstanceExecutableWithFactoryBeanSetInBeanClassThatDoesNotMatchTargetType() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(NumberHolder.class, String.class));
		beanDefinition.setBeanClass(NumberHolderFactoryBean.class);
		assertThatIllegalStateException().isThrownBy(() -> detectBeanInstanceExecutable(beanFactory, beanDefinition))
				.withMessageContaining("Incompatible target type").withMessageContaining(NumberHolder.class.getName())
				.withMessageContaining(NumberHolderFactoryBean.class.getName());
	}

	private Executable detectBeanInstanceExecutable(DefaultListableBeanFactory beanFactory, BeanDefinition beanDefinition) {
		return new BeanInstanceExecutableSupplier(beanFactory).detectBeanInstanceExecutable(beanDefinition);
	}

}

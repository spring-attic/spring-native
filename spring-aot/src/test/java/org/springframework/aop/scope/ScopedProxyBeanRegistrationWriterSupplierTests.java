package org.springframework.aop.scope;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.samples.simple.SimpleComponent;
import org.springframework.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.context.bootstrap.generator.sample.factory.NumberHolder;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.core.ResolvableType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ScopedProxyBeanRegistrationWriterSupplier}.
 *
 * @author Stephane Nicoll
 */
class ScopedProxyBeanRegistrationWriterSupplierTests {

	@Test
	void getWithNonScopedProxy() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(PropertiesFactoryBean.class)
				.getBeanDefinition();
		assertThat(getBeanRegistrationWriter(beanFactory, "test", beanDefinition)).isNull();
	}

	@Test
	void getWithScopedProxyWithoutTargetBeanName() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanDefinition scopeBean = BeanDefinitionBuilder.rootBeanDefinition(ScopedProxyFactoryBean.class)
				.getBeanDefinition();
		assertThat(getBeanRegistrationWriter(beanFactory, "test", scopeBean)).isNull();
	}

	@Test
	void getWithScopedProxyWithInvalidTargetBeanName() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanDefinition scopeBean = BeanDefinitionBuilder.rootBeanDefinition(ScopedProxyFactoryBean.class)
				.addPropertyValue("targetBeanName", "testDoesNotExist").getBeanDefinition();
		assertThat(getBeanRegistrationWriter(beanFactory, "test", scopeBean)).isNull();
	}

	@Test
	void getWithScopedProxyWithTargetBeanName() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanDefinition targetBean = BeanDefinitionBuilder.rootBeanDefinition(SimpleComponent.class)
				.getBeanDefinition();
		beanFactory.registerBeanDefinition("simpleComponent", targetBean);
		BeanDefinition scopeBean = BeanDefinitionBuilder.rootBeanDefinition(ScopedProxyFactoryBean.class)
				.addPropertyValue("targetBeanName", "simpleComponent").getBeanDefinition();
		assertThat(getBeanRegistrationWriter(beanFactory, "test", scopeBean)).isNotNull();
	}

	@Test
	void writeBeanRegistrationForScopedProxy() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		RootBeanDefinition targetBean = new RootBeanDefinition();
		targetBean.setTargetType(ResolvableType.forClassWithGenerics(NumberHolder.class, Integer.class));
		targetBean.setScope("custom");
		beanFactory.registerBeanDefinition("numberHolder", targetBean);
		BeanDefinition scopeBean = BeanDefinitionBuilder.rootBeanDefinition(ScopedProxyFactoryBean.class)
				.addPropertyValue("targetBeanName", "numberHolder").getBeanDefinition();
		assertThat(writeBeanRegistration(beanFactory, "test", scopeBean)).lines().containsOnly(
				"BeanDefinitionRegistrar.of(\"test\", ResolvableType.forClassWithGenerics(NumberHolder.class, Integer.class))",
				"    .instanceSupplier(() ->  {",
				"      ScopedProxyFactoryBean factory = new ScopedProxyFactoryBean();",
				"      factory.setTargetBeanName(\"numberHolder\");",
				"      factory.setBeanFactory(context.getBeanFactory());",
				"      return factory.getObject();",
				"    }).register(context);");
	}

	private CodeSnippet writeBeanRegistration(BeanFactory beanFactory, String beanName, BeanDefinition beanDefinition) {
		return CodeSnippet.of((code) -> getBeanRegistrationWriter(beanFactory, beanName, beanDefinition)
				.writeBeanRegistration(new BootstrapWriterContext(BootstrapClass.of("com.example")), code));
	}

	BeanRegistrationWriter getBeanRegistrationWriter(BeanFactory beanFactory, String beanName, BeanDefinition beanDefinition) {
		ScopedProxyBeanRegistrationWriterSupplier supplier = new ScopedProxyBeanRegistrationWriterSupplier();
		supplier.setBeanFactory(beanFactory);
		return supplier.get(beanName, beanDefinition);
	}

}

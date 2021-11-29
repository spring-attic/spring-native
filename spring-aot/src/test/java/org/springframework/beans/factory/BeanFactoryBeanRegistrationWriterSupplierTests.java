package org.springframework.beans.factory;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.infrastructure.DefaultBootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.sample.factory.NumberHolder;
import org.springframework.aot.context.bootstrap.generator.sample.factory.NumberHolderFactoryBean;
import org.springframework.aot.context.bootstrap.generator.sample.factory.TestGenericFactoryBean;
import org.springframework.aot.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BeanFactoryBeanRegistrationWriterSupplier}.
 *
 * @author Stephane Nicoll
 */
class BeanFactoryBeanRegistrationWriterSupplierTests {

	@Test
	void getForFactoryBeanWithObjectTypeResolvableType() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(TestGenericFactoryBean.class);
		beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE,
				ResolvableType.forClassWithGenerics(NumberHolder.class, Integer.class));
		assertThat(get(beanDefinition)).isNotNull();
	}

	@Test
	void writeBeanRegistrationForFactoryBeanWithUnresolvedGenericAndResolvableTypeObjectTypeAttribute() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(TestGenericFactoryBean.class);
		beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE,
				ResolvableType.forClassWithGenerics(NumberHolder.class, Integer.class));
		BeanRegistrationWriter beanRegistrationWriter = get(beanDefinition);
		assertThat(beanRegistrationWriter).isNotNull();
		assertThat(CodeSnippet.of((code) -> beanRegistrationWriter.writeBeanRegistration(createBootstrapContext(), code))).lines().contains(
				"BeanDefinitionRegistrar.of(\"test\", TestGenericFactoryBean.class).withConstructor(Serializable.class)",
				"    .instanceSupplier((instanceContext) -> instanceContext.create(context, (attributes) -> new TestGenericFactoryBean(attributes.get(0)))).customize((bd) -> bd.setAttribute(\"factoryBeanObjectType\", ResolvableType.forClassWithGenerics(NumberHolder.class, Integer.class))).register(context);");
	}

	@Test
	void getForFactoryBeanWithObjectTypeClass() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(TestGenericFactoryBean.class);
		beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, Integer.class);
		assertThat(get(beanDefinition)).isNotNull();
	}

	@Test
	void writeBeanRegistrationForFactoryBeanWithUnresolvedGenericAndClassObjetTypeAttribute() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(TestGenericFactoryBean.class);
		beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, Integer.class);
		BeanRegistrationWriter beanRegistrationWriter = get(beanDefinition);
		assertThat(beanRegistrationWriter).isNotNull();
		assertThat(CodeSnippet.of((code) -> beanRegistrationWriter.writeBeanRegistration(createBootstrapContext(), code))).lines().contains(
				"BeanDefinitionRegistrar.of(\"test\", TestGenericFactoryBean.class).withConstructor(Serializable.class)",
				"    .instanceSupplier((instanceContext) -> instanceContext.create(context, (attributes) -> new TestGenericFactoryBean(attributes.get(0)))).customize((bd) -> bd.setAttribute(\"factoryBeanObjectType\", Integer.class)).register(context);");
	}

	@Test
	void getForFactoryBeanWithObjectTypeThatIsNotCompatibleWithBeanFactorBound() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(TestGenericFactoryBean.class);
		beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE,
				ResolvableType.forClassWithGenerics(Consumer.class, Integer.class));
		assertThat(get(beanDefinition)).isNull();
	}

	@Test
	void getForFactoryBeanWithoutObjectTypeAttribute() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(
				NumberHolderFactoryBean.class, Integer.class));
		assertThat(get(beanDefinition)).isNull();
	}

	@Test
	void getForNonFactoryBean() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(Function.class, Integer.class, String.class));
		beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, Integer.class);
		assertThat(get(beanDefinition)).isNull();
	}

	@Nullable
	private BeanRegistrationWriter get(RootBeanDefinition beanDefinition) {
		BeanFactoryBeanRegistrationWriterSupplier supplier = new BeanFactoryBeanRegistrationWriterSupplier();
		supplier.setBeanFactory(new DefaultListableBeanFactory());
		return supplier.get("test", beanDefinition);
	}

	private static BootstrapWriterContext createBootstrapContext() {
		return new DefaultBootstrapWriterContext("com.example", "Test");
	}

}

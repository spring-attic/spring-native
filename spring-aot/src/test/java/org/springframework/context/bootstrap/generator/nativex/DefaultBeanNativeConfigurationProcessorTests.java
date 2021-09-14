package org.springframework.context.bootstrap.generator.nativex;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionComponent;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultBeanNativeConfigurationProcessor}.
 *
 * @author Stephane Nicoll
 */
class DefaultBeanNativeConfigurationProcessorTests {

	@Test
	void registerReflectionEntriesForInstanceCreator() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).build());
		assertThat(registry.reflection().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionComponent.class);
			assertThat(entry.getConstructors()).contains(instanceCreator);
			assertThat(entry.getMethods()).isEmpty();
			assertThat(entry.getFields()).isEmpty();
		});
	}

	@Test
	void registerReflectionEntriesForMethodInjectionPoint() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		Method injectionPoint = ReflectionUtils.findMethod(InjectionComponent.class, "setCounter", Integer.class);
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).withInjectionPoint(injectionPoint, false).build());
		assertThat(registry.reflection().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionComponent.class);
			assertThat(entry.getConstructors()).contains(instanceCreator);
			assertThat(entry.getMethods()).containsOnly(injectionPoint);
			assertThat(entry.getFields()).isEmpty();
		});
	}

	@Test
	void registerReflectionEntriesForMethodInjectionPointUseTargetClass() {
		Constructor<?> instanceCreator = IntegerFactoryBean.class.getDeclaredConstructors()[0];
		Method injectionPoint = ReflectionUtils.findMethod(IntegerFactoryBean.class, "setNamingStrategy", String.class);
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(IntegerFactoryBean.class)
				.withInstanceCreator(instanceCreator).withInjectionPoint(injectionPoint, false).build());
		assertThat(registry.reflection().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(IntegerFactoryBean.class);
			assertThat(entry.getConstructors()).contains(instanceCreator);
			assertThat(entry.getMethods()).containsOnly(injectionPoint);
			assertThat(entry.getFields()).isEmpty();
		});
	}

	@Test
	void registerReflectionEntriesForFieldInjectionPoint() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		Field injectionPoint = ReflectionUtils.findField(InjectionComponent.class, "counter");
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).withInjectionPoint(injectionPoint, false).build());
		assertThat(registry.reflection().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionComponent.class);
			assertThat(entry.getConstructors()).containsOnly(instanceCreator);
			assertThat(entry.getMethods()).isEmpty();
			assertThat(entry.getFields()).containsOnly(injectionPoint);
		});
	}

	@Test
	void registerReflectionEntriesForFieldInjectionPointUseTargetClass() {
		Constructor<?> instanceCreator = IntegerFactoryBean.class.getDeclaredConstructors()[0];
		Field injectionPoint = ReflectionUtils.findField(IntegerFactoryBean.class, "strategy");
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(IntegerFactoryBean.class)
				.withInstanceCreator(instanceCreator).withInjectionPoint(injectionPoint, false).build());
		assertThat(registry.reflection().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(IntegerFactoryBean.class);
			assertThat(entry.getConstructors()).containsOnly(instanceCreator);
			assertThat(entry.getMethods()).isEmpty();
			assertThat(entry.getFields()).containsOnly(injectionPoint);
		});
	}

	@Test
	void registerReflectionEntriesForPropertiesUseTargetClass() {
		Constructor<?> instanceCreator = IntegerFactoryBean.class.getDeclaredConstructors()[0];
		Method nameWriteMethod = ReflectionUtils.findMethod(IntegerFactoryBean.class, "setName", String.class);
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(IntegerFactoryBean.class)
				.withInstanceCreator(instanceCreator).withProperty(nameWriteMethod, new PropertyValue("name", "Hello"))
				.build());
		assertThat(registry.reflection().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(IntegerFactoryBean.class);
			assertThat(entry.getConstructors()).contains(instanceCreator);
			assertThat(entry.getMethods()).containsOnly(nameWriteMethod);
			assertThat(entry.getFields()).isEmpty();
		});
	}

	@Test
	void registerReflectionEntriesForProperties() {
		Constructor<?> instanceCreator = InjectionConfiguration.class.getDeclaredConstructors()[0];
		Method nameWriteMethod = ReflectionUtils.findMethod(InjectionConfiguration.class, "setName", String.class);
		Method counterWriteMethod = ReflectionUtils.findMethod(InjectionConfiguration.class, "setCounter", Integer.class);
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(InjectionConfiguration.class)
				.withInstanceCreator(instanceCreator).withProperty(nameWriteMethod, new PropertyValue("name", "Hello"))
				.withProperty(counterWriteMethod, new PropertyValue("counter", 42)).build());
		assertThat(registry.reflection().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionConfiguration.class);
			assertThat(entry.getConstructors()).contains(instanceCreator);
			assertThat(entry.getMethods()).containsOnly(nameWriteMethod, counterWriteMethod);
			assertThat(entry.getFields()).isEmpty();
		});
	}

	@Test
	void registerReflectionEntriesForInnerBeanDefinition() {
		Constructor<?> instanceCreator = InjectionConfiguration.class.getDeclaredConstructors()[0];
		Method counterWriteMethod = ReflectionUtils.findMethod(InjectionConfiguration.class, "setCounter", Integer.class);
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(InjectionConfiguration.class)
				.withInstanceCreator(instanceCreator).withProperty(counterWriteMethod, new PropertyValue("counter",
						BeanDefinitionBuilder.rootBeanDefinition(IntegerFactoryBean.class).getBeanDefinition())).build());
		assertThat(registry.reflection().getEntries()).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionConfiguration.class);
			assertThat(entry.getConstructors()).contains(instanceCreator);
			assertThat(entry.getMethods()).containsOnly(counterWriteMethod);
			assertThat(entry.getFields()).isEmpty();
		});
		assertThat(registry.reflection().getEntries()).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(IntegerFactoryBean.class);
			assertThat(entry.getConstructors()).containsOnly(IntegerFactoryBean.class.getDeclaredConstructors()[0]);
			assertThat(entry.getMethods()).containsOnly(ReflectionUtils.findMethod(
					IntegerFactoryBean.class, "setNamingStrategy", String.class));
			assertThat(entry.getFields()).isEmpty();
		});
		assertThat(registry.reflection().getEntries()).hasSize(2);
	}

	private NativeConfigurationRegistry register(BeanInstanceDescriptor descriptor) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		DefaultBeanNativeConfigurationProcessor processor = new DefaultBeanNativeConfigurationProcessor();
		processor.setBeanFactory(new DefaultListableBeanFactory());
		processor.process(descriptor, registry);
		return registry;
	}

	static abstract class BaseFactoryBean {

		private String strategy;

		@Autowired
		void setNamingStrategy(String strategy) {
			this.strategy = strategy;
		}

		void setName(String name) {

		}

	}

	@SuppressWarnings("unused")
	static class IntegerFactoryBean extends BaseFactoryBean implements FactoryBean<Integer> {

		public IntegerFactoryBean(Environment environment) {

		}

		@Override
		public Class<?> getObjectType() {
			return Integer.class;
		}

		@Override
		public Integer getObject() {
			return 42;
		}

	}

}

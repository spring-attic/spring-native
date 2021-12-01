/*
 * Copyright 2019-2021 the original author or authors.
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

package org.springframework.aot.context.bootstrap.generator.nativex;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.sample.injection.InjectionComponent;
import org.springframework.aot.context.bootstrap.generator.sample.injection.InjectionConfiguration;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultBeanNativeConfigurationProcessor}.
 *
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 */
class DefaultBeanNativeConfigurationProcessorTests {

	@Test
	void registerReflectionEntriesForInstanceCreator() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).build());
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) -> {
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
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionComponent.class);
			assertThat(entry.getConstructors()).contains(instanceCreator);
			assertThat(entry.getMethods()).containsOnly(injectionPoint);
			assertThat(entry.getFields()).isEmpty();
		});
	}

	@Test
	void registerReflectionEntriesForMethodInjectionPointUseDeclaringClass() {
		Constructor<?> instanceCreator = IntegerFactoryBean.class.getDeclaredConstructors()[0];
		Method injectionPoint = ReflectionUtils.findMethod(IntegerFactoryBean.class, "setNamingStrategy", String.class);
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(IntegerFactoryBean.class)
				.withInstanceCreator(instanceCreator).withInjectionPoint(injectionPoint, false).build());
		assertThat(registry.reflection().reflectionEntries()).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(BaseFactoryBean.class);
			assertThat(entry.getConstructors()).isEmpty();
			assertThat(entry.getMethods()).containsOnly(injectionPoint);
			assertThat(entry.getFields()).isEmpty();
		}).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(IntegerFactoryBean.class);
			assertThat(entry.getConstructors()).containsOnly(instanceCreator);
			assertThat(entry.getMethods()).isEmpty();
			assertThat(entry.getFields()).isEmpty();
		});
		assertThat(registry.reflection().reflectionEntries()).hasSize(2);
	}

	@Test
	void registerReflectionEntriesForFieldInjectionPoint() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		Field injectionPoint = ReflectionUtils.findField(InjectionComponent.class, "counter");
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).withInjectionPoint(injectionPoint, false).build());
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionComponent.class);
			assertThat(entry.getConstructors()).containsOnly(instanceCreator);
			assertThat(entry.getMethods()).isEmpty();
			assertThat(entry.getFields()).containsOnly(injectionPoint);
		});
	}

	@Test
	void registerReflectionEntriesForFieldInjectionPointUseDeclaringClass() {
		Constructor<?> instanceCreator = IntegerFactoryBean.class.getDeclaredConstructors()[0];
		Field injectionPoint = ReflectionUtils.findField(IntegerFactoryBean.class, "strategy");
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(IntegerFactoryBean.class)
				.withInstanceCreator(instanceCreator).withInjectionPoint(injectionPoint, false).build());
		assertThat(registry.reflection().reflectionEntries()).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(BaseFactoryBean.class);
			assertThat(entry.getConstructors()).isEmpty();
			assertThat(entry.getMethods()).isEmpty();
			assertThat(entry.getFields()).containsOnly(injectionPoint);
		}).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(IntegerFactoryBean.class);
			assertThat(entry.getMethods()).isEmpty();
			assertThat(entry.getConstructors()).containsOnly(instanceCreator);
			assertThat(entry.getFields()).isEmpty();
		});
		assertThat(registry.reflection().reflectionEntries()).hasSize(2);
	}

	@Test
	void registerReflectionEntriesForPropertiesUseDeclaringClass() {
		Constructor<?> instanceCreator = IntegerFactoryBean.class.getDeclaredConstructors()[0];
		Method nameWriteMethod = ReflectionUtils.findMethod(IntegerFactoryBean.class, "setName", String.class);
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(IntegerFactoryBean.class)
				.withInstanceCreator(instanceCreator).withProperty(nameWriteMethod, new PropertyValue("name", "Hello"))
				.build());
		assertThat(registry.reflection().reflectionEntries()).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(BaseFactoryBean.class);
			assertThat(entry.getConstructors()).isEmpty();
			assertThat(entry.getMethods()).containsOnly(nameWriteMethod);
			assertThat(entry.getFields()).isEmpty();
		}).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(IntegerFactoryBean.class);
			assertThat(entry.getConstructors()).contains(instanceCreator);
			assertThat(entry.getMethods()).isEmpty();
			assertThat(entry.getFields()).isEmpty();
		});
		assertThat(registry.reflection().reflectionEntries()).hasSize(2);
	}

	@Test
	void registerReflectionEntriesForProperties() {
		Constructor<?> instanceCreator = InjectionConfiguration.class.getDeclaredConstructors()[0];
		Method nameWriteMethod = ReflectionUtils.findMethod(InjectionConfiguration.class, "setName", String.class);
		Method counterWriteMethod = ReflectionUtils.findMethod(InjectionConfiguration.class, "setCounter", Integer.class);
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(InjectionConfiguration.class)
				.withInstanceCreator(instanceCreator).withProperty(nameWriteMethod, new PropertyValue("name", "Hello"))
				.withProperty(counterWriteMethod, new PropertyValue("counter", 42)).build());
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) -> {
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
		assertThat(registry.reflection().reflectionEntries()).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionConfiguration.class);
			assertThat(entry.getConstructors()).contains(instanceCreator);
			assertThat(entry.getMethods()).containsOnly(counterWriteMethod);
			assertThat(entry.getFields()).isEmpty();
		});
		assertThat(registry.reflection().reflectionEntries()).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(BaseFactoryBean.class);
			assertThat(entry.getMethods()).containsOnly(ReflectionUtils.findMethod(
					BaseFactoryBean.class, "setNamingStrategy", String.class));
			assertThat(entry.getFields()).isEmpty();
		});
		assertThat(registry.reflection().reflectionEntries()).anySatisfy(defaultConstructor(IntegerFactoryBean.class));
		assertThat(registry.reflection().reflectionEntries()).hasSize(3);
	}

	@Test
	void registerReflectionEntriesForListOfInnerBeanDefinition() {
		Constructor<?> instanceCreator = InjectionConfiguration.class.getDeclaredConstructors()[0];
		Method countersWriteMethod = ReflectionUtils.findMethod(InjectionConfiguration.class, "setCounters", List.class);
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(InjectionConfiguration.class)
				.withInstanceCreator(instanceCreator).withProperty(countersWriteMethod, new PropertyValue("counters",
						List.of(BeanDefinitionBuilder.rootBeanDefinition(IntegerFactoryBean.class).getBeanDefinition(),
								BeanDefinitionBuilder.rootBeanDefinition(AnotherIntegerFactoryBean.class).getBeanDefinition())))
				.build());
		assertThat(registry.reflection().reflectionEntries()).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionConfiguration.class);
			assertThat(entry.getConstructors()).contains(instanceCreator);
			assertThat(entry.getMethods()).containsOnly(countersWriteMethod);
			assertThat(entry.getFields()).isEmpty();
		});
		assertThat(registry.reflection().reflectionEntries()).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(BaseFactoryBean.class);
			assertThat(entry.getMethods()).containsOnly(ReflectionUtils.findMethod(
					BaseFactoryBean.class, "setNamingStrategy", String.class));
			assertThat(entry.getFields()).isEmpty();
		});
		assertThat(registry.reflection().reflectionEntries()).anySatisfy(defaultConstructor(IntegerFactoryBean.class));
		assertThat(registry.reflection().reflectionEntries()).anySatisfy(defaultConstructor(AnotherIntegerFactoryBean.class));
		assertThat(registry.reflection().reflectionEntries()).hasSize(4);
	}

	private Consumer<DefaultNativeReflectionEntry> defaultConstructor(Class<?> type) {
		return (entry) -> {
			assertThat(entry.getType()).isEqualTo(type);
			assertThat(entry.getConstructors()).containsOnly(type.getDeclaredConstructors()[0]);
			assertThat(entry.getFields()).isEmpty();
			assertThat(entry.getMethods()).isEmpty();
			assertThat(entry.getAccess()).isEmpty();
		};
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

	@SuppressWarnings("unused")
	static class AnotherIntegerFactoryBean extends IntegerFactoryBean {

		public AnotherIntegerFactoryBean(Environment environment) {
			super(environment);
		}

	}

}

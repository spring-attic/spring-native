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

package org.springframework.boot.context.properties;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntry.Builder;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.BeanInfoFactory;
import org.springframework.beans.ExtendedBeanInfoFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.nativex.hint.Flag;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link BeanFactoryNativeConfigurationProcessor} that allows reflection access on
 * all declared methods of {@link ConfigurationProperties @ConfigurationProperties}
 * annotated types, their nested types and any complex types that are exposed as a sub-
 * namespace.
 *
 * @author Stephane Nicoll
 * @author Christoph Strobl
 * @author Sebastien Deleuze
 */
class ConfigurationPropertiesNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		String[] beanNames = beanFactory.getBeanNamesForAnnotation(ConfigurationProperties.class);
		for (String beanName : beanNames) {
			processConfigurationProperties(registry, beanFactory.getMergedBeanDefinition(beanName));
		}
	}

	private void processConfigurationProperties(NativeConfigurationRegistry registry, BeanDefinition beanDefinition) {
		Class<?> type = ClassUtils.getUserClass(beanDefinition.getResolvableType().toClass());
		new TypeProcessor(type).process(registry);
	}

	/**
	 * Process a given type for binding purposes, discovering any nested type it may
	 * expose via a property.
	 */
	private static class TypeProcessor {

		private static final BeanInfoFactory beanInfoFactory = new ExtendedBeanInfoFactory();

		private final Class<?> type;

		private final boolean constructorBinding;

		private final BeanInfo beanInfo;

		TypeProcessor(Class<?> type) {
			this(type, hasConstructorBinding(type));
		}

		private TypeProcessor(Class<?> type, boolean constructorBinding) {
			this.type = type;
			this.constructorBinding = constructorBinding;
			this.beanInfo = getBeanInfo(type);
		}

		private static boolean hasConstructorBinding(AnnotatedElement element) {
			return MergedAnnotations.from(element).isPresent(ConstructorBinding.class);
		}

		public void process(NativeConfigurationRegistry registry) {
			Builder reflection = registry.reflection().forType(this.type);
			if (!this.type.getPackageName().startsWith("java.")) {
				reflection.withFlags(Flag.allDeclaredMethods);
				Constructor<?> constructor = handleConstructor(reflection);
				if (this.constructorBinding && constructor != null) {
					handleValueObjectProperties(registry, constructor);
				}
				else if (this.beanInfo != null) {
					handleJavaBeanProperties(registry);
				}
			}
		}

		private Constructor<?> handleConstructor(Builder reflection) {
			Constructor<?> bindingConstructor = findBindingConstructor();
			if (bindingConstructor != null) {
				reflection.withExecutables(bindingConstructor);
				return bindingConstructor;
			}
			else {
				reflection.withFlags(Flag.allDeclaredConstructors);
				return null;
			}
		}

		private void handleValueObjectProperties(NativeConfigurationRegistry registry, Constructor<?> constructor) {
			for (int i = 0; i < constructor.getParameterCount(); i++) {
				ResolvableType propertyType = ResolvableType.forConstructorParameter(constructor, i);
				Class<?> nestedType = getNestedType(constructor.getParameters()[i].getName(), propertyType);
				if (nestedType != null) {
					new TypeProcessor(nestedType, true).process(registry);
				}
			}
		}

		private void handleJavaBeanProperties(NativeConfigurationRegistry registry) {
			for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
				Method readMethod = propertyDescriptor.getReadMethod();
				if (readMethod != null) {
					ResolvableType propertyType = ResolvableType.forMethodReturnType(readMethod, this.type);
					Class<?> nestedType = getNestedType(propertyDescriptor.getName(), propertyType);
					if (nestedType != null) {
						new TypeProcessor(nestedType).process(registry);
					}
				}
			}
		}

		private Class<?> getNestedType(String propertyName, ResolvableType propertyType) {
			Class<?> propertyClass = propertyType.toClass();
			if (propertyType.isArray()) {
				return propertyType.getComponentType().toClass();
			}
			else if (Collection.class.isAssignableFrom(propertyClass)) {
				return propertyType.as(Collection.class).getGeneric(0).toClass();
			}
			else if (Map.class.isAssignableFrom(propertyClass)) {
				return propertyType.as(Map.class).getGeneric(1).toClass();
			}
			else if (this.type.equals(propertyClass.getDeclaringClass())) {
				return propertyClass;
			}
			else {
				Field field = ReflectionUtils.findField(this.type, propertyName);
				if (field != null && isNestedConfigurationProperties(field)) {
					return propertyClass;
				}
			}
			return null;
		}

		private Constructor<?> findBindingConstructor() {
			Constructor<?>[] allConstructors = this.type.getDeclaredConstructors();
			if (allConstructors.length == 1) {
				return allConstructors[0];
			}
			if (this.constructorBinding) {
				List<Constructor<?>> candidates = Arrays.stream(allConstructors)
						.filter(TypeProcessor::hasConstructorBinding).collect(Collectors.toList());
				if (candidates.size() == 1) {
					return candidates.get(0);
				}
			}
			else {
				try {
					return this.type.getDeclaredConstructor();
				}
				catch (NoSuchMethodException ex) {
					// No default constructor
				}
			}
			return null;
		}


		private boolean isNestedConfigurationProperties(Field field) {
			return MergedAnnotations.from(field).isPresent(NestedConfigurationProperty.class);
		}

		private static BeanInfo getBeanInfo(Class<?> beanType) {
			try {
				BeanInfo beanInfo = beanInfoFactory.getBeanInfo(beanType);
				if (beanInfo != null) {
					return beanInfo;
				}
				return Introspector.getBeanInfo(beanType, Introspector.IGNORE_ALL_BEANINFO);
			}
			catch (IntrospectionException ex) {
				return null;
			}
		}

	}

}

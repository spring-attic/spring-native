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

package org.springframework.context.bootstrap.generator.bean.descriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Provide the {@link Executable} to use for a particular {@link BeanDefinition}.
 *
 * @author Stephane Nicoll
 */
class BeanInstanceExecutableSupplier {

	private static final Log logger = LogFactory.getLog(BeanInstanceExecutableSupplier.class);

	private final ConfigurableBeanFactory beanFactory;

	private final ClassLoader classLoader;

	BeanInstanceExecutableSupplier(ConfigurableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		this.classLoader = beanFactory.getBeanClassLoader();
	}

	Executable detectBeanInstanceExecutable(BeanDefinition beanDefinition) {
		Supplier<Class<?>> beanClass = () -> getBeanClass(beanDefinition);
		List<Class<?>> parameterTypes = determineParameterTypes(beanDefinition);
		Method resolvedFactoryMethod = resolveFactoryMethod(beanDefinition, beanClass, parameterTypes);
		if (resolvedFactoryMethod != null) {
			return resolvedFactoryMethod;
		}
		Class<?> factoryBeanClass = getFactoryBeanClass(beanDefinition);
		if (factoryBeanClass != null && !factoryBeanClass.equals(beanDefinition.getResolvableType().toClass())) {
			ResolvableType resolvableType = beanDefinition.getResolvableType();
			boolean isCompatible = ResolvableType.forClass(factoryBeanClass).as(FactoryBean.class)
					.getGeneric(0).isAssignableFrom(resolvableType);
			if (isCompatible) {
				return resolveConstructor(() -> factoryBeanClass, beanDefinition.getConstructorArgumentValues());
			}
			else {
				throw new IllegalStateException(String.format("Incompatible target type '%s' for factory bean '%s'",
						resolvableType.toClass().getName(), factoryBeanClass.getName()));
			}
		}
		Executable resolvedConstructor = resolveConstructor(beanClass, beanDefinition.getConstructorArgumentValues());
		if (resolvedConstructor != null) {
			return resolvedConstructor;
		}
		Executable resolvedConstructorOrFactoryMethod = getField(beanDefinition,
				"resolvedConstructorOrFactoryMethod", Executable.class);
		if (resolvedConstructorOrFactoryMethod != null) {
			logger.error("resolvedConstructorOrFactoryMethod required for " + beanDefinition);
			return resolvedConstructorOrFactoryMethod;
		}
		return null;
	}

	private List<Class<?>> determineParameterTypes(BeanDefinition beanDefinition) {
		List<Class<?>> parameterTypes = new ArrayList<>();
		ConstructorArgumentValues constructorArgumentValues = beanDefinition.getConstructorArgumentValues();
		if (constructorArgumentValues.isEmpty()) {
			return parameterTypes;
		}
		for (ValueHolder valueHolder : constructorArgumentValues.getIndexedArgumentValues().values()) {
			if (valueHolder.getType() != null) {
				parameterTypes.add(loadClass(valueHolder.getType()));
			}
			else {
				Object value = valueHolder.getValue();
				if (value instanceof BeanReference) {
					parameterTypes.add(this.beanFactory.getType(((BeanReference) value).getBeanName(), false));
				}
				else {
					parameterTypes.add(value.getClass());
				}
			}
		}
		return parameterTypes;
	}

	private Method resolveFactoryMethod(BeanDefinition beanDefinition, Supplier<Class<?>> beanClass,
			List<Class<?>> parameterTypes) {
		if (beanDefinition instanceof RootBeanDefinition) {
			RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) beanDefinition;
			Method resolvedFactoryMethod = rootBeanDefinition.getResolvedFactoryMethod();
			if (resolvedFactoryMethod != null) {
				return resolvedFactoryMethod;
			}
		}
		String factoryMethodName = beanDefinition.getFactoryMethodName();
		if (factoryMethodName != null) {
			List<Method> methods = new ArrayList<>();
			ReflectionUtils.doWithMethods(beanClass.get(), methods::add,
					(method) -> method.getName().equals(factoryMethodName));
			if (methods.size() >= 1) {
				return (Method) filter(methods, parameterTypes);
			}
		}
		return null;
	}

	private Executable resolveConstructor(Supplier<Class<?>> beanClass, ConstructorArgumentValues argumentValues) {
		Class<?> type = beanClass.get();
		Constructor<?>[] constructors = type.getDeclaredConstructors();
		if (constructors.length == 1) {
			return constructors[0];
		}
		for (Constructor<?> constructor : constructors) {
			if (MergedAnnotations.from(constructor).isPresent(Autowired.class)) {
				return constructor;
			}
			if (constructorMatchesArguments(constructor, argumentValues)) {
				return constructor;
			}
		}
		return null;
	}

	private boolean constructorMatchesArguments(Constructor<?> constructor, ConstructorArgumentValues argumentValues) {
		if (constructor.getParameterTypes().length != argumentValues.getArgumentCount()) {
			return false;
		}
		return Arrays.stream(constructor.getParameterTypes()).allMatch(paramType -> {
			for (int index = 0; index < argumentValues.getArgumentCount(); index++) {
				if (argumentValues.getArgumentValue(index, paramType) != null)
					return true;
			}
			return false;
		});
	}

	private Executable filter(List<? extends Executable> executables, List<Class<?>> parameterTypes) {
		List<? extends Executable> matches = executables.stream()
				.filter((executable) -> match(executable, parameterTypes)).collect(Collectors.toList());
		if (matches.size() > 1) {
			throw new IllegalStateException("Multiple matches with parameters " + parameterTypes);
		}
		return (matches.size() == 1) ? matches.get(0) : null;
	}

	private boolean match(Executable executable, List<Class<?>> parameterTypes) {
		if (executable.getParameterCount() != parameterTypes.size()) {
			return false;
		}
		Class<?>[] types = executable.getParameterTypes();
		for (int i = 0; i < types.length; i++) {
			if (!types[i].isAssignableFrom(parameterTypes.get(i))) {
				return false;
			}
		}
		return true;
	}

	private Class<?> getFactoryBeanClass(BeanDefinition beanDefinition) {
		if (beanDefinition instanceof RootBeanDefinition) {
			RootBeanDefinition rbd = (RootBeanDefinition) beanDefinition;
			if (rbd.hasBeanClass()) {
				Class<?> beanClass = rbd.getBeanClass();
				return FactoryBean.class.isAssignableFrom(beanClass) ? beanClass : null;
			}
		}
		return null;
	}

	private Class<?> getBeanClass(BeanDefinition beanDefinition) {
		ResolvableType resolvableType = beanDefinition.getResolvableType();
		if (resolvableType != ResolvableType.NONE) {
			return resolvableType.toClass();
		}
		String beanClassName = beanDefinition.getBeanClassName();
		if (beanClassName != null) {
			return loadClass(beanClassName);
		}
		throw new IllegalStateException("Failed to determine bean class of " + beanDefinition);
	}

	private Class<?> loadClass(String beanClassName) {
		try {
			return ClassUtils.forName(beanClassName, this.classLoader);
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalStateException("Failed to load class " + beanClassName);
		}
	}

	private <T> T getField(BeanDefinition beanDefinition, String fieldName, Class<T> targetType) {
		Field field = ReflectionUtils.findField(RootBeanDefinition.class, fieldName);
		ReflectionUtils.makeAccessible(field);
		return targetType.cast(ReflectionUtils.getField(field, beanDefinition));
	}

}

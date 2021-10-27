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

package org.springframework.aot.context.bootstrap.generator.infrastructure;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Extract init and destroy methods from {@link BeanDefinition}.
 *
 * @author Stephane Nicoll
 */
class InitDestroyMethodsDiscoverer {

	private static final String CLOSE_METHOD_NAME = "close";

	private static final String SHUTDOWN_METHOD_NAME = "shutdown";

	private final ConfigurableListableBeanFactory beanFactory;

	public InitDestroyMethodsDiscoverer(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Identify the beans that have init methods and return a mapping from bean name to
	 * init method names
	 * @param nativeConfiguration the registry to use to declare the init methods that
	 * should be available by reflection
	 * @return the mapping
	 */
	Map<String, List<Method>> registerInitMethods(NativeConfigurationRegistry nativeConfiguration) {
		return processLifecycleMethods(nativeConfiguration, this::processInitMethods);
	}

	private Set<Method> processInitMethods(BeanDefinition beanDefinition, Class<?> beanType) {
		Set<Method> methods = new LinkedHashSet<>();
		String initMethodName = beanDefinition.getInitMethodName();
		if (StringUtils.hasText(initMethodName)) {
			methods.add(findMethod(beanType, initMethodName));
		}
		for (String methodName : getExternallyManagedInitMethods(beanDefinition)) {
			methods.add(findMethod(beanType, methodName));
		}
		return methods;
	}

	/**
	 * Identify the beans that have destroy methods and return a mapping from bean name to
	 * destroy method names
	 * @param nativeConfiguration the registry to use to declare the destroy methods that
	 * should be available by reflection
	 * @return the mapping
	 */
	Map<String, List<Method>> registerDestroyMethods(NativeConfigurationRegistry nativeConfiguration) {
		return processLifecycleMethods(nativeConfiguration, this::processDestroyMethods);
	}

	Map<String, List<Method>> processLifecycleMethods(NativeConfigurationRegistry registry, BiFunction<BeanDefinition, Class<?>, Set<Method>> lifecycleMethodsFactory) {
		Map<String, List<Method>> lifecycleMethods = new LinkedHashMap<>();
		for (String beanName : this.beanFactory.getBeanDefinitionNames()) {
			BeanDefinition beanDefinition = this.beanFactory.getMergedBeanDefinition(beanName);
			Class<?> beanType = getBeanType(beanDefinition);
			Set<Method> methods = lifecycleMethodsFactory.apply(beanDefinition, beanType);
			if (!ObjectUtils.isEmpty(methods)) {
				lifecycleMethods.put(beanName, new ArrayList<>(methods));
				methods.forEach((method) -> registry.reflection().addExecutable(method));
			}
		}
		return lifecycleMethods;
	}

	private Set<Method> processDestroyMethods(BeanDefinition beanDefinition, Class<?> beanType) {
		Set<Method> methods = new LinkedHashSet<>();
		String destroyMethodName = beanDefinition.getDestroyMethodName();
		if (AbstractBeanDefinition.INFER_METHOD.equals(destroyMethodName)) {
			// AutoCloseable is handled explicitly, no need to infer
			if (!AutoCloseable.class.isAssignableFrom(beanType)) {
				Method method = detectInferredDestroyMethod(beanType);
				if (method != null) {
					methods.add(method);
				}
			}
		}
		else if (StringUtils.hasText(destroyMethodName)) {
			methods.add(findMethod(beanType, destroyMethodName));
		}
		for (String methodName : getExternallyManagedDestroyMethods(beanDefinition)) {
			methods.add(findMethod(beanType, methodName));
		}
		return methods;
	}

	private Iterable<String> getExternallyManagedInitMethods(BeanDefinition beanDefinition) {
		if (beanDefinition instanceof RootBeanDefinition) {
			return normalizeExternallyManagedLifecycleMethodNames(
					((RootBeanDefinition) beanDefinition).getExternallyManagedInitMethods());
		}
		return Collections.emptyList();
	}

	private Iterable<String> getExternallyManagedDestroyMethods(BeanDefinition beanDefinition) {
		if (beanDefinition instanceof RootBeanDefinition) {
			return normalizeExternallyManagedLifecycleMethodNames(
					((RootBeanDefinition) beanDefinition).getExternallyManagedDestroyMethods());
		}
		return Collections.emptyList();
	}

	private Iterable<String> normalizeExternallyManagedLifecycleMethodNames(Set<String> candidates) {
		return candidates.stream().map(this::normalizeMethodName)
				.collect(Collectors.toSet());
	}

	/**
	 * Normalize an externally managed method name as it may contain either the method
	 * name itself or a {@link ClassUtils#getQualifiedMethodName(Method) qualified
	 * representation}.
	 * @param candidate the candidate method name representation
	 * @return a method name
	 */
	private String normalizeMethodName(String candidate) {
		return (candidate.contains("."))
				? candidate.substring(candidate.lastIndexOf('.') + 1)
				: candidate;
	}

	private Method detectInferredDestroyMethod(Class<?> beanType) {
		if (!DisposableBean.class.isAssignableFrom(beanType)) {
			try {
				return beanType.getMethod(CLOSE_METHOD_NAME);
			}
			catch (NoSuchMethodException ex) {
				try {
					return beanType.getMethod(SHUTDOWN_METHOD_NAME);
				}
				catch (NoSuchMethodException ex2) {
					// no candidate destroy method found
				}
			}
		}
		return null;
	}

	private Method findMethod(Class<?> beanType, String methodName) {
		Method method = ReflectionUtils.findMethod(beanType, methodName);
		if (method == null) {
			throw new IllegalStateException("Lifecycle method annotation '" + methodName + "' not found on: " + beanType);
		}
		return method;
	}

	private Class<?> getBeanType(BeanDefinition beanDefinition) {
		ResolvableType resolvableType = beanDefinition.getResolvableType();
		if (resolvableType != ResolvableType.NONE) {
			return resolvableType.toClass();
		}
		if (beanDefinition.getBeanClassName() != null) {
			return loadBeanClassName(beanDefinition.getBeanClassName());
		}
		return Object.class;
	}

	private Class<?> loadBeanClassName(String className) {
		try {
			return ClassUtils.forName(className, this.beanFactory.getBeanClassLoader());
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalStateException(
					"Bean definition refers to invalid class '" + className + "'", ex);
		}
	}

}

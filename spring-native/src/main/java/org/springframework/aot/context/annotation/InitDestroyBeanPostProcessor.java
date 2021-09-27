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

package org.springframework.aot.context.annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link BeanPostProcessor} that provides the same init/destroy callback features than
 * {@link CommonAnnotationBeanPostProcessor} using a pre-computed list.
 *
 * @author Stephane Nicoll
 */
public class InitDestroyBeanPostProcessor implements BeanPostProcessor, DestructionAwareBeanPostProcessor, Ordered {

	private static final Log logger = LogFactory.getLog(InitDestroyBeanPostProcessor.class);

	private final Map<String, List<String>> initMethods;

	private final Map<String, List<String>> destroyMethods;

	public InitDestroyBeanPostProcessor(Map<String, List<String>> initMethods,
			Map<String, List<String>> destroyMethods) {
		this.initMethods = initMethods;
		this.destroyMethods = destroyMethods;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		List<String> methodNames = this.initMethods.getOrDefault(beanName, Collections.emptyList());
		for (String methodName : methodNames) {
			invokeInitMethod(bean, beanName, methodName);
		}
		return bean;
	}

	private void invokeInitMethod(Object bean, String beanName, String methodName) {
		Method method = findMethod(bean, methodName);
		if (logger.isTraceEnabled()) {
			logger.trace("Invoking init method on bean '" + beanName + "': " + method);
		}
		try {
			invokeMethod(bean, method);
		}
		catch (InvocationTargetException ex) {
			throw new BeanCreationException(beanName, "Invocation of init method failed", ex.getTargetException());
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Failed to invoke init method", ex);
		}
	}

	@Override
	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		List<String> methodNames = this.destroyMethods.getOrDefault(beanName, Collections.emptyList());
		for (String methodName : methodNames) {
			invokeDestroyMethod(bean, beanName, methodName);
		}
	}

	private void invokeDestroyMethod(Object bean, String beanName, String methodName) {
		Method method = findMethod(bean, methodName);
		if (logger.isTraceEnabled()) {
			logger.trace("Invoking destroy method on bean '" + beanName + "': " + method);
		}
		try {
			invokeMethod(bean, method);
		}
		catch (InvocationTargetException ex) {
			String msg = "Destroy method on bean with name '" + beanName + "' threw an exception";
			if (logger.isDebugEnabled()) {
				logger.warn(msg, ex.getTargetException());
			}
			else {
				logger.warn(msg + ": " + ex.getTargetException());
			}
		}
		catch (Throwable ex) {
			logger.warn("Failed to invoke destroy method on bean with name '" + beanName + "'", ex);
		}
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	private Method findMethod(Object bean, String methodName) {
		return ReflectionUtils.findMethod(bean.getClass(), methodName);
	}

	private void invokeMethod(Object target, Method method) throws InvocationTargetException, IllegalAccessException {
		ReflectionUtils.makeAccessible(method);
		method.invoke(target, (Object[]) null);
	}

}

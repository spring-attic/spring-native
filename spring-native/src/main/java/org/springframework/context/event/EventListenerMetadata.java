/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.context.event;

import java.lang.reflect.Method;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.util.ReflectionUtils;

/**
 * Captures the necessary metadata to build an {@link ApplicationListener} based on a
 * method annotated with {@link EventListener}.
 *
 * @author Stephane Nicoll
 */
public final class EventListenerMetadata {

	private final String beanName;

	private final Class<?> beanType;

	private final String eventListenerFactoryBeanName;

	private final Method method;

	EventListenerMetadata(Builder builder) {
		this.beanName = builder.beanName;
		this.beanType = builder.beanType;
		this.eventListenerFactoryBeanName = builder.eventListenerFactoryBeanName;
		this.method = builder.method;
	}

	public String getBeanName() {
		return this.beanName;
	}

	public Class<?> getBeanType() {
		return this.beanType;
	}

	public String getEventListenerFactoryBeanName() {
		return this.eventListenerFactoryBeanName;
	}

	public Method getMethod() {
		return this.method;
	}

	public static Builder forBean(String beanName, Class<?> beanType) {
		return new Builder(beanName, beanType);
	}

	public static class Builder {

		private final String beanName;

		private final Class<?> beanType;

		private String eventListenerFactoryBeanName;

		private Method method;

		private Builder(String beanName, Class<?> beanType) {
			this.beanName = beanName;
			this.beanType = beanType;
		}

		public Builder eventListenerFactoryBeanName(String eventListenerFactoryBeanName) {
			this.eventListenerFactoryBeanName = eventListenerFactoryBeanName;
			return this;
		}

		public EventListenerMetadata annotatedMethod(String methodName, Class<?>... parameterTypes) {
			this.method = findMethod(methodName, parameterTypes);
			return new EventListenerMetadata(this);
		}

		private Method findMethod(String methodName, Class<?>... parameterTypes) {
			Method method = ReflectionUtils.findMethod(this.beanType, methodName, parameterTypes);
			if (method == null) {
				throw new IllegalStateException("No method named '" + methodName + "' found on " + this.beanType);
			}
			return AopUtils.selectInvocableMethod(method, this.beanType);
		}

	}

}

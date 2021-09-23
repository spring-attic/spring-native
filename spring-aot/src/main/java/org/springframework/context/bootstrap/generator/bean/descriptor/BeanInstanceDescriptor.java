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

import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.squareup.javapoet.CodeBlock;

import org.springframework.beans.PropertyValue;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Describe how an instance of a bean can be supplied.
 *
 * @author Stephane Nicoll
 * @author Christoph Strobl
 */
public final class BeanInstanceDescriptor {

	private final ResolvableType beanType;

	private final MemberDescriptor<Executable> instanceCreator;

	private final List<InstanceCallback> instanceCallbacks;

	private final List<MemberDescriptor<?>> injectionPoints;

	private final List<PropertyDescriptor> properties;

	private final List<MemberDescriptor<Method>> initializationMethods;

	private BeanInstanceDescriptor(Builder builder) {
		this.beanType = builder.beanType;
		this.instanceCreator = builder.instanceCreator;
		this.instanceCallbacks = new ArrayList<>(builder.instanceCallbacks);
		this.injectionPoints = new ArrayList<>(builder.injectionPoints);
		this.properties = new ArrayList<>(builder.properties);
		this.initializationMethods = new ArrayList<>(builder.initializationMethods);
	}

	/**
	 * Create a new builder for the specified bean type.
	 * @param beanType the type of the bean
	 * @return a new builder
	 */
	public static Builder of(ResolvableType beanType) {
		return new Builder(beanType);
	}

	/**
	 * Create a new builder for the specified bean type.
	 * @param beanType the type of the bean
	 * @return a new builder
	 */
	public static Builder of(Class<?> beanType) {
		return of(ResolvableType.forClass(beanType));
	}

	/**
	 * Return the {@link ResolvableType type} of the bean.
	 * @return the type of the bean
	 */
	public ResolvableType getBeanType() {
		return this.beanType;
	}

	/**
	 * Return the bean type {@link Class}.
	 * @return the class of the bean, as defined by the user
	 */
	public Class<?> getUserBeanClass() {
		return ClassUtils.getUserClass(beanType.resolve(Object.class));
	}

	/**
	 * Return the {@link Executable} that should be used to instantiate the bean or
	 * {@code null} if no such information is available.
	 * @return the method or constructor to use to create the bean
	 */
	public MemberDescriptor<Executable> getInstanceCreator() {
		return this.instanceCreator;
	}

	/**
	 * Return the callbacks that should be honored after the instance is created but
	 * before injection points apply.
	 * @return the instance callbacks, if any.
	 */
	public List<InstanceCallback> getInstanceCallbacks() {
		return this.instanceCallbacks;
	}

	/**
	 * Return the injection points to invoke to populate extra dependencies for the bean,
	 * in the order they should be invoked.
	 * @return the injection points, if any
	 */
	public List<MemberDescriptor<?>> getInjectionPoints() {
		return this.injectionPoints;
	}

	/**
	 * Return the properties that should be set for the bean. Properties are automatically
	 * applied by the bean factory.
	 * @return the properties, if any
	 */
	public List<PropertyDescriptor> getProperties() {
		return this.properties;
	}

	/**
	 * Return the {@link MemberDescriptor methods} that should be honored once to initialize the bean after creation.
	 * @return the initialization callbacks, if any. Never {@literal null}.
	 */
	public List<MemberDescriptor<Method>> getInitializationMethods() {
		return initializationMethods;
	}

	/**
	 * Describe a {@link Member} that is used to initialize a Bean instance.
	 * @param <T> the member type
	 */
	public static class MemberDescriptor<T extends Member> {

		private final T member;

		private final boolean required;

		public MemberDescriptor(T member, boolean required) {
			this.member = member;
			this.required = required;
		}

		public T getMember() {
			return this.member;
		}

		public boolean isRequired() {
			return this.required;
		}

	}

	/**
	 * Wraps the code that's necessary to honor instance callbacks if any.
	 */
	public static class InstanceCallback {

		private final Function<String, CodeBlock> code;

		public InstanceCallback(Function<String, CodeBlock> code) {
			this.code = code;
		}

		public CodeBlock write(String beanVariable) {
			return this.code.apply(beanVariable);
		}
	}

	/**
	 * Describe a property that is used to initialize a Bean instance.
	 */
	public static class PropertyDescriptor {

		private final Method writeMethod;

		private final PropertyValue propertyValue;

		public PropertyDescriptor(Method writeMethod, PropertyValue propertyValue) {
			this.writeMethod = writeMethod;
			this.propertyValue = propertyValue;
		}

		public Method getWriteMethod() {
			return this.writeMethod;
		}

		public PropertyValue getPropertyValue() {
			return this.propertyValue;
		}

	}

	public static class Builder {

		private final ResolvableType beanType;

		private MemberDescriptor<Executable> instanceCreator;

		private final List<InstanceCallback> instanceCallbacks = new ArrayList<>();

		private final List<MemberDescriptor<?>> injectionPoints = new ArrayList<>();

		private final List<PropertyDescriptor> properties = new ArrayList<>();

		private final List<MemberDescriptor<Method>> initializationMethods = new ArrayList<>();

		Builder(ResolvableType beanType) {
			Assert.notNull(beanType, "BeanType must not be null");
			this.beanType = beanType;
		}

		public Builder withInstanceCreator(Executable executable) {
			this.instanceCreator = (executable != null) ? new MemberDescriptor<>(executable, true) : null;
			return this;
		}

		public Builder withInstanceCallback(Function<String, CodeBlock> code) {
			this.instanceCallbacks.add(new InstanceCallback(code));
			return this;
		}

		public Builder withInstanceCallbacks(List<InstanceCallback> instanceCallbacks) {
			this.instanceCallbacks.addAll(instanceCallbacks);
			return this;
		}

		public Builder withInjectionPoint(Member member, boolean required) {
			this.injectionPoints.add(new MemberDescriptor<>(member, required));
			return this;
		}

		public Builder withInjectionPoints(List<MemberDescriptor<?>> injectionPoints) {
			this.injectionPoints.addAll(injectionPoints);
			return this;
		}

		public Builder withProperty(Method writeMethod, PropertyValue propertyValue) {
			this.properties.add(new PropertyDescriptor(writeMethod, propertyValue));
			return this;
		}

		public Builder withProperties(List<PropertyDescriptor> propertyValues) {
			this.properties.addAll(propertyValues);
			return this;
		}

		public Builder withInitMethods(List<MemberDescriptor<Method>> initMethods) {
			this.initializationMethods.addAll(initMethods);
			return this;
		}

		public Builder withInitMethod(MemberDescriptor<Method> initMethod) {
			this.initializationMethods.add(initMethod);
			return this;
		}

		public BeanInstanceDescriptor build() {
			return new BeanInstanceDescriptor(this);
		}

	}

}

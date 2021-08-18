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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Describe how an instance of a bean can be supplied.
 *
 * @author Stephane Nicoll
 */
public final class BeanInstanceDescriptor {

	private final ResolvableType beanType;

	private final MemberDescriptor<Executable> instanceCreator;

	private final List<MemberDescriptor<?>> injectionPoints;

	public BeanInstanceDescriptor(ResolvableType beanType, Executable instanceCreator, List<MemberDescriptor<?>> injectionPoints) {
		Assert.notNull(beanType, "BeanType must not be null");
		this.beanType = beanType;
		this.instanceCreator = (instanceCreator != null) ? new MemberDescriptor<>(instanceCreator, true) : null;
		this.injectionPoints = new ArrayList<>(injectionPoints);
	}

	public BeanInstanceDescriptor(ResolvableType beanType, Executable instanceCreator) {
		this(beanType, instanceCreator, Collections.emptyList());
	}

	public BeanInstanceDescriptor(Class<?> beanType, Executable instanceCreator) {
		this(ResolvableType.forClass(beanType), instanceCreator);
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
	 * Return the injection points to invoke to populate extra dependencies for the bean,
	 * in the order they should be invoked.
	 * @return the injection points, if any
	 */
	public List<MemberDescriptor<?>> getInjectionPoints() {
		return this.injectionPoints;
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

}

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
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Describe how an instance of a bean can be supplied.
 *
 * @author Stephane Nicoll
 */
public final class BeanInstanceDescriptor {

	private final Class<?> beanType;

	private final MemberDescriptor<Executable> instanceCreator;

	private final List<MemberDescriptor<?>> injectionPoints;

	public BeanInstanceDescriptor(Class<?> beanType, Executable instanceCreator, List<MemberDescriptor<?>> injectionPoints) {
		Assert.notNull(beanType, "BeanType must not be null");
		this.beanType = ClassUtils.getUserClass(beanType);
		this.instanceCreator = (instanceCreator != null) ? new MemberDescriptor<>(instanceCreator, true) : null;
		this.injectionPoints = new ArrayList<>(injectionPoints);
	}

	public BeanInstanceDescriptor(Class<?> beanType, Executable instanceCreator) {
		this(beanType, instanceCreator, Collections.emptyList());
	}

	/**
	 * Return the {@link Class type} of the bean.
	 * @return the type of the bean
	 */
	public Class<?> getBeanType() {
		return this.beanType;
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

		/**
		 * Specify if accessing this member to instantiate the bean instance is allowed
		 * from the specified {@code packageName}.
		 * @param packageName the package name to use to instantiate the bean
		 * @return {@link true} if this member and its dependencies can be accessed from the specified package
		 */
		public boolean isAccessibleFrom(String packageName) {
			if (!isAccessibleFrom(ResolvableType.forClass(this.member.getDeclaringClass()), packageName)) {
				return false;
			}
			return isAccessibleFrom(this.member, packageName);
		}

		private static boolean isAccessibleFrom(Member member, String packageName) {
			if (!isAccessibleFrom(member.getModifiers(), member.getDeclaringClass().getPackageName(), packageName)) {
				return false;
			}
			if (member instanceof Constructor) {
				Constructor<?> constructor = (Constructor<?>) member;
				return isAccessibleFrom(constructor.getParameters(), (i) -> ResolvableType.forConstructorParameter(constructor, i), packageName);
			}
			else if (member instanceof Field) {
				return isAccessibleFrom(ResolvableType.forField((Field) member), packageName);
			}
			else if (member instanceof Method) {
				Method method = (Method) member;
				if (!isAccessibleFrom(method.getReturnType(), packageName)) {
					return false;
				}
				return isAccessibleFrom(method.getParameters(), (i) -> ResolvableType.forMethodParameter(method, i), packageName);
			}
			return true;
		}

		private static boolean isAccessibleFrom(Parameter[] parameters, Function<Integer, ResolvableType> parameterTypeFactory, String packageName) {
			for (int i = 0; i < parameters.length; i++) {
				if (!isAccessibleFrom(parameterTypeFactory.apply(i), packageName)) {
					return false;
				}
			}
			return true;
		}

		private static boolean isAccessibleFrom(ResolvableType target, String packageName) {
			// resolve to the actual class as the proxy won't have the same characteristics
			ResolvableType nonProxyTarget = target.as(ClassUtils.getUserClass(target.toClass()));
			if (!isAccessibleFrom(nonProxyTarget.toClass(), packageName)) {
				return false;
			}
			Class<?> declaringClass = nonProxyTarget.toClass().getDeclaringClass();
			if (declaringClass != null) {
				if (!isAccessibleFrom(declaringClass, packageName)) {
					return false;
				}
			}
			if (nonProxyTarget.hasGenerics()) {
				for (ResolvableType generic : nonProxyTarget.getGenerics()) {
					if (!isAccessibleFrom(generic, packageName)) {
						return false;
					}
				}
			}
			return true;
		}

		private static boolean isAccessibleFrom(Class<?> target, String packageName) {
			Class<?> candidate = ClassUtils.getUserClass(target);
			return isAccessibleFrom(candidate.getModifiers(), candidate.getPackageName(), packageName);
		}

		private static boolean isAccessibleFrom(int modifiers, String actualPackageName, String targetPackageName) {
			return Modifier.isPublic(modifiers) || targetPackageName.equals(actualPackageName);
		}

	}

}

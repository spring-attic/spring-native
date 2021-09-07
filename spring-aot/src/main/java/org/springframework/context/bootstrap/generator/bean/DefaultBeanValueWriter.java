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

package org.springframework.context.bootstrap.generator.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.squareup.javapoet.CodeBlock.Builder;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.InstanceCallback;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;

/**
 * Default {@link BeanValueWriter} implementation.
 *
 * @author Stephane Nicoll
 */
public class DefaultBeanValueWriter implements BeanValueWriter {

	private final BeanInstanceDescriptor descriptor;

	private final BeanDefinition beanDefinition;

	private final InjectionPointWriter injectionPointWriter;

	public DefaultBeanValueWriter(BeanInstanceDescriptor descriptor, BeanDefinition beanDefinition) {
		this.descriptor = descriptor;
		this.beanDefinition = beanDefinition;
		this.injectionPointWriter = new InjectionPointWriter();
	}

	@Override
	public BeanInstanceDescriptor getDescriptor() {
		return this.descriptor;
	}

	@Override
	public void writeValueSupplier(Builder code) {
		MemberDescriptor<Executable> descriptor = getDescriptor().getInstanceCreator();
		if (descriptor == null) {
			throw new IllegalStateException("Could not handle " + this.beanDefinition + ": no instance creator available");
		}
		Executable member = descriptor.getMember();
		if (member instanceof Constructor) {
			writeBeanInstantiation(code, (Constructor<?>) member);
		}
		if (member instanceof Method) {
			writeBeanInstantiation(code, (Method) member);
		}
	}

	private void writeBeanInstantiation(Builder code, Constructor<?> constructor) {
		Class<?> declaringType = getDescriptor().getUserBeanClass();
		boolean innerClass = isInnerClass(declaringType);
		List<Class<?>> parameterTypes = new ArrayList<>(Arrays.asList(constructor.getParameterTypes()));
		if (innerClass) { // Remove the implicit argument
			parameterTypes.remove(0);
		}
		boolean multiStatements = !this.descriptor.getInstanceCallbacks().isEmpty() ||
				!this.descriptor.getInjectionPoints().isEmpty();
		int minArgs = isInnerClass(declaringType) ? 2 : 1;
		// Shortcut for common case
		if (!multiStatements && constructor.getParameterTypes().length < minArgs) {
			if (innerClass) {
				code.add("() -> context.getBean($T.class).new $L()", declaringType.getEnclosingClass(), declaringType.getSimpleName());
			}
			else {
				code.add("() -> new $T()", getDescriptor().getUserBeanClass());
			}
			return;
		}
		code.add("(instanceContext) ->");
		branch(multiStatements, () -> code.beginControlFlow(""), () -> code.add(" "));
		if (multiStatements) {
			code.add("$T bean = ", declaringType);
		}
		code.add(this.injectionPointWriter.writeInstantiation(constructor));
		if (multiStatements) {
			code.add(";\n");
		}
		for (InstanceCallback instanceCallback : this.descriptor.getInstanceCallbacks()) {
			code.addStatement(instanceCallback.write("bean"));
		}
		for (MemberDescriptor<?> injectionPoint : this.descriptor.getInjectionPoints()) {
			code.add(this.injectionPointWriter.writeInjection(injectionPoint.getMember(), injectionPoint.isRequired())).add(";\n");
		}
		if (multiStatements) {
			code.add("return bean;\n");
			code.unindent().add("}");
		}
	}

	private static boolean isInnerClass(Class<?> type) {
		return type.isMemberClass() && !Modifier.isStatic(type.getModifiers());
	}

	private void writeBeanInstantiation(Builder code, Method method) {
		List<Class<?>> parameterTypes = new ArrayList<>(Arrays.asList(method.getParameterTypes()));
		boolean multiStatements = !this.descriptor.getInstanceCallbacks().isEmpty() ||
				!this.descriptor.getInjectionPoints().isEmpty();
		Class<?> declaringType = method.getDeclaringClass();
		// Shortcut for common case
		if (!multiStatements && parameterTypes.isEmpty()) {
			code.add("() -> ");
			branch(Modifier.isStatic(method.getModifiers()),
					() -> code.add("$T", declaringType),
					() -> code.add("context.getBean($T.class)", declaringType));
			code.add(".$L()", method.getName());
			return;
		}
		code.add("(instanceContext) ->");
		branch(multiStatements, () -> code.beginControlFlow(""), () -> code.add(" "));
		if (multiStatements) {
			code.add("$T bean = ", getDescriptor().getUserBeanClass());
		}
		code.add(this.injectionPointWriter.writeInstantiation(method));
		if (multiStatements) {
			code.add(";\n");
		}
		for (InstanceCallback instanceCallback : this.descriptor.getInstanceCallbacks()) {
			code.addStatement(instanceCallback.write("bean"));
		}
		for (MemberDescriptor<?> injectionPoint : this.descriptor.getInjectionPoints()) {
			code.add(this.injectionPointWriter.writeInjection(injectionPoint.getMember(), injectionPoint.isRequired())).add(";\n");
		}
		if (multiStatements) {
			code.add("return bean;\n");
			code.unindent().add("}");
		}
	}

	private static void branch(boolean condition, Runnable ifTrue, Runnable ifFalse) {
		if (condition) {
			ifTrue.run();
		}
		else {
			ifFalse.run();
		}
	}

}

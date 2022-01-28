/*
 * Copyright 2019-2022 the original author or authors.
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

package org.springframework.aot.context.bootstrap.generator.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;

import org.springframework.aot.beans.factory.BeanDefinitionRegistrar.InstanceSupplierContext;
import org.springframework.aot.context.bootstrap.generator.bean.support.ParameterWriter;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Write the necessary code to {@link #writeInstantiation(Executable) create a bean
 * instance} or {@link #writeInjection(Member, boolean) inject dependencies}.
 * <p/>
 * The writer expects a number of variables to be available and/or accessible:
 * <ul>
 *     <li>{@code beanFactory}: the general {@code DefaultListableBeanFactory}</li>
 *     <li>{@code instanceContext}: the {@link InstanceSupplierContext} callback</li>
 *     <li>{@code bean}: the variable that refers to the bean instance</li>
 * </ul>
 *
 * @author Stephane Nicoll
 * @author Brian Clozel
 */
class InjectionPointWriter {

	private final ParameterWriter parameterWriter = new ParameterWriter();

	CodeBlock writeInstantiation(Executable creator) {
		if (creator instanceof Constructor) {
			return write((Constructor<?>) creator);
		}
		if (creator instanceof Method) {
			return writeMethodInstantiation((Method) creator);
		}
		throw new IllegalArgumentException("Could not handle creator " + creator);
	}

	CodeBlock writeInjection(Member member, boolean required) {
		if (member instanceof Method) {
			return writeMethodInjection((Method) member, required);
		}
		if (member instanceof Field) {
			return writeFieldInjection((Field) member, required);
		}
		throw new IllegalArgumentException("Could not handle member " + member);
	}

	private CodeBlock write(Constructor<?> creator) {
		CodeBlock.Builder code = CodeBlock.builder();
		Class<?> declaringType = ClassUtils.getUserClass(creator.getDeclaringClass());
		boolean innerClass = isInnerClass(declaringType);
		Class<?>[] parameterTypes = Arrays.stream(creator.getParameters()).map(Parameter::getType)
				.toArray(Class<?>[]::new);
		// Shortcut for common case
		if (innerClass && parameterTypes.length == 1) {
			code.add("beanFactory.getBean($T.class).new $L()", declaringType.getEnclosingClass(),
					declaringType.getSimpleName());
			return code.build();
		}
		if (parameterTypes.length == 0) {
			code.add("new $T()", declaringType);
			return code.build();
		}
		boolean isAmbiguous = Arrays.stream(creator.getDeclaringClass().getDeclaredConstructors())
				.filter(constructor -> constructor.getParameterCount() == parameterTypes.length).count() > 1;
		code.add("instanceContext.create(beanFactory, (attributes) ->");
		List<CodeBlock> parameters = resolveParameters(creator.getParameters(), isAmbiguous);
		if (innerClass) { // Remove the implicit argument
			parameters.remove(0);
		}

		code.add(" ");
		if (innerClass) {
			code.add("beanFactory.getBean($T.class).new $L(", declaringType.getEnclosingClass(),
					declaringType.getSimpleName());
		}
		else {
			code.add("new $T(", declaringType);
		}
		for (int i = 0; i < parameters.size(); i++) {
			code.add(parameters.get(i));
			if (i < parameters.size() - 1) {
				code.add(", ");
			}
		}
		code.add(")");
		code.add(")");
		return code.build();
	}

	private static boolean isInnerClass(Class<?> type) {
		return type.isMemberClass() && !Modifier.isStatic(type.getModifiers());
	}

	private CodeBlock writeMethodInstantiation(Method injectionPoint) {
		if (injectionPoint.getParameterCount() == 0) {
			Builder code = CodeBlock.builder();
			Class<?> declaringType = injectionPoint.getDeclaringClass();
			if (Modifier.isStatic(injectionPoint.getModifiers())) {
				code.add("$T", declaringType);
			}
			else {
				code.add("beanFactory.getBean($T.class)", declaringType);
			}
			code.add(".$L()", injectionPoint.getName());
			return code.build();
		}
		return write(injectionPoint, (code) -> code.add(".create(beanFactory, (attributes) ->"), true);
	}

	private CodeBlock writeMethodInjection(Method injectionPoint, boolean required) {
		Consumer<Builder> attributesResolver = (code) -> {
			if (required) {
				code.add(".invoke(beanFactory, (attributes) ->");
			}
			else {
				code.add(".resolve(beanFactory, false).ifResolved((attributes) ->");
			}
		};
		return write(injectionPoint, attributesResolver, false);
	}

	private CodeBlock write(Method injectionPoint, Consumer<Builder> attributesResolver, boolean instantiation) {
		CodeBlock.Builder code = CodeBlock.builder();
		code.add("instanceContext");
		if (!instantiation) {
			code.add(".method($S, ", injectionPoint.getName());
			code.add(this.parameterWriter.writeExecutableParameterTypes(injectionPoint));
			code.add(")\n").indent().indent();
		}
		attributesResolver.accept(code);
		List<CodeBlock> parameters = resolveParameters(injectionPoint.getParameters(), false);
		code.add(" ");
		if (instantiation) {
			if (Modifier.isStatic(injectionPoint.getModifiers())) {
				code.add("$T", injectionPoint.getDeclaringClass());
			}
			else {
				code.add("beanFactory.getBean($T.class)", injectionPoint.getDeclaringClass());
			}
		}
		else {
			code.add("bean");
		}
		code.add(".$L(", injectionPoint.getName());
		for (int i = 0; i < parameters.size(); i++) {
			code.add(parameters.get(i));
			if (i < parameters.size() - 1) {
				code.add(", ");
			}
		}
		code.add(")");
		code.add(")");
		if (!instantiation) {
			code.unindent().unindent();
		}
		return code.build();
	}

	CodeBlock writeFieldInjection(Field injectionPoint, boolean required) {
		CodeBlock.Builder code = CodeBlock.builder();
		code.add("instanceContext.field($S, $T.class", injectionPoint.getName(), injectionPoint.getType());
		code.add(")\n").indent().indent();
		if (required) {
			code.add(".invoke(beanFactory, (attributes) ->");
		}
		else {
			code.add(".resolve(beanFactory, false).ifResolved((attributes) ->");
		}
		boolean hasAssignment = Modifier.isPrivate(injectionPoint.getModifiers());
		if (hasAssignment) {
			code.beginControlFlow("");
			String fieldName = String.format("%sField", injectionPoint.getName());
			code.addStatement("$T $L = $T.findField($T.class, $S, $T.class)", Field.class, fieldName, ReflectionUtils.class,
					injectionPoint.getDeclaringClass(), injectionPoint.getName(), injectionPoint.getType());
			code.addStatement("$T.makeAccessible($L)", ReflectionUtils.class, fieldName);
			code.addStatement("$T.setField($L, bean, attributes.get(0))", ReflectionUtils.class, fieldName);
			code.unindent().add("}");
		}
		else {
			code.add(" bean.$L = attributes.get(0)", injectionPoint.getName());
		}
		code.add(")").indent().indent();
		return code.build();
	}

	private List<CodeBlock> resolveParameters(Parameter[] parameters, boolean shouldCast) {
		List<CodeBlock> parameterValues = new ArrayList<>();
		for (int i = 0; i < parameters.length; i++) {
			if (shouldCast) {
				parameterValues.add(CodeBlock.of("attributes.get($L, $T.class)", i, parameters[i].getType()));
			}
			else {
				parameterValues.add(CodeBlock.of("attributes.get($L)", i));
			}
		}
		return parameterValues;
	}

}

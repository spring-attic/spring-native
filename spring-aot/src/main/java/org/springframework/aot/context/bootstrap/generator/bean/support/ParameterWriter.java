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

package org.springframework.aot.context.bootstrap.generator.bean.support;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;

import org.springframework.core.ResolvableType;
import org.springframework.util.ObjectUtils;

/**
 * Support for writing parameters.
 *
 * @author Stephane Nicoll
 */
public final class ParameterWriter {

	/**
	 * Write the specified parameter {@code value}.
	 * @param value the value of the parameter
	 * @param parameterType the type of the parameter
	 * @return the value of the parameter
	 */
	public CodeBlock writeParameterValue(Object value, ResolvableType parameterType) {
		Builder code = CodeBlock.builder();
		writeParameterValue(code, value, parameterType);
		return code.build();
	}

	/**
	 * Write the parameter types of the specified {@link Executable}.
	 * @param executable the executable
	 * @return the parameter types of the executable as a comma separated list
	 */
	public CodeBlock writeExecutableParameterTypes(Executable executable) {
		Class<?>[] parameterTypes = Arrays.stream(executable.getParameters())
				.map(Parameter::getType).toArray(Class<?>[]::new);
		return CodeBlock.of(Arrays.stream(parameterTypes).map((d) -> "$T.class")
				.collect(Collectors.joining(", ")), (Object[]) parameterTypes);
	}

	private void writeParameterValue(Builder code, Object value, ResolvableType parameterType) {
		if (parameterType.isArray()) {
			code.add("new $T { ", parameterType.toClass());
			writeAll(code, Arrays.asList(ObjectUtils.toObjectArray(value)), parameterType.getComponentType());
			code.add(" }");
		}
		else if (value instanceof List) {
			List<?> list = (List<?>) value;
			if (list.isEmpty()) {
				code.add("$T.emptyList()", Collections.class);
			}
			else {
				code.add("$T.of(", List.class);
				ResolvableType collectionType = parameterType.as(List.class).getGenerics()[0];
				writeAll(code, list, collectionType);
				code.add(")");
			}
		}
		else if (value instanceof Set) {
			Set<?> set = (Set<?>) value;
			if (set.isEmpty()) {
				code.add("$T.emptySet()", Collections.class);
			}
			else {
				code.add("$T.of(", Set.class);
				ResolvableType collectionType = parameterType.as(Set.class).getGenerics()[0];
				writeAll(code, set, collectionType);
				code.add(")");
			}
		}
		else if (value instanceof Character) {
			String result = '\'' + characterLiteralWithoutSingleQuotes((Character) value) + '\'';
			code.add(result);
		}
		else if (isPrimitiveOrWrapper(value)) {
			code.add("$L", value);
		}
		else if (value instanceof String) {
			code.add("$S", value);
		}
		else if (value instanceof Enum) {
			Enum<?> enumValue = (Enum<?>) value;
			code.add("$T.$N", enumValue.getClass(), enumValue.name());
		}
		else if (value instanceof Class) {
			code.add("$T.class", value);
		}
	}

	private void writeAll(Builder code, Iterable<?> items, ResolvableType elementType) {
		Iterator<?> it = items.iterator();
		while (it.hasNext()) {
			writeParameterValue(code, it.next(), elementType);
			if (it.hasNext()) {
				code.add(", ");
			}
		}
	}

	private boolean isPrimitiveOrWrapper(Object value) {
		Class<?> valueType = value.getClass();
		return (valueType.isPrimitive() || valueType == Double.class || valueType == Float.class
				|| valueType == Long.class || valueType == Integer.class || valueType == Short.class
				|| valueType == Character.class || valueType == Byte.class || valueType == Boolean.class);
	}

	// Copied from com.squareup.javapoet.Util
	private static String characterLiteralWithoutSingleQuotes(char c) {
		// see https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6
		switch (c) {
			case '\b':
				return "\\b"; /* \u0008: backspace (BS) */
			case '\t':
				return "\\t"; /* \u0009: horizontal tab (HT) */
			case '\n':
				return "\\n"; /* \u000a: linefeed (LF) */
			case '\f':
				return "\\f"; /* \u000c: form feed (FF) */
			case '\r':
				return "\\r"; /* \u000d: carriage return (CR) */
			case '\"':
				return "\""; /* \u0022: double quote (") */
			case '\'':
				return "\\'"; /* \u0027: single quote (') */
			case '\\':
				return "\\\\"; /* \u005c: backslash (\) */
			default:
				return Character.isISOControl(c) ? String.format("\\u%04x", (int) c) : Character.toString(c);
		}
	}

}

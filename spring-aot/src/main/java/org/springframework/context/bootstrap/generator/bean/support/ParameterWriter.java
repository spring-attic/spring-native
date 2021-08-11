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

package org.springframework.context.bootstrap.generator.bean.support;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;

import org.springframework.core.ResolvableType;

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

	private void writeParameterValue(Builder code, Object value, ResolvableType parameterType) {
		if (parameterType.isArray()) {
			code.add("new $T { ", parameterType.toClass());
			if (value instanceof char[]) {
				char[] array = (char[]) value;
				for (int i = 0; i < array.length; i++) {
					writeParameterValue(code, array[i], ResolvableType.forClass(char.class));
					if (i < array.length - 1) {
						code.add(", ");
					}
				}
			}
			else if (value instanceof String[]) {
				String[] array = (String[]) value;
				for (int i = 0; i < array.length; i++) {
					writeParameterValue(code, array[i], ResolvableType.forClass(String.class));
					if (i < array.length - 1) {
						code.add(", ");
					}
				}
			}
			code.add(" }");
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
		else if (value instanceof Class) {
			code.add("$T.class", value);
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

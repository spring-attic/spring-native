/*
 * Copyright 2021 the original author or authors.
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.squareup.javapoet.CodeBlock;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christoph Strobl
 */
public final class MethodInvocationWriter {

	public static CodeBlock writeMethodInvocationOn(String variable, Method method) {

		if(!Modifier.isPrivate(method.getModifiers())) {
			return CodeBlock.of("$L.$L();\n", variable, method.getName());
		}

		String initMethodName = method.getName();
		String targetMethodNameName = String.format("%sMethod", initMethodName);

		CodeBlock.Builder code = CodeBlock.builder();
		code.add("$T $L = $T.findMethod($T.class, $S);\n", java.lang.reflect.Method.class, targetMethodNameName, ReflectionUtils.class,
				method.getDeclaringClass(), method.getName());

		code.add("$T.makeAccessible($L);\n", ReflectionUtils.class, targetMethodNameName);
		code.add("$T.invokeMethod($L, $L);\n", ReflectionUtils.class, targetMethodNameName, variable);

		return code.build();
	}
}

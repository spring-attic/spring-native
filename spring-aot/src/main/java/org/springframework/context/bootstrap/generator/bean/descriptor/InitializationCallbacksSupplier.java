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

package org.springframework.context.bootstrap.generator.bean.descriptor;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.squareup.javapoet.CodeBlock;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.InitializationCallback;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Provide the {@link InitializationCallback initialization (post construct) callbacks} of a given class.
 *
 * @author Christoph Strobl
 */
class InitializationCallbacksSupplier {

	private static final Field EXTERNALLY_MANAGED_INIT_METHODS;

	static {

		EXTERNALLY_MANAGED_INIT_METHODS = ReflectionUtils.findField(RootBeanDefinition.class, "externallyManagedInitMethods");
		ReflectionUtils.makeAccessible(EXTERNALLY_MANAGED_INIT_METHODS);
	}

	List<InitializationCallback> detectInstanceCallbacks(BeanDefinition beanDefinition) {

		if(!(beanDefinition instanceof RootBeanDefinition)) {
			return Collections.emptyList();
		}

		RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) beanDefinition;
		Object field = ReflectionUtils.getField(EXTERNALLY_MANAGED_INIT_METHODS, rootBeanDefinition);

		if (ObjectUtils.isEmpty(field)) {
			return Collections.emptyList();
		}

		List<InitializationCallback> callbacks = new ArrayList<>();
		Class<?> targetType = beanDefinition.getResolvableType().toClass();
		for (Object initMethodName : (Iterable<? extends Object>) field) {
			callbacks.add(getInitMethodCallback(initMethodName.toString(), targetType));
		}
		return callbacks;
	}

	List<InitializationCallback> detectInstanceCallbacks(Class<?> type) {

		List<InitializationCallback> callbacks = new ArrayList<>();
		ReflectionUtils.doWithMethods(type, method -> callbacks.add(getInitMethodCallback(method)), method -> !ObjectUtils.isEmpty(method.getAnnotationsByType(PostConstruct.class)));
		return callbacks;
	}

	private InitializationCallback getInitMethodCallback(Method initMethod) {

		return new InitializationCallback(initMethod, (variable) -> {

			if(!Modifier.isPrivate(initMethod.getModifiers())) {
				return CodeBlock.of("$L.$L()", variable, initMethod.getName());
			}

			String initMethodName = initMethod.getName();
			String targetMethodNameName = String.format("%sMethod", initMethodName);

			CodeBlock.Builder code = CodeBlock.builder();
			code.add("$T $L = $T.findMethod($T.class, $S);\n", Method.class, targetMethodNameName, ReflectionUtils.class,
					initMethod.getDeclaringClass(), initMethod.getName());

			code.add("$T.makeAccessible($L);\n", ReflectionUtils.class, targetMethodNameName);
			code.add("$T.invokeMethod($L, $L)", ReflectionUtils.class, targetMethodNameName, variable);

			return code.build();
		});
	}

	private InitializationCallback getInitMethodCallback(String initMethod, Class<?> targetType) {
		return getInitMethodCallback(ReflectionUtils.findMethod(targetType, extractInitMethodName(initMethod, targetType)));
	}

	private String extractInitMethodName(Object initMethodName, Class<?> targetType) {
		return initMethodName.toString().replace(targetType.getName() + ".", "");
	}
}

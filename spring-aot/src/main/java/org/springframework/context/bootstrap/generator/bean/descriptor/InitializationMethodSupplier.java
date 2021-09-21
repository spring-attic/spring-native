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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Provide the {@link MemberDescriptor initialization (post construct) methods} of a given class.
 *
 * @author Christoph Strobl
 */
class InitializationMethodSupplier {

	private static final Field EXTERNALLY_MANAGED_INIT_METHODS;

	static { // TODO: remove once https://github.com/spring-projects/spring-framework/issues/27449 is resolved

		EXTERNALLY_MANAGED_INIT_METHODS = ReflectionUtils.findField(RootBeanDefinition.class, "externallyManagedInitMethods");
		ReflectionUtils.makeAccessible(EXTERNALLY_MANAGED_INIT_METHODS);
	}

	List<MemberDescriptor<Method>> getInstanceCallbacks(BeanDefinition beanDefinition) {

		if(!(beanDefinition instanceof RootBeanDefinition)) {
			return Collections.emptyList();
		}

		RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) beanDefinition;
		Object field = ReflectionUtils.getField(EXTERNALLY_MANAGED_INIT_METHODS, rootBeanDefinition);

		if (ObjectUtils.isEmpty(field)) {
			return Collections.emptyList();
		}

		List<MemberDescriptor<Method>> callbacks = new ArrayList<>();
		Class<?> targetType = beanDefinition.getResolvableType().toClass();
		for (Object initMethodName : (Iterable<? extends Object>) field) {
			callbacks.add(getInitMethodDescriptor(initMethodName.toString(), targetType));
		}
		return callbacks;
	}

	private MemberDescriptor<Method> getInitMethodDescriptor(Method method) {
		return new MemberDescriptor<>(method, true);
	}

	private MemberDescriptor<Method>  getInitMethodDescriptor(String initMethod, Class<?> targetType) {
		return getInitMethodDescriptor(ReflectionUtils.findMethod(targetType, extractInitMethodName(initMethod, targetType)));
	}

	private String extractInitMethodName(Object initMethodName, Class<?> targetType) {
		return initMethodName.toString().replace(targetType.getName() + ".", "");
	}
}

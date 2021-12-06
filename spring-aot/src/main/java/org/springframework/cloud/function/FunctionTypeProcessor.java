/*
 * Copyright 2021-2021 the original author or authors.
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

package org.springframework.cloud.function;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.support.BeanFactoryProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cloud.function.context.catalog.FunctionTypeUtils;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.util.ClassUtils;

/**
 * Ensures that Function/Consumer input types declared b the user are reflectively available.
 *
 * @author Oleg Zhurakousky
 *
 */
public class FunctionTypeProcessor implements BeanFactoryNativeConfigurationProcessor {

	private static final String FUNC_CONFIG_CLASS_NAME = "org.springframework.cloud.function.context.FunctionCatalog";


	@Override
	public void process(ConfigurableListableBeanFactory beanFactory,
			NativeConfigurationRegistry registry) {
		if (ClassUtils.isPresent(FUNC_CONFIG_CLASS_NAME, beanFactory.getBeanClassLoader())) {
			new Processor().process(beanFactory, registry);
		}
	}

	private static class Processor {

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			final Set<String> added = new HashSet<>();
			new BeanFactoryProcessor(beanFactory).processBeans(this::isFunction,
				(beanName, functionBeanType) -> {
					Type functionType = FunctionTypeUtils.discoverFunctionTypeFromClass(functionBeanType);
					Type inputType = FunctionTypeUtils.getInputType(functionType);
					String name = inputType.getTypeName();
					if (!name.startsWith("java.") &&
						!name.startsWith("javax.")) {
						if (added.add(name)) {
							registry.reflection().forType(FunctionTypeUtils.getRawType(inputType))
								.withAccess(TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS);
						}
					}
				});
		}

		private boolean isFunction(Class<?> beanType) {
			return Function.class.isAssignableFrom(beanType)
					|| Consumer.class.isAssignableFrom(beanType);
			// we don't care about Suppliers since it's output type is handled by the user code.
		}
	}

}

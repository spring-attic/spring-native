/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.context.bootstrap.generator.event;

import java.lang.reflect.Method;
import java.util.EventListener;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import org.springframework.context.event.EventListenerMetadata;

/**
 * Write the necessary code to identify an {@link EventListener @EventListener}-annotated
 * method.
 *
 * @author Stephane Nicoll
 * @see EventListenerMetadata
 */
class EventListenerMetadataGenerator {

	private static final ClassName METADATA = ClassName.get("org.springframework.context.event",
			"EventListenerMetadata");

	private final String beanName;

	private final Class<?> beanType;

	private final Method method;

	private final String eventListenerFactoryBeanName;

	EventListenerMetadataGenerator(String beanName, Class<?> beanType, Method method,
			String eventListenerFactoryBeanName) {
		this.beanName = beanName;
		this.beanType = beanType;
		this.method = method;
		this.eventListenerFactoryBeanName = eventListenerFactoryBeanName;
	}

	/**
	 * Write an {@link EventListenerMetadata} registration.
	 */
	public void writeEventListenerMetadata(CodeBlock.Builder code) {
		code.add("$T.forBean($S, $T.class)", METADATA, this.beanName, this.beanType);
		if (this.eventListenerFactoryBeanName != null) {
			code.add(".eventListenerFactoryBeanName($S)", this.eventListenerFactoryBeanName);
		}
		code.add(".annotatedMethod($S",this.method.getName());
		Class<?>[] parameterTypes = this.method.getParameterTypes();
		if (parameterTypes.length > 0) {
			code.add(", ");
		}
		for (int i = 0; i < parameterTypes.length; i++) {
			code.add("$T.class", parameterTypes[i]);
			if (i < parameterTypes.length - 1) {
				code.add(", ");
			}
		}
		code.add("))");
	}

}

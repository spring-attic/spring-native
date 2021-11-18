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

package org.springframework.aot.support;

import java.lang.annotation.Annotation;
import java.util.function.BiConsumer;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.ClassUtils;

/**
 * BeanFactory helper to process bean definitions.
 *
 * @author Stephane Nicoll
 */
public class BeanFactoryProcessor {

	private final ListableBeanFactory beanFactory;

	public BeanFactoryProcessor(ListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Process bean definitions matching the given type (including subclasses), as defined
	 * by {@link ListableBeanFactory#getBeanNamesForType(Class)}. Eager init is disabled.
	 * <p/>
	 * If the bean type cannot be determined, the entry is skipped. If the type is a proxy
	 * the user-facing class is extracted from it.
	 * @param type the class or interface to match, or {@code null} for all bean definitions
	 * @param consumer a callback with the name of the bean and the user type
	 */
	public void processBeansWithType(Class<?> type,
			BiConsumer<String, Class<?>> consumer) {
		String[] beanNames = this.beanFactory.getBeanNamesForType(type, true, false);
		for (String beanName : beanNames) {
			invokeConsumer(consumer, beanName);
		}
	}

	/**
	 * Process bean definitions annotated with the specified {@code annotationType}, as
	 * defined by {@link ListableBeanFactory#getBeanNamesForAnnotation(Class)}.
	 * <p/>
	 * If the bean type cannot be determined, the entry is skipped. If the type is a proxy
	 * the user-facing class is extracted from it.
	 * @param annotationType the annotation that must be present on the bean
	 * @param consumer a callback with the name of the bean and the user type
	 */
	public void processBeansWithAnnotation(Class<? extends Annotation> annotationType,
			BiConsumer<String, Class<?>> consumer) {
		String[] beanNames = this.beanFactory.getBeanNamesForAnnotation(annotationType);
		for (String beanName : beanNames) {
			invokeConsumer(consumer, beanName);
		}
	}

	private void invokeConsumer(BiConsumer<String, Class<?>> consumer, String beanName) {
		Class<?> type = this.beanFactory.getType(beanName);
		if (type != null) {
			consumer.accept(beanName, ClassUtils.getUserClass(type));
		}
	}

}

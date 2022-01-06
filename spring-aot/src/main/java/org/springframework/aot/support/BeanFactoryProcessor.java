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

package org.springframework.aot.support;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.ClassUtils;

/**
 * BeanFactory helper to process bean definitions.
 *
 * @author Stephane Nicoll
 * @author Christoph Strobl
 */
public class BeanFactoryProcessor {

	private final ListableBeanFactory beanFactory;

	public BeanFactoryProcessor(ListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Process bean definitions matching the given {@link Predicate}.
	 * <p>
	 * If the bean type cannot be determined, the entry is skipped. If the type is a proxy
	 * the user-facing class is extracted.
	 * @param filter the predicate to apply on the bean type, must not be {@literal null}
	 * @param consumer a callback with the name of the matching bean and the user type
	 */
	public void processBeans(Predicate<Class<?>> filter, BiConsumer<String, Class<?>> consumer) {
		String[] beanNames = this.beanFactory.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			invokeConsumer(beanName, (name, type) -> {
				if (filter.test(type)) {
					consumer.accept(name, type);
				}
			});
		}
	}

	/**
	 * Process bean definitions matching the given type (including subclasses), as defined
	 * by {@link ListableBeanFactory#getBeanNamesForType(Class)}. Eager init is disabled.
	 * <p>
	 * If the bean type cannot be determined, the entry is skipped. If the type is a proxy
	 * the user-facing class is extracted from it.
	 * @param type the class or interface to match, or {@code null} for all bean definitions
	 * @param consumer a callback with the name of the bean and the user type
	 */
	public void processBeansWithType(Class<?> type,
			BiConsumer<String, Class<?>> consumer) {
		String[] beanNames = this.beanFactory.getBeanNamesForType(type, true, false);
		for (String beanName : beanNames) {
			invokeConsumer(beanName, consumer);
		}
	}

	/**
	 * Process bean definitions annotated with the specified {@code annotationType}, as
	 * defined by {@link ListableBeanFactory#getBeanNamesForAnnotation(Class)}.
	 * <p>
	 * If the bean type cannot be determined, the entry is skipped. If the type is a proxy
	 * the user-facing class is extracted from it.
	 * @param annotationType the annotation that must be present on the bean
	 * @param consumer a callback with the name of the bean and the user type
	 */
	public void processBeansWithAnnotation(Class<? extends Annotation> annotationType,
			BiConsumer<String, Class<?>> consumer) {
		String[] beanNames = this.beanFactory.getBeanNamesForAnnotation(annotationType);
		for (String beanName : beanNames) {
			invokeConsumer(beanName, consumer);
		}
	}

	private void invokeConsumer(String beanName, BiConsumer<String, Class<?>> consumer) {
		Class<?> type = this.beanFactory.getType(beanName);
		if (type != null) {
			consumer.accept(beanName, ClassUtils.getUserClass(type));
		}
	}

	/**
	 * Return a stream of {@link AnnotatedBeanDescriptor} for all bean definitions
	 * annotated with the specified {@code annotationType}, as defined by
	 * {@link ListableBeanFactory#getBeanNamesForAnnotation(Class)}.
	 * <p>
	 * If the bean type or annotation cannot be determined, the entry is skipped. If the
	 * type is a proxy the user-facing class is extracted from it.
	 * @param annotationType the annotation that must be present on the bean
	 * @param <A> the type of the annotation
	 * @return a stream of annotated bean descriptor for all matching bean definitions
	 */
	public <A extends Annotation> Stream<AnnotatedBeanDescriptor<A>> beansWithAnnotation(Class<A> annotationType) {
		return Arrays.stream(this.beanFactory.getBeanNamesForAnnotation(annotationType))
				.map((beanName) -> toAnnotatedBeanDescriptor(beanName, annotationType))
				.filter(Objects::nonNull);
	}

	private <A extends Annotation> AnnotatedBeanDescriptor<A> toAnnotatedBeanDescriptor(String beanName, Class<A> annotationType) {
		Class<?> type = this.beanFactory.getType(beanName);
		if (type != null) {
			Class<?> userClass = ClassUtils.getUserClass(type);
			A annotation = this.beanFactory.findAnnotationOnBean(beanName, annotationType);
			if (annotation != null) {
				return new AnnotatedBeanDescriptor<>(beanName, userClass, annotation);
			}
		}
		return null;
	}

	/**
	 * Describe a bean definition that is annotated with a specified annotation.
	 *
	 * @param <A> the type of the annotation
	 */
	public static final class AnnotatedBeanDescriptor<A extends Annotation> {

		private final String beanName;

		private final Class<?> beanType;

		private final A annotation;

		AnnotatedBeanDescriptor(String beanName, Class<?> beanType, A annotation) {
			this.beanName = beanName;
			this.beanType = beanType;
			this.annotation = annotation;
		}

		public String getBeanName() {
			return this.beanName;
		}

		public Class<?> getBeanType() {
			return this.beanType;
		}

		public A getAnnotation() {
			return this.annotation;
		}

	}

}

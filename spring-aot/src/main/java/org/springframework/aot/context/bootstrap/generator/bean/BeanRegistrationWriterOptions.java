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

package org.springframework.aot.context.bootstrap.generator.bean;

import java.util.function.BiFunction;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Bean registration writer options.
 *
 * @author Stephane Nicoll
 */
public class BeanRegistrationWriterOptions {

	/**
	 * Constant for the default options.
	 */
	public static final BeanRegistrationWriterOptions DEFAULTS = builder().build();

	private final BiFunction<String, BeanDefinition, BeanRegistrationWriter> writerFactory;

	private BeanRegistrationWriterOptions(Builder builder) {
		this.writerFactory = builder.writerFactory;
	}

	/**
	 * Return a {@link BeanRegistrationWriter} for the specified bean definition.
	 * @param beanName the name of the bean
	 * @param beanDefinition the definition of the bean
	 * @return a {@link BeanRegistrationWriter} for the specified bean definition, or
	 * {@code null} if none could be provided
	 */
	public BeanRegistrationWriter getWriterFor(String beanName, BeanDefinition beanDefinition) {
		return (this.writerFactory != null) ? this.writerFactory.apply(beanName, beanDefinition) : null;
	}

	/**
	 * Create a new options {@link Builder}
	 * @return a builder with default settings
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Builder() {
		}

		private BiFunction<String, BeanDefinition, BeanRegistrationWriter> writerFactory;

		public Builder withWriterFactory(BiFunction<String, BeanDefinition, BeanRegistrationWriter> writerFactory) {
			this.writerFactory = writerFactory;
			return this;
		}


		public BeanRegistrationWriterOptions build() {
			return new BeanRegistrationWriterOptions(this);
		}

	}

}

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

package org.springframework.context.bootstrap.generator.bean;

import com.squareup.javapoet.CodeBlock;

import org.springframework.context.bootstrap.generator.BootstrapWriterContext;

/**
 * Abstract how to register a bean in the context.
 *
 * @author Stephane Nicoll
 */
@FunctionalInterface
public interface BeanRegistrationWriter {

	/**
	 * Generate the necessary {@code statements} to register a bean in the context.
	 * @param context the writer context
	 * @param code the builder to use to add the registration statement(s)
	 */
	void writeBeanRegistration(BootstrapWriterContext context, CodeBlock.Builder code);

}

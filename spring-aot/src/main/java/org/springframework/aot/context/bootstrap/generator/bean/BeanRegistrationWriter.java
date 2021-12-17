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

import com.squareup.javapoet.CodeBlock;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapWriterContext;

/**
 * Abstract how to register a bean in the bean factory.
 *
 * @author Stephane Nicoll
 */
public interface BeanRegistrationWriter {

	/**
	 * Property to set on a bean definition to request the target type to be preserved.
	 */
	String PRESERVE_TARGET_TYPE = "beanRegistrationWriterPreserveType";
	// FIXME: leaking abstraction, we need something better

	/**
	 * Generate the necessary {@code statements} to register a bean in the bean factory.
	 * @param context the writer context
	 * @param code the builder to use to add the registration statement(s)
	 */
	void writeBeanRegistration(BootstrapWriterContext context, CodeBlock.Builder code);

	/**
	 * Return the {@link BeanInstanceDescriptor descriptor} of the bean instance handled
	 * by this instance
	 * @return the bean instance descriptor
	 */
	BeanInstanceDescriptor getBeanInstanceDescriptor();

}

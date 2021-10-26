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

package org.springframework.boot.actuate.autoconfigure.web;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ManagementContextBeanRegistrationWriterSupplier}.
 *
 * @author Stephane Nicoll
 */
class ManagementContextBeanRegistrationWriterSupplierTests {

	public static final String SERVLET_MANAGEMENT_CONTEXT_FACTORY = "org.springframework.boot.actuate.autoconfigure.web.servlet.ServletManagementContextFactory";

	public static final String REACTIVE_MANAGEMENT_CONTEXT_FACTORY = "org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementContextFactory";

	private static final String DIFFERENT_MANAGEMENT_CONTEXT_TRIGGER_CLASS_NAME = "org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration.DifferentManagementContextConfiguration";

	@Test
	void getForServletManagementContextFactoryWithoutDifferentManagementContextTrigger() {
		GenericApplicationContext context = new GenericApplicationContext();
		ManagementContextBeanRegistrationWriterSupplier supplier = new ManagementContextBeanRegistrationWriterSupplier();
		supplier.setApplicationContext(context);
		assertThat(supplier.get("test", new RootBeanDefinition(forName(SERVLET_MANAGEMENT_CONTEXT_FACTORY)))).isNull();
	}

	@Test
	void getForServletManagementContextFactory() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBeanDefinition("trigger", new RootBeanDefinition(forName(DIFFERENT_MANAGEMENT_CONTEXT_TRIGGER_CLASS_NAME)));
		ManagementContextBeanRegistrationWriterSupplier supplier = new ManagementContextBeanRegistrationWriterSupplier();
		supplier.setApplicationContext(context);
		BeanRegistrationWriter writer = supplier.get("test", new RootBeanDefinition(forName(SERVLET_MANAGEMENT_CONTEXT_FACTORY)));
		assertThat(writer).isNotNull().hasFieldOrPropertyWithValue("reactive", false).hasFieldOrPropertyWithValue("parent", context);
	}

	@Test
	void getForReactiveManagementContextFactoryWithoutDifferentManagementContextTrigger() throws ClassNotFoundException {
		GenericApplicationContext context = new GenericApplicationContext();
		ManagementContextBeanRegistrationWriterSupplier supplier = new ManagementContextBeanRegistrationWriterSupplier();
		supplier.setApplicationContext(context);
		assertThat(supplier.get("test", new RootBeanDefinition(ClassUtils.forName(REACTIVE_MANAGEMENT_CONTEXT_FACTORY, null)))).isNull();
	}

	@Test
	void getForReactiveManagementContextFactory() throws ClassNotFoundException {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBeanDefinition("trigger", new RootBeanDefinition(forName(DIFFERENT_MANAGEMENT_CONTEXT_TRIGGER_CLASS_NAME)));
		ManagementContextBeanRegistrationWriterSupplier supplier = new ManagementContextBeanRegistrationWriterSupplier();
		supplier.setApplicationContext(context);
		BeanRegistrationWriter writer = supplier.get("test", new RootBeanDefinition(ClassUtils.forName(REACTIVE_MANAGEMENT_CONTEXT_FACTORY, null)));
		assertThat(writer).isNotNull().hasFieldOrPropertyWithValue("reactive", true).hasFieldOrPropertyWithValue("parent", context);
	}

	@Test
	void getForNonManagementContextFactory() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBeanDefinition("trigger", new RootBeanDefinition(forName(DIFFERENT_MANAGEMENT_CONTEXT_TRIGGER_CLASS_NAME)));
		ManagementContextBeanRegistrationWriterSupplier supplier = new ManagementContextBeanRegistrationWriterSupplier();
		supplier.setApplicationContext(context);
		assertThat(supplier.get("test", new RootBeanDefinition(String.class))).isNull();
	}

	@Test
	void getWithoutManagementContextFactoryAvailable() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBeanDefinition("trigger", new RootBeanDefinition(forName(DIFFERENT_MANAGEMENT_CONTEXT_TRIGGER_CLASS_NAME)));
		context.setClassLoader(new FilteredClassLoader(ManagementContextFactory.class.getName()));
		ManagementContextBeanRegistrationWriterSupplier supplier = new ManagementContextBeanRegistrationWriterSupplier();
		supplier.setApplicationContext(context);
		assertThat(supplier.get("test", new RootBeanDefinition(ManagementContextFactory.class))).isNull();
	}

	private static Class<?> forName(String name) {
		try {
			return ClassUtils.forName(name, null);
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalStateException(ex);
		}
	}

}

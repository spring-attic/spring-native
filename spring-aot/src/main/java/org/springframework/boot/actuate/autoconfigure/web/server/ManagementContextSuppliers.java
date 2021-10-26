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

package org.springframework.boot.actuate.autoconfigure.web.server;

import java.util.Arrays;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextFactory;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.support.GenericApplicationContext;

/**
 * An AOT variant of the {@link ManagementContextFactory} implementations available in
 * Spring Boot. Required as they assume that the parent context is refreshed.
 * <p/>
 * Note also that this implementation uses {@code registerBean}, rather than
 * {@code register}, which helps process the bean definitions without having to call
 * protected methods on the bean factory ({@code prepareBeanFactory} and
 * {@code postProcessBeanFactory}).
 *
 * @author Stephane Nicoll
 */
public class ManagementContextSuppliers {

	/**
	 * The Servlet variant.
	 */
	public static class Servlet {

		public static GenericApplicationContext createManagementContext(GenericApplicationContext parent) {
			AnnotationConfigServletWebServerApplicationContext child = new AnnotationConfigServletWebServerApplicationContext();
			child.setParent(parent);
			child.registerBean(EnableChildManagementContextConfiguration.class);
			child.registerBean(PropertyPlaceholderAutoConfiguration.class);
			child.registerBean(ServletWebServerFactoryAutoConfiguration.class);
			registerServletWebServerFactory(parent, child);
			return child;
		}

		private static void registerServletWebServerFactory(GenericApplicationContext parent,
				AnnotationConfigServletWebServerApplicationContext child) {
			try {
				Class<?> webServerFactoryType = determineWebServerFactoryType(parent, ServletWebServerFactory.class);
				child.registerBeanDefinition("ServletWebServerFactory", new RootBeanDefinition(webServerFactoryType));
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore and assume auto-configuration
			}
		}

	}

	/**
	 * The Reactive variant.
	 */
	public static class Reactive {

		public static GenericApplicationContext createManagementContext(GenericApplicationContext parent) {
			AnnotationConfigReactiveWebServerApplicationContext child = new AnnotationConfigReactiveWebServerApplicationContext();
			child.setParent(parent);
			child.registerBean(EnableChildManagementContextConfiguration.class);
			child.registerBean(PropertyPlaceholderAutoConfiguration.class);
			child.registerBean(ReactiveWebServerFactoryAutoConfiguration.class);
			registerReactiveWebServerFactory(parent, child);
			return child;
		}

		private static void registerReactiveWebServerFactory(GenericApplicationContext parent,
				AnnotationConfigReactiveWebServerApplicationContext child) {
			try {
				Class<?> webServerFactoryType = determineWebServerFactoryType(parent, ReactiveWebServerFactory.class);
				child.registerBeanDefinition("ReactiveWebServerFactory", new RootBeanDefinition(webServerFactoryType));
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore and assume auto-configuration
			}
		}

	}

	private static Class<?> determineWebServerFactoryType(GenericApplicationContext parent, Class<?> webServerFactoryType)
			throws NoSuchBeanDefinitionException {
		String[] beanNames = parent.getBeanFactory().getBeanNamesForType(webServerFactoryType, false, false);
		if (beanNames.length == 1) {
			return parent.getBeanFactory().getType(beanNames[0]);
		}
		throw new NoSuchBeanDefinitionException("Expected a single " + webServerFactoryType.getName()
				+ " bean, got " + Arrays.toString(beanNames));
	}

}

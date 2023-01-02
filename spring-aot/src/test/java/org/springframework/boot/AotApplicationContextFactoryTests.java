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

package org.springframework.boot;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.junit.jupiter.api.Test;

import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.support.StandardServletEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AotApplicationContextFactory}.
 *
 * @author Stephane Nicoll
 */
class AotApplicationContextFactoryTests {

	@Test
	void createApplicationContextDetectTypeFromClasspath() {
		GenericApplicationContext context = new AotApplicationContextFactory(createResourceLoader("empty"))
				.createApplicationContext(TestApplication.class);
		assertThat(context).isInstanceOf(AnnotationConfigServletWebServerApplicationContext.class);
		assertThat(context.getEnvironment()).isInstanceOf(StandardServletEnvironment.class);
		assertThat(context.getEnvironment().getClass().getName()).endsWith("ApplicationServletEnvironment");
		assertThat(context.isRunning()).isFalse();
	}

	@Test
	void createApplicationLoadApplicationProperties() {
		GenericApplicationContext context = new AotApplicationContextFactory(createResourceLoader("config"))
				.createApplicationContext(TestApplication.class);
		ConfigurableEnvironment environment = context.getEnvironment();
		assertThat(environment.getProperty("server.port", Integer.class)).isEqualTo(9090);
		assertThat(environment.getProperty("test.name")).isEqualTo("Hello");
	}

	@Test
	void createApplicationContextCanReactFromApplicationProperties() {
		GenericApplicationContext context = new AotApplicationContextFactory(createResourceLoader("no-web"))
				.createApplicationContext(TestApplication.class);
		assertThat(context).isInstanceOf(AnnotationConfigApplicationContext.class);
		assertThat(context.getEnvironment()).isInstanceOf(ApplicationEnvironment.class);
	}

	private ResourceLoader createResourceLoader(String directory) {
		ClassLoader classloader = new CustomApplicationConfigurationClassLoader(directory);
		return new DefaultResourceLoader(classloader);
	}

	static class TestApplication {

	}

	static class CustomApplicationConfigurationClassLoader extends ClassLoader {

		private final String directory;

		CustomApplicationConfigurationClassLoader(String directory) {
			super(CustomApplicationConfigurationClassLoader.class.getClassLoader());
			this.directory = directory;
		}

		@Override
		public URL getResource(String name) {
			if (match(name)) {
				return super.getResource("aot-application-context-tests/" + this.directory + "/" + name);
			}
			return super.getResource(name);
		}

		@Override
		public Enumeration<URL> getResources(String name) throws IOException {
			if (match(name)) {
				return super.getResources("aot-application-context-tests/" + this.directory + "/" + name);
			}
			return super.getResources(name);
		}

		private boolean match(String name) {
			return name != null && (name.endsWith(".properties") || name.endsWith(".yml") || name.endsWith(".yaml"));
		}

	}

}

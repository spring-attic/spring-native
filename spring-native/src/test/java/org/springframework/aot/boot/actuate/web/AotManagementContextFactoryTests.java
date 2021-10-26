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

package org.springframework.aot.boot.actuate.web;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link AotManagementContextFactory}.
 *
 * @author Stephane Nicoll
 */
class AotManagementContextFactoryTests {

	@Test
	void createManagementContextWithServletEnv() {
		AotManagementContextFactory factory = new AotManagementContextFactory(this::testContextInitializer, false);
		GenericApplicationContext parent = mock(GenericApplicationContext.class);
		ConfigurableWebServerApplicationContext managementContext = factory.createManagementContext(parent, DummyConfiguration.class);
		assertThat(managementContext).isExactlyInstanceOf(ServletWebServerApplicationContext.class).satisfies(managementContext(parent));
	}

	@Test
	void createManagementContextWithReactiveEnv() {
		AotManagementContextFactory factory = new AotManagementContextFactory(this::testContextInitializer, true);
		GenericApplicationContext parent = mock(GenericApplicationContext.class);
		ConfigurableWebServerApplicationContext managementContext = factory.createManagementContext(parent, DummyConfiguration.class);
		assertThat(managementContext).isExactlyInstanceOf(ReactiveWebServerApplicationContext.class).satisfies(managementContext(parent));
	}

	private Consumer<ConfigurableWebServerApplicationContext> managementContext(GenericApplicationContext parent) {
		return (context) -> {
			assertThat(context.containsBeanDefinition("test")).isTrue();
			assertThat(context.getBeanFactory().getBeanNamesForType(DummyConfiguration.class)).isEmpty();
			assertThat(context.getParent()).isEqualTo(parent);
		};
	}


	private ApplicationContextInitializer<GenericApplicationContext> testContextInitializer() {
		return (context) -> context.registerBeanDefinition("test", new RootBeanDefinition(String.class, () -> "Hello"));
	}

	@Configuration(proxyBeanMethods = false)
	static class DummyConfiguration {

	}

}

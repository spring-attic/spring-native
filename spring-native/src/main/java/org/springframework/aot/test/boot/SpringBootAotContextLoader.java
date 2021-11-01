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

package org.springframework.aot.test.boot;

import java.util.Collections;

import org.springframework.aot.SpringApplicationAotUtils;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.test.context.ReactiveWebMergedContextConfiguration;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.SmartContextLoader;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.context.web.WebMergedContextConfiguration;
import org.springframework.util.ObjectUtils;

/**
 * A {@link SmartContextLoader} that use an {@link ApplicationContextInitializer}
 * to bootstrap a Spring Boot test. This includes support for {@code @SpringBootTest} as
 * well as slice tests.
 *
 * @author Stephane Nicoll
 */
public class SpringBootAotContextLoader extends SpringBootContextLoader {

	private final Class<? extends ApplicationContextInitializer<?>> testContextInitializer;

	/**
	 * Create a new instance using the specified {@link ApplicationContextInitializer}.
	 * @param testContextInitializer the context initializer to use
	 */
	public SpringBootAotContextLoader(Class<? extends ApplicationContextInitializer<?>> testContextInitializer) {
		this.testContextInitializer = testContextInitializer;
	}

	@Override
	public ConfigurableApplicationContext loadContext(MergedContextConfiguration config) {
		// TODO: handle application arguments
		String[] args = new String[0];

		SpringApplication application = new SpringTestApplication(config.getTestClass().getClassLoader(), testContextInitializer);
		application.setMainApplicationClass(config.getTestClass());
		application.setSources(Collections.singleton(testContextInitializer.getName()));
		ConfigurableEnvironment environment = getEnvironment();
		if (!ObjectUtils.isEmpty(config.getActiveProfiles())) {
			setActiveProfiles(environment, config.getActiveProfiles());
		}
		TestPropertySourceUtils.addPropertiesFilesToEnvironment(environment, application.getResourceLoader(),
				config.getPropertySourceLocations());
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(environment, getInlinedProperties(config));
		application.setEnvironment(environment);
		application.setWebApplicationType(detectWebApplicationType(config));
		application.setApplicationContextFactory(SpringApplicationAotUtils.AOT_FACTORY);
		ConfigurableApplicationContext context = application.run(args);

		// FIXME: register Autowired support after the context has started for the test class
		configureAutowiringSupport(context.getBeanFactory());
		return context;
	}

	private WebApplicationType detectWebApplicationType(MergedContextConfiguration config) {
		if (config instanceof WebMergedContextConfiguration) {
			return WebApplicationType.SERVLET;
		}
		else if (config instanceof ReactiveWebMergedContextConfiguration) {
			return WebApplicationType.REACTIVE;
		}
		else {
			return WebApplicationType.NONE;
		}
	}

	// Copy of SpringBootContextLoader
	private void setActiveProfiles(ConfigurableEnvironment environment, String[] profiles) {
		environment.setActiveProfiles(profiles);
		// Also add as properties to override any application.properties
		String[] pairs = new String[profiles.length];
		for (int i = 0; i < profiles.length; i++) {
			pairs[i] = "spring.profiles.active[" + i + "]=" + profiles[i];
		}
		TestPropertyValues.of(pairs).applyTo(environment);
	}

	private void configureAutowiringSupport(ConfigurableListableBeanFactory beanFactory) {
		AutowiredAnnotationBeanPostProcessor beanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
		beanPostProcessor.setBeanFactory(beanFactory);
		beanFactory.addBeanPostProcessor(beanPostProcessor);
	}

}

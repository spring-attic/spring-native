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

import java.util.Set;

import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.DefaultBootstrapContext;
import org.springframework.boot.LazyInitializationBeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.logging.DeferredLogs;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.nativex.utils.NativeUtils;
import org.springframework.util.Assert;

/**
 * A {@link SpringApplication} suitable for used in the preparation of a test context.
 *
 * @author Stephane Nicoll
 */
class BuildTimeTestSpringApplication extends SpringApplication {

	private ConfigurableEnvironment environment;

	private boolean lazyInitialization = false;

	private boolean allowBeanDefinitionOverriding = false;

	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment(environment);
		this.environment = environment;
	}

	@Override
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		super.setAllowBeanDefinitionOverriding(allowBeanDefinitionOverriding);
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	@Override
	public void setLazyInitialization(boolean lazyInitialization) {
		super.setLazyInitialization(lazyInitialization);
		this.lazyInitialization = lazyInitialization;
	}

	@Override
	public GenericApplicationContext run(String... args) {
		ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
		GenericApplicationContext context = createContext();
		context.setEnvironment(prepareEnvironment());
		prepareContext(context, applicationArguments);
		return context;
	}

	private void prepareContext(ConfigurableApplicationContext context, ApplicationArguments applicationArguments) {
		configureIgnoreBeanInfo(context.getEnvironment());
		postProcessApplicationContext(context);
		applyInitializers(context);
		// Add boot specific singleton beans
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
		((DefaultListableBeanFactory) beanFactory)
				.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
		if (this.lazyInitialization) {
			context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
		}
		// Load the sources
		Set<Object> sources = getAllSources();
		Assert.notEmpty(sources, "Sources must not be empty");
		load(context, sources.toArray(new Object[0]));
	}

	private GenericApplicationContext createContext() {
		return (GenericApplicationContext) ApplicationContextFactory.DEFAULT.create(getWebApplicationType());
	}

	private void configureIgnoreBeanInfo(ConfigurableEnvironment environment) {
		if (System.getProperty(CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME) == null) {
			Boolean ignore = environment.getProperty("spring.beaninfo.ignore", Boolean.class, Boolean.TRUE);
			System.setProperty(CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME, ignore.toString());
		}
	}

	private ConfigurableEnvironment prepareEnvironment() {
		ConfigurableEnvironment environment = (this.environment != null)
				? this.environment : new StandardEnvironment();
		environment.setConversionService(new ApplicationConversionService());
		ConfigurationPropertySources.attach(environment);
		ConfigDataEnvironmentPostProcessor configDataEnvironmentPostProcessor =
				new ConfigDataEnvironmentPostProcessor(new DeferredLogs(), new DefaultBootstrapContext());
		configDataEnvironmentPostProcessor.postProcessEnvironment(environment, this);
		bindToSpringApplication(environment, this);
		environment.getPropertySources().addFirst(new PropertiesPropertySource("native", NativeUtils.getNativeProperties()));
		return environment;
	}


	private void bindToSpringApplication(ConfigurableEnvironment environment, SpringApplication application) {
		try {
			Binder.get(environment).bind("spring.main", Bindable.ofInstance(application));
		}
		catch (Exception ex) {
			throw new IllegalStateException("Cannot bind to SpringApplication", ex);
		}
	}

}

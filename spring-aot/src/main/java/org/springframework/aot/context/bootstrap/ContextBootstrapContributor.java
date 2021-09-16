/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.aot.context.bootstrap;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aot.BootstrapContributor;
import org.springframework.aot.BuildContext;
import org.springframework.aot.SourceFiles;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.DefaultBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.logging.DeferredLogs;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
import org.springframework.boot.web.reactive.context.StandardReactiveWebEnvironment;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.BuildTimeBeanDefinitionsRegistrar;
import org.springframework.context.bootstrap.generator.BootstrapGenerationResult;
import org.springframework.context.bootstrap.generator.ContextBootstrapGenerator;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.nativex.AotOptions;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * @author Brian Clozel
 * @author Sebastien Deleuze
 * @author Stephane Nicoll
 */
public class ContextBootstrapContributor implements BootstrapContributor {

	// Copied from Spring Boot WebApplicationType
	private static final String[] SERVLET_INDICATOR_CLASSES = {"javax.servlet.Servlet",
			"org.springframework.web.context.ConfigurableWebApplicationContext"};

	private static final String WEBMVC_INDICATOR_CLASS = "org.springframework.web.servlet.DispatcherServlet";

	private static final String WEBFLUX_INDICATOR_CLASS = "org.springframework.web.reactive.DispatcherHandler";

	private static final String JERSEY_INDICATOR_CLASS = "org.glassfish.jersey.servlet.ServletContainer";

	private static final Log logger = LogFactory.getLog(ContextBootstrapContributor.class);

	@Override
	public void contribute(BuildContext context, AotOptions aotOptions) {
		ResourceLoader resourceLoader = context.getTypeSystem().getResourceLoader();
		ClassLoader classLoader = resourceLoader.getClassLoader();
		Class<?> applicationClass;
		String applicationClassName = context.getApplicationClass();
		try {
			logger.info("Detected application class: " + applicationClassName);
			applicationClass = ClassUtils.forName(applicationClassName, classLoader);
		}
		catch (ClassNotFoundException exc) {
			throw new IllegalStateException("Could not load application class " + applicationClassName, exc);
		}

		WebApplicationType webApplicationType = deduceWebApplicationType(classLoader);
		GenericApplicationContext applicationContext = createApplicationContext(webApplicationType);
		applicationContext.setResourceLoader(resourceLoader);

		StandardEnvironment environment = createEnvironment(webApplicationType);
		configureEnvironment(environment, resourceLoader, applicationClass);

		applicationContext.setEnvironment(environment);
		applicationContext.registerBean(applicationClass);

		ConfigurableListableBeanFactory beanFactory = new BuildTimeBeanDefinitionsRegistrar().processBeanDefinitions(applicationContext);
		ContextBootstrapGenerator bootstrapGenerator = new ContextBootstrapGenerator(classLoader);
		BootstrapGenerationResult bootstrapGenerationResult = bootstrapGenerator.generateBootstrapClass(beanFactory, "org.springframework.aot");
		bootstrapGenerationResult.getSourceFiles().forEach(javaFile -> context.addSourceFiles(SourceFiles.fromJavaFile(javaFile)));
		context.getOptions().addAll(bootstrapGenerationResult.getOptions());
		context.describeReflection(reflectionDescriptor -> bootstrapGenerationResult.getClassDescriptors().forEach(reflectionDescriptor::merge));
		context.describeResources(resourcesDescriptor -> resourcesDescriptor.merge(bootstrapGenerationResult.getResourcesDescriptor()));
		context.describeProxies(proxiesDescriptor -> proxiesDescriptor.merge(bootstrapGenerationResult.getProxiesDescriptor()));
		context.describeInitialization(initializationDescriptor -> initializationDescriptor.merge(bootstrapGenerationResult.getInitializationDescriptor()));
	}

	private void configureEnvironment(StandardEnvironment environment, ResourceLoader resourceLoader, Class<?> applicationClass) {
		environment.setConversionService(new ApplicationConversionService());
		ConfigurationPropertySources.attach(environment);
		ConfigDataEnvironmentPostProcessor configDataEnvironmentPostProcessor =
				new ConfigDataEnvironmentPostProcessor(new DeferredLogs(), new DefaultBootstrapContext());

		SpringApplication application = new SpringApplication(resourceLoader, applicationClass);
		configDataEnvironmentPostProcessor.postProcessEnvironment(environment, application);

		Properties properties = new Properties();
		properties.put("spring.aop.proxy-target-class", "false"); // Not supported in native images
		properties.put("spring.cloud.refresh.enabled", "false"); // Sampler is a class and can't be proxied
		properties.put("spring.sleuth.async.enabled", "false"); // Too much proxy created
		properties.put("spring.devtools.restart.enabled", "false"); // Deactivate dev tools
		environment.getPropertySources().addFirst(new PropertiesPropertySource("native", properties));
	}

	// TODO Avoid duplication with WebApplicationType and SpringApplicationAotUtils.AOT_FACTORY
	private GenericApplicationContext createApplicationContext(WebApplicationType webApplicationType) {
		switch (webApplicationType) {
			case REACTIVE:
				return ReactiveContextDelegate.createApplicationContext();
			case SERVLET:
				return ServletContextDelegate.createApplicationContext();
		}
		return new AnnotationConfigApplicationContext();
	}

	private StandardEnvironment createEnvironment(WebApplicationType webApplicationType) {
		switch (webApplicationType) {
			case SERVLET:
				return ServletEnvironmentDelegate.createServletEnvironment();
			case REACTIVE:
				return new StandardReactiveWebEnvironment();
			default:
				return new StandardEnvironment();
		}
	}

	private WebApplicationType deduceWebApplicationType(ClassLoader classLoader) {
		if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, classLoader) && !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, classLoader)
				&& !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, classLoader)) {
			return WebApplicationType.REACTIVE;
		}
		for (String className : SERVLET_INDICATOR_CLASSES) {
			if (!ClassUtils.isPresent(className, classLoader)) {
				return WebApplicationType.NONE;
			}
		}
		return WebApplicationType.SERVLET;
	}

	// To avoid NoClassDefFoundError:
	static class ServletContextDelegate {

		public static GenericApplicationContext createApplicationContext() {
			return new AnnotationConfigServletWebServerApplicationContext();
		}
	}

	// To avoid NoClassDefFoundError:
	static class ServletEnvironmentDelegate {

		public static StandardEnvironment createServletEnvironment() {
			return new StandardServletEnvironment();
		}
	}

	// To avoid NoClassDefFoundError:
	static class ReactiveContextDelegate {

		public static GenericApplicationContext createApplicationContext() {
			return new AnnotationConfigReactiveWebServerApplicationContext();
		}
	}
}

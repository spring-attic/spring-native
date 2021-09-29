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
import org.springframework.boot.AotApplicationContextFactory;
import org.springframework.context.annotation.BuildTimeBeanDefinitionsRegistrar;
import org.springframework.context.bootstrap.generator.BootstrapGenerationResult;
import org.springframework.context.bootstrap.generator.ContextBootstrapGenerator;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.nativex.AotOptions;
import org.springframework.util.ClassUtils;

/**
 * @author Brian Clozel
 * @author Sebastien Deleuze
 * @author Stephane Nicoll
 */
public class ContextBootstrapContributor implements BootstrapContributor {

	private static final Log logger = LogFactory.getLog(ContextBootstrapContributor.class);

	@Override
	public void contribute(BuildContext context, AotOptions aotOptions) {
		ResourceLoader resourceLoader = context.getTypeSystem().getResourceLoader();
		ClassLoader classLoader = resourceLoader.getClassLoader();
		Class<?> applicationClass;
		String applicationClassName = context.getApplicationClass();
		if (applicationClassName == null) {
			logger.warn("No application class detected, skipping context bootstrap generation");
			return;
		}
		try {
			logger.info("Detected application class: " + applicationClassName);
			applicationClass = ClassUtils.forName(applicationClassName, classLoader);
		}
		catch (ClassNotFoundException exc) {
			throw new IllegalStateException("Could not load application class " + applicationClassName, exc);
		}

		GenericApplicationContext applicationContext = new AotApplicationContextFactory(resourceLoader)
				.createApplicationContext(applicationClass);
		configureEnvironment(applicationContext.getEnvironment());
		ConfigurableListableBeanFactory beanFactory = new BuildTimeBeanDefinitionsRegistrar().processBeanDefinitions(applicationContext);
		ContextBootstrapGenerator bootstrapGenerator = new ContextBootstrapGenerator(classLoader);
		BootstrapGenerationResult bootstrapGenerationResult = bootstrapGenerator.generateBootstrapClass(beanFactory, "org.springframework.aot");
		bootstrapGenerationResult.getSourceFiles().forEach(javaFile -> context.addSourceFiles(SourceFiles.fromJavaFile(javaFile)));
		context.getOptions().addAll(bootstrapGenerationResult.getOptions());
		context.describeReflection(reflectionDescriptor -> bootstrapGenerationResult.getClassDescriptors().forEach(reflectionDescriptor::merge));
		context.describeResources(resourcesDescriptor -> resourcesDescriptor.merge(bootstrapGenerationResult.getResourcesDescriptor()));
		context.describeProxies(proxiesDescriptor -> proxiesDescriptor.merge(bootstrapGenerationResult.getProxiesDescriptor()));
		context.describeInitialization(initializationDescriptor -> initializationDescriptor.merge(bootstrapGenerationResult.getInitializationDescriptor()));
		context.describeSerialization(serializationDescriptor -> serializationDescriptor.merge(bootstrapGenerationResult.getSerializationDescriptor()));
		context.describeJNIReflection(reflectionDescriptor -> bootstrapGenerationResult.getJniClassDescriptors().forEach(reflectionDescriptor::merge));
	}

	private void configureEnvironment(ConfigurableEnvironment environment) {
		Properties properties = new Properties();
		properties.put("spring.aop.proxy-target-class", "false"); // Not supported in native images
		properties.put("spring.cloud.refresh.enabled", "false"); // Sampler is a class and can't be proxied
		properties.put("spring.cloud.compatibility-verifier.enabled", "false"); // To avoid false positive due to SpringApplication patched copy
		properties.put("spring.sleuth.async.enabled", "false"); // Too much proxy created
		properties.put("spring.devtools.restart.enabled", "false"); // Deactivate dev tools
		environment.getPropertySources().addFirst(new PropertiesPropertySource("native", properties));
	}

}

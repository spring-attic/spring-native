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

package org.springframework.context.annotation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Parse the {@link Configuration @Configuration classes} and provide the bean definitions
 * at build time.
 *
 * @author Stephane Nicoll
 * @see ConditionEvaluationStateReport
 */
public class BuildTimeBeanDefinitionsRegistrar {

	private static final Log logger = LogFactory.getLog(BuildTimeBeanDefinitionsRegistrar.class);

	/**
	 * Process bean definitions without creating any instance and return the
	 * {@link ConfigurableListableBeanFactory bean factory}.
	 * @param context the context to process
	 * @return the bean factory with the result of the processing
	 */
	public ConfigurableListableBeanFactory processBeanDefinitions(GenericApplicationContext context) {
		Assert.notNull(context, "Context must not be null");
		Assert.state(!context.isActive(), () -> "Context must not be active");
		if (logger.isDebugEnabled()) {
			logger.debug("Parsing configuration classes");
		}
		parseConfigurationClasses(context);
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		resolveBeanDefinitionTypes(beanFactory);
		postProcessBeanDefinitions(beanFactory);
		registerImportOriginRegistryIfNecessary(beanFactory);
		return beanFactory;
	}

	private void parseConfigurationClasses(GenericApplicationContext context) {
		ConfigurationClassPostProcessor configurationClassPostProcessor = new ConfigurationClassPostProcessor();
		configurationClassPostProcessor.setApplicationStartup(context.getApplicationStartup());
		configurationClassPostProcessor.setBeanClassLoader(context.getClassLoader());
		configurationClassPostProcessor.setEnvironment(context.getEnvironment());
		configurationClassPostProcessor.setResourceLoader(context);
		configurationClassPostProcessor.postProcessBeanFactory(context.getBeanFactory());
	}

	private void resolveBeanDefinitionTypes(ConfigurableListableBeanFactory beanFactory) {
		if (logger.isDebugEnabled()) {
			logger.debug("Resolving types for " + beanFactory.getBeanDefinitionCount() + " bean definitions");
		}
		for (String name : beanFactory.getBeanDefinitionNames()) {
			beanFactory.getType(name);
		}
	}

	private void postProcessBeanDefinitions(ConfigurableListableBeanFactory beanFactory) {
		if (logger.isDebugEnabled()) {
			logger.debug("Post processing " + beanFactory.getBeanDefinitionCount() + " bean definitions");
		}
		List<BeanDefinitionPostProcessor> postProcessors = SpringFactoriesLoader.loadFactories(
				BeanDefinitionPostProcessor.class, beanFactory.getBeanClassLoader());
		postProcessors.stream().filter(BeanFactoryAware.class::isInstance)
				.map(BeanFactoryAware.class::cast).forEach((aca) -> aca.setBeanFactory(beanFactory));
		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			RootBeanDefinition bd = (RootBeanDefinition) beanFactory.getMergedBeanDefinition(beanName);
			postProcessors.forEach((postProcessor) -> postProcessor.postProcessBeanDefinition(beanName, bd));
		}
	}

	private void registerImportOriginRegistryIfNecessary(ConfigurableListableBeanFactory beanFactory) {
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieving import origins if necessary");
		}
		String importRegistryBeanName = ConfigurationClassPostProcessor.class.getName() + ".importRegistry";
		if (!beanFactory.containsBean(importRegistryBeanName)) {
			// No configuration classes were processed
			return;
		}
		ImportRegistry importRegistry = beanFactory.getBean(importRegistryBeanName, ImportRegistry.class);
		Map<String, Class<?>> origins = new LinkedHashMap<>();
		for (String name : beanFactory.getBeanDefinitionNames()) {
			Class<?> beanType = beanFactory.getType(name);
			if (beanType != null && ImportAware.class.isAssignableFrom(beanType)) {
				String type = ClassUtils.getUserClass(beanType).getName();
				AnnotationMetadata importingClassMetadata = importRegistry.getImportingClassFor(type);
				if (importingClassMetadata != null) {
					Class<?> importingClass = loadClass(importingClassMetadata.getClassName(),
							beanFactory.getBeanClassLoader());
					origins.put(type, importingClass);
				}
			}
		}
		ImportOriginRegistry.register(beanFactory, origins);
	}

	private Class<?> loadClass(String beanClassName, ClassLoader classLoader) {
		try {
			return ClassUtils.forName(beanClassName, classLoader);
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalStateException("Failed to load class " + beanClassName);
		}
	}

}

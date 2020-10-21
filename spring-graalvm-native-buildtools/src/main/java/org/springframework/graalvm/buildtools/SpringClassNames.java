/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.graalvm.buildtools;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * @author Dave Syer
 *
 */
public class SpringClassNames {

	public static final ClassName IMPORT_SELECTOR = ClassName.get(ImportSelector.class);

	public static final ClassName OBJECT_PROVIDER = ClassName.get(ObjectProvider.class);

	public static final ClassName IMPORT_BEAN_DEFINITION_REGISTRAR = ClassName.get(ImportBeanDefinitionRegistrar.class);

	public static final ClassName ANNOTATION_METADATA = ClassName.get(AnnotationMetadata.class);

	public static final ClassName CONFIGURATION = ClassName.get(Configuration.class);

	public static final ClassName COMPONENT = ClassName.get(Component.class);

	public static final ClassName QUALIFIER = ClassName.get(Qualifier.class);

	public static final ClassName LAZY = ClassName.get(Lazy.class);

	public static final ClassName BEAN_FACTORY_ANNOTATION_UTILS = ClassName.get(BeanFactoryAnnotationUtils.class);

	public static final ClassName COMPONENT_SCAN = ClassName.get(ComponentScan.class);

	public static final ClassName SPRING_BOOT_CONFIGURATION = ClassName.get("org.springframework.boot",
			"SpringBootConfiguration");

	public static final ClassName ENABLE_AUTO_CONFIGURATION = ClassName.get("org.springframework.boot.autoconfigure",
			"EnableAutoConfiguration");

	public static final ClassName BEAN = ClassName.get(Bean.class);

	public static final ClassName IMPORT = ClassName.get(Import.class);

	public static final ClassName IMPORT_RESOURCE = ClassName.get(ImportResource.class);

	public static final ClassName NULLABLE = ClassName.get(Nullable.class);

	public static final ClassName APPLICATION_CONTEXT_INITIALIZER = ClassName.get(ApplicationContextInitializer.class);

	public static final ClassName APPLICATION_CONTEXT = ClassName.get(ApplicationContext.class);

	public static final ClassName RESOURCE_LOADER = ClassName.get(ResourceLoader.class);

	public static final ClassName METADATA_READER_FACTORY = ClassName.get(MetadataReaderFactory.class);

	public static final ClassName BEAN_NAME_GENERATOR = ClassName.get(BeanNameGenerator.class);

	public static final ClassName APPLICATION_EVENT_PUBLISHER = ClassName.get(ApplicationEventPublisher.class);

	public static final ClassName WEB_APPLICATION_CONTEXT = ClassName.get("org.springframework.web.context",
			"WebApplicationContext");

	public static final ClassName CONFIGURABLE_APPLICATION_CONTEXT = ClassName
			.get(ConfigurableApplicationContext.class);

	public static final ClassName BEAN_FACTORY = ClassName.get(BeanFactory.class);

	public static final ClassName ROOT_BEAN_DEFINITION = ClassName.get(RootBeanDefinition.class);

	public static final ClassName LISTABLE_BEAN_FACTORY = ClassName.get(ListableBeanFactory.class);

	public static final ClassName CONFIGURABLE_LISTABLE_BEAN_FACTORY = ClassName
			.get(ConfigurableListableBeanFactory.class);

	public static final ClassName GENERIC_APPLICATION_CONTEXT = ClassName.get(GenericApplicationContext.class);

	public static final ParameterizedTypeName INITIALIZER_TYPE = ParameterizedTypeName
			.get(APPLICATION_CONTEXT_INITIALIZER, GENERIC_APPLICATION_CONTEXT);

	public static final ClassName CONDITIONAL = ClassName.get(Conditional.class);

	public static final ClassName CONDITIONAL_ON_CLASS = ClassName
			.get("org.springframework.boot.autoconfigure.condition", "ConditionalOnClass");

	public static final ClassName ENABLE_CONFIGURATION_PROPERTIES = ClassName
			.get("org.springframework.boot.context.properties", "EnableConfigurationProperties");

	public static final ClassName ENABLE_CONFIGURATION_PROPERTIES_REGISTRAR = ClassName
			.get("org.springframework.boot.context.properties", "EnableConfigurationPropertiesRegistrar");

	public static final ClassName REACTIVE_BEAN_POST_PROCESSORS = ClassName.get(
			"org.springframework.boot.autoconfigure.web.reactive",
			"ReactiveWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar");

	public static final ClassName SERVLET_BEAN_POST_PROCESSORS = ClassName.get(
			"org.springframework.boot.autoconfigure.web.servlet",
			"ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar");

	public static final ClassName PRIMARY_DEFAULT_VALIDATOR_REGISTRAR = ClassName
			.get("org.springframework.boot.autoconfigure.validation", "PrimaryDefaultValidatorPostProcessor");

	public static final ClassName DATA_SOURCE_INITIALIZATION_REGISTRAR = ClassName
			.get("org.springframework.boot.autoconfigure.jdbc", "DataSourceInitializationConfiguration.Registrar");

	public static final ClassName CONFIGURATION_PROPERTIES = ClassName
			.get("org.springframework.boot.context.properties", "ConfigurationProperties");

	public static final ClassName CONFIGURATION_PROPERTIES_BINDING_POST_PROCESSOR = ClassName
			.get("org.springframework.boot.context.properties", "ConfigurationPropertiesBindingPostProcessor");

	public static final ClassName AUTOCONFIGURATION_PACKAGES = ClassName.get("org.springframework.boot.autoconfigure",
			"AutoConfigurationPackages");

	public static final ClassName RESOLVABLE_TYPE = ClassName.get(ResolvableType.class);

	public static final ClassName CLASS_UTILS = ClassName.get(ClassUtils.class);

	public static final ClassName FACTORY_BEAN = ClassName.get(FactoryBean.class);

	public static final ClassName PARAMETERIZED_TYPE_REFERENCE = ClassName.get(ParameterizedTypeReference.class);

}

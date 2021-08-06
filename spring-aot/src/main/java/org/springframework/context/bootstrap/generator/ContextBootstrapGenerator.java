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

package org.springframework.context.bootstrap.generator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.bootstrap.generator.bean.BeanRegistrationGenerator;
import org.springframework.context.bootstrap.generator.bean.BeanValueWriter;
import org.springframework.context.bootstrap.generator.bean.BeanValueWriterSupplier;
import org.springframework.context.bootstrap.generator.bean.DefaultBeanRegistrationGenerator;
import org.springframework.context.bootstrap.generator.event.EventListenerMethodRegistrationGenerator;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * A simple experiment to generate a bootstrap class that represents the state of a fully
 * initialized {@link BeanFactory}.
 *
 * @author Stephane Nicoll
 */
public class ContextBootstrapGenerator {

	private static final Log logger = LogFactory.getLog(ContextBootstrapGenerator.class);

	private final List<BeanValueWriterSupplier> beanValueWriterSuppliers;

	public ContextBootstrapGenerator(ClassLoader classLoader) {
		this(SpringFactoriesLoader.loadFactories(BeanValueWriterSupplier.class, classLoader));
	}

	ContextBootstrapGenerator(List<BeanValueWriterSupplier> beanValueWriterSuppliers) {
		this.beanValueWriterSuppliers = beanValueWriterSuppliers;
	}

	/**
	 * Generate the code that is required to restore the state of the specified
	 * {@link BeanFactory}.
	 * @param beanFactory the bean factory state to replicate in code
	 * @param packageName the root package for the main {@code ContextBoostrap} class
	 * @param excludeTypes the types to exclude
	 * @return a list of {@linkplain JavaFile java source files}
	 */
	public BootstrapGenerationResult generateBootstrapClass(ConfigurableListableBeanFactory beanFactory, String packageName,
			Class<?>... excludeTypes) {
		BootstrapClass defaultBoostrapJavaFile = createDefaultBoostrapJavaFile(packageName);
		BootstrapWriterContext writerContext = new BootstrapWriterContext(defaultBoostrapJavaFile);
		this.beanValueWriterSuppliers.stream().filter(BeanFactoryAware.class::isInstance)
				.map(BeanFactoryAware.class::cast).forEach((callback) -> callback.setBeanFactory(beanFactory));
		DefaultBeanDefinitionSelector selector = new DefaultBeanDefinitionSelector(
				Arrays.stream(excludeTypes).map(Class::getName).collect(Collectors.toList()));
		defaultBoostrapJavaFile.addMethod(generateBootstrapMethod(beanFactory, writerContext, selector));
		return new BootstrapGenerationResult(writerContext.toJavaFiles(),
				writerContext.getRuntimeReflectionRegistry().getClassDescriptors()) ;
	}

	public BootstrapClass createDefaultBoostrapJavaFile(String packageName) {
		ParameterizedTypeName typeName = ParameterizedTypeName.get(
				ClassName.get(ApplicationContextInitializer.class),
				ClassName.get(GenericApplicationContext.class));
		return new BootstrapClass(packageName, BootstrapWriterContext.BOOTSTRAP_CLASS_NAME,
				(type) -> type.addSuperinterface(typeName).addModifiers(Modifier.PUBLIC));
	}

	private MethodSpec generateBootstrapMethod(ConfigurableListableBeanFactory beanFactory, BootstrapWriterContext writerContext,
			BeanDefinitionSelector selector) {
		ClassLoader classLoader = beanFactory.getBeanClassLoader();
		MethodSpec.Builder method = MethodSpec.methodBuilder("initialize").addModifiers(Modifier.PUBLIC)
				.addParameter(GenericApplicationContext.class, "context").addAnnotation(Override.class);
		CodeBlock.Builder code = CodeBlock.builder();
		registerApplicationContextInfrastructure(code);
		String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
			if (selector.select(beanName, beanDefinition)) {
				BeanRegistrationGenerator beanRegistrationGenerator = getBeanRegistrationGenerator(beanName,
						beanDefinition, classLoader);
				if (beanRegistrationGenerator != null) {
					beanRegistrationGenerator.writeBeanRegistration(writerContext, code);
				}
			}
		}
		// FIXME: provide SPI for this
		new EventListenerMethodRegistrationGenerator(beanFactory).writeEventListenersRegistration(code);

		method.addCode(code.build());
		return method.build();
	}

	private void registerApplicationContextInfrastructure(CodeBlock.Builder code) {
		code.add("// infrastructure\n");
		code.addStatement("context.getDefaultListableBeanFactory().setAutowireCandidateResolver(new $T())",
				ContextAnnotationAutowireCandidateResolver.class);
		code.add("\n");
	}

	private BeanRegistrationGenerator getBeanRegistrationGenerator(String beanName, BeanDefinition beanDefinition,
			ClassLoader classLoader) {
		BeanValueWriter beanValueWriter = getBeanValueSupplier(beanDefinition, classLoader);
		return (beanValueWriter != null) ? new DefaultBeanRegistrationGenerator(beanName, beanDefinition, beanValueWriter) : null;
	}

	private BeanValueWriter getBeanValueSupplier(BeanDefinition beanDefinition, ClassLoader classLoader) {
		for (BeanValueWriterSupplier supplier : this.beanValueWriterSuppliers) {
			BeanValueWriter writer = supplier.get(beanDefinition, classLoader);
			if (writer != null) {
				return writer;
			}
		}
		logger.error("Failed to handle bean with definition " + beanDefinition);
		return null;
	}

}

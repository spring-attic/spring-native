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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
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
import org.springframework.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.context.bootstrap.generator.bean.BeanRegistrationWriterSupplier;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.event.EventListenerMethodRegistrationGenerator;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistrar;
import org.springframework.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.context.bootstrap.generator.infrastructure.BootstrapInfrastructureWriter;
import org.springframework.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * A simple experiment to generate a bootstrap class that represents the state of a fully
 * initialized {@link BeanFactory}.
 *
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 */
public class ContextBootstrapGenerator {

	private static final Log logger = LogFactory.getLog(ContextBootstrapGenerator.class);

	private final List<BeanRegistrationWriterSupplier> beanRegistrationWriterSuppliers;

	public ContextBootstrapGenerator(ClassLoader classLoader) {
		this(SpringFactoriesLoader.loadFactories(BeanRegistrationWriterSupplier.class, classLoader));
	}

	ContextBootstrapGenerator(List<BeanRegistrationWriterSupplier> beanRegistrationWriterSuppliers) {
		this.beanRegistrationWriterSuppliers = beanRegistrationWriterSuppliers;
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
		NativeConfigurationRegistry nativeConfigurationRegistry = writerContext.getNativeConfigurationRegistry();
		this.beanRegistrationWriterSuppliers.stream().filter(BeanFactoryAware.class::isInstance)
				.map(BeanFactoryAware.class::cast).forEach((callback) -> callback.setBeanFactory(beanFactory));
		DefaultBeanDefinitionSelector selector = new DefaultBeanDefinitionSelector(
				Arrays.stream(excludeTypes).map(Class::getName).collect(Collectors.toList()));
		defaultBoostrapJavaFile.addMethod(generateBootstrapMethod(beanFactory, writerContext, selector));
		return new BootstrapGenerationResult(writerContext.toJavaFiles(),
				nativeConfigurationRegistry.reflection().toClassDescriptors(),
				nativeConfigurationRegistry.resources().toResourcesDescriptor(),
				nativeConfigurationRegistry.proxy().toProxiesDescriptor(),
				nativeConfigurationRegistry.initialization().toInitializationDescriptor(),
				nativeConfigurationRegistry.options()
		);
	}

	public BootstrapClass createDefaultBoostrapJavaFile(String packageName) {
		ParameterizedTypeName typeName = ParameterizedTypeName.get(
				ClassName.get(ApplicationContextInitializer.class),
				ClassName.get(GenericApplicationContext.class));
		return BootstrapClass.of(packageName, (type) ->
				type.addSuperinterface(typeName).addModifiers(Modifier.PUBLIC));
	}

	private MethodSpec generateBootstrapMethod(ConfigurableListableBeanFactory beanFactory, BootstrapWriterContext writerContext,
			BeanDefinitionSelector selector) {
		MethodSpec.Builder method = MethodSpec.methodBuilder("initialize").addModifiers(Modifier.PUBLIC)
				.addParameter(GenericApplicationContext.class, "context").addAnnotation(Override.class);
		CodeBlock.Builder code = CodeBlock.builder();
		registerApplicationContextInfrastructure(beanFactory, writerContext, code);
		List<BeanInstanceDescriptor> descriptors = writeBeanDefinitions(beanFactory, writerContext, selector, code);

		NativeConfigurationRegistrar nativeConfigurationRegistrar = new NativeConfigurationRegistrar(beanFactory);
		NativeConfigurationRegistry nativeConfigurationRegistry = writerContext.getNativeConfigurationRegistry();
		nativeConfigurationRegistrar.processBeanFactory(nativeConfigurationRegistry);
		nativeConfigurationRegistrar.processBeans(nativeConfigurationRegistry, descriptors);

		// FIXME: provide SPI for this
		new EventListenerMethodRegistrationGenerator(beanFactory).writeEventListenersRegistration(writerContext, code);

		method.addCode(code.build());
		return method.build();
	}

	private List<BeanInstanceDescriptor> writeBeanDefinitions(ConfigurableListableBeanFactory beanFactory,
			BootstrapWriterContext writerContext, BeanDefinitionSelector selector, Builder code) {
		List<BeanInstanceDescriptor> descriptors = new ArrayList<>();
		String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
			if (selector.select(beanName, beanDefinition)) {
				BeanRegistrationWriter beanRegistrationWriter = getBeanRegistrationGenerator(
						beanName, beanDefinition);
				if (beanRegistrationWriter != null) {
					beanRegistrationWriter.writeBeanRegistration(writerContext, code);
					descriptors.add(beanRegistrationWriter.getBeanInstanceDescriptor());
				}
			}
		}
		return descriptors;
	}

	private void registerApplicationContextInfrastructure(ConfigurableListableBeanFactory beanFactory,
			BootstrapWriterContext writerContext, CodeBlock.Builder code) {
		BootstrapInfrastructureWriter writer = new BootstrapInfrastructureWriter(beanFactory, writerContext);
		code.add("// infrastructure\n");
		writer.writeInfrastructure(code);
		code.add("\n");
	}

	private BeanRegistrationWriter getBeanRegistrationGenerator(String beanName, BeanDefinition beanDefinition) {
		for (BeanRegistrationWriterSupplier supplier : this.beanRegistrationWriterSuppliers) {
			BeanRegistrationWriter writer = supplier.get(beanName, beanDefinition);
			if (writer != null) {
				return writer;
			}
		}
		logger.error("Failed to handle bean " + beanName + " with definition " + beanDefinition);
		return null;
	}

}

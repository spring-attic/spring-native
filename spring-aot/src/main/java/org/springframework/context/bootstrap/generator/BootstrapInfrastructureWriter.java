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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;

import org.springframework.aot.context.annotation.ImportAwareInvoker;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.bootstrap.generator.reflect.RuntimeReflectionRegistry;
import org.springframework.context.bootstrap.generator.reflect.RuntimeResourceEntry;
import org.springframework.context.origin.BeanDefinitionDescriptor;
import org.springframework.context.origin.BeanDefinitionDescriptor.Type;
import org.springframework.context.origin.BeanFactoryStructure;
import org.springframework.context.origin.BeanFactoryStructureAnalyzer;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

/**
 * Write the necessary code to prepare the infrastructure so that the application
 * context can be bootstrapped.
 *
 * @author Stephane Nicoll
 */
class BootstrapInfrastructureWriter {

	private final ConfigurableListableBeanFactory beanFactory;

	private final BootstrapWriterContext writerContext;

	BootstrapInfrastructureWriter(ConfigurableListableBeanFactory beanFactory, BootstrapWriterContext writerContext) {
		this.beanFactory = beanFactory;
		this.writerContext = writerContext;
	}

	void writeInfrastructure(CodeBlock.Builder code) {
		code.addStatement("context.getDefaultListableBeanFactory().setAutowireCandidateResolver(new $T())",
				ContextAnnotationAutowireCandidateResolver.class);
		MethodSpec importAwareInvokerMethod = handleImportAwareInvoker();
		if (importAwareInvokerMethod != null) {
			this.writerContext.getBootstrapClass(this.writerContext.getPackageName()).addMethod(importAwareInvokerMethod);
			code.addStatement("$T.register(context, this::createImportAwareInvoker)", ImportAwareInvoker.class);
		}
	}

	MethodSpec handleImportAwareInvoker() {
		BeanFactoryStructure structure = createBeanFactoryStructure();
		Map<String, Class<?>> importLinks = new ImportAwareInfrastructureBuilder(structure, this.beanFactory.getBeanClassLoader())
				.buildImportAwareLinks(writerContext.getRuntimeReflectionRegistry());
		if (importLinks.isEmpty()) {
			return null;
		}
		Builder code = CodeBlock.builder();
		code.addStatement("$T mappings = new $T<>()", ParameterizedTypeName.get(Map.class, String.class, String.class), LinkedHashMap.class);
		importLinks.forEach((key, value) -> {
			code.addStatement("mappings.put($S, $S)", key, value.getName());
		});
		code.addStatement("return new $T($L)", ImportAwareInvoker.class, "mappings");
		return MethodSpec.methodBuilder("createImportAwareInvoker").returns(ImportAwareInvoker.class)
				.addModifiers(Modifier.PRIVATE).addCode(code.build()).build();
	}

	private BeanFactoryStructure createBeanFactoryStructure() {
		BeanFactoryStructureAnalyzer analyzer = new BeanFactoryStructureAnalyzer(
				this.beanFactory.getBeanClassLoader());
		return analyzer.analyze(this.beanFactory);
	}

	static class ImportAwareInfrastructureBuilder {

		private final BeanFactoryStructure structure;

		private final ClassLoader classLoader;

		ImportAwareInfrastructureBuilder(BeanFactoryStructure structure, ClassLoader classLoader) {
			this.structure = structure;
			this.classLoader = classLoader;
		}

		/**
		 * Identify the configuration classes that are {@link ImportAware} and return a
		 * mapping to their import class.
		 * @param runtimeReflectionRegistry the registry to use to declare the classes
		 * that should be loadable via ASM
		 * @return the mapping
		 */
		Map<String, Class<?>> buildImportAwareLinks(RuntimeReflectionRegistry runtimeReflectionRegistry) {
			Map<String, Class<?>> result = new LinkedHashMap<>();
			this.structure.getDescriptors().forEach((beanName, descriptor) -> {
				if (isImportAwareCandidate(descriptor)) {
					Class<?> importingClass = getBestEffortImportingClass(descriptor);
					if (importingClass != null) {
						String beanClassName = descriptor.getBeanDefinition().getResolvableType().toClass().getName();
						result.put(beanClassName, importingClass);
						runtimeReflectionRegistry.addResource(RuntimeResourceEntry.of(importingClass));
					}
				}
			});
			return result;
		}

		private boolean isImportAwareCandidate(BeanDefinitionDescriptor descriptor) {
			return descriptor.getType().equals(Type.CONFIGURATION) && descriptor.getOrigins() != null
					&& ImportAware.class.isAssignableFrom(descriptor.getBeanDefinition().getResolvableType().toClass());
		}

		private Class<?> getBestEffortImportingClass(BeanDefinitionDescriptor descriptor) {
			if (descriptor.getOrigins().isEmpty()) {
				return null;
			}
			LinkedList<String> tmp = new LinkedList<>(descriptor.getOrigins());
			BeanDefinitionDescriptor importingDescriptor = this.structure.getDescriptors().get(tmp.getLast());
			if (importingDescriptor != null) {
				AbstractBeanDefinition bd = (AbstractBeanDefinition) importingDescriptor.getBeanDefinition();
				if (!bd.getResolvableType().equals(ResolvableType.NONE)) {
					return ClassUtils.getUserClass(bd.getResolvableType().toClass());
				}
				if (bd.getBeanClassName() != null) {
					return loadBeanClassName(bd.getBeanClassName());
				}
			}
			return null;
		}

		private Class<?> loadBeanClassName(String className) {
			try {
				return ClassUtils.forName(className, this.classLoader);
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalStateException(
						"Bean definition refers to invalid class '" + className + "'", ex);
			}
		}

	}

}

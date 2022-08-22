/*
 * Copyright 2019-2022 the original author or authors.
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

package org.springframework.aot.context.bootstrap.generator.infrastructure;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;

import org.springframework.aot.context.annotation.ImportAwareBeanPostProcessor;
import org.springframework.aot.context.annotation.InitDestroyBeanPostProcessor;
import org.springframework.aot.context.bootstrap.generator.bean.support.ParameterWriter;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ResourcesConfiguration;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeResourcesEntry;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.annotation.ImportOriginRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * Write the necessary code to prepare the infrastructure so that the application
 * context can be bootstrapped.
 *
 * @author Stephane Nicoll
 */
public class BootstrapInfrastructureWriter {

	private final ConfigurableListableBeanFactory beanFactory;

	private final BootstrapWriterContext writerContext;

	private final ParameterWriter parameterWriter;

	public BootstrapInfrastructureWriter(ConfigurableListableBeanFactory beanFactory,
			BootstrapWriterContext writerContext) {
		this.beanFactory = beanFactory;
		this.writerContext = writerContext;
		this.parameterWriter = new ParameterWriter();
	}

	public void writeInfrastructure(CodeBlock.Builder code) {
		code.addStatement("$T beanFactory = context.getDefaultListableBeanFactory()", DefaultListableBeanFactory.class);
		code.addStatement("beanFactory.setAutowireCandidateResolver(new $T())", ContextAnnotationAutowireCandidateResolver.class);
		code.addStatement("beanFactory.setDependencyComparator($T.INSTANCE)", AnnotationAwareOrderComparator.class);
		MethodSpec.Builder importAwareBeanPostProcessorMethod = handleImportAwareBeanPostProcessor();
		BootstrapClass bootstrapClass = this.writerContext.getMainBootstrapClass();
		if (importAwareBeanPostProcessorMethod != null) {
			MethodSpec method = bootstrapClass.addMethod(importAwareBeanPostProcessorMethod);
			code.addStatement("beanFactory.addBeanPostProcessor($N())", method);
		}
		MethodSpec.Builder initDestroyBeanPostProcessorMethod = handleInitDestroyBeanPostProcessor();
		if (initDestroyBeanPostProcessorMethod != null) {
			MethodSpec method = bootstrapClass.addMethod(initDestroyBeanPostProcessorMethod);
			code.addStatement("beanFactory.addBeanPostProcessor($N(beanFactory))", method);
		}
	}

	private MethodSpec.Builder handleImportAwareBeanPostProcessor() {
		ImportOriginRegistry importOriginRegistry = ImportOriginRegistry.get(this.beanFactory);
		if (importOriginRegistry == null || importOriginRegistry.getImportOrigins().isEmpty()) {
			return null;
		}
		Map<String, Class<?>> importLinks = importOriginRegistry.getImportOrigins();
		ResourcesConfiguration resourcesConfiguration = this.writerContext.getNativeConfigurationRegistry().resources();
		importLinks.values().forEach((importingClass) -> resourcesConfiguration.add(
				NativeResourcesEntry.ofClass(importingClass)));

		Builder code = CodeBlock.builder();
		code.addStatement("$T mappings = new $T<>()", ParameterizedTypeName.get(Map.class, String.class, String.class),
				LinkedHashMap.class);
		importLinks.forEach((key, value) -> {
			code.addStatement("mappings.put($S, $S)", key, value.getName());
		});
		code.addStatement("return new $T($L)", ImportAwareBeanPostProcessor.class, "mappings");
		return MethodSpec.methodBuilder("createImportAwareBeanPostProcessor").returns(ImportAwareBeanPostProcessor.class)
				.addModifiers(Modifier.PRIVATE).addCode(code.build());
	}

	private MethodSpec.Builder handleInitDestroyBeanPostProcessor() {
		InitDestroyMethodsDiscoverer initDestroyMethodsDiscoverer = new InitDestroyMethodsDiscoverer(this.beanFactory);
		Map<String, List<Method>> initMethods = initDestroyMethodsDiscoverer.registerInitMethods(
				this.writerContext.getNativeConfigurationRegistry());
		Map<String, List<Method>> destroyMethods = initDestroyMethodsDiscoverer.registerDestroyMethods(
				this.writerContext.getNativeConfigurationRegistry());
		if (initMethods.isEmpty() && destroyMethods.isEmpty()) {
			return null;
		}
		Builder code = CodeBlock.builder();
		writeLifecycleMethods(code, initMethods, "initMethods");
		writeLifecycleMethods(code, destroyMethods, "destroyMethods");
		code.addStatement("return new $T($L, $L, $L)", InitDestroyBeanPostProcessor.class, "beanFactory", "initMethods", "destroyMethods");
		return MethodSpec.methodBuilder("createInitDestroyBeanPostProcessor").addParameter(ConfigurableBeanFactory.class, "beanFactory")
				.returns(InitDestroyBeanPostProcessor.class)
				.addModifiers(Modifier.PRIVATE).addCode(code.build());
	}

	private void writeLifecycleMethods(Builder code, Map<String, List<Method>> lifecycleMethods, String variableName) {
		code.addStatement("$T $L = new $T<>()", ParameterizedTypeName.get(ClassName.get(Map.class),
				ClassName.get(String.class), ParameterizedTypeName.get(List.class, String.class)), variableName, LinkedHashMap.class);
		lifecycleMethods.forEach((key, value) -> {
			code.addStatement("$L.put($S, $L)", variableName, key, this.parameterWriter.writeParameterValue(
					value.stream().map(Method::getName).collect(Collectors.toList()), () -> ResolvableType.forClassWithGenerics(List.class, String.class)));
		});
	}

}

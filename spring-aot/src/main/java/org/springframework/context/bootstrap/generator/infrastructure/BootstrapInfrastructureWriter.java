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

package org.springframework.context.bootstrap.generator.infrastructure;

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

import org.springframework.aot.context.annotation.ImportAwareInvoker;
import org.springframework.aot.context.annotation.InitDestroyBeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.bootstrap.generator.bean.support.ParameterWriter;
import org.springframework.context.origin.BeanFactoryStructure;
import org.springframework.context.origin.BeanFactoryStructureAnalyzer;
import org.springframework.core.ResolvableType;

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

	public BootstrapInfrastructureWriter(ConfigurableListableBeanFactory beanFactory, BootstrapWriterContext writerContext) {
		this.beanFactory = beanFactory;
		this.writerContext = writerContext;
		this.parameterWriter = new ParameterWriter();
	}

	public void writeInfrastructure(CodeBlock.Builder code) {
		code.addStatement("context.getDefaultListableBeanFactory().setAutowireCandidateResolver(new $T())",
				ContextAnnotationAutowireCandidateResolver.class);
		MethodSpec importAwareInvokerMethod = handleImportAwareInvoker();
		if (importAwareInvokerMethod != null) {
			this.writerContext.getBootstrapClass(this.writerContext.getPackageName()).addMethod(importAwareInvokerMethod);
			code.addStatement("$T.register(context, this::createImportAwareInvoker)", ImportAwareInvoker.class);
		}
		MethodSpec initDestroyBeanPostProcessorMethod = handleInitDestroyBeanPostProcessor();
		if (initDestroyBeanPostProcessorMethod != null) {
			this.writerContext.getBootstrapClass(this.writerContext.getPackageName()).addMethod(initDestroyBeanPostProcessorMethod);
			code.addStatement("context.getBeanFactory().addBeanPostProcessor($N())", initDestroyBeanPostProcessorMethod);
		}
	}

	private MethodSpec handleImportAwareInvoker() {
		BeanFactoryStructure structure = createBeanFactoryStructure();
		Map<String, Class<?>> importLinks = new ImportAwareLinksDiscoverer(structure, this.beanFactory.getBeanClassLoader())
				.buildImportAwareLinks(writerContext.getNativeConfigurationRegistry());
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

	private MethodSpec handleInitDestroyBeanPostProcessor() {
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
		code.addStatement("return new $T($L, $L)", InitDestroyBeanPostProcessor.class, "initMethods", "destroyMethods");
		return MethodSpec.methodBuilder("createInitDestroyBeanPostProcessor").returns(InitDestroyBeanPostProcessor.class)
				.addModifiers(Modifier.PRIVATE).addCode(code.build()).build();
	}

	private void writeLifecycleMethods(Builder code, Map<String, List<Method>> lifecycleMethods, String variableName) {
		code.addStatement("$T $L = new $T<>()", ParameterizedTypeName.get(ClassName.get(Map.class),
				ClassName.get(String.class), ParameterizedTypeName.get(List.class, String.class)), variableName, LinkedHashMap.class);
		lifecycleMethods.forEach((key, value) -> {
			code.addStatement("$L.put($S, $L)", variableName, key, this.parameterWriter.writeParameterValue(
					value.stream().map(Method::getName).collect(Collectors.toList()), ResolvableType.forClassWithGenerics(List.class, String.class)));
		});
	}

	private BeanFactoryStructure createBeanFactoryStructure() {
		BeanFactoryStructureAnalyzer analyzer = new BeanFactoryStructureAnalyzer(
				this.beanFactory.getBeanClassLoader());
		return analyzer.analyze(this.beanFactory);
	}

}

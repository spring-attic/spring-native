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

package org.springframework;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Field;
import org.springframework.nativex.type.Method;
import org.springframework.nativex.type.Type;

/**
 * Recognize annotations referenced in spring.components that need Synthesized annotation
 * proxies.
 *
 * @author Andy Clement
 */
public class SynthesizerComputationComponentProcessor implements ComponentProcessor {
	
	// The @AliasFor'd attributes don't always seem to be accessed via a proxy
	// so there is no need to create a proxy. This is the list that don't
	// seem to need a proxy:
	private static String[] DONT_NEED_PROXY = new String[] {
			"org.springframework.boot.autoconfigure.SpringBootApplication",
			"org.springframework.boot.SpringBootConfiguration",
			"org.springframework.context.annotation.Configuration",
			"org.springframework.context.annotation.ComponentScan",
			"org.springframework.web.bind.annotation.RestController",
			"org.springframework.stereotype.Controller",
			"org.springframework.web.bind.annotation.GetMapping"
	};

	@Override
	public boolean handle(NativeContext imageContext, String componentType, List<String> classifiers) {
		// Need to do deeper digging here so let's look at everything that can be resolved 
		Type type = imageContext.getTypeSystem().resolveName(componentType);
		return type != null;
	}

	@Override
	public void process(NativeContext imageContext, String componentType, List<String> classifiers) {
		Type type = imageContext.getTypeSystem().resolveName(componentType);
		Predicate<Type> isSpringAnnotation =  anno -> anno.getDottedName().startsWith("org.springframework");

		Set<Type> collector = new HashSet<>();
		for (Type annotationType: type.getAnnotations()) {
			annotationType.collectAnnotations(collector, isSpringAnnotation);
		}

		List<Method> methods = type.getMethods();
		// Example with annotations on the method and against parameters:
		// @GetMapping("/greeting")
		// public String greeting( @RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) {
		for (Method method: methods) {
			for (Type methodAnnotationType: method.getAnnotationTypes()) {
				methodAnnotationType.collectAnnotations(collector, isSpringAnnotation);
			}
			for (int pi=0;pi<method.getParameterCount();pi++) {
				List<Type> parameterAnnotationTypes = method.getParameterAnnotationTypes(pi);
				for (Type parameterAnnotationType: parameterAnnotationTypes) {
					parameterAnnotationType.collectAnnotations(collector, isSpringAnnotation);
				}
			}
		}
		
		List<Field> fields = type.getFields();
		for (Field field: fields) {
			for (Type fieldAnnotationType: field.getAnnotationTypes()) {
				fieldAnnotationType.collectAnnotations(collector, isSpringAnnotation);
			}
		}

		// From the candidate annotations, determine those that are truly the target
		// of aliases (either because @AliasFor'd from another annotation or using
		// internal @AliasFor references amongst its own attributes)
		Set<String> aliasForTargets = new HashSet<>();
		for (Type annotationType: collector) {
			annotationType.collectAliasReferencedMetas(aliasForTargets);
		}

		List<String> proxied = new ArrayList<>();
		for (String aliasForTarget: aliasForTargets) {
			if (!ignore(aliasForTarget)) {
				List<String> interfaces = new ArrayList<>();
				interfaces.add(aliasForTarget);
				interfaces.add("org.springframework.core.annotation.SynthesizedAnnotation");
				imageContext.addProxy(interfaces);
				proxied.add(aliasForTarget);
			}
		}
		imageContext.log("SynthesizerComputerComponentProcessor: From examining "+type.getDottedName()+" determined "+proxied.size()+" types as synthesized proxies: "+proxied);
	}
	
	private boolean ignore(String name) {
		for (String entry: DONT_NEED_PROXY) {
			if (entry.equals(name)) {
				return true;
			}
		}
		return false;
	}
}

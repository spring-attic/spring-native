/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.graalvm.extension.ComponentProcessor;
import org.springframework.graalvm.extension.NativeImageContext;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.graalvm.type.Method;
import org.springframework.graalvm.type.Type;

/**
 * Basic WebComponentProcessor - adds reflective access to types used in controller mappings if they are non
 * JDK or Spring Framework types.
 * 
 * @author Andy Clement
 */
public class WebComponentProcessor implements ComponentProcessor {

	private Set<String> added = new HashSet<>();

	@Override
	public boolean handle(NativeImageContext imageContext, String componentType, List<String> classifiers) {
		Type resolvedComponentType = imageContext.getTypeSystem().resolveDotted(componentType,true);
		return resolvedComponentType!=null && resolvedComponentType.isAtController();
	}

	@Override
	public void process(NativeImageContext imageContext, String componentType, List<String> classifiers) {
		Type controllerType = imageContext.getTypeSystem().resolveDotted(componentType,true);
		List<Method> mappings = controllerType.getMethods(m -> m.isAtMapping());
		System.out.println("WebComponentProcessor: in controller "+componentType+" processing mappings "+mappings);
		for (Method mapping: mappings) {
			List<Type> toProcess = new ArrayList<>();
			toProcess.addAll(mapping.getParameterTypes());
			toProcess.add(mapping.getReturnType());
			for (Type type: toProcess) {
				if (type == null) {
					continue;
				}
				String typename = type.getDottedName();
				if (typename.startsWith("java.") ||
					typename.startsWith("org.springframework.ui.") ||
					typename.startsWith("org.springframework.validation.")) {
					continue;
				}
				if (added.add(typename)) {
					Set<String> added = 
							imageContext.addReflectiveAccessHierarchy(typename, 
									AccessBits.CLASS|AccessBits.DECLARED_METHODS|AccessBits.DECLARED_CONSTRUCTORS);
					imageContext.log("WebComponentProcessor: adding reflective access to "+added+" (whilst introspecting controller "+componentType+")");
				}
			}
		}
	}

}

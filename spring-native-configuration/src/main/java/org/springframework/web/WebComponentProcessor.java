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

package org.springframework.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.type.Field;
import org.springframework.nativex.type.Method;
import org.springframework.nativex.type.Type;

/**
 * Basic WebComponentProcessor - adds reflective access to types used in controller mappings if they are non
 * JDK or Spring Framework types.
 * 
 * @author Andy Clement
 */
public class WebComponentProcessor implements ComponentProcessor {

	private Set<String> added = new HashSet<>();

	@Override
	public boolean handle(NativeContext imageContext, String componentType, List<String> classifiers) {
		Type resolvedComponentType = imageContext.getTypeSystem().resolveDotted(componentType,true);
		return resolvedComponentType!=null && resolvedComponentType.isAtController();
	}

	@Override
	public void process(NativeContext imageContext, String componentType, List<String> classifiers) {
		Type controllerType = imageContext.getTypeSystem().resolveDotted(componentType,true);
		List<Method> mappings = controllerType.getMethods(m -> m.isAtMapping());
		imageContext.log("WebComponentProcessor: in controller "+componentType+" processing mappings "+mappings);
		for (Method mapping: mappings) {
			List<Type> toProcess = new ArrayList<>();
			toProcess.addAll(mapping.getParameterTypes());
			toProcess.addAll(mapping.getSignatureTypes(true));
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
					Set<String> added = imageContext.addReflectiveAccessHierarchy(typename, AccessBits.FULL_REFLECTION);
					analyze(imageContext, type, added);
					imageContext.log("WebComponentProcessor: adding reflective access to "+added+" (whilst introspecting controller "+componentType+")");
				}
			}
		}
	}

	private void analyze(NativeContext imageContext, Type type, Set<String> added) {
		List<Field> fields = type.getFields();
		for (Field field: fields) {
			Set<String> fieldTypenames = field.getTypesInSignature();
			for (String fieldTypename: fieldTypenames) {
				if (fieldTypename == null) {
					continue;
				}
				String dottedFieldTypename = fieldTypename.replace("/", ".");
				if (!ignore(dottedFieldTypename) && added.add(dottedFieldTypename)) {
					added.addAll(imageContext.addReflectiveAccessHierarchy(dottedFieldTypename, AccessBits.FULL_REFLECTION));
					// Recursive analysis - helps with something like a Vets type that includes a List<Vet>. Vet gets
					// recognized too.
					analyze(imageContext, imageContext.getTypeSystem().resolveDotted(dottedFieldTypename,true), added);
				}
			}
		}
	}

	private boolean ignore(String name) {
		return (name.startsWith("java.") ||
				name.startsWith("org.springframework.ui.") ||
				name.startsWith("org.springframework.validation."));
	}
}

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

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Configuration;

/**
 * @author Andy Clement
 * @author Dave Syer
 */
public class CachingSubclassSpecs {

	private Map<Class<?>, CachingSubclassSpec> subclasses = new LinkedHashMap<>();

	private ElementUtils utils;

	private Imports imports;

	public CachingSubclassSpecs(ElementUtils utils, Imports imports) {
		this.utils = utils;
		this.imports = imports;
	}

	public Set<CachingSubclassSpec> getCachingSubclasses() {
		return new LinkedHashSet<>(this.subclasses.values());
	}

	public void addConfigurationType(Class<?> type) {
		if (subclasses.containsKey(type)) {
			return;
		}
		subclasses.put(type, new CachingSubclassSpec(this.utils, type, imports));
		findNestedInitializers(type, new HashSet<>());
	}

	private void findNestedInitializers(Class<?> type, Set<Class<?>> types) {
		if (!types.contains(type) && !Modifier.isAbstract(type.getModifiers())
				&& utils.hasAnnotation(type, Configuration.class.getName())) {
			try {
				for (Class<?> element : type.getDeclaredClasses()) {
					if (Modifier.isStatic(element.getModifiers())) {
						if (utils.hasAnnotation(element, SpringClassNames.CONFIGURATION.toString())) {
							imports.addNested(type, element);
						}
						findNestedInitializers(element, types);
					}
				}
			}
			catch (NoClassDefFoundError e) {
				return;
			}
			addConfigurationType(type);
			types.add(type);
		}

	}

}

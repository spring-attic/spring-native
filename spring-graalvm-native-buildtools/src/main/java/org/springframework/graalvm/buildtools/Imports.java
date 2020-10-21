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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;

/**
 * @author Dave Syer
 *
 */
public class Imports {

	private ElementUtils utils;

	private Map<Class<?>, Set<Class<?>>> imports = new ConcurrentHashMap<>();

	private Set<Class<?>> included = new LinkedHashSet<>();

	private Set<Class<?>> registrars = new LinkedHashSet<>();

	private Set<Class<?>> selectors = new LinkedHashSet<>();

	public Imports(ElementUtils utils) {
		this.utils = utils;
	}

	public void addImport(Class<?> owner, Class<?> imported) {
		this.included.add(imported);
		if (owner.equals(imported)) {
			return;
		}
		imports.computeIfAbsent(owner, key -> new LinkedHashSet<>()).add(imported);
		classify(imported);
	}

	public void addNested(Class<?> owner, Class<?> nested) {
		Set<Class<?>> set = imports.computeIfAbsent(owner, key -> new LinkedHashSet<>());
		set.add(nested);
		this.included.add(nested);
	}

	private void classify(Class<?> imported) {
		if (utils.implementsInterface(imported, ImportBeanDefinitionRegistrar.class)) {
			registrars.add(imported);
		}
		else if (utils.implementsInterface(imported, ImportSelector.class)) {
			selectors.add(imported);
		}
	}

	public Set<Class<?>> getIncluded() {
		return this.included;
	}

	public Map<Class<?>, Set<Class<?>>> getImports() {
		return this.imports;
	}

	public Set<Class<?>> getImports(Class<?> type) {
		return this.imports.containsKey(type) ? this.getImports().get(type) : Collections.emptySet();
	}

	public Set<Class<?>> getRegistrars() {
		return this.registrars;
	}

	public Set<Class<?>> getSelectors() {
		return this.selectors;
	}

}

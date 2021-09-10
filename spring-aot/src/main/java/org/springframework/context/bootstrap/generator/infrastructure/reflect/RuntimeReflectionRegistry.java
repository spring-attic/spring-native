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

package org.springframework.context.bootstrap.generator.infrastructure.reflect;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;

/**
 * Collect the need for runtime reflection on Classes, Methods, and Fields.
 *
 * @author Brian Clozel
 */
public class RuntimeReflectionRegistry {

	private final Map<Class<?>, RuntimeReflectionEntry.Builder> classes = new LinkedHashMap<>();

	private final Set<RuntimeResourceEntry> resources = new LinkedHashSet<>();


	/**
	 * Register the specified {@link Executable method or constructor}.
	 * @param executable the executable to register
	 * @return this for method chaining
	 */
	public RuntimeReflectionRegistry addMethod(Executable executable) {
		add(executable.getDeclaringClass()).withMethods(executable);
		return this;
	}

	/**
	 * Register the specified {@link Field field}.
	 * @param field the field to register
	 * @return this for method chaining
	 */
	public RuntimeReflectionRegistry addField(Field field) {
		add(field.getDeclaringClass()).withFields(field);
		return this;
	}

	/**
	 * Return the {@link RuntimeReflectionEntry.Builder} to further describe the specified
	 * type.
	 * @param type a type to provide runtime reflection for
	 * @return a builder to further describe the need for runtime reflection
	 */
	public RuntimeReflectionEntry.Builder add(Class<?> type) {
		return this.classes.computeIfAbsent(type, RuntimeReflectionEntry.Builder::new);
	}

	/**
	 * Register the specified {@link RuntimeResourceEntry resource}.
	 * @param resource the resource that should be available
	 * @return this for method chaining
	 */
	public RuntimeReflectionRegistry addResource(RuntimeResourceEntry resource) {
		this.resources.add(resource);
		return this;
	}

	/**
	 * Return the {@link RuntimeReflectionEntry entries} of this registry.
	 * @return the entries in the registry
	 */
	public List<RuntimeReflectionEntry> getEntries() {
		return this.classes.values().stream().map(RuntimeReflectionEntry.Builder::build)
				.collect(Collectors.toList());
	}

	/**
	 * Return the {@link ClassDescriptor entries} of this registry.
	 * @return the classes entries in the registry, as {@link ClassDescriptor} instances
	 */
	public List<ClassDescriptor> getClassDescriptors() {
		return this.classes.values().stream().map((builder) -> builder.build()
				.toClassDescriptor()).collect(Collectors.toList());
	}

	/**
	 * Return the {@link ResourcesDescriptor} of this registry.
	 * @return the resources entries in the registry, as {@link ResourcesDescriptor} instances
	 */
	public ResourcesDescriptor getResourcesDescriptor() {
		ResourcesDescriptor resourcesDescriptor = new ResourcesDescriptor();
		this.resources.forEach((resource) -> resource.contribute(resourcesDescriptor));
		return resourcesDescriptor;
	}

}

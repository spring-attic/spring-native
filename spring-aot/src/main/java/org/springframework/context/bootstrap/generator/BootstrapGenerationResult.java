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

import java.util.List;
import java.util.Set;

import com.squareup.javapoet.JavaFile;

import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;


/**
 * Provide the result of the the {@link ContextBootstrapGenerator} processing.
 *
 * @author Brian Clozel
 */
public class BootstrapGenerationResult {

	private final List<JavaFile> sourceFiles;

	private final List<ClassDescriptor> classDescriptors;

	private final ResourcesDescriptor resourcesDescriptor;

	private final ProxiesDescriptor proxiesDescriptor;

	private final InitializationDescriptor initializationDescriptor;

	private final Set<String> options;

	BootstrapGenerationResult(List<JavaFile> sourceFiles, List<ClassDescriptor> classDescriptors,
			ResourcesDescriptor resourcesDescriptor, ProxiesDescriptor proxiesDescriptor,
			InitializationDescriptor initializationDescriptor, Set<String> options) {
		this.sourceFiles = sourceFiles;
		this.classDescriptors = classDescriptors;
		this.resourcesDescriptor = resourcesDescriptor;
		this.proxiesDescriptor = proxiesDescriptor;
		this.initializationDescriptor = initializationDescriptor;
		this.options = options;
	}

	/**
	 * Return the {@link JavaFile source files}.
	 * @return the context bootstrap classes
	 */
	public List<JavaFile> getSourceFiles() {
		return this.sourceFiles;
	}

	/**
	 * Return the {@link ClassDescriptor reflection entries} that are necessary to process
	 * the bootstrap of the context.
	 * @return the reflection entries
	 */
	public List<ClassDescriptor> getClassDescriptors() {
		return this.classDescriptors;
	}

	/**
	 * Return the {@link ResourcesDescriptor resources} that are necessary to process
	 * the bootstrap of the context.
	 * @return the resources
	 */
	public ResourcesDescriptor getResourcesDescriptor() {
		return this.resourcesDescriptor;
	}

	/**
	 * Return the {@link ProxiesDescriptor proxies} that are necessary to process
	 * the bootstrap of the context.
	 * @return the proxies
	 */
	public ProxiesDescriptor getProxiesDescriptor() {
		return this.proxiesDescriptor;
	}

	/**
	 * Return the {@link InitializationDescriptor initialization entries} that are necessary to process
	 * the bootstrap of the context.
	 * @return the initialization entries
	 */
	public InitializationDescriptor getInitializationDescriptor() {
		return this.initializationDescriptor;
	}

	/**
	 * Return the native-image options that are necessary to process
	 * the bootstrap of the context.
	 * @return the options
	 */
	public Set<String> getOptions() {
		return this.options;
	}

}

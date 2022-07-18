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

package org.springframework.aot.build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.aot.build.context.BuildContext;
import org.springframework.aot.build.context.ResourceFile;
import org.springframework.aot.build.context.SourceFile;
import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;
import org.springframework.nativex.domain.serialization.SerializationDescriptor;

/**
 * Default implementation for the {@link BuildContext}
 */
class DefaultBuildContext implements BuildContext {

	private final ClassLoader classLoader;

	private final String mainClass;

	private final String applicationClass;

	private final List<String> primaryClasses;

	private final List<String> testClasses;

	private final List<String> classpath;

	private final Set<String> options = new LinkedHashSet<>();

	private final List<SourceFile> sourceFiles = new ArrayList<>();

	private final List<ResourceFile> resourceFiles = new ArrayList<>();

	private final ReflectionDescriptor reflectionDescriptor = new ReflectionDescriptor();

	private final ProxiesDescriptor proxiesDescriptor = new ProxiesDescriptor();

	private final ResourcesDescriptor resourcesDescriptor = new ResourcesDescriptor();

	private final SerializationDescriptor serializationDescriptor = new SerializationDescriptor();

	private final ReflectionDescriptor jniReflectionDescriptor = new ReflectionDescriptor();

	private final InitializationDescriptor initializationDescriptor = new InitializationDescriptor();

	DefaultBuildContext(ApplicationStructure applicationStructure) {
		this.mainClass = applicationStructure.getMainClass();
		this.applicationClass = applicationStructure.getApplicationClass();
		this.primaryClasses = applicationStructure.getPrimaryClasses();
		this.testClasses = applicationStructure.getTestClasses();
		this.classpath = applicationStructure.getClasspath();
		this.classLoader = applicationStructure.getClassLoader();
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	@Override
	public List<String> getClasspath() {
		return this.classpath;
	}

	@Override
	public String getMainClass() {
		return this.mainClass;
	}

	@Override
	public Set<String> getOptions() {
		return this.options;
	}

	@Override
	public String getApplicationClass() {
		return this.applicationClass;
	}

	@Override
	public List<String> getPrimaryClasses() {
		return primaryClasses;
	}

	@Override
	public List<String> getTestClasses() {
		return this.testClasses;
	}

	@Override
	public void addSourceFiles(SourceFile... sourceFiles) {
		this.sourceFiles.addAll(Arrays.asList(sourceFiles));
	}

	@Override
	public void addResources(ResourceFile... resourceFiles) {
		this.resourceFiles.addAll(Arrays.asList(resourceFiles));
	}

	@Override
	public void describeReflection(Consumer<ReflectionDescriptor> consumer) {
		consumer.accept(this.reflectionDescriptor);
	}

	@Override
	public void describeJNIReflection(Consumer<ReflectionDescriptor> consumer) {
		consumer.accept(this.jniReflectionDescriptor);
	}

	@Override
	public void describeSerialization(Consumer<SerializationDescriptor> consumer) {
		consumer.accept(this.serializationDescriptor);
	}

	@Override
	public void describeInitialization(Consumer<InitializationDescriptor> consumer) {
		consumer.accept(this.initializationDescriptor);
	}

	@Override
	public void describeProxies(Consumer<ProxiesDescriptor> consumer) {
		consumer.accept(this.proxiesDescriptor);
	}

	@Override
	public void describeResources(Consumer<ResourcesDescriptor> consumer) {
		consumer.accept(this.resourcesDescriptor);
	}

	List<SourceFile> getSourceFiles() {
		return this.sourceFiles;
	}

	List<ResourceFile> getResourceFiles() {
		return this.resourceFiles;
	}

	public ReflectionDescriptor getReflectionDescriptor() {
		return this.reflectionDescriptor;
	}

	public SerializationDescriptor getSerializationDescriptor() {
		return this.serializationDescriptor;
	}

	public ReflectionDescriptor getJNIReflectionDescriptor() {
		return this.jniReflectionDescriptor;
	}

	public ProxiesDescriptor getProxiesDescriptor() {
		return this.proxiesDescriptor;
	}

	public ResourcesDescriptor getResourcesDescriptor() {
		return this.resourcesDescriptor;
	}

	public InitializationDescriptor getInitializationDescriptor() {
		return this.initializationDescriptor;
	}

}

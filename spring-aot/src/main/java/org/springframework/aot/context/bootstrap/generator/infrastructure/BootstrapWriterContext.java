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

package org.springframework.aot.context.bootstrap.generator.infrastructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.JavaFile;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;

/**
 * Context for components that write code to boostrap the context.
 *
 * @author Stephane Nicoll
 */
public class BootstrapWriterContext {

	private final String packageName;

	private final ProtectedAccessAnalyzer protectedAccessAnalyzer;

	private final Map<String, BootstrapClass> bootstrapClasses = new HashMap<>();

	private final NativeConfigurationRegistry nativeConfigurationRegistry = new NativeConfigurationRegistry();

	public BootstrapWriterContext(BootstrapClass defaultJavaFile) {
		this.packageName = defaultJavaFile.getClassName().packageName();
		this.protectedAccessAnalyzer = new ProtectedAccessAnalyzer(this.packageName);
		this.bootstrapClasses.put(packageName, defaultJavaFile);
	}

	/**
	 * Return the package name in which the main bootstrap class is located.
	 * @return the default package name
	 */
	public String getPackageName() {
		return this.packageName;
	}

	/**
	 * Return the {@link ProtectedAccessAnalyzer} to use.
	 * @return the protected access analyzer
	 */
	public ProtectedAccessAnalyzer getProtectedAccessAnalyzer() {
		return this.protectedAccessAnalyzer;
	}

	/**
	 * Return a {@link BootstrapClass} for the specified package name. If it does not
	 * exist, it is created
	 * @param packageName the package name to use
	 * @return the bootstrap class
	 */
	public BootstrapClass getBootstrapClass(String packageName) {
		return this.bootstrapClasses.computeIfAbsent(packageName, (p) ->
				BootstrapClass.of(packageName, (type) ->
						type.addModifiers(Modifier.PUBLIC, Modifier.FINAL)));
	}

	/**
	 * Return the default {@link BootstrapClass}.
	 * @return the bootstrap class for the target package
	 * @see #getPackageName()
	 */
	public BootstrapClass getMainBootstrapClass() {
		return getBootstrapClass(this.packageName);
	}

	/**
	 * Specify if a {@link BootstrapClass} for the specified package name is registered.
	 * @param packageName the package name to use
	 * @return {@code true} if the class is registered for that package
	 */
	public boolean hasBootstrapClass(String packageName) {
		return this.bootstrapClasses.containsKey(packageName);
	}

	/**
	 * Return the list of {@link JavaFile} of known bootstrap classes.
	 * @return the java files of bootstrap classes in this instance
	 */
	public List<JavaFile> toJavaFiles() {
		return this.bootstrapClasses.values().stream().map(BootstrapClass::toJavaFile).collect(Collectors.toList());
	}

	/**
	 * Return a {@link NativeConfigurationRegistry} for recording the necessary native
	 * configuration for this context
	 * @return the native configuration registry
	 */
	public NativeConfigurationRegistry getNativeConfigurationRegistry() {
		return this.nativeConfigurationRegistry;
	}

}

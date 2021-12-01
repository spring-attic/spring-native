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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.ApplicationContextInitializer;

/**
 * A default {@link BootstrapWriterContext} implementation. Exposes generated classes
 * as {@link JavaFile java files} so that they can be generated to disk.
 *
 * @author Stephane Nicoll
 */
public class DefaultBootstrapWriterContext implements BootstrapWriterContext {

	private final String packageName;

	private final NativeConfigurationRegistry nativeConfigurationRegistry;

	private final ProtectedAccessAnalyzer protectedAccessAnalyzer;

	private final Map<String, DefaultBootstrapWriterContext> allContexts;

	private final Function<String, BootstrapClass> bootstrapClassFactory;

	private final Map<String, BootstrapClass> bootstrapClasses;

	DefaultBootstrapWriterContext(DefaultBootstrapWriterContext origin,
			Function<String, BootstrapClass> bootstrapClassFactory) {
		this.packageName = origin.packageName;
		this.nativeConfigurationRegistry = origin.nativeConfigurationRegistry;
		this.protectedAccessAnalyzer = origin.protectedAccessAnalyzer;
		this.allContexts = origin.allContexts;
		this.bootstrapClassFactory = bootstrapClassFactory;
		this.bootstrapClasses = new LinkedHashMap<>();
	}

	/**
	 * Create a context targeting the specified package name and using the specified
	 * factory to create a {@link BootstrapClass} per requested package name.
	 * @param packageName the main package name
	 * @param bootstrapClassFactory the factory to use to create a {@link BootstrapClass}
	 * based on a package name.
	 */
	public DefaultBootstrapWriterContext(String packageName, Function<String, BootstrapClass> bootstrapClassFactory) {
		this.packageName = packageName;
		this.nativeConfigurationRegistry = new NativeConfigurationRegistry();
		this.protectedAccessAnalyzer = new ProtectedAccessAnalyzer(this.packageName);
		this.allContexts = new HashMap<>();
		this.allContexts.put("main", this);
		this.bootstrapClassFactory = bootstrapClassFactory;
		this.bootstrapClasses = new LinkedHashMap<>();
	}

	/**
	 * Create a context targeting the specified package name and using a unique name for
	 * all classes handled by this instance, applying a factory that creates a
	 * {@link BootstrapClass} for the main context as an
	 * {@link ApplicationContextInitializer} and the package protected boostrap classes
	 * as empty {@code public final} types.
	 * @param packageName the main package name
	 * @param className the name to use for bootstrap classes handled by this instance
	 * @see #bootstrapClassFactory
	 */
	public DefaultBootstrapWriterContext(String packageName, String className) {
		this(packageName, BootstrapWriterContext.bootstrapClassFactory(packageName, className));
	}

	@Override
	public NativeConfigurationRegistry getNativeConfigurationRegistry() {
		return this.nativeConfigurationRegistry;
	}

	@Override
	public ProtectedAccessAnalyzer getProtectedAccessAnalyzer() {
		return this.protectedAccessAnalyzer;
	}

	@Override
	public BootstrapClass getBootstrapClass(String packageName) {
		return this.bootstrapClasses.computeIfAbsent(packageName, this.bootstrapClassFactory);
	}

	@Override
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

	@Override
	public BootstrapWriterContext fork(ClassName className) {
		return fork(className.canonicalName(), BootstrapWriterContext.bootstrapClassFactory(this.packageName,
				determineDefaultSimpleName(className.simpleName(), 0)));
	}

	@Override
	public BootstrapWriterContext fork(String id, Function<String, BootstrapClass> bootstrapClassFactory) {
		if (this.allContexts.containsKey(id)) {
			throw new IllegalArgumentException("context with id '" + id + "' already exists");
		}
		DefaultBootstrapWriterContext writerContext = new DefaultBootstrapWriterContext(this, bootstrapClassFactory);
		this.allContexts.put(id, writerContext);
		return writerContext;
	}

	/**
	 * Return the list of {@link JavaFile} of known bootstrap classes.
	 * @return the java files of bootstrap classes in this instance
	 */
	public List<JavaFile> toJavaFiles() {
		return this.allContexts.values().stream()
				.flatMap((context) -> context.bootstrapClasses.values().stream().map(BootstrapClass::toJavaFile))
				.collect(Collectors.toList());
	}

	/**
	 * Determine a candidate name for the specified simple name and index.
	 * @param simpleName the chosen {@link Class#getSimpleName() simple name}
	 * @param index the current index (0 for the first attempt)
	 * @return the simple name to use (unchanged if no such name is in use already)
	 */
	private String determineDefaultSimpleName(String simpleName, int index) {
		String candidate = (index == 0) ? simpleName : simpleName + index;
		DefaultBootstrapWriterContext existingContext = this.allContexts.values().stream().filter((context) -> {
			BootstrapClass bootstrapClass = context.bootstrapClassFactory.apply(this.packageName);
			return bootstrapClass.getClassName().simpleName().equals(candidate);
		}).findFirst().orElse(null);
		if (existingContext == null) {
			return candidate;
		}
		return determineDefaultSimpleName(simpleName, ++index);
	}

}

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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.loader.tools.MainClassFinder;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public class ApplicationStructure {

	private final Path sourcesPath;

	private final Path resourcesPath;

	private final Set<Path> resourceFolders;
	
	private final List<Path> classesPaths;

	private final List<String> classpath;

	private final String mainClass;

	private final String applicationClass;

	private final List<String> testClasses;

	private final ClassLoader classLoader;

	public ApplicationStructure(Path sourcesPath, Path resourcesPath, Set<Path> resourceFolders, List<Path> classesPaths,
			@Nullable String mainClass, @Nullable String applicationClass, List<String> testClasses, List<String> classpath,
			@Nullable ClassLoader classLoader) throws IOException {
		this.sourcesPath = sourcesPath;
		this.resourcesPath = resourcesPath;
		this.resourceFolders = resourceFolders;
		this.classesPaths = classesPaths;
		this.mainClass = mainClass != null ? mainClass : detectMainClass(classesPaths);
		if (applicationClass != null) {
			this.applicationClass = applicationClass;
		}
		else {
			String detectedApplicationClass = detectApplicationClass(classesPaths);
			this.applicationClass = detectedApplicationClass != null ? detectedApplicationClass : mainClass;
		}
		this.testClasses = testClasses;
		this.classpath = classpath;
		this.classLoader = classLoader;
	}

	public Path getSourcesPath() {
		return this.sourcesPath;
	}

	public Path getResourcesPath() {
		return this.resourcesPath;
	}

	public Set<Path> getResourceFolders() {
		return this.resourceFolders;
	}

	public List<Path> getClassesPath() {
		return this.classesPaths;
	}

	public String getMainClass() {
		return this.mainClass;
	}

	public String getApplicationClass() {
		return applicationClass;
	}

	public List<String> getTestClasses() {
		return this.testClasses;
	}

	public List<String> getClasspath() {
		return this.classpath;
	}

	public ClassLoader getClassLoader() {
		return this.classLoader != null ? this.classLoader : getSyntheticClassLoader();
	}

	private String detectMainClass(List<Path> classesPaths) throws IOException {
		for (Path path : classesPaths) {
			File file = path.toFile();
			// For now only search in directories, could be extended to JARs using the JarFile parameter variant
			if (file.isDirectory()) {
				try {
					String mainClass = MainClassFinder.findSingleMainClass(file);
					if (StringUtils.hasText(mainClass)) {
						return mainClass;
					}
				} catch (IllegalStateException ex) {
					// More that one class have been found
				}
			}
		}
		return null;
	}

	private String detectApplicationClass(List<Path> classesPaths) throws IOException {
		for (Path path : classesPaths) {
			File file = path.toFile();
			// For now only search in directories, could be extended to JARs using the JarFile parameter variant
			if (file.isDirectory()) {
				try {
					String applicationClass = AnnotatedClassFinder.findSingleAnnotatedClass(file, SpringBootApplication.class.getName());
					if (StringUtils.hasText(applicationClass)) {
						return applicationClass;
					}
					else {
						String configurationClass = AnnotatedClassFinder.findSingleAnnotatedClass(file, SpringBootConfiguration.class.getName());
						if (StringUtils.hasText(configurationClass)) {
							return configurationClass;
						}
					}
				} catch (IllegalStateException ex) {
					// More that one class have been found
				}
			}
		}
		return null;
	}

	private ClassLoader getSyntheticClassLoader() {
		try {
			List<URL> urls = new ArrayList<>();
			List<URI> uris = this.classpath.stream().map(File::new).map(File::toURI).collect(Collectors.toList());
			for (URI uri : uris) {
				urls.add(uri.toURL());
			}
			ClassLoader parentClassLoader =  null;
			// If we're on JDK9+, we need to use the PlatformClassLoader
			// or we'll miss JDK classes that aren't in the base module.
			if (ClassUtils.hasMethod(Optional.class, "stream", new Class[0])) {
				Method getPlatformClassLoader = ReflectionUtils.findMethod(ClassLoader.class, "getPlatformClassLoader");
				parentClassLoader = (ClassLoader) ReflectionUtils.invokeMethod(getPlatformClassLoader, null);
			}
			return new URLClassLoader(urls.toArray(new URL[0]), parentClassLoader);
		}
		catch (Exception ex) {
			throw new CodeGenerationException("Unable to build classpath", ex);
		}
	}


}

/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.aot;

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

import org.springframework.boot.loader.tools.MainClassFinder;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

public class ApplicationStructure {

	private final Path sourcesPath;

	private final Path resourcesPath;

	private final Set<Path> resourceFolders;
	
	private final Path classesPath;

	private final List<String> classpath;

	private String mainClass;

	private ClassLoader classLoader;

	public ApplicationStructure(Path sourcesPath, Path resourcesPath, Set<Path> resourceFolders, Path classesPath,
			@Nullable String mainClass, List<String> classpath, @Nullable ClassLoader classLoader) throws IOException {
		this.sourcesPath = sourcesPath;
		this.resourcesPath = resourcesPath;
		this.resourceFolders = resourceFolders;
		this.classesPath = classesPath;
		this.mainClass = mainClass != null ? mainClass : detectMainClass(classesPath);
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

	public Path getClassesPath() {
		return this.classesPath;
	}

	public String getMainClass() {
		return this.mainClass;
	}

	public List<String> getClasspath() {
		return this.classpath;
	}

	public ClassLoader getClassLoader() {
		return this.classLoader != null ? this.classLoader : getSyntheticClassLoader();
	}

	private String detectMainClass(Path classesPath) throws IOException {
		return MainClassFinder.findSingleMainClass(classesPath.toFile());
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

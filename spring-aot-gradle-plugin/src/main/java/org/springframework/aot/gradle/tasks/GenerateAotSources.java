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

package org.springframework.aot.gradle.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import org.springframework.aot.BootstrapCodeGenerator;
import org.springframework.aot.gradle.dsl.SpringAotExtension;

/**
 * {@link org.gradle.api.Task} that generates AOT sources using the {@link BootstrapCodeGenerator}.
 *
 * @author Brian Clozel
 * @author Andy Wilkinson
 */
public class GenerateAotSources extends DefaultTask {

	private FileCollection classpath;

	private SourceDirectorySet resourceDirectories;

	private final DirectoryProperty sourcesOutputDirectory;

	private final DirectoryProperty resourcesOutputDirectory;

	private final GenerateAotOptions aotOptions;

	public GenerateAotSources() {
		this.sourcesOutputDirectory = getProject().getObjects().directoryProperty();
		this.resourcesOutputDirectory = getProject().getObjects().directoryProperty();
		this.aotOptions = new GenerateAotOptions(getProject().getExtensions().findByType(SpringAotExtension.class));
	}

	@InputFiles
	public FileCollection getClasspath() {
		return this.classpath;
	}

	public void setClasspath(FileCollection classpath) {
		this.classpath = classpath;
	}

	@InputFiles
	public FileCollection getResourceDirectories() {
		return this.resourceDirectories;
	}

	public void setResourceInputDirectories(SourceDirectorySet resourceDirectories) {
		this.resourceDirectories = resourceDirectories;
	}

	@OutputDirectory
	public DirectoryProperty getSourcesOutputDirectory() {
		return this.sourcesOutputDirectory;
	}

	@OutputDirectory
	public DirectoryProperty getResourcesOutputDirectory() {
		return this.resourcesOutputDirectory;
	}

	@Nested
	public GenerateAotOptions getAotOptions() {
		return this.aotOptions;
	}

	@TaskAction
	public void generateSources() {
		List<String> classpathElements = this.classpath.getFiles().stream()
				.map(File::getAbsolutePath).collect(Collectors.toList());
		Set<Path> resourcesElements = this.resourceDirectories.getSrcDirs().stream().map(File::toPath).collect(Collectors.toSet());
		BootstrapCodeGenerator generator = new BootstrapCodeGenerator(this.aotOptions.toAotOptions());
		try {
			generator.generate(this.sourcesOutputDirectory.get().getAsFile().toPath(),
					this.resourcesOutputDirectory.get().getAsFile().toPath(),
					classpathElements, resourcesElements);
		}
		catch (IOException exc) {
			throw new TaskExecutionException(this, exc);
		}
	}

}

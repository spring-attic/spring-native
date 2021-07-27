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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.process.CommandLineArgumentProvider;

import org.springframework.aot.BootstrapCodeGenerator;
import org.springframework.aot.context.bootstrap.BootstrapCodeGeneratorRunner;
import org.springframework.aot.gradle.dsl.SpringAotExtension;
import org.springframework.util.StringUtils;

/**
 * {@link org.gradle.api.Task} that generates AOT sources using the {@link BootstrapCodeGenerator}.
 *
 * @author Brian Clozel
 * @author Andy Wilkinson
 */
public class GenerateAotSources extends JavaExec {

	private SourceDirectorySet resourceDirectories;
	
	private final DirectoryProperty mainSourceSetOutputDirectory;

	private final DirectoryProperty sourcesOutputDirectory;

	private final DirectoryProperty resourcesOutputDirectory;

	private final GenerateAotOptions aotOptions;

	public GenerateAotSources() {
		this.mainSourceSetOutputDirectory = getProject().getObjects().directoryProperty();
		this.sourcesOutputDirectory = getProject().getObjects().directoryProperty();
		this.resourcesOutputDirectory = getProject().getObjects().directoryProperty();
		this.aotOptions = new GenerateAotOptions(getProject().getExtensions().findByType(SpringAotExtension.class));
		setMain(BootstrapCodeGeneratorRunner.class.getCanonicalName());
		getArgumentProviders().add(new BootstrapGeneratorArgumentProvider());
	}

	@InputFiles
	public FileCollection getResourceDirectories() {
		return this.resourceDirectories;
	}

	public void setResourceInputDirectories(SourceDirectorySet resourceDirectories) {
		this.resourceDirectories = resourceDirectories;
	}

	@InputDirectory
	public DirectoryProperty getMainSourceSetOutputDirectory() {
		return this.mainSourceSetOutputDirectory;
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

	private class BootstrapGeneratorArgumentProvider implements CommandLineArgumentProvider {

		@Override
		public Iterable<String> asArguments() {
			List<String> arguments = new ArrayList<>();
			// path to generated sources
			arguments.add(GenerateAotSources.this.sourcesOutputDirectory.get().getAsFile().toPath().toString());
			// path to generated resources
			arguments.add(GenerateAotSources.this.resourcesOutputDirectory.get().getAsFile().toPath().toString());
			// application resources locations
			arguments.add(toPathArgument(GenerateAotSources.this.resourceDirectories.getSrcDirs()));
			// application classes locations
			arguments.add(GenerateAotSources.this.mainSourceSetOutputDirectory.get().getAsFile().toPath().toString());
			// application classpath
			arguments.add(toPathArgument(getClasspath().getFiles()));
			
			// main application class
			if (GenerateAotSources.this.aotOptions.getMainClass().isPresent()) {
				arguments.add(GenerateAotSources.this.aotOptions.getMainClass().get());
			}
			return arguments;
		}

		private String toPathArgument(Set<File> files) {
			List<String> paths = files.stream().map(File::toPath).map(Path::toString).collect(Collectors.toList());
			return StringUtils.collectionToDelimitedString(paths, File.pathSeparator);
		}
	}

}

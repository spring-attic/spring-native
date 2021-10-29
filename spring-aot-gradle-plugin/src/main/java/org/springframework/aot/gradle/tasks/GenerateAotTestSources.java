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
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.process.CommandLineArgumentProvider;

import org.springframework.aot.test.boot.GenerateTestBootstrapCommand;
import org.springframework.aot.test.context.bootstrap.generator.TestContextAotProcessor;
import org.springframework.util.StringUtils;

/**
 * {@link org.gradle.api.Task} that generates AOT test sources using {@link TestContextAotProcessor}.
 *
 * @author Brian Clozel
 */
public class GenerateAotTestSources extends JavaExec {

	private SourceDirectorySet resourceDirectories;

	private SourceSetOutput testSourceSetOutputDirectories;

	private final DirectoryProperty generatedSourcesOutputDirectory;

	private final DirectoryProperty generatedResourcesOutputDirectory;

	public GenerateAotTestSources() {
		this.generatedSourcesOutputDirectory = getProject().getObjects().directoryProperty();
		this.generatedResourcesOutputDirectory = getProject().getObjects().directoryProperty();
		getMainClass().set(GenerateTestBootstrapCommand.class.getCanonicalName());
		getArgumentProviders().add(new TestBootstrapGeneratorArgumentProvider());
	}

	@InputFiles
	public FileCollection getResourceDirectories() {
		return this.resourceDirectories;
	}

	public void setResourceInputDirectories(SourceDirectorySet resourceDirectories) {
		this.resourceDirectories = resourceDirectories;
	}

	@InputFiles
	public FileCollection getTestSourceSetOutputDirectories() {
		return this.testSourceSetOutputDirectories;
	}

	public void setTestSourceSetOutputDirectories(SourceSetOutput testSourceSetOutputDirectories) {
		this.testSourceSetOutputDirectories = testSourceSetOutputDirectories;
	}

	@OutputDirectory
	public DirectoryProperty getGeneratedSourcesOutputDirectory() {
		return this.generatedSourcesOutputDirectory;
	}

	@OutputDirectory
	public DirectoryProperty getGeneratedResourcesOutputDirectory() {
		return this.generatedResourcesOutputDirectory;
	}


	private class TestBootstrapGeneratorArgumentProvider implements CommandLineArgumentProvider {

		@Override
		public Iterable<String> asArguments() {
			List<String> arguments = new ArrayList<>();

			if (getLogLevel().equals("DEBUG")) {
				arguments.add("--debug");
			}
			arguments.add("--sources-out=" + GenerateAotTestSources.this.generatedSourcesOutputDirectory.get().getAsFile().toPath());
			arguments.add("--resources-out=" + GenerateAotTestSources.this.generatedResourcesOutputDirectory.get().getAsFile().toPath());
			arguments.add("--resources=" + toPathArgument(GenerateAotTestSources.this.resourceDirectories.getSrcDirs()));

			for (File directory : GenerateAotTestSources.this.testSourceSetOutputDirectories.getClassesDirs()) {
				arguments.add(directory.getAbsolutePath());
			}
			return arguments;
		}

		private String getLogLevel() {
			Logger rootLogger = Logging.getLogger(Logger.ROOT_LOGGER_NAME);
			if (rootLogger.isDebugEnabled()) {
				return "DEBUG";
			}
			else if (rootLogger.isInfoEnabled()) {
				return "INFO";
			}
			else if (rootLogger.isWarnEnabled()) {
				return "WARN";
			}
			else {
				return "ERROR";
			}
		}

		private String toPathArgument(Set<File> files) {
			List<String> paths = files.stream().map(File::toPath).map(Path::toString).collect(Collectors.toList());
			return StringUtils.collectionToDelimitedString(paths, File.pathSeparator);
		}

	}

}

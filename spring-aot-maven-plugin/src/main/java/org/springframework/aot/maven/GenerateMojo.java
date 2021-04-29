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

package org.springframework.aot.maven;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.springframework.aot.BootstrapCodeGenerator;

/**
 * @author Brian Clozel
 * @author Sebastien Deleuze
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresProject = true, threadSafe = true,
		requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
		requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateMojo extends AbstractBootstrapMojo {

	/**
	 * Location of generated source files created by Spring AOT to bootstrap the application.
	 */
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/spring-aot/")
	private File generatedSourcesDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Set<Path> resourceFolders = new HashSet<>();
		for (Resource r: project.getResources()) {
			// TODO respect includes/excludes
			resourceFolders.add(new File(r.getDirectory()).toPath());
		}
		recreateGeneratedSourcesFolder(this.generatedSourcesDirectory);
		Path sourcesPath = this.generatedSourcesDirectory.toPath().resolve(Paths.get("src", "main", "java"));
		Path resourcesPath = this.generatedSourcesDirectory.toPath().resolve(Paths.get("src", "main", "resources"));
		try {
			List<String> runtimeClasspathElements = project.getRuntimeClasspathElements();
			BootstrapCodeGenerator generator = new BootstrapCodeGenerator(getAotOptions());
			generator.generate(sourcesPath, resourcesPath, runtimeClasspathElements, resourceFolders);
			compileGeneratedSources(sourcesPath, runtimeClasspathElements);
			processGeneratedResources(resourcesPath, Paths.get(project.getBuild().getOutputDirectory()));
			this.buildContext.refresh(this.buildDir);
		}
		catch (Throwable exc) {
			logger.error(exc);
			logger.error(Arrays.toString(exc.getStackTrace()));
			throw new MojoFailureException("Build failed during Spring AOT code generation", exc);
		}
	}

}

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
@Mojo(name = "test-generate", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES, requiresProject = true, threadSafe = true,
		requiresDependencyResolution = ResolutionScope.TEST,
		requiresDependencyCollection = ResolutionScope.TEST)
public class TestGenerateMojo extends AbstractBootstrapMojo {

	/**
	 * The location of the generated bootstrap test sources.
	 */
	@Parameter(defaultValue = "${project.build.directory}/generated-test-sources/spring-aot/")
	private File outputDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Set<Path> resourceFolders = new HashSet<>();
		for (Resource r: project.getTestResources()) {
			// TODO respect includes/excludes
			resourceFolders.add(new File(r.getDirectory()).toPath());
		}
		Path sourcesPath = this.outputDirectory.toPath().resolve(Paths.get("src", "test", "java"));
		Path resourcesPath = this.outputDirectory.toPath().resolve(Paths.get("src", "test", "resources"));
		try {
			List<String> testClasspathElements = this.project.getTestClasspathElements();
			BootstrapCodeGenerator generator = new BootstrapCodeGenerator(getAotOptions());
			generator.generate(sourcesPath, resourcesPath, testClasspathElements, resourceFolders);
			compileGeneratedTestSources(sourcesPath, testClasspathElements);
			processGeneratedTestResources(resourcesPath, Paths.get(project.getBuild().getTestOutputDirectory()));
			this.buildContext.refresh(this.buildDir);
		}
		catch (Throwable exc) {
			logger.error(exc);
			logger.error(Arrays.toString(exc.getStackTrace()));
			throw new MojoFailureException("Build failed during Spring AOT test code generation", exc);
		}
	}

}

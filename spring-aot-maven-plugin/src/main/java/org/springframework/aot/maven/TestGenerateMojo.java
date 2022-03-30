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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.utils.cli.CommandLineUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import org.springframework.aot.test.build.GenerateTestBootstrapCommand;
import org.springframework.util.StringUtils;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

/**
 * @author Brian Clozel
 * @author Sebastien Deleuze
 */
@Mojo(name = "test-generate", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES, requiresProject = true, threadSafe = true,
		requiresDependencyResolution = ResolutionScope.TEST,
		requiresDependencyCollection = ResolutionScope.TEST)
public class TestGenerateMojo extends AbstractBootstrapMojo {

	/**
	 * Location of generated source files created by Spring AOT to bootstrap the test context.
	 */
	@Parameter(defaultValue = "${project.build.directory}/generated-runtime-test-sources/spring-aot/")
	private File generatedTestSourcesDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			// Remove existing spring.properties to not default to AOT execution if previously created
			Path springProperties = Path.of(project.getBuild().getTestOutputDirectory(), "spring.properties");
			if (springProperties.toFile().exists()) {
				Files.delete(springProperties);
			}

			if ("true".equals(System.getProperty("skipTests")) || "true".equals(System.getProperty("maven.test.skip"))) {
				getLog().info("Skip Spring AOT test generation since tests are skipped");
				return;
			}

			if ("false".equals(System.getProperty("springAot"))) {
				getLog().info("Skip Spring AOT test generation since disabled with -DspringAot=false");
				return;
			}

			if (this.project.getPackaging().equals("pom")) {
				getLog().debug("test-generate goal could not be applied to pom project.");
				return;
			}
			Path testOutputDirectory = Paths.get(project.getBuild().getTestOutputDirectory());
			if (Files.notExists(testOutputDirectory)) {
				getLog().info("Skip Spring AOT test generation since no test have been detected");
				return;
			}

			List<String> testClasspathElements = this.project.getTestClasspathElements()
					.stream()
					.filter(element -> !element.contains("spring-boot-devtools"))
					.collect(Collectors.toList());
			Files.createDirectories(this.generatedTestSourcesDirectory.toPath());

			Path sourcesPath = this.generatedTestSourcesDirectory.toPath().resolve(Paths.get("src", "test", "java"));
			Path resourcesPath = this.generatedTestSourcesDirectory.toPath().resolve(Paths.get("src", "test", "resources"));
			Files.createDirectories(sourcesPath);

			findJarFile(this.pluginArtifacts, "org.springframework.experimental", "spring-native-configuration")
					.ifPresent(artifact -> prependDependency(artifact, testClasspathElements));
			findJarFile(this.pluginArtifacts, "org.springframework.experimental", "spring-aot")
					.ifPresent(artifact -> prependDependency(artifact, testClasspathElements));
			findJarFile(this.pluginArtifacts, "org.springframework.experimental", "spring-aot-test")
					.ifPresent(artifact -> prependDependency(artifact, testClasspathElements));
			findJarFile(this.pluginArtifacts, "org.springframework.boot", "spring-boot-loader-tools")
					.ifPresent(artifact -> prependDependency(artifact, testClasspathElements));
			findJarFile(this.pluginArtifacts, "com.squareup", "javapoet")
					.ifPresent(artifact -> prependDependency(artifact, testClasspathElements));
			findJarFile(this.pluginArtifacts, "info.picocli", "picocli")
					.ifPresent(artifact -> prependDependency(artifact, testClasspathElements));
			findJarFile(this.pluginArtifacts, "net.bytebuddy", "byte-buddy")
					.ifPresent(artifact -> prependDependency(artifact, testClasspathElements));

			// consider main and test resources
			Set<Path> resourceFolders = new HashSet<>();
			for (Resource r : project.getResources()) {
				resourceFolders.add(new File(r.getDirectory()).toPath());
			}
			for (Resource r : project.getTestResources()) {
				resourceFolders.add(new File(r.getDirectory()).toPath());
			}

			List<String> args = new ArrayList<>();
			// remote debug
			if ("true".equals(this.debug)) {
				args.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000");
			}
			else {
				args.addAll(Arrays.asList(CommandLineUtils.translateCommandline(this.debug)));
			}
			args.add("-cp");
			args.add(asClasspathArgument(testClasspathElements));
			args.add(GenerateTestBootstrapCommand.class.getCanonicalName());
			args.add("--sources-out=" + sourcesPath.toAbsolutePath());
			args.add("--resources-out=" + resourcesPath.toAbsolutePath());
			args.add("--resources=" + StringUtils.collectionToDelimitedString(resourceFolders, File.pathSeparator));
			applyAotOptions(args);
			// test output directory
			args.add(testOutputDirectory.toString());

			forkJvm(Paths.get(this.project.getBuild().getDirectory()).toFile(), args, Collections.emptyMap());

			compileGeneratedTestSources(sourcesPath);
			processGeneratedTestResources(resourcesPath, Paths.get(project.getBuild().getTestOutputDirectory()));

			// Write system property as spring.properties file in test resources.
			Files.write(springProperties,
					Collections.singletonList("spring.test.context.default.CacheAwareContextLoaderDelegate=org.springframework.aot.test.AotCacheAwareContextLoaderDelegate"),
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			this.buildContext.refresh(this.buildDir);
		}
		catch (Throwable exc) {
			getLog().error(exc);
			getLog().error(Arrays.toString(exc.getStackTrace()));
			throw new MojoFailureException("Build failed during Spring AOT test code generation", exc);
		}
	}

	protected void compileGeneratedTestSources(Path sourcesPath) throws MojoExecutionException {
		String compilerVersion = this.project.getProperties().getProperty("maven-compiler-plugin.version", DEFAULT_COMPILER_PLUGIN_VERSION);
		project.addTestCompileSourceRoot(sourcesPath.toString());
		Xpp3Dom compilerConfig = configuration(
				element("compileSourceRoots", element("compileSourceRoot", sourcesPath.toString()))
		);
		executeMojo(
				plugin(groupId("org.apache.maven.plugins"), artifactId("maven-compiler-plugin"), version(compilerVersion)),
				goal("testCompile"), compilerConfig, executionEnvironment(this.project, this.session, this.pluginManager));
	}


	protected void processGeneratedTestResources(Path sourcePath, Path destinationPath) throws MojoExecutionException {
		String resourcesVersion = this.project.getProperties().getProperty("maven-resources-plugin.version", "3.2.0");
		Xpp3Dom resourceConfig = configuration(element("resources", element("resource", element("directory", sourcePath.toString()))),
				element("outputDirectory", destinationPath.toString()));
		Resource resource = new Resource();
		resource.setDirectory(sourcePath.toString());
		project.addTestResource(resource);
		executeMojo(
				plugin(groupId("org.apache.maven.plugins"), artifactId("maven-resources-plugin"), version(resourcesVersion)),
				goal("copy-resources"), resourceConfig, executionEnvironment(this.project, this.session, this.pluginManager));
	}

}

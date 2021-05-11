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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import org.springframework.nativex.AotOptions;
import org.springframework.util.FileSystemUtils;

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
 */
abstract class AbstractBootstrapMojo extends AbstractMojo {

	private static final String DEFAULT_COMPILER_PLUGIN_VERSION = "3.8.1";

	protected static Log logger = LogFactory.getLog(AbstractBootstrapMojo.class);

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	protected MavenProject project;

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	protected MavenSession session;

	@Component
	protected BuildContext buildContext;

	@Component
	protected BuildPluginManager pluginManager;

	@Parameter(defaultValue = "${project.build.directory}")
	protected File buildDir;

	@Parameter
	protected String mode;

	@Parameter
	private boolean debugVerify;

	@Parameter
	private boolean ignoreHintsOnExcludedConfig;

	@Parameter
	private boolean removeUnusedConfig = true;

	@Parameter
	private boolean verify = true;

	@Parameter
	private boolean removeYamlSupport;

	@Parameter
	private boolean removeJmxSupport = true;

	@Parameter
	private boolean removeXmlSupport = true;

	@Parameter
	private boolean removeSpelSupport;

	@Parameter
	private boolean buildTimePropertiesMatchIfMissing;

	@Parameter
	private String[] buildTimePropertiesChecks;

	@Parameter
	private boolean failOnMissingSelectorHint;


	protected AotOptions getAotOptions() {
		AotOptions aotOptions = new AotOptions();
		aotOptions.setMode(mode);
		aotOptions.setDebugVerify(debugVerify);
		aotOptions.setIgnoreHintsOnExcludedConfig(ignoreHintsOnExcludedConfig);
		aotOptions.setRemoveUnusedConfig(removeUnusedConfig);
		aotOptions.setVerify(verify);
		aotOptions.setRemoveYamlSupport(removeYamlSupport);
		aotOptions.setRemoveJmxSupport(removeJmxSupport);
		aotOptions.setRemoveXmlSupport(removeXmlSupport);
		aotOptions.setRemoveSpelSupport(removeSpelSupport);
		aotOptions.setBuildTimePropertiesMatchIfMissing(buildTimePropertiesMatchIfMissing);
		aotOptions.setBuildTimePropertiesChecks(buildTimePropertiesChecks);
		aotOptions.setFailOnMissingSelectorHint(failOnMissingSelectorHint);
		return aotOptions;
	}

	protected void recreateGeneratedSourcesFolder(File generatedSourcesFolder) throws MojoFailureException {
		try {
			FileSystemUtils.deleteRecursively(generatedSourcesFolder);
			Files.createDirectories(generatedSourcesFolder.toPath());
		}
		catch (IOException exc) {
			throw new MojoFailureException("Build failed during Spring AOT code generation", exc);
		}
	}

	protected void compileGeneratedSources(Path sourcesPath, List<String> runtimeClasspathElements) throws MojoExecutionException {
		String compilerVersion = this.project.getProperties().getProperty("maven-compiler-plugin.version", DEFAULT_COMPILER_PLUGIN_VERSION);
		project.addCompileSourceRoot(sourcesPath.toString());
		Xpp3Dom compilerConfig = configuration(
				element("compileSourceRoots", element("compileSourceRoot", sourcesPath.toString())),
				element("compilePath", runtimeClasspathElements.stream()
						.map(classpathElement -> element("compilePath", classpathElement)).toArray(MojoExecutor.Element[]::new))
		);
		executeMojo(
				plugin(groupId("org.apache.maven.plugins"), artifactId("maven-compiler-plugin"), version(compilerVersion)),
				goal("compile"), compilerConfig, executionEnvironment(this.project, this.session, this.pluginManager));
	}

	protected void compileGeneratedTestSources(Path sourcesPath, List<String> testClasspathElements) throws MojoExecutionException {
		String compilerVersion = this.project.getProperties().getProperty("maven-compiler-plugin.version", DEFAULT_COMPILER_PLUGIN_VERSION);
		project.addTestCompileSourceRoot(sourcesPath.toString());
		Xpp3Dom compilerConfig = configuration(
				element("compileSourceRoots", element("compileSourceRoot", sourcesPath.toString())),
				element("compilePath", testClasspathElements.stream()
						.map(classpathElement -> element("compilePath", classpathElement)).toArray(MojoExecutor.Element[]::new))
		);
		executeMojo(
				plugin(groupId("org.apache.maven.plugins"), artifactId("maven-compiler-plugin"), version(compilerVersion)),
				goal("testCompile"), compilerConfig, executionEnvironment(this.project, this.session, this.pluginManager));
	}

	protected void processGeneratedResources(Path sourcePath, Path destinationPath) throws MojoExecutionException {
		String resourcesVersion = this.project.getProperties().getProperty("maven-resources-plugin.version", "3.2.0");
		Xpp3Dom resourceConfig = configuration(element("resources", element("resource", element("directory", sourcePath.toString()))),
				element("outputDirectory", destinationPath.toString()));
		Resource resource = new Resource();
		resource.setDirectory(sourcePath.toString());
		project.addResource(resource);
		executeMojo(
				plugin(groupId("org.apache.maven.plugins"), artifactId("maven-resources-plugin"), version(resourcesVersion)),
				goal("copy-resources"), resourceConfig, executionEnvironment(this.project, this.session, this.pluginManager));
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

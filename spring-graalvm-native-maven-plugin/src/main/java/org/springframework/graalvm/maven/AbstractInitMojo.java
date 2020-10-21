/*
 * Copyright 2020-2020 the original author or authors.
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
package org.springframework.graalvm.maven;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.squareup.javapoet.ClassName;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.util.Scanner;
import org.json.JSONObject;
import org.sonatype.plexus.build.incremental.BuildContext;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.loader.tools.MainClassFinder;
import org.springframework.graalvm.buildtools.CodeGeneratorApplication;
import org.springframework.util.ClassUtils;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

/**
 * @author dsyer
 *
 */
public abstract class AbstractInitMojo extends AbstractMojo {

	private static final String SPRING_BOOT_APPLICATION_CLASS_NAME = "org.springframework.boot.SpringBootConfiguration";

	private static final String TOOLS_APPLICATION_CLASS_NAME = "org.springframework.graalvm.buildtools.CodeGeneratorApplication";

	/**
	 * The Maven project.
	 * 
	 * @since 1.0.0
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	@Component
	private BuildContext buildContext;

	@Component
	private BuildPluginManager pluginManager;

	/**
	 * Skip the execution.
	 * 
	 * @since 1.3.2
	 */
	@Parameter(property = "spring-graalvm.generate.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * The name of the main class. If not specified the first compiled class found that
	 * contains a 'main' method will be used.
	 * 
	 * @since 1.0.0
	 */
	@Parameter(property = "spring-graalvm.generate.main-class")
	private String mainClass;

	/**
	 * The name of the main class. If not specified the first compiled class found that
	 * contains a 'main' method will be used.
	 * 
	 * @since 1.0.0
	 */
	@Parameter(property = "spring-graalvm.generate.base-package")
	private String basePackage;

	public AbstractInitMojo() {
		super();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (this.skip) {
			getLog().debug("skipping run as per configuration.");
			return;
		}
		preProcess(project);
		Scanner scanner = buildContext.newScanner(project.getBasedir());
		scanner.setIncludes(new String[] { "src/main/java/" });
		scanner.scan();
		// TODO: better way to tell if we are scanning a jar not src/main/java
		if (scanner.getIncludedFiles().length > 0 || this.basePackage != null || !new File("src/main/java").exists()) {
			String compilerVersion = project.getProperties().getProperty("maven-compiler-plugin.version", "3.8.1");
			executeMojo(
					plugin(groupId("org.apache.maven.plugins"), artifactId("maven-compiler-plugin"),
							version(compilerVersion)),
					goal("compile"), configuration(), executionEnvironment(project, session, pluginManager));
			generate(getStart());
		}
		postProcess(project);
	}

	protected abstract void postProcess(MavenProject project);

	protected abstract void preProcess(MavenProject project);

	private void generate(String start) throws MojoExecutionException {
		ClassLoader realm = getClass().getClassLoader();
		ClassLoader parent = realm instanceof ClassRealm ? ((ClassRealm) realm).getParentClassLoader()
				: realm.getParent();
		URLClassLoader loader = new URLClassLoader(getClassPathUrls(), parent);
		ClassLoader original = null;
		getLog().info("Project: " + project);
		try {
			original = ClassUtils.overrideThreadContextClassLoader(loader);
			Class<?> type = loader.loadClass(TOOLS_APPLICATION_CLASS_NAME);
			getLog().info("Generating: " + start + " in: " + getOutputDirectory());
			type.getMethod("main", String[].class).invoke(null,
					new Object[] { new String[] { start, getOutputDirectory().getAbsolutePath() } });
			buildContext.refresh(getOutputDirectory());
		}
		catch (Exception e) {
			throw new MojoExecutionException("Cannot generate initializer class: " + TOOLS_APPLICATION_CLASS_NAME, e);
		}
		finally {
			if (original != null) {
				ClassUtils.overrideThreadContextClassLoader(original);
			}
		}
	}

	protected abstract File getOutputDirectory();

	private String getStart() throws MojoExecutionException {
		String mainClass = this.mainClass;
		if (mainClass == null) {
			if (this.basePackage == null) {
				try {
					mainClass = MainClassFinder.findSingleMainClass(getMainClassesDirectory(),
							SPRING_BOOT_APPLICATION_CLASS_NAME);
				}
				catch (IOException ex) {
					throw new MojoExecutionException(ex.getMessage(), ex);
				}
			}
			else {
				return this.basePackage;
			}
		}
		if (mainClass == null) {
			throw new MojoExecutionException("Unable to find a suitable main class, please add a 'mainClass' property");
		}
		return mainClass.substring(0, mainClass.lastIndexOf("."));
	}

	protected abstract File getMainClassesDirectory();

	protected URL[] getClassPathUrls() throws MojoExecutionException {
		try {
			List<URL> urls = new ArrayList<>();
			addProjectClasses(urls);
			addDependencies(urls);
			addTools(urls);
			getLog().debug("Classpath: " + urls);
			return urls.toArray(new URL[0]);
		}
		catch (IOException ex) {
			throw new MojoExecutionException("Unable to build classpath", ex);
		}
	}

	private void addTools(List<URL> urls) {
		if (!findJar(urls, "spring-graalvm-buildtools")) {
			urls.add(CodeGeneratorApplication.class.getProtectionDomain().getCodeSource().getLocation());
		}
		if (!findJar(urls, "javapoet")) {
			urls.add(ClassName.class.getProtectionDomain().getCodeSource().getLocation());
		}
		if (!findJar(urls, "spring-boot-configuration-metadata")) {
			urls.add(ConfigurationMetadataRepository.class.getProtectionDomain().getCodeSource().getLocation());
		}
		if (!findJar(urls, "android-json")) {
			urls.add(JSONObject.class.getProtectionDomain().getCodeSource().getLocation());
		}
	}

	private boolean findJar(List<URL> urls, String string) {
		for (URL url : urls) {
			if (url.toString().contains(string)) {
				return true;
			}
		}
		return false;
	}

	private void addProjectClasses(List<URL> urls) {
		urls.addAll(this.getClassesDirectories().stream().map(file -> {
			try {
				return file.toURI().toURL();
			}
			catch (MalformedURLException e) {
				throw new IllegalStateException(e);
			}
		}).collect(Collectors.toList()));
	}

	protected abstract List<File> getClassesDirectories();

	private void addDependencies(List<URL> urls) throws MalformedURLException, MojoExecutionException {
		Set<Artifact> artifacts = this.project.getArtifacts();
		for (Artifact artifact : artifacts) {
			if (artifact.getFile() != null) {
				urls.add(artifact.getFile().toURI().toURL());
			}
		}
	}

}

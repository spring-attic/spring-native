package org.springframework.nativex.maven;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.plexus.build.incremental.BuildContext;

import org.springframework.nativex.buildtools.BootstrapCodeGenerator;

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

	/**
	 * The location of the generated bootstrap sources.
	 */
	@Parameter(defaultValue = "${project.build.directory}/spring-bootstrap/")
	protected File outputDirectory;


	protected void compileGeneratedSources(Path generatedRootFolder) throws MojoExecutionException {
		String compilerVersion = this.project.getProperties().getProperty("maven-compiler-plugin.version", DEFAULT_COMPILER_PLUGIN_VERSION);
		Path sourcePath = generatedRootFolder.resolve(Paths.get("src", "main", "java"));
		Xpp3Dom compilerConfig = configuration(element("compileSourceRoots", element("compileSourceRoot", sourcePath.toString())));
		executeMojo(
				plugin(groupId("org.apache.maven.plugins"), artifactId("maven-compiler-plugin"), version(compilerVersion)),
				goal("compile"), compilerConfig, executionEnvironment(this.project, this.session, this.pluginManager));
	}

	protected void compileGeneratedTestSources(Path generatedRootFolder) throws MojoExecutionException {
		String compilerVersion = this.project.getProperties().getProperty("maven-compiler-plugin.version", DEFAULT_COMPILER_PLUGIN_VERSION);
		Path sourcePath = generatedRootFolder.resolve(Paths.get("src", "main", "java"));
		Xpp3Dom compilerConfig = configuration(element("compileSourceRoots", element("compileSourceRoot", sourcePath.toString())));
		executeMojo(
				plugin(groupId("org.apache.maven.plugins"), artifactId("maven-compiler-plugin"), version(compilerVersion)),
				goal("testCompile"), compilerConfig, executionEnvironment(this.project, this.session, this.pluginManager));
	}

	protected void processGeneratedResources(Path sourcePath, Path destinationPath) throws MojoExecutionException {
		String resourcesVersion = this.project.getProperties().getProperty("maven-resources-plugin.version", "3.2.0");
		Xpp3Dom resourceConfig = configuration(element("resources", element("resource", element("directory", sourcePath.toString()))),
				element("outputDirectory", destinationPath.toString()));
		executeMojo(
				plugin(groupId("org.apache.maven.plugins"), artifactId("maven-resources-plugin"), version(resourcesVersion)),
				goal("copy-resources"), resourceConfig, executionEnvironment(this.project, this.session, this.pluginManager));
	}

	  /*

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Set<Path> resourceFolders = new HashSet<>();
		for (Resource r: project.getResources()) {
			// TODO respect includes/excludes
			resourceFolders.add(new File(r.getDirectory()).toPath());
		}
		try {
			BootstrapCodeGenerator generator = new BootstrapCodeGenerator();
			generator.generate(Paths.get(this.outputDirectory.toURI()), this.project.getRuntimeClasspathElements(), resourceFolders);
		}
		catch (Throwable exc) {
			logger.error(exc);
			logger.error(Arrays.toString(exc.getStackTrace()));
			throw new MojoExecutionException("Could not generate source files", exc);
		}
		String compilerVersion = this.project.getProperties().getProperty("maven-compiler-plugin.version", "3.8.1");
		Path sourcePath = this.outputDirectory.toPath().resolve(Paths.get("src", "main", "java"));
		Xpp3Dom compilerConfig = configuration(element("compileSourceRoots", element("compileSourceRoot", sourcePath.toString())));
		executeMojo(
				plugin(groupId("org.apache.maven.plugins"), artifactId("maven-compiler-plugin"),
						version(compilerVersion)),
				goal("compile"), compilerConfig, executionEnvironment(this.project, this.session, this.pluginManager));

		String resourcesVersion = this.project.getProperties().getProperty("maven-resources-plugin.version", "3.2.0");
		Path resourcePath = this.outputDirectory.toPath().resolve(Paths.get("src", "main", "resources"));
		Xpp3Dom resourceConfig = configuration(element("resources", element("resource", element("directory", resourcePath.toString()))),
				element("outputDirectory", project.getBuild().getOutputDirectory()));
		executeMojo(
				plugin(groupId("org.apache.maven.plugins"), artifactId("maven-resources-plugin"),
						version(resourcesVersion)),
				goal("copy-resources"), resourceConfig, executionEnvironment(this.project, this.session, this.pluginManager));
		this.buildContext.refresh(this.outputDirectory);
	}
	   */

}

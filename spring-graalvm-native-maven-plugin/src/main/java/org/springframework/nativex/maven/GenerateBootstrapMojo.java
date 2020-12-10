package org.springframework.nativex.maven;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.execution.MavenSession;
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
@Mojo(name = "bootstrap", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresProject = true, threadSafe = true,
		requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
		requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateBootstrapMojo extends AbstractMojo {

	private static Log logger = LogFactory.getLog(GenerateBootstrapMojo.class);

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	@Component
	private BuildContext buildContext;

	@Component
	private BuildPluginManager pluginManager;

	/**
	 * The location of the generated bootstrap sources.
	 */
	@Parameter(defaultValue = "${project.build.directory}/spring-bootstrap/")
	private File outputDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			BootstrapCodeGenerator generator = new BootstrapCodeGenerator();
			generator.generate(Paths.get(this.outputDirectory.toURI()), this.project.getRuntimeClasspathElements());
		}
		catch (Throwable exc) {
			logger.error(exc);
			logger.error(Arrays.toString(exc.getStackTrace()));
			throw new MojoExecutionException("Could not generate source files", exc);
		}
		String compilerVersion = this.project.getProperties().getProperty("maven-compiler-plugin.version", "3.8.1");
		Path sourcePath = this.outputDirectory.toPath().resolve(Paths.get("src", "main", "java"));
		Xpp3Dom configuration = configuration(element("compileSourceRoots", element("compileSourceRoot", sourcePath.toString())));
		executeMojo(
				plugin(groupId("org.apache.maven.plugins"), artifactId("maven-compiler-plugin"),
						version(compilerVersion)),
				goal("compile"), configuration, executionEnvironment(this.project, this.session, this.pluginManager));
		this.buildContext.refresh(this.outputDirectory);
	}

}

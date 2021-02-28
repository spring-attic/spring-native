package org.springframework.aot.maven;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
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
	 * The location of the generated bootstrap sources.
	 */
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/spring-aot/")
	private File outputDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Set<Path> resourceFolders = new HashSet<>();
		for (Resource r: project.getResources()) {
			// TODO respect includes/excludes
			resourceFolders.add(new File(r.getDirectory()).toPath());
		}
		try {
			BootstrapCodeGenerator generator = new BootstrapCodeGenerator(getAotOptions());
			generator.generate(Paths.get(this.outputDirectory.toURI()), this.project.getRuntimeClasspathElements(), resourceFolders);
		}
		catch (Throwable exc) {
			logger.error(exc);
			logger.error(Arrays.toString(exc.getStackTrace()));
			throw new MojoFailureException("Build failed during Spring AOT code generation", exc);
		}

		compileGeneratedSources(this.outputDirectory.toPath());

		Path resourcePath = this.outputDirectory.toPath().resolve(Paths.get("src", "main", "resources"));
		processGeneratedResources(resourcePath, Paths.get(project.getBuild().getOutputDirectory()));

		this.buildContext.refresh(this.buildDir);
	}

}

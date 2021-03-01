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
			BootstrapCodeGenerator generator = new BootstrapCodeGenerator(getAotOptions());
			generator.generate(sourcesPath, resourcesPath, this.project.getTestClasspathElements(), resourceFolders);
		}
		catch (Throwable exc) {
			logger.error(exc);
			logger.error(Arrays.toString(exc.getStackTrace()));
			throw new MojoFailureException("Build failed during Spring AOT test code generation", exc);
		}

		compileGeneratedTestSources(sourcesPath);
		processGeneratedTestResources(resourcesPath, Paths.get(project.getBuild().getOutputDirectory()));

		this.buildContext.refresh(this.buildDir);
	}

}

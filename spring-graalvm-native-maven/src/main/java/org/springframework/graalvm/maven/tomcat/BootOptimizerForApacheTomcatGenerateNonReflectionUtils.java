package org.springframework.graalvm.maven.tomcat;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Generates org.apache.tomcat.util.XReflectionIntrospectionUtils class that does setProperty
 * invocations for Apache Tomcat without reflection
 */
@Mojo(
    name = "spring-graalvm-remove-apache-tomcat-reflection",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class BootOptimizerForApacheTomcatGenerateNonReflectionUtils
    extends AbstractMojo {

    @Parameter(defaultValue = "true", required = false)
    private boolean enabled = true;
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The Maven project.
     */
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/tomcat", required = false)
    private File generatedSourcesLocation;

    /**
     * Maven project helper utils.
     */
    @Component
    protected MavenProjectHelper projectHelper;

	@Component
	private BuildContext buildContext;

    private ClassLoader projectClassLoader = null;

    public BootOptimizerForApacheTomcatGenerateNonReflectionUtils() {
    }

    public void execute()
        throws MojoExecutionException {

        try {

            if (isTomcatPresent()) {

                if (buildContext.hasDelta(generatedSourcesLocation)) {
                    
                    if (!generatedSourcesLocation.exists()) {
                        generatedSourcesLocation.mkdirs();
                    }
                    getLog().info("Generating Non Reflection Code for Apache Tomcat to: " +
                    generatedSourcesLocation.getAbsolutePath());
                    generateXReflectionSources(generatedSourcesLocation.getAbsolutePath());
                    project.addCompileSourceRoot(generatedSourcesLocation.getAbsolutePath());

                }

            } else {
                getLog().info("No Apache Tomcat Library Present. Skipping Apache Tomcat optimization");
            }
        } catch (RuntimeException e) {
            getLog().error(e);
            throw e;
        }
    }

    private void generateXReflectionSources(String destinationDirectory) {
        String[] args = {destinationDirectory};
        ClassLoader loader = getProjectClassLoader();
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            Class clazz = Class.forName("org.springframework.graalvm.maven.tomcat.ObjectReflectionPropertyInspector");
            Method main = clazz.getDeclaredMethod("main", String[].class);
            main.invoke(null, (Object) args);
        } catch (ClassNotFoundException | InvocationTargetException |NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    private boolean isTomcatPresent() {
        boolean present;
        try {
            ClassLoader classLoader = getProjectClassLoader();
            Class.forName("org.apache.coyote.http11.Http11NioProtocol", false, classLoader);
            present = true;
        } catch (Exception e) {
            getLog().error(e);
            present = false;
        }
        return present;
    }

    private ClassLoader getProjectClassLoader() {
        if (projectClassLoader == null) {
            List<URL> classpathElements = new LinkedList<>();
            try {
                for(String mavenCompilePath: project.getCompileClasspathElements()) {
                    classpathElements.add(new File(mavenCompilePath).toURI().toURL());
                }
            } catch (DependencyResolutionRequiredException | MalformedURLException e) {
                throw new RuntimeException(e);
            }
            URL[] classpath = classpathElements.toArray(new URL[classpathElements.size()]);
            projectClassLoader = new URLClassLoader(classpath, Thread.currentThread().getContextClassLoader());
        }

        return projectClassLoader;
    }


}

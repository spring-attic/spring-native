package org.springframework.graalvm.maven;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "spring-graalvm-optimize-jar", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class BootOptimizerForApacheTomcat
    extends AbstractMojo {

    @Parameter(defaultValue = "true", required = false)
    private boolean enabled = true;
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    /**
     * Name of the generated archive.
     */
    @Parameter(defaultValue = "${project.build.finalName}", readonly = true)
    private String finalName;

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /**
     * Maven project helper utils.
     */
    @Component
    protected MavenProjectHelper projectHelper;

    private Map<String, String> fileSystemProps = new HashMap<>();

    public BootOptimizerForApacheTomcat() {
        fileSystemProps.put("update", "true");
        fileSystemProps.put("create", "false");
    }

    public void execute()
        throws MojoExecutionException {
        File f = outputDirectory;

        if (!f.exists()) {
            f.mkdirs();
        }

        File logFile = new File(f, "spring-graalvm-optimize-jar.log");
        File bootJar = new File(this.outputDirectory, this.finalName+".jar");

        Writer writer = null;
        try {
            final FileWriter w = new FileWriter(logFile, false);
            writer = w;

            if (!enabled) {
                w.write("Skipping Boot Library Optimization: "+this.finalName+".jar\n");
                return;
            }

            w.write("Processing Boot library: "+this.finalName+".jar\n");

            URI uri = new URI("jar:file:"+bootJar.getAbsolutePath());
            try (FileSystem zipfs = FileSystems.newFileSystem(uri, fileSystemProps)) {
                final AtomicReference<Path> foundTomcatLibrary = new AtomicReference<>();
                Files.walkFileTree(zipfs.getPath("/"), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                        String name =file.getFileName().toString();
                        w.write("Checking file: "+name+" with path: "+file+"\n");
                        if (name.startsWith("tomcat-embed-core") || name.startsWith("tomcat-embed-programmatic")) {
                            w.write("Removing from JAR: "+file.getFileName().toString()+"\n");
                            Files.delete(file);
                            foundTomcatLibrary.set(file);
                            return FileVisitResult.TERMINATE;
                        } else {
                            return FileVisitResult.CONTINUE;
                        }
                    }
                });
                Path destination = foundTomcatLibrary.get();
                if (destination != null) {
                    String replacementFileName = "tomcat-embed-programmatic-9.0.38-dev.jar";
                    String name = destination.toString();
                    w.write("Replacing '" + name + "' with '" + replacementFileName+"'\n");
                    destination = zipfs.getPath(name.replaceFirst("tomcat-embed.*\\.jar", replacementFileName));
                    //download a replacement JAR
                    URL replacementLibraryUrl = new URL("https://drive.google.com/u/0/uc?id=1tKjLkn_dVFdEejsL7IKkg6ljeTB-hDe-&export=download");
                    w.write("Downloading new JAR: "+replacementLibraryUrl.toString()+"\n");

                    Path replacementJar = Paths.get(new File(this.outputDirectory, replacementFileName).getAbsolutePath());
                    Files.copy(replacementLibraryUrl.openStream(), replacementJar, StandardCopyOption.REPLACE_EXISTING);
                    w.write("Updating Boot Jar With: "+replacementJar.toString()+"\n");
                    //update the Spring Boot JAR with the newly downloaded replacement
                    Files.copy(replacementJar, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (URISyntaxException e) {
            throw new MojoExecutionException("Error optimizing file: " + bootJar.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new MojoExecutionException("Error optimizing file: " + bootJar.getAbsolutePath(), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private void processTomcatOptimizations(File tempFilePath) throws MojoExecutionException {
        try {
            URI uri = new URI("jar:file:"+tempFilePath.getAbsolutePath());
            try (FileSystem zipfs = FileSystems.newFileSystem(uri, fileSystemProps)) {
                Path path = zipfs.getPath("/META-INF/native-image/org.apache.tomcat.embed/tomcat-embed-programmatic/native-image.properties");
                Files.delete(path);
                System.err.println("FILIP EX: File deleted: "+path.toString());
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to delete file in ZIP", e);
            }
        } catch (URISyntaxException e) {
            throw new MojoExecutionException("Unable to optimize JAR file: "+tempFilePath.getAbsolutePath());
        }
    }
}

package org.springframework.graalvm.maven.tomcat;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
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
 * Goal which touches a timestamp file.
 */
@Mojo(name = "spring-graalvm-optimize-apache-tomcat", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class BootOptimizerForApacheTomcatEmbedCore extends AbstractMojo {

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

    @Component
    private BuildContext buildContext;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Component
    private BuildPluginManager pluginManager;

    private Map<String, String> fileSystemProps = new HashMap<>();

    public BootOptimizerForApacheTomcatEmbedCore() {
        fileSystemProps.put("update", "true");
        fileSystemProps.put("create", "false");
    }

    public void execute() throws MojoExecutionException {
        final File f = outputDirectory;

        if (!f.exists()) {
            f.mkdirs();
        }

        File bootJar = new File(this.outputDirectory, this.finalName + ".jar");

        try {

            if (!enabled) {
                getLog().info("Skipping Boot Library Optimization: " + this.finalName + ".jar");
                return;
            }

            URI uri = new URI("jar:file:" + bootJar.getAbsolutePath());
            try (FileSystem zipfs = FileSystems.newFileSystem(uri, fileSystemProps)) {

                if (!bootJar.exists() || !Files.exists(zipfs.getPath("BOOT-INF/lib"))) {
                    getLog().info("Creating Boot executable: " + this.finalName + ".jar");
                    project.getProperties().setProperty("spring-boot.repackage.skip", "false");
                    String bootVersion = project.getProperties().getProperty("spring-boot.version", "2.4.0-SNAPSHOT");
                    executeMojo(
                            plugin(groupId("org.springframework.boot"), artifactId("spring-boot-maven-plugin"),
                                    version(bootVersion)),
                            goal("repackage"), configuration(), executionEnvironment(project, session, pluginManager));

                }
                project.getProperties().setProperty("spring-boot.repackage.skip", "true");

            } catch (IOException e) {
                throw new MojoExecutionException("Error optimizing file: " + bootJar.getAbsolutePath(), e);
            }
            getLog().info("Processing Boot executable: " + this.finalName + ".jar");

            try (FileSystem zipfs = FileSystems.newFileSystem(uri, fileSystemProps)) {
                AtomicReference<Path> foundTomcatLibrary = new AtomicReference<>();
                AtomicReference<Path> newTomcatLibrary = new AtomicReference<>();
                Files.walkFileTree(zipfs.getPath("/"), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String name = file.getFileName().toString();
                        if (name.startsWith("tomcat-embed-core")) {
                            String modifiedName = name.replace("embed-core", "embed-core-optimized");
                            getLog().info("Removing from JAR: " + name + " to: " + modifiedName);
                            Path path = Paths.get(new File(f, modifiedName).getAbsolutePath());
                            Files.deleteIfExists(path);
                            Files.move(file, path);
                            foundTomcatLibrary.set(file);
                            newTomcatLibrary.set(path);
                            return FileVisitResult.CONTINUE;
                        } else if (name.startsWith("classpath.idx")) {
                            getLog().info("Updating classpath.idx");
                            String idx = new String(Files.readAllBytes(file));
                            idx = idx.replaceAll("embed-core-([0-9])", "embed-core-optimized-$1");
                            Files.write(file, idx.getBytes());
                            return FileVisitResult.CONTINUE;
                            
                        } else {
                            return FileVisitResult.CONTINUE;
                        }
                    }
                });
                Path destination = foundTomcatLibrary.get();
                if (destination != null) {
                    String replacementFileName = newTomcatLibrary.get().getFileName().toString();
                    String name = destination.toString();
                    getLog().info("Replacing '" + name + "' with '" + replacementFileName + "'");
                    destination = zipfs.getPath(name.replaceFirst("tomcat-embed.*\\.jar", replacementFileName));
                    Path replacementJar = Paths
                            .get(new File(this.outputDirectory, replacementFileName).getAbsolutePath());
                    processTomcatEmbedCoreOptimizations(replacementJar);
                    getLog().info("Updating Boot Jar With: " + replacementJar.toString());
                    // update the Spring Boot JAR with the newly downloaded replacement
                    Files.copy(replacementJar, destination, StandardCopyOption.REPLACE_EXISTING);
                    buildContext.refresh(bootJar);
                    Files.delete(replacementJar);
                    buildContext.refresh(replacementJar.toFile());
                }
            }
        } catch (URISyntaxException e) {
            throw new MojoExecutionException("Error optimizing file: " + bootJar.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new MojoExecutionException("Error optimizing file: " + bootJar.getAbsolutePath(), e);
        }
    }

    private void processTomcatEmbedCoreOptimizations(Path tempFilePath) throws MojoExecutionException {
        FileOptimizationMatcher matcher = new FileOptimizationMatcher();
        try (FileSystem zipfs = FileSystems.newFileSystem(tempFilePath, null)) {
            // walk the file tree
            Files.walkFileTree(zipfs.getPath("/"), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String name = file.toAbsolutePath().toString().substring(1);
                    if (matcher.match(name) == MatcherResult.DELETE) {
                        getLog().debug("Optimizing/removing in tomcat-embed-core: " + name);
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            // replace the graalvm reflection/resource files
            Properties replaceTheseFiles = new Properties();
            replaceTheseFiles.load(getClass()
                    .getResourceAsStream("/org/apache/tomcat/embed/tomcat-embed-core/default-replacements.properties"));
            for (String file : replaceTheseFiles.stringPropertyNames()) {
                getLog().info("Optimizing: " + file + " in: " + tempFilePath.getFileName().toString());
                Path path = zipfs.getPath("/" + file);
                Files.copy(
                        getClass().getResourceAsStream(
                                "/org/apache/tomcat/embed/tomcat-embed-core/" + replaceTheseFiles.getProperty(file)),
                        path, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new MojoExecutionException("Unable to optimize file: " + tempFilePath.toString(), e);
        }
    }

    private class FileOptimizationMatcher {
        private List<MatcherPattern> matchers = new LinkedList<>();
        private final String resource = "/org/apache/tomcat/embed/tomcat-embed-core/default-files.txt";

        public FileOptimizationMatcher() throws MojoExecutionException {
            loadMatchersFromStream(getClass().getResourceAsStream(resource));
        }

        public MatcherResult match(String path) {
            boolean keep = true;
            boolean foundOneMatch = false;
            for (MatcherPattern pattern : matchers) {
                if (pattern.matches(path)) {
                    keep = pattern.isInclude();
                    foundOneMatch = true;
                }
                if (!keep) {
                    break;
                }
            }
            return keep && foundOneMatch ? MatcherResult.KEEP : MatcherResult.DELETE;
        }

        private void loadMatchersFromStream(InputStream is) throws MojoExecutionException {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    try {
                        matchers.add(new MatcherPattern(line));
                    } catch (IllegalArgumentException e) {
                        // invalid pattern
                    }
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to load resource: " + resource, e);
            }
        }
    }

    private enum MatcherResult {
        DELETE, KEEP
    }

    private class MatcherPattern {
        private boolean include;
        private String pattern;
        private AntPathMatcher matcher;

        public MatcherPattern(String line) {
            if (line.startsWith("files.include=") || line.startsWith("files.exclude=")) {
                if (line.startsWith("files.include=")) {
                    this.include = true;
                } else if (line.startsWith("files.exclude=")) {
                    this.include = false;
                }
                this.pattern = line.substring(line.indexOf("=") + 1);
            } else {
                throw new IllegalArgumentException("Invalid pattern line: " + line);
            }
            this.matcher = new AntPathMatcher("/");
        }

        public boolean isInclude() {
            return include;
        }

        public boolean matches(String filePath) {
            return this.matcher.match(this.pattern, filePath);
        }
    }

}

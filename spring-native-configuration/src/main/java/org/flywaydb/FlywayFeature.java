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

package org.flywaydb;

import com.oracle.svm.core.annotate.AutomaticFeature;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.graalvm.nativeimage.hosted.Feature;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A GraalVM feature that calculates the migration to apply at compile time.
 * <p>
 * Forked from micronaut:
 * https://github.com/micronaut-projects/micronaut-flyway/blob/758da7d56280f5b923b2e09a1849e82974679e64/flyway/src/main/java/io/micronaut/flyway/graalvm/FlywayFeature.java
 * <p>
 * Which was forked from Quarkus:
 * https://github.com/quarkusio/quarkus/blob/7a5efed2a97d88656484b431b472210e2bb7d2f3/extensions/flyway/deployment/src/main/java/io/quarkus/flyway/FlywayProcessor.java
 */
@AutomaticFeature
public final class FlywayFeature implements Feature {

		private static final Log LOG = LogFactory.getLog(FlywayFeature.class);

		private static final String CLASSPATH_APPLICATION_MIGRATIONS_PROTOCOL = "classpath";
		private static final String JAR_APPLICATION_MIGRATIONS_PROTOCOL = "jar";
		private static final String FILE_APPLICATION_MIGRATIONS_PROTOCOL = "file";

		private static final String FLYWAY_LOCATIONS = "flyway.locations";
		private static final String DEFAULT_FLYWAY_LOCATIONS = "classpath:db/migration";

		@Override
		public void beforeAnalysis(BeforeAnalysisAccess access) {
				List<String> locations = Stream
						.of(System.getProperty(FLYWAY_LOCATIONS, DEFAULT_FLYWAY_LOCATIONS).split(",")).collect(Collectors.toList());

				try {
						List<String> migrations = discoverApplicationMigrations(locations);
						NativePathLocationScanner.setApplicationMigrationFiles(migrations);
				} catch (IOException | URISyntaxException e) {
						LOG.error("There was an error discovering the Flyway migrations: " + e.getMessage());
				}
		}

		private List<String> discoverApplicationMigrations(List<String> locations) throws IOException, URISyntaxException {
				List<String> applicationMigrationResources = new ArrayList<>();
				// Locations can be a comma separated list
				for (String location : locations) {
						// Strip any 'classpath:' protocol prefixes because they are assumed
						// but not recognized by ClassLoader.getResources()
						if (location != null && location.startsWith(CLASSPATH_APPLICATION_MIGRATIONS_PROTOCOL + ':')) {
								location = location.substring(CLASSPATH_APPLICATION_MIGRATIONS_PROTOCOL.length() + 1);
						}
						Enumeration<URL> migrations = Thread.currentThread().getContextClassLoader().getResources(location);
						while (migrations.hasMoreElements()) {
								URL path = migrations.nextElement();
								LOG.debug("Adding application migrations in path '" + path.getPath() + "' using protocol '" + path.getProtocol() + "'");
								final Set<String> applicationMigrations;
								if (JAR_APPLICATION_MIGRATIONS_PROTOCOL.equals(path.getProtocol())) {
										try (FileSystem fileSystem = initFileSystem(path.toURI())) {
												applicationMigrations = getApplicationMigrationsFromPath(location, path);
										}
								} else if (FILE_APPLICATION_MIGRATIONS_PROTOCOL.equals(path.getProtocol())) {
										applicationMigrations = getApplicationMigrationsFromPath(location, path);
								} else {
										LOG.warn("Unsupported URL protocol '" + path.getProtocol() + "' for path '" + path.getPath() + "'. Migration files will not be discovered.");
										applicationMigrations = null;
								}
								if (applicationMigrations != null) {
										applicationMigrationResources.addAll(applicationMigrations);
								}
						}
				}
				return applicationMigrationResources;
		}

		private Set<String> getApplicationMigrationsFromPath(final String location, final URL path)
				throws IOException, URISyntaxException {
				try (Stream<Path> pathStream = Files.walk(Paths.get(path.toURI()))) {
						return pathStream.filter(Files::isRegularFile)
								.map(it -> Paths.get(location, it.getFileName().toString()).toString())
								// we don't want windows paths here since the paths are going to be used as classpath paths anyway
								.map(it -> it.replace('\\', '/'))
								.peek(it -> LOG.debug("Discovered path: " + it))
								.collect(Collectors.toSet());
				}
		}

		private FileSystem initFileSystem(final URI uri) throws IOException {
				final Map<String, String> env = new HashMap<>();
				env.put("create", "true");
				return FileSystems.newFileSystem(uri, env);
		}

}

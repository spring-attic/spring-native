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

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.scanner.LocationScannerCache;
import org.flywaydb.core.internal.scanner.ResourceNameCache;
import org.flywaydb.core.internal.scanner.classpath.ResourceAndClassScanner;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * This substitution replaces the Flyway dynamic scanners with a fixed path scanner in native mode.
 * <p>
 * Forked from micronaut:
 * https://github.com/micronaut-projects/micronaut-flyway/blob/758da7d56280f5b923b2e09a1849e82974679e64/flyway/src/main/java/io/micronaut/flyway/graalvm/ScannerSubstitutions.java
 * <p>
 * Which was forked from Quarkus:
 * https://github.com/quarkusio/quarkus/blob/2c99a30985e7cae42933b949ecd2ee82d546c4aa/extensions/flyway/runtime/src/main/java/io/quarkus/flyway/runtime/graal/ScannerSubstitutions.java
 */
@TargetClass(className = "org.flywaydb.core.internal.scanner.Scanner")
public final class ScannerSubstitutions {

		@Alias
		private List<LoadableResource> resources = new ArrayList<>();

		@Alias
		private List<Class<?>> classes = new ArrayList<>();

		@Alias
		private HashMap<String, LoadableResource> relativeResourceMap = new HashMap<>();

		/**
		 * Creates only {@link org.flywaydb.NativePathLocationScanner} instances.
		 * Replaces the original method that tries to detect migrations using reflection techniques that are not allowed
		 * in native mode.
		 *
		 * @see org.flywaydb.core.internal.scanner.Scanner#Scanner(Class, Collection, ClassLoader, Charset, boolean, boolean, ResourceNameCache, LocationScannerCache, boolean)
		 */
		@Substitute
		public ScannerSubstitutions(
				Class<?> implementedInterface,
				Collection<Location> locations,
				ClassLoader classLoader,
				Charset encoding,
				boolean detectEncoding,
				boolean stream,
				ResourceNameCache resourceNameCache,
				LocationScannerCache locationScannerCache,
				boolean throwOnMissingLocations) {
				ResourceAndClassScanner scanner = new NativePathLocationScanner(locations);

				Collection resources = scanner.scanForResources();
				this.resources.addAll(resources);

				Collection scanForClasses = scanner.scanForClasses();
				classes.addAll(scanForClasses);

				for (LoadableResource resource : this.resources) {
						relativeResourceMap.put(resource.getRelativePath().toLowerCase(), resource);
				}
		}
}

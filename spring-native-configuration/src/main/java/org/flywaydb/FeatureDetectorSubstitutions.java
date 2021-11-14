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

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * FeatureDetector substitution.
 * <p>
 * Forked from micronaut:
 * https://github.com/micronaut-projects/micronaut-flyway/blob/758da7d56280f5b923b2e09a1849e82974679e64/flyway/src/main/java/io/micronaut/flyway/graalvm/FeatureDetectorSubstitutions.java
 * <p>
 * Which was forked from Quarkus:
 * https://github.com/quarkusio/quarkus/blob/38280358d8cf04b91dad7ef5a9d463823f8ae675/extensions/flyway/runtime/src/main/java/io/quarkus/flyway/runtime/graal/FeatureDetectorSubstitutions.java
 */
@Substitute
@TargetClass(className = "org.flywaydb.core.internal.util.FeatureDetector")
public final class FeatureDetectorSubstitutions {

		@Substitute
		public FeatureDetectorSubstitutions(ClassLoader classLoader) {
		}

		@Substitute
		public boolean isApacheCommonsLoggingAvailable() {
				return false;
		}

		@Substitute
		public boolean isLog4J2Available() {
				return false;
		}

		@Substitute
		public boolean isSlf4jAvailable() {
				return true;
		}

		@Substitute
		public boolean isJBossVFSv2Available() {
				return false;
		}

		@Substitute
		public boolean isJBossVFSv3Available() {
				return false;
		}

		@Substitute
		public boolean isOsgiFrameworkAvailable() {
				return false;
		}

		@Substitute
		public boolean isAwsAvailable() {
				return false;
		}

		@Substitute
		public boolean isGCSAvailable() {
				return false;
		}

}

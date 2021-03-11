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

package org.springframework.aot.gradle.dsl;

import org.gradle.api.Incubating;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

/**
 * Entry point to SpringAot DSL extension.
 *
 * @author Brian Clozel
 */
public class SpringAotExtension {

	private final Property<AotMode> mode;

	private final Property<Boolean> debugVerify;

	private final Property<Boolean> removeXmlSupport;

	private final Property<Boolean> removeSpelSupport;

	private final Property<Boolean> removeYamlSupport;

	private final Property<Boolean> removeJmxSupport;

	private final Property<Boolean> verify;

	private final Property<Boolean> removeUnusedConfig;

	private final Property<Boolean> failOnMissingSelectorHint;

	private final Property<Boolean> buildTimePropertiesMatchIfMissing;

	private final Property<String[]> buildTimePropertiesChecks;

	public SpringAotExtension(ObjectFactory objectFactory) {
		this.mode = objectFactory.property(AotMode.class).convention(AotMode.NATIVE);
		this.debugVerify = objectFactory.property(Boolean.class).convention(false);
		this.removeXmlSupport = objectFactory.property(Boolean.class).convention(true);
		this.removeSpelSupport = objectFactory.property(Boolean.class).convention(false);
		this.removeYamlSupport = objectFactory.property(Boolean.class).convention(false);
		this.removeJmxSupport = objectFactory.property(Boolean.class).convention(true);
		this.verify = objectFactory.property(Boolean.class).convention(true);
		this.removeUnusedConfig = objectFactory.property(Boolean.class).convention(true);
		this.failOnMissingSelectorHint = objectFactory.property(Boolean.class).convention(true);
		this.buildTimePropertiesMatchIfMissing = objectFactory.property(Boolean.class).convention(true);
		this.buildTimePropertiesChecks = objectFactory.property(String[].class).convention(new String[0]);
	}

	/**
	 * Switches how much configuration the feature actually provides to the native image compiler.
	 */
	public Property<AotMode> getMode() {
		return this.mode;
	}

	/**
	 * Enable verification debug (false by default).
	 */
	public Property<Boolean> getDebugVerify() {
		return this.debugVerify;
	}

	/**
	 * Remove XML support for converters, codecs and application context (true by default).
	 */
	public Property<Boolean> getRemoveXmlSupport() {
		return this.removeXmlSupport;
	}

	/**
	 * Remove Spring SpEL support (false by default).
	 */
	public Property<Boolean> getRemoveSpelSupport() {
		return this.removeSpelSupport;
	}

	/**
	 * Remove Spring Yaml support (false by default).
	 */
	public Property<Boolean> getRemoveYamlSupport() {
		return this.removeYamlSupport;
	}

	/**
	 * Remove Spring Boot JMX support (true by default).
	 */
	public Property<Boolean> getRemoveJmxSupport() {
		return this.removeJmxSupport;
	}

	/**
	 * Perform automated verification to ensure the application is native compliant (true by default).
	 */
	public Property<Boolean> getVerify() {
		return this.verify;
	}

	/**
	 * Remove unused application configuration (true by default).
	 */
	public Property<Boolean> getRemoveUnusedConfig() {
		return this.removeUnusedConfig;
	}

	/**
	 * Whether to throw an error when no hint is provided for an active selector (simply logs a warning if disabled).
	 */
	public Property<Boolean> getFailOnMissingSelectorHint() {
		return this.failOnMissingSelectorHint;
	}

	/**
	 * Whether AOT should honor the {@code matchIfMissing} directive in configuration properties.
	 * If disabled, the application needs to explicitly activate {@code *.enable} configuration properties
	 * since {@code matchIfMissing} is not respected.
	 * This is enabled by default.
	 */
	@Incubating
	public Property<Boolean> getBuildTimePropertiesMatchIfMissing() {
		return this.buildTimePropertiesMatchIfMissing;
	}

	/**
	 * Switches on build time evaluation of some configuration conditions related to properties.
	 * <p>It must include at least an initial setting of {@code default-include-all} or {@code default-exclude-all}
	 * and that may be followed by a comma separated list of prefixes to explicitly include or exclude.
	 * For example {@code VersionExtractor}.
	 * When considering a property the longest matching prefix in this setting will apply (in cases where a property matches multiple prefixes).
	 */
	@Incubating
	public Property<String[]> getBuildTimePropertiesChecks() {
		return this.buildTimePropertiesChecks;
	}

}

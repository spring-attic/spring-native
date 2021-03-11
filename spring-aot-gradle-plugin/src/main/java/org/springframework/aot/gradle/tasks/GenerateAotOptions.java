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

package org.springframework.aot.gradle.tasks;

import java.io.Serializable;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;

import org.springframework.aot.gradle.dsl.SpringAotExtension;
import org.springframework.nativex.AotOptions;

/**
 * Nested Task input for {@link GenerateAotSources}.
 * 
 * @author Brian Clozel
 */
public class GenerateAotOptions implements Serializable {

	private final Provider<String> mode;

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

	public GenerateAotOptions(SpringAotExtension extension) {
		this.mode = extension.getMode().map(aotMode -> aotMode.getSlug());
		this.debugVerify = extension.getDebugVerify();
		this.removeXmlSupport = extension.getRemoveXmlSupport();
		this.removeSpelSupport = extension.getRemoveSpelSupport();
		this.removeYamlSupport = extension.getRemoveYamlSupport();
		this.removeJmxSupport = extension.getRemoveJmxSupport();
		this.verify = extension.getVerify();
		this.removeUnusedConfig = extension.getRemoveUnusedConfig();
		this.failOnMissingSelectorHint = extension.getFailOnMissingSelectorHint();
		this.buildTimePropertiesMatchIfMissing = extension.getBuildTimePropertiesMatchIfMissing();
		this.buildTimePropertiesChecks = extension.getBuildTimePropertiesChecks();
	}

	@Input
	public Provider<String> getMode() {
		return this.mode;
	}

	@Input
	public Property<Boolean> getDebugVerify() {
		return this.debugVerify;
	}

	@Input
	public Property<Boolean> getRemoveXmlSupport() {
		return this.removeXmlSupport;
	}

	@Input
	public Property<Boolean> getRemoveSpelSupport() {
		return this.removeSpelSupport;
	}

	@Input
	public Property<Boolean> getRemoveYamlSupport() {
		return this.removeYamlSupport;
	}

	@Input
	public Property<Boolean> getRemoveJmxSupport() {
		return this.removeJmxSupport;
	}

	@Input
	public Property<Boolean> getVerify() {
		return this.verify;
	}

	@Input
	public Property<Boolean> getRemoveUnusedConfig() {
		return this.removeUnusedConfig;
	}

	@Input
	public Property<Boolean> getFailOnMissingSelectorHint() {
		return this.failOnMissingSelectorHint;
	}

	@Input
	public Property<Boolean> getBuildTimePropertiesMatchIfMissing() {
		return this.buildTimePropertiesMatchIfMissing;
	}

	@Input
	public Property<String[]> getBuildTimePropertiesChecks() {
		return this.buildTimePropertiesChecks;
	}

	AotOptions toAotOptions() {
		AotOptions options = new AotOptions();
		options.setMode(this.mode.get());
		options.setDebugVerify(this.debugVerify.get());
		options.setRemoveXmlSupport(this.removeXmlSupport.get());
		options.setRemoveSpelSupport(this.removeSpelSupport.get());
		options.setRemoveYamlSupport(this.removeYamlSupport.get());
		options.setRemoveJmxSupport(this.removeJmxSupport.get());
		options.setVerify(this.verify.get());
		options.setRemoveUnusedConfig(this.removeUnusedConfig.get());
		options.setFailOnMissingSelectorHint(this.failOnMissingSelectorHint.get());
		options.setBuildTimePropertiesMatchIfMissing(this.buildTimePropertiesMatchIfMissing.get());
		options.setBuildTimePropertiesChecks(this.buildTimePropertiesChecks.get());
		return options;
	}
}

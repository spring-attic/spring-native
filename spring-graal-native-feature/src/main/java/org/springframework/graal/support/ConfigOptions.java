/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.graal.support;

/**
 * Encapsulate configurable feature behaviour.
 * 
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
public abstract class ConfigOptions {

	private final static boolean REMOVE_UNUSED_AUTOCONFIG;

	private final static boolean REMOVE_YAML_SUPPORT;
	
	private final static String DUMP_CONFIG;

	private final static boolean VERBOSE;
	
	private final static String MISSING_SELECTOR_HINTS;

	// In light mode the feature only supplies initialization information and nothing
	// about reflection/proxies/etc - this is useful if using the agent to produce that
	// configuration data.
	private final static String MODE; // 'default'/'initialization-only'

	static {
		REMOVE_UNUSED_AUTOCONFIG = Boolean.valueOf(System.getProperty("spring.graal.remove-unused-autoconfig", "false"));
		if(REMOVE_UNUSED_AUTOCONFIG) {
			System.out.println("Removing unused configurations");
		}
		VERBOSE = Boolean.valueOf(System.getProperty("spring.graal.verbose","false"));
		if (VERBOSE) {
			System.out.println("Turning on verbose mode for the feature");
		}
		MISSING_SELECTOR_HINTS = System.getProperty("spring.graal.missing-selector-hints","error");
		if (MISSING_SELECTOR_HINTS.equals("warning")) {
			System.out.println("Selectors missing hints will be reported as a warning, not an error");
		} else if (!MISSING_SELECTOR_HINTS.equals("error")) {
			throw new IllegalStateException("Supported values for 'spring.graal.missing-selector-hints' are 'error' (default) or 'warning'");
		}
		MODE = System.getProperty("spring.graal.mode","default");
		if (MODE.equals("initialization-only")) {
			System.out.println("Feature operating in initialization-only mode, only supplying substitutions and initialization data");
		} else if (!MODE.equals("default")) {
			throw new IllegalStateException("Supported modes are 'default' or 'initialization-only', not '"+MODE+"'");
		}
		REMOVE_YAML_SUPPORT = Boolean.valueOf(System.getProperty("spring.graal.remove-yaml-support", "false"));
		if (REMOVE_YAML_SUPPORT) {
			System.out.println("Skipping Yaml support");
		}
		DUMP_CONFIG = System.getProperty("spring.graal.dump-config");
		if (DUMP_CONFIG!=null) {
			System.out.println("Dumping computed config to "+DUMP_CONFIG);
		}
	}

	public static boolean shouldRemoveUnusedAutoconfig() {
		return REMOVE_UNUSED_AUTOCONFIG;
	}
	
	public static boolean areMissingSelectorHintsAnError() {
		return MISSING_SELECTOR_HINTS.equals("error");
	}

	public static boolean isVerbose() {
		return VERBOSE;
	}

	public static boolean shouldDumpConfig() {
		return DUMP_CONFIG != null;
	}

	public static boolean shouldRemoveYamlSupport() {
		return REMOVE_YAML_SUPPORT;
	}

	public static boolean isInitializationModeOnly() {
		return MODE.equals("initialization-only");
	}

	public static String getDumpConfigLocation() {
		return DUMP_CONFIG;
	}
}
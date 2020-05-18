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
package org.springframework.graalvm.support;

/**
 * Encapsulate configurable feature behaviour.
 * 
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
public abstract class ConfigOptions {

	private final static boolean REMOVE_UNUSED_AUTOCONFIG;

	private final static boolean REMOVE_YAML_SUPPORT;

	private final static boolean REMOVE_XML_SUPPORT;

	private final static boolean REMOVE_SPEL_SUPPORT;

	private final static boolean REMOVE_JMX_SUPPORT;
	
	private final static String DUMP_CONFIG;

	private final static boolean VERBOSE;
	
	private final static String MISSING_SELECTOR_HINTS;
	
	private final static boolean VERIFIER_ON;
	
	private final static Mode MODE; // Default is 'feature'
	
	enum Mode {
		AGENT, // initialization-only configuration provided from the feature
		HYBRID, // assume agent for 'basic stuff' - feature provides initialization plus other (@Controller analysis?)
		FEATURE, // Default mode, provide everything
		FUNCTIONAL; // For functional style, feature provides initialization and resource configuration
	}

	static {
		String modeValue = System.getProperty("spring.native.mode","FEATURE");
		Mode inferredMode = Mode.valueOf(modeValue.toUpperCase());
		if (inferredMode == null) {
			MODE = Mode.FEATURE;
		} else {
			MODE = inferredMode;
		}
		System.out.println("Feature operating in "+MODE+" mode");
		REMOVE_UNUSED_AUTOCONFIG = Boolean.valueOf(System.getProperty("spring.native.remove-unused-autoconfig", "false"));
		if(REMOVE_UNUSED_AUTOCONFIG) {
			System.out.println("Removing unused configurations");
		}
		VERIFIER_ON = Boolean.valueOf(System.getProperty("spring.native.verify","false"));
		if(VERIFIER_ON) {
			System.out.println("Verification turned on");
		}
		VERBOSE = Boolean.valueOf(System.getProperty("spring.native.verbose","false"));
		if (VERBOSE) {
			System.out.println("Turning on verbose mode for the feature");
		}
		MISSING_SELECTOR_HINTS = System.getProperty("spring.native.missing-selector-hints","error");
		if (MISSING_SELECTOR_HINTS.equals("warning")) {
			System.out.println("Selectors missing hints will be reported as a warning, not an error");
		} else if (!MISSING_SELECTOR_HINTS.equals("error")) {
			throw new IllegalStateException("Supported values for 'spring.native.missing-selector-hints' are 'error' (default) or 'warning'");
		}
		REMOVE_YAML_SUPPORT = Boolean.valueOf(System.getProperty("spring.native.remove-yaml-support", "false"));
		if (REMOVE_YAML_SUPPORT) {
			System.out.println("Removing Yaml support");
		}
		REMOVE_XML_SUPPORT = Boolean.valueOf(System.getProperty("spring.native.remove-xml-support", "false"));
		if (REMOVE_XML_SUPPORT) {
			System.out.println("Removing XML support");
		}
		REMOVE_SPEL_SUPPORT = Boolean.valueOf(System.getProperty("spring.native.remove-spel-support", "false"));
		if (REMOVE_SPEL_SUPPORT) {
			System.out.println("Removing SpEL support");
		}
		REMOVE_JMX_SUPPORT = Boolean.valueOf(System.getProperty("spring.native.remove-jmx-support", "false"));
		if (REMOVE_JMX_SUPPORT) {
			System.out.println("Removing JMX support");
		}
		DUMP_CONFIG = System.getProperty("spring.native.dump-config");
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

	public static boolean isVerifierOn() {
		return VERIFIER_ON;
	}

	public static boolean shouldDumpConfig() {
		return DUMP_CONFIG != null;
	}

	public static boolean shouldRemoveYamlSupport() {
		return REMOVE_YAML_SUPPORT;
	}

	public static boolean shouldRemoveXmlSupport() {
		return REMOVE_XML_SUPPORT;
	}

	public static boolean shouldRemoveSpelSupport() {
		return REMOVE_SPEL_SUPPORT;
	}

	public static boolean shouldRemoveJmxSupport() {
		return REMOVE_JMX_SUPPORT;
	}

	public static boolean isAgentMode() {
		return MODE==Mode.AGENT;
	}
	
	public static boolean isHybridMode() {
		return MODE==Mode.HYBRID;
	}

	public static boolean isFeatureMode() {
		return MODE==Mode.FEATURE;
	}

	public static boolean isFunctionalMode() {
		return MODE==Mode.FUNCTIONAL;
	}

	public static String getDumpConfigLocation() {
		return DUMP_CONFIG;
	}

}
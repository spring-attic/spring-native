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
 */
public abstract class ConfigOptions {

	private final static boolean AVOID_LOGBACK;

	private final static boolean REMOVE_YAML_SUPPORT;
	
    private final static String DUMP_CONFIG;

	private final static boolean VERBOSE;

	// In light mode the feature only supplies initialization information and nothing
	// about reflection/proxies/etc - this is useful if using the agent to produce that
	// configuration data.
	private final static String MODE; // 'default'/'light'
    
    static {
		VERBOSE = Boolean.valueOf(System.getProperty("verbose","false"));
		if (VERBOSE) {
			System.out.println("Turning on verbose mode for the feature");
		}
		MODE = System.getProperty("mode","default");
		if (MODE.equals("light")) {
			System.out.println("Feature operating in light mode, only supplying substitutions and initialization data");
        } else if (!MODE.equals("default")) {
            throw new IllegalStateException("Supported modes are 'default' or 'light', not '"+MODE+"'");
        }
		AVOID_LOGBACK = Boolean.valueOf(System.getProperty("avoidLogback", "false"));
		if (AVOID_LOGBACK) {
			System.out.println("Avoiding logback configuration");
		}
		REMOVE_YAML_SUPPORT = Boolean.valueOf(System.getProperty("removeYamlSupport", "false"));
		if (REMOVE_YAML_SUPPORT) {
			System.out.println("Removing Yaml support");
		}
		DUMP_CONFIG = System.getProperty("dumpConfig");
		if (DUMP_CONFIG!=null) {
			System.out.println("Dumping computed config to "+DUMP_CONFIG);
		}
    }

    public static boolean isVerbose() {
        return VERBOSE;
    }

    public static boolean shouldDumpConfig() {
        return DUMP_CONFIG != null;
    }

    public static boolean shouldAvoidLogback() {
        return AVOID_LOGBACK;
    }

	public static boolean shouldRemoveYamlSupport() {
		return REMOVE_YAML_SUPPORT;
	}

    public static boolean isLightweightMode() {
        return MODE.equals("light");
    }

	public static String getDumpConfigLocation() {
        return DUMP_CONFIG;
	}
}
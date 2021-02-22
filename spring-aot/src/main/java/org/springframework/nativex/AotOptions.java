package org.springframework.nativex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.nativex.support.Mode;

/**
 * Options for Spring Native.
 * TODO Manage XML removal (enabled by default)
 * TODO Manage SpEL removal
 * TODO Manage JMX removal (enabled by default)
 *
 * @author Sebastien Deleuze
 */
public class AotOptions {

	private static Log logger = LogFactory.getLog(AotOptions.class);

	/**
	 * Defines how much native configuration is actually provided to the native-image compiler, can be {@code native}, {@code native-agent} or {@code native-init}.
	 */
	private String mode = "native";

	/**
	 * Verification debug.
	 */
	private boolean debugVerify;

	/**
	 * Ignore hints on class listed in {@code spring.autoconfigure.exclude}.
	 */
	private boolean ignoreHintsOnExcludedConfig = true;

	/**
	 * Enable the removal of unused configurations.
	 */
	private boolean removeUnusedConfig = true;

	/**
	 * Enable the verifications.
	 */
	private boolean verify = true;

	/**
	 * Remove Spring Boot Yaml support to optimize the footprint.
	 */
	private boolean removeYamlSupport;

	/**
	 * Remove Spring Boot JMX support to optimize the footprint.
	 */
	private boolean removeJmxSupport = true;

	/**
	 * Remove Spring XML support to optimize the footprint.
	 */
	private boolean removeXmlSupport = true;

	/**
	 * Remove Spring SpEL support to optimize the footprint.
	 */
	private boolean removeSpelSupport;

	/**
	 * For matchIfMissing property checks system will assume if the property is not specified the check will fail.
	 */
	private boolean buildTimePropertiesMatchIfMissing;

	private String[] buildTimePropertiesChecks;


	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public boolean isIgnoreHintsOnExcludedConfig() {
		return ignoreHintsOnExcludedConfig;
	}

	public void setIgnoreHintsOnExcludedConfig(boolean ignoreHintsOnExcludedConfig) {
		this.ignoreHintsOnExcludedConfig = ignoreHintsOnExcludedConfig;
	}

	public boolean isVerify() {
		return verify;
	}

	public void setVerify(boolean verify) {
		this.verify = verify;
	}

	public boolean isRemoveYamlSupport() {
		return removeYamlSupport;
	}

	public void setRemoveYamlSupport(boolean removeYamlSupport) {
		this.removeYamlSupport = removeYamlSupport;
	}

	public boolean isRemoveJmxSupport() {
		return removeJmxSupport;
	}

	public void setRemoveJmxSupport(boolean removeJmxSupport) {
		this.removeJmxSupport = removeJmxSupport;
	}

	public boolean isDebugVerify() {
		return debugVerify;
	}

	public void setDebugVerify(boolean debugVerify) {
		this.debugVerify = debugVerify;
	}

	public boolean isRemoveUnusedConfig() {
		return removeUnusedConfig;
	}

	public void setRemoveUnusedConfig(boolean removeUnusedConfig) {
		this.removeUnusedConfig = removeUnusedConfig;
	}

	public boolean isBuildTimePropertiesMatchIfMissing() {
		return buildTimePropertiesMatchIfMissing;
	}

	public void setBuildTimePropertiesMatchIfMissing(boolean buildTimePropertiesMatchIfMissing) {
		this.buildTimePropertiesMatchIfMissing = buildTimePropertiesMatchIfMissing;
	}

	public String[] getBuildTimePropertiesChecks() {
		return buildTimePropertiesChecks;
	}

	public void setBuildTimePropertiesChecks(String[] buildTimePropertiesChecks) {
		this.buildTimePropertiesChecks = buildTimePropertiesChecks;
	}

	public boolean isRemoveXmlSupport() {
		return removeXmlSupport;
	}

	public void setRemoveXmlSupport(boolean removeXmlSupport) {
		this.removeXmlSupport = removeXmlSupport;
	}

	public boolean isRemoveSpelSupport() {
		return removeSpelSupport;
	}

	public void setRemoveSpelSupport(boolean removeSpelSupport) {
		this.removeSpelSupport = removeSpelSupport;
	}

	public Mode toMode() {
		if (this.mode == null || this.mode.equals(Mode.NATIVE.toString())) {
			return Mode.NATIVE;
		} else if (this.mode.equals(Mode.NATIVE_AGENT.toString())) {
			return Mode.NATIVE_AGENT;
		} else if (this.mode.equals(Mode.NATIVE_INIT.toString())) {
			return Mode.NATIVE_INIT;
		}
		throw new IllegalStateException(this.mode + " is not a valid mode. Valid modes are: " + Mode.NATIVE.toString() + ", " +
				Mode.NATIVE_AGENT.toString() + ", " + Mode.NATIVE_INIT.toString());
	}

	/**
	 * Determine if the specified property should be checked at build time.
	 *
	 * @param key the property key (e.g. spring.application.name)
	 * @return true if the property should be checked at build time
	 */
	public boolean buildTimeCheckableProperty(String key) {
		if (!isBuildTimePropertyChecking()) {
			return false;
		}
		boolean defaultResult = true;
		int maxExplicitExclusionMatchLength = -1;
		int maxExplicitInclusionMatchLength = -1;
		for (String btpcPattern: buildTimePropertiesChecks) {
			if (btpcPattern.equals("default-include-all")) {
				defaultResult = true;
			} else if (btpcPattern.equals("default-exclude-all")) {
				defaultResult = false;
			} else if (btpcPattern.startsWith("!")) {
				// Exclusion: e.g. !management.foo.bar.
				if (key.startsWith(btpcPattern.substring(1))) {
					if ((btpcPattern.length()-1)>maxExplicitExclusionMatchLength) {
						maxExplicitExclusionMatchLength = btpcPattern.length()-1;
					}
				}
			} else {
				// Inclusion: e.g. spring.foo.
				if (key.startsWith(btpcPattern)) {
					if ((btpcPattern.length())>maxExplicitInclusionMatchLength) {
						maxExplicitInclusionMatchLength = btpcPattern.length();
					}
				}
			}
		}
		if (maxExplicitExclusionMatchLength==-1 && maxExplicitInclusionMatchLength==-1) {
			return defaultResult;
		}
		if (maxExplicitExclusionMatchLength>maxExplicitInclusionMatchLength) {
			// Explicit exclusion match was more specific
			return false;
		} else {
			return true;
		}
	}

	public boolean isBuildTimePropertyChecking() {
		if (buildTimePropertiesChecks == null) {
			return false;
		}
		if (buildTimePropertiesChecks.length > 0) {
			return true;
		}
		return false;
	}
}

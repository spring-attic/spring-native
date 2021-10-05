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

package org.springframework.nativex;

import org.springframework.nativex.support.Mode;

/**
 * Options for Spring Native.
 *
 * @author Sebastien Deleuze
 * @author Andy Clement
 */
public class AotOptions {

	private String mode = Mode.NATIVE.toString();

	/**
	 * Determine if extra debug should come out related to verification of reflection requests.
	 * Spring Native will compute configuration but a final stage verification runs before it is
	 * written out to try and ensure what is requested will not lead to problems when the
	 * native-image is built. This flag enables debugging of that final stage verification.
	 */
	private boolean debugVerify;

	/**
	 * Determine whether to check Spring components are suitable for inclusion in a native-image.
	 * For a concrete example, verification currently checks whether bean factory methods are being
	 * invoked directly in configuration classes. If they are then that must be enforced by using
	 * CGLIB proxies. CGLIB proxies are not supported in native-image and so a verification error
	 * will come out with a message indicating the code should switch to method parameter
	 * injection (allowing Spring to control bean method invocation).
	 */
	private boolean verify = true;

	private boolean removeYamlSupport;

	private boolean removeJmxSupport = true;

	private boolean removeXmlSupport = true;

	private boolean removeSpelSupport;


	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
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
		}
		throw new IllegalStateException(this.mode + " is not a valid mode. Valid modes are: "
				+ Mode.NATIVE + ", " + Mode.NATIVE_AGENT);
	}

}

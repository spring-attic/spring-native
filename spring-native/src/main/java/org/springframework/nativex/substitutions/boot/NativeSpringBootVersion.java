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

package org.springframework.nativex.substitutions.boot;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.springframework.boot.origin.Origin;

/**
 * See {@link Target_SpringBootVersion}.
 */
final class NativeSpringBootVersion {

	private static String VERSION = determineSpringBootVersion();

	private NativeSpringBootVersion() {
	}

	public static String getVersion() {
		return VERSION;
	}

	private static String determineSpringBootVersion() {
		// Use Origin to avoid potential conflict with generated code in org.springframework.boot package
		String implementationVersion = Origin.class.getPackage().getImplementationVersion();
		if (implementationVersion != null) {
			return implementationVersion;
		}
		// Use Origin to avoid potential conflict with generated code in org.springframework.boot package
		CodeSource codeSource = Origin.class.getProtectionDomain().getCodeSource();
		if (codeSource == null) {
			return null;
		}
		URL codeSourceLocation = codeSource.getLocation();
		try {
			URLConnection connection = codeSourceLocation.openConnection();
			if (connection instanceof JarURLConnection) {
				return getImplementationVersion(((JarURLConnection) connection).getJarFile());
			}
			try (JarFile jarFile = new JarFile(new File(codeSourceLocation.toURI()))) {
				return getImplementationVersion(jarFile);
			}
		}
		catch (Exception ex) {
			return null;
		}
	}

	private static String getImplementationVersion(JarFile jarFile) throws IOException {
		return jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
	}

}

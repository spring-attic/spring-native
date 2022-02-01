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

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.nativex.substitutions.OnlyIfPresent;

/**
 * Why this substitution exists?
 * SpringBootVersion#determineSpringBootVersion() current implementation is triggering usage of a lot of infrastructure
 * (especially via jarFile.getManifest()) to identify the Spring Boot version at runtime.
 *
 * How this substitution workarounds the problem?
 * NativeSpringBootVersion#determineSpringBootVersion() is invoked at build time because it is invoked to populate
 * NativeSpringBootVersion#VERSION + NativeSpringBootVersion configured to be initialized at build time in SpringBootHints.
 */
@TargetClass(className = "org.springframework.boot.SpringBootVersion", onlyWith = OnlyIfPresent.class)
final class Target_SpringBootVersion {

	@Substitute
	public static String getVersion() {
		return NativeSpringBootVersion.getVersion();
	}

}

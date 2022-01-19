/*
 * Copyright 2002-2021 the original author or authors.
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

import org.springframework.core.NativeDetector;
import org.springframework.core.SpringProperties;
import org.springframework.util.ClassUtils;

/**
 * Detects whether the application should be using AOT-generated classes at runtime.
 * 
 * @author Brian Clozel
 * @author Sebastien Deleuze
 */
public abstract class AotModeDetector {

	private static final String GENERATED_CLASS = "org.springframework.aot.StaticSpringFactories";

	private static final boolean generatedClassPresent = ClassUtils.isPresent(GENERATED_CLASS, null);

	private static final boolean aotTestClassPresent = ClassUtils.isPresent("org.springframework.aot.test.context.bootstrap.generator.AotTestContextProcessor", null);

	/**
	 * Returns whether AOT-generated code should be considered at runtime.
	 * @return {@code true} only if:
	 * <ul>
	 *     <li>The {@code "springAot"} system property is set
	 *     <li>or {@link NativeDetector#inNativeImage()} returns true
	 * </ul>
	 */
	public static boolean isAotModeEnabled() {
		if ("true".equals(System.getProperty("springAot")) || NativeDetector.inNativeImage()) {
			if (!generatedClassPresent) {
				throw new GeneratedClassNotFoundException(GENERATED_CLASS);
			}
			return true;
		}
		return false;
	}

	/**
	 * @return {@code true} when running AOT tests
	 */
	public static boolean isRunningAotTests() {
			return "org.springframework.aot.test.AotCacheAwareContextLoaderDelegate"
					.equals(SpringProperties.getProperty("spring.test.context.default.CacheAwareContextLoaderDelegate"));
	}

	/**
	 * @return {@code true} when generating AOT tests
	 */
	public static boolean isGeneratingAotTests() {
		return aotTestClassPresent;
	}

}

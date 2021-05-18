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
import org.springframework.util.ClassUtils;

/**
 * Detects whether the application should be using AOT-generated classes at runtime.
 * 
 * @author Brian Clozel
 */
public abstract class AotModeDetector {

	private static final boolean AOT_MODE_PROPERTY = System.getProperty("springAot") != null;

	private static final String GENERATED_CLASS = "org.springframework.aot.StaticSpringFactories";

	private static final boolean generatedClassPresent = ClassUtils.isPresent(GENERATED_CLASS, null);

	/**
	 * Returns whether AOT-generated code should be considered at runtime.
	 * This should return {@code true} only if:
	 * <ul>
	 *     <li>The {@code "springAot"} system property is set
	 *     <li>or {@link NativeDetector#inNativeImage()} returns true
	 * </ul>
	 */
	public static boolean isAotModeEnabled() {
		if (AOT_MODE_PROPERTY || NativeDetector.inNativeImage()) {
			if (!generatedClassPresent) {
				throw new GeneratedClassNotFoundException(GENERATED_CLASS);
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns if the thread is for a main invocation.
	 * @param thread the thread to check
	 * @return {@code true} if this is a main invocation, otherwise {@code false}
	 */
	protected static boolean isMain(Thread thread) {
		return thread.getName().equals("main") &&
				thread.getContextClassLoader().getClass().getName().contains("AppClassLoader");
	}

}

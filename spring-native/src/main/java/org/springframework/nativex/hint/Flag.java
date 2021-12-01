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

package org.springframework.nativex.hint;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The various types of access that can be requested. The names of these match
 * the names that go into the json configuration for native-image.
 * 
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
public enum Flag {

	/**
	 * Inferred with:
	 * <ul>
	 *     <li>{@link Flag#allPublicMethods} for annotations and interfaces</li>
	 *     <li>None for arrays (class access)</li>
	 *     <li>{@link Flag#allDeclaredConstructors} for other types</li>
	 * </ul>
	 */
	autoDetect,

	/**
	 * Configure related JNI reflection entry, to be combined with other `Flag` entries.
	 */
	jni,

	/**
	 * Configure access to the *.class file resource.
	 */
	resource,

	allPublicFields,
	allDeclaredFields,
	allDeclaredConstructors,
	allPublicConstructors,
	allDeclaredMethods,
	allPublicMethods,
	allDeclaredClasses,
	allPublicClasses,
	queryAllDeclaredMethods,
	queryAllPublicMethods,
	queryAllDeclaredConstructors,
	queryAllPublicConstructors;

	public static String toString(Flag[] flags) {
		List<Flag> asList = Arrays.asList(flags);
		Collections.sort(asList);
		return asList.toString();
	}
}
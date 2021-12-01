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
 * The various types of access that can be requested. The values of these match
 * the names that go into the json configuration for native-image.
 * 
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
public enum TypeAccess {

	/**
	 * Inferred with:
	 * <ul>
	 *     <li>{@link TypeAccess#PUBLIC_METHODS} for annotations and interfaces</li>
	 *     <li>None for arrays (class access)</li>
	 *     <li>{@link TypeAccess#DECLARED_CONSTRUCTORS} for other types</li>
	 * </ul>
	 */
	AUTO_DETECT("autoDetect"),

	/**
	 * Configure related JNI reflection entry, to be combined with other `Flag` entries.
	 */
	JNI("jni"),

	/**
	 * Configure access to the *.class file resource.
	 */
	RESOURCE("resource"),

	PUBLIC_FIELDS("allPublicFields"),
	DECLARED_FIELDS("allDeclaredFields"),
	DECLARED_CONSTRUCTORS("allDeclaredConstructors"),
	PUBLIC_CONSTRUCTORS("allPublicConstructors"),
	DECLARED_METHODS("allDeclaredMethods"),
	PUBLIC_METHODS("allPublicMethods"),
	DECLARED_CLASSES("allDeclaredClasses"),
	PUBLIC_CLASSES("allPublicClasses"),
	QUERY_DECLARED_METHODS("queryAllDeclaredMethods"),
	QUERY_PUBLIC_METHODS("queryAllPublicMethods"),
	QUERY_DECLARED_CONSTRUCTORS("queryAllDeclaredConstructors"),
	QUERY_PUBLIC_CONSTRUCTORS("queryAllPublicConstructors");

	private final String value;

	TypeAccess(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public static String toString(TypeAccess[] access) {
		List<TypeAccess> asList = Arrays.asList(access);
		Collections.sort(asList);
		return asList.toString();
	}
}
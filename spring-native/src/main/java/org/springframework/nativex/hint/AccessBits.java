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

import java.util.ArrayList;
import java.util.List;

/**
 * Specifies the reflective access desired for a type and whether it needs to be accessible as
 * a resource in the image (i.e. the {@code .class} resource to be readable as a byte array).
 *
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/Reflection/#manual-configuration">Manual configuration of reflection use in native images</a>
 * @author Andy Clement
 * @author Sebastien Deleuze
 * @author Christoph Strobl
 * @deprecated Use {@link Flag} instead.
 */
@Deprecated
public class AccessBits {

	/**
	 * Resource access required when ASM is used at runtime to access to {@code *.class} resources.
	 */
	public static final int RESOURCE              = 0x0001;

	/**
	 * Class access, for example when {@code Class.forName(String)} is invoked with a non constant parameter that can't
	 * be recognized automatically by the native image compiler.
	 */
	public static final int CLASS                 = 0x0002;

	/**
	 * Declared constructors access: public, protected, default (package) access, and private ones.
	 * @see Class#getDeclaredConstructors()
	 */
	public static final int DECLARED_CONSTRUCTORS = 0x0004;

	/**
	 * Declared methods access: public, protected, default (package) access, and private, but excluding inherited ones.
	 * Consider whether you need this or @link {@link #PUBLIC_METHODS}.
	 * @see Class#getDeclaredMethods()
	 */
	public static final int DECLARED_METHODS      = 0x0008;

	/**
	 * Declared fields access: public, protected, default (package) access, and private, but excluding inherited ones.
	 * @see Class#getDeclaredFields()
	 */
	public static final int DECLARED_FIELDS       = 0x0010;

	/**
	 * Public methods access: public methods of the class including inherited ones.
	 * Consider whether you need this or @link {@link #DECLARED_METHODS}.
	 * @see Class#getMethods()
	 */
	public static final int PUBLIC_METHODS        = 0x0020;

	/**
	 * Public constructors.
	 * @see Class#getConstructors()
	 */
	public static final int PUBLIC_CONSTRUCTORS   = 0x0040;

	/**
	 * Type must be accessible from JNI code (it will be placed in the jni-config.json and not reflect-config.json)
	 */
	public static final int JNI = 0x0080;

	/**
	 * Declared constructor's metadata query: public, protected, default (package) access, and private ones.
	 * @see Class#getDeclaredConstructors()
	 */
	public static final int QUERY_DECLARED_CONSTRUCTORS = 0x0100;

	/**
	 * Declared method's metadata query: public, protected, default (package) access, and private, but excluding inherited ones.
	 * Consider whether you need this or @link {@link #QUERY_PUBLIC_METHODS}.
	 * @see Class#getDeclaredMethods()
	 */
	public static final int QUERY_DECLARED_METHODS      = 0x0200;

	/**
	 * Public method's metadata query access: public methods of the class including inherited ones.
	 * Consider whether you need this or @link {@link #QUERY_DECLARED_METHODS}.
	 * @see Class#getMethods()
	 */
	public static final int QUERY_PUBLIC_METHODS        = 0x0400;

	/**
	 * Queried public constructors.
	 * @see Class#getConstructors()
	 */
	public static final int QUERY_PUBLIC_CONSTRUCTORS   = 0x0800;
	
	/**
	 * No access.
	 */
	public static final int NONE = 0;

	/**
	 * Full reflection access.
	 */
	public static final int FULL_REFLECTION = (CLASS | DECLARED_CONSTRUCTORS | PUBLIC_CONSTRUCTORS | DECLARED_METHODS | PUBLIC_METHODS | DECLARED_FIELDS);

	/**
	 * Combine all kinds of access, including {@link #RESOURCE}.
	 * @see #FULL_REFLECTION
	 */
	public static final int ALL = (RESOURCE | FULL_REFLECTION);

	/**
	 * Predefined set of access suitable for annotations.
	 */
	public static final int ANNOTATION = (CLASS | PUBLIC_METHODS);

	/**
	 * Predefined set of access suitable for interfaces.
	 */
	public static final int INTERFACE = (CLASS | PUBLIC_METHODS);

	/**
	 * Class and declared constructor access (default).
	 */
	public static final int LOAD_AND_CONSTRUCT = (CLASS | DECLARED_CONSTRUCTORS);

	/**
	 * Class, declared constructor and public method access.
	 */
	public static final int LOAD_AND_CONSTRUCT_AND_PUBLIC_METHODS = LOAD_AND_CONSTRUCT | PUBLIC_METHODS;

	private int value;

	public int hashCode() {
		return value;
	}

	public boolean equals(Object that) {
		if (that instanceof AccessBits) {
			return this.value == ((AccessBits) that).value;
		}
		return false;
	}

	public String toString() {
		return toString(value);
	}

	public static Flag[] getFlags(int value) {
		List<Flag> flags = new ArrayList<>();
		if ((value & DECLARED_FIELDS) != 0) {
			flags.add(Flag.allDeclaredFields);
		}
		if ((value & DECLARED_CONSTRUCTORS) != 0) {
			flags.add(Flag.allDeclaredConstructors);
		}
		if ((value & PUBLIC_CONSTRUCTORS) != 0) {
			flags.add(Flag.allPublicConstructors);
		}
		if ((value & DECLARED_METHODS) != 0) {
			flags.add(Flag.allDeclaredMethods);
		}
		if ((value & PUBLIC_METHODS) != 0) {
			flags.add(Flag.allPublicMethods);
		}
		if ((value & QUERY_DECLARED_CONSTRUCTORS) != 0) {
			flags.add(Flag.queryAllDeclaredConstructors);
		}
		if ((value & QUERY_PUBLIC_CONSTRUCTORS) != 0) {
			flags.add(Flag.queryAllPublicConstructors);
		}
		if ((value & QUERY_DECLARED_METHODS) != 0) {
			flags.add(Flag.queryAllDeclaredMethods);
		}
		if ((value & QUERY_PUBLIC_METHODS) != 0) {
			flags.add(Flag.queryAllPublicMethods);
		}
		return flags.toArray(new Flag[0]);
	}

	public AccessBits() {
		value = 0;
	}

	public AccessBits(int value) {
		this.value = value;
	}

	public final static AccessBits forValue(int value) {
		return new AccessBits(value);
	}

	public final static AccessBits forBits(int... bits) {
		int value = 0;
		for (int i = 0; i < bits.length; i++) {
			value |= bits[i];
		}
		return new AccessBits(value);
	}

	public boolean isResourceAccessRequired() {
		return (value & RESOURCE) != 0;
	}

	public boolean hasAccess(AccessBits accessBitsToCheck) {
		return ((value ^ accessBitsToCheck.value) == 0);
	}

	public AccessBits with(AccessBits accessRequired) {
		return forValue(value | accessRequired.value);
	}

	public static String toString(Integer value) {
		StringBuilder s = new StringBuilder();
		s.append("ACS(");
		if ((value & RESOURCE) != 0) {
			s.append("RES ");
		}
		if ((value & CLASS) != 0) {
			s.append("CLS ");
		}
		if ((value & DECLARED_CONSTRUCTORS) != 0) {
			s.append("DCONS ");
		}
		if ((value & PUBLIC_CONSTRUCTORS) != 0) {
			s.append("PCONS ");
		}
		if ((value & DECLARED_METHODS) != 0) {
			s.append("DMETHS ");
		}
		if ((value & PUBLIC_METHODS) != 0) {
			s.append("PMETHS ");
		}
		if ((value & DECLARED_FIELDS) != 0) {
			s.append("FLDS ");
		}
		if ((value & QUERY_DECLARED_CONSTRUCTORS) != 0) {
			s.append("QDCONS ");
		}
		if ((value & QUERY_PUBLIC_CONSTRUCTORS) != 0) {
			s.append("QPCONS ");
		}
		if ((value & QUERY_DECLARED_METHODS) != 0) {
			s.append("QDMETHS ");
		}
		if ((value & QUERY_PUBLIC_METHODS) != 0) {
			s.append("QPMETHS ");
		}
		if (value == 0) {
			s.append("NONE");
		}
		return s.toString().trim() + ")";
	}

	public static boolean isResourceAccessRequired(Integer typeKind) {
		return (typeKind & RESOURCE)!=0;
	}

	public int getValue() {
		return value;
	}

	/**
	 * Compare a current access level with a proposed access level and return what
	 * the new proposed access is adding.
	 * @param currentAccess the current access level
	 * @param newAccess the new access level
	 * @return what the new proposed access is adding
	 */
	public static int compareAccess(int currentAccess, int newAccess) {
		int result = 0;
		if ((currentAccess&RESOURCE)==0 && (newAccess&RESOURCE)!=0) {
			result = result|RESOURCE;
		}
		if ((currentAccess&CLASS)==0 && (newAccess&CLASS)!=0) {
			result = result|CLASS;
		}
		if ((currentAccess&DECLARED_CONSTRUCTORS)==0 && (newAccess&DECLARED_CONSTRUCTORS)!=0) {
			result = result|DECLARED_CONSTRUCTORS;
		}
		if ((currentAccess&PUBLIC_CONSTRUCTORS)==0 && (newAccess&PUBLIC_CONSTRUCTORS)!=0) {
			result = result|PUBLIC_CONSTRUCTORS;
		}
		if ((currentAccess&DECLARED_METHODS)==0 && (newAccess&DECLARED_METHODS)!=0) {
			result = result|DECLARED_METHODS;
		}
		if ((currentAccess&DECLARED_FIELDS)==0 && (newAccess&DECLARED_FIELDS)!=0) {
			result = result|DECLARED_FIELDS;
		}
		if ((currentAccess&PUBLIC_METHODS)==0 && (newAccess&PUBLIC_METHODS)!=0) {
			result = result|PUBLIC_METHODS;
		}
		if ((currentAccess&QUERY_DECLARED_CONSTRUCTORS)==0 && (newAccess&QUERY_DECLARED_CONSTRUCTORS)!=0) {
			result = result|QUERY_DECLARED_CONSTRUCTORS;
		}
		if ((currentAccess&QUERY_PUBLIC_CONSTRUCTORS)==0 && (newAccess&QUERY_PUBLIC_CONSTRUCTORS)!=0) {
			result = result|QUERY_PUBLIC_CONSTRUCTORS;
		}
		if ((currentAccess&QUERY_DECLARED_METHODS)==0 && (newAccess&QUERY_DECLARED_METHODS)!=0) {
			result = result|QUERY_DECLARED_METHODS;
		}
		if ((currentAccess&QUERY_PUBLIC_METHODS)==0 && (newAccess&QUERY_PUBLIC_METHODS)!=0) {
			result = result|QUERY_PUBLIC_METHODS;
		}
		return result;
	}

	public static AccessBits fromFlags(Flag... flags) {

		Integer value = 0;

		if (flags.length == 0) {
				value = AccessBits.CLASS;
		}

		for (Flag flag : flags) { // TODO: is this sufficient?
			if (Flag.allDeclaredConstructors.equals(flag)) {
				value = value | AccessBits.DECLARED_CONSTRUCTORS;
			}
			if (Flag.allPublicConstructors.equals(flag)) {
				value = value | AccessBits.PUBLIC_CONSTRUCTORS;
			}
			if (Flag.allDeclaredMethods.equals(flag)) {
				value = value | AccessBits.DECLARED_METHODS;
			}
			if (Flag.allPublicMethods.equals(flag)) {
				value = value | AccessBits.PUBLIC_METHODS;
			}
			if (Flag.allDeclaredFields.equals(flag)) {
				value = value | AccessBits.DECLARED_FIELDS;
			}
			if (Flag.queryAllDeclaredConstructors.equals(flag)) {
				value = value | AccessBits.QUERY_DECLARED_CONSTRUCTORS;
			}
			if (Flag.queryAllPublicConstructors.equals(flag)) {
				value = value | AccessBits.QUERY_PUBLIC_CONSTRUCTORS;
			}
			if (Flag.queryAllDeclaredMethods.equals(flag)) {
				value = value | AccessBits.QUERY_DECLARED_METHODS;
			}
			if (Flag.queryAllPublicMethods.equals(flag)) {
				value = value | AccessBits.QUERY_PUBLIC_METHODS;
			}
			if (Flag.resource.equals(flag)) {
				value = value | AccessBits.RESOURCE;
			}
			if (Flag.jni.equals(flag)) {
				value = value | AccessBits.JNI;
			}
		}

		return new AccessBits(value);
	}

	public static boolean isSet(int value, int mask) {
		return (value&mask)!=0;
	}
}
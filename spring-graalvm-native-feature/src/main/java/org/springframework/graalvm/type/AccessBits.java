/*
 * Copyright 2020 Contributors
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
package org.springframework.graalvm.type;

import java.util.ArrayList;
import java.util.List;

import org.springframework.graalvm.domain.reflect.Flag;

/**
 * Specifies the reflective access desired for a type and whether it needs to be accessible as
 * a resource in the image (i.e. the .class is going to be read as a byte array).
 * 
 * @author Andy Clement
 */
public class AccessBits {
	public static final int RESOURCE = 0x0001;
	public static final int CLASS = 0x0002;
	public static final int DECLARED_CONSTRUCTORS = 0x0004;
	public static final int DECLARED_METHODS = 0x0008;
	public static final int DECLARED_FIELDS = 0x0010;

	public static final int NONE = 0;
	public static final int FULL_REFLECTION = (CLASS | DECLARED_CONSTRUCTORS | DECLARED_METHODS | DECLARED_FIELDS);
	public static final int ALL = (RESOURCE | CLASS | DECLARED_CONSTRUCTORS | DECLARED_METHODS | DECLARED_FIELDS);
	public static final int CONFIGURATION = (RESOURCE | CLASS | DECLARED_METHODS | DECLARED_CONSTRUCTORS | DECLARED_FIELDS);
	public static final int EVERYTHING = (RESOURCE | CLASS | DECLARED_METHODS | DECLARED_CONSTRUCTORS | DECLARED_FIELDS);
	public static final int ANNOTATION = (RESOURCE | CLASS | DECLARED_METHODS);
	public static final int LOAD_AND_CONSTRUCT = (CLASS | DECLARED_CONSTRUCTORS);

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
		StringBuilder s = new StringBuilder();
		s.append("AccessBits(");
		if ((value & RESOURCE) != 0) {
			s.append("RES ");
		}
		if ((value & CLASS) != 0) {
			s.append("CLS ");
		}
		if ((value & DECLARED_CONSTRUCTORS) != 0) {
			s.append("CONS ");
		}
		if ((value & DECLARED_METHODS) != 0) {
			s.append("METHS ");
		}
		if ((value & DECLARED_FIELDS) != 0) {
			s.append("FLDS ");
		}
		if (value == 0) {
			s.append("NONE");
		}
		return s.toString().trim() + ")";
	}

	public static Flag[] getFlags(int value) {
		List<Flag> flags = new ArrayList<>();
		if ((value & DECLARED_FIELDS) != 0) {
			flags.add(Flag.allDeclaredFields);
		}
		if ((value & DECLARED_CONSTRUCTORS) != 0) {
			flags.add(Flag.allDeclaredConstructors);
		}
		if ((value & DECLARED_METHODS) != 0) {
			flags.add(Flag.allDeclaredMethods);
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
		s.append("AccessBits(");
		if ((value & RESOURCE) != 0) {
			s.append("RES ");
		}
		if ((value & CLASS) != 0) {
			s.append("CLS ");
		}
		if ((value & DECLARED_CONSTRUCTORS) != 0) {
			s.append("CONS ");
		}
		if ((value & DECLARED_METHODS) != 0) {
			s.append("METHS ");
		}
		if ((value & DECLARED_FIELDS) != 0) {
			s.append("FLDS ");
		}
		if (value == 0) {
			s.append("NONE");
		}
		return s.toString().trim() + ")";
	}

	public static boolean isResourceAccessRequired(Integer typeKind) {
		return (typeKind & RESOURCE)!=0;
	}
}
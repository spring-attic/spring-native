package org.springframework.graal.type;

import java.util.ArrayList;
import java.util.List;

import org.springframework.graal.domain.reflect.Flag;

public class AccessBits {
	public static final int RESOURCE = 0x0001;
	public static final int CLASS = 0x0002;
	public static final int PUBLIC_CONSTRUCTORS = 0x0004;
	public static final int PUBLIC_METHODS = 0x0008;
	public static final int PUBLIC_FIELDS = 0x0010;

	public static final int NONE = 0;
	public static final int FULL_REFLECTION = (CLASS | PUBLIC_CONSTRUCTORS | PUBLIC_METHODS | PUBLIC_FIELDS);
	public static final int ALL = (RESOURCE | CLASS | PUBLIC_CONSTRUCTORS | PUBLIC_METHODS | PUBLIC_FIELDS);
	public static final int CONFIGURATION = (RESOURCE | CLASS | PUBLIC_METHODS | PUBLIC_CONSTRUCTORS | PUBLIC_FIELDS);
	public static final int EVERYTHING = (RESOURCE | CLASS | PUBLIC_METHODS | PUBLIC_CONSTRUCTORS | PUBLIC_FIELDS);
	public static final int ANNOTATION = (RESOURCE | CLASS | PUBLIC_METHODS);
	public static final int LOAD_AND_CONSTRUCT = (CLASS | PUBLIC_CONSTRUCTORS);

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
			s.append("RESOURCE ");
		}
		if ((value & CLASS) != 0) {
			s.append("CLASS ");
		}
		if ((value & PUBLIC_CONSTRUCTORS) != 0) {
			s.append("CONSTRUCTORS ");
		}
		if ((value & PUBLIC_METHODS) != 0) {
			s.append("METHODS ");
		}
		if ((value & PUBLIC_FIELDS) != 0) {
			s.append("FIELDS ");
		}
		if (value == 0) {
			s.append("NONE");
		}
		return s.toString().trim() + ")";
	}

	public static Flag[] getFlags(int value) {
		List<Flag> flags = new ArrayList<>();
		if ((value & PUBLIC_FIELDS) != 0) {
			flags.add(Flag.allDeclaredFields);
		}
		if ((value & PUBLIC_CONSTRUCTORS) != 0) {
			flags.add(Flag.allDeclaredConstructors);
		}
		if ((value & PUBLIC_METHODS) != 0) {
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
			s.append("RESOURCE ");
		}
		if ((value & CLASS) != 0) {
			s.append("CLASS ");
		}
		if ((value & PUBLIC_CONSTRUCTORS) != 0) {
			s.append("CONSTRUCTORS ");
		}
		if ((value & PUBLIC_METHODS) != 0) {
			s.append("METHODS ");
		}
		if ((value & PUBLIC_FIELDS) != 0) {
			s.append("FIELDS ");
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
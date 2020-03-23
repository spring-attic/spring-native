package org.springframework.graal.type;

import static org.springframework.graal.type.AccessBits.*;

public enum TypeKind {

	CONFIGURATION(RESOURCE,CLASS, PUBLIC_METHODS,PUBLIC_CONSTRUCTORS, PUBLIC_FIELDS), // A Spring @Configuration type
	REGISTRAR(CLASS,PUBLIC_CONSTRUCTORS), //
	NORMAL(CLASS, PUBLIC_METHODS, PUBLIC_CONSTRUCTORS), //
	UNRECOGNIZED(), // Catch all for everything else
	RESOURCE_AND_INSTANTIATION(RESOURCE, CLASS, PUBLIC_CONSTRUCTORS),
	EXISTENCE_CHECK(CLASS), //
	ANNOTATION(RESOURCE, CLASS, PUBLIC_METHODS), //
	EVERYTHING(RESOURCE, CLASS, PUBLIC_CONSTRUCTORS, PUBLIC_METHODS, PUBLIC_FIELDS);

	private AccessBits accessBits;

	private TypeKind(int... bits) {
		accessBits = AccessBits.forBits(bits);
	}

	public AccessBits getAccessBits() {
		return accessBits;
	}

	public boolean isResourceAccessRequired() {
		return accessBits.isResourceAccessRequired();
	}

}

package org.springframework.graal.type;

public @interface CompilationHint {

	// When attached to an annotation, this indicates which fields in that annotation
	// hold type references, e.g. if on ConditionalOnClass, names={"value","name"}
	String[] fieldNames();
	
	// If difficult to tell the types involved from the thing being annotated, the info can be put here
	// (e.g. you have an ImportSelector returning classnames, the possible names should be in here)
	String[] name();
	Class<?>[] value();	
	
	// If true then whatever class is annotated/meta-annotateed with this is useless if
	// the types visible through the names() fields are not found.
	boolean skipIfTypesMissing();
	
	// If true, then whatever types are referenced need to be followed because they may
	// be further annotated/meta-annotated with compilation hints
	boolean follow();
	
	// Do we need to specify what reflection should be accessible? (Fields/Methods/Ctors)?
	// Reducing the amount could likely help the image size
	
	// If true, whatever is (meta-)annotated with this must be accessible via getResource too.
	boolean accessibleAsResource();
}

package org.springframework.graal.type;

import java.util.List;

/**
 * Represents a usage of @CompilationHint.
 * 
 * @author Andy Clement
 */
public class HintDescriptor {
	
	// This is the annotation 'chain' from the type that got asked about to the thing with @CompilationHint
	// This chain may be short (e.g. if an autoconfig has @ConditionalOnClass on it which itself
	// is meta annotated with @CompilationHint): chain will be [ConditionalOnClass]
	// or it may be long: (e.g. if the autoconfig has an @EnableFoo on it which itself is marked
	// with @ConditionalOnClass which in turn has CompilationHint) chain will be [EnableFoo, ConditionalOnClass]
	private List<Type> annotationChain;
	
	// If any types hinted at are missing, is this type effectively redundant?
	private boolean skipIfTypesMissing;

	// Should any types references be followed because they may also have further
	// hints on them (e.g. @Import(Foo) where Foo has @Import(Bar) on it)
	private boolean follow;

	private String[] name;

	public HintDescriptor(List<Type> annotationChain, boolean skipIfTypesMissing2, boolean follow, String[] name) {
		this.annotationChain = annotationChain;
		this.skipIfTypesMissing = skipIfTypesMissing2;
		this.follow = follow;
		this.name = name;
	}

	public List<Type> getAnnotationChain() {
		return annotationChain;
	}

	public boolean isSkipIfTypesMissing() {
		return skipIfTypesMissing;
	}

	public boolean isFollow() {
		return follow;
	}
	
	public String[] getName() {
		return name;
	}

}
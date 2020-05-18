/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.graalvm.type;

import java.util.List;
import java.util.Map;

/**
 * Represents an inferred application of @CompilationHint.
 * 
 * @author Andy Clement
 */
public class Hint {
	
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

	// These are the types pulled directly from the compilation hint (e.g. for ImportSelectors/registrars)
	private Map<String, Integer> specificTypes;

	// These are pulled from the particular application of the hint (e.g. @ConditionalOnClass has a hint and when
	// @COC seen, these are the types pulled from the @COC)
	private Map<String, Integer> inferredTypes;

	public Hint(List<Type> annotationChain, boolean skipIfTypesMissing, 
			boolean follow, Map<String, Integer> specificTypes,Map<String,Integer> inferredTypes) {
		this.annotationChain = annotationChain;
		this.skipIfTypesMissing = skipIfTypesMissing;
		this.follow = follow;
		this.specificTypes = specificTypes;
		this.inferredTypes = inferredTypes;
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
	
	public Map<String,Integer> getSpecificTypes() {
		return specificTypes;
	}
	
	public Map<String, Integer> getInferredTypes() {
		return inferredTypes;
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Hint{");
		s.append(shortChain());
		s.append(",skipIfTypesMissing=").append(skipIfTypesMissing);
		s.append(",follow=").append(follow);
		s.append(",specificTypes=").append(shorten(specificTypes));
		s.append(",inferredTypes=").append(shorten(inferredTypes));
		s.append("}");
		return s.toString();
	}
	
	private String shorten(Map<String, Integer> types) {
		StringBuilder s = new StringBuilder();
		int i=0;
		s.append("[");
		for (Map.Entry<String, Integer> entry: types.entrySet()) {
			if (i>0) {
				s.append(",");
			}
			s.append(shorten(entry.getKey())).append(":").append(AccessBits.toString(entry.getValue()));
			i++;
		}
		s.append("]");
		return s.toString();
	}

	/**
	 * Convert a list of types into a more compact form where package names are single letters.
	 */
	public String shortChain() {
		StringBuilder s = new StringBuilder();
		s.append("[");
		for (int i=0;i<annotationChain.size();i++) {
			Type t = annotationChain.get(i);
			if (i>0) {
				s.append(",");
			}
			s.append(shorten(t.getDottedName())); 
		}
		s.append("]");
		return s.toString();
	}

	private String shorten(String dname) {
		StringBuilder s = new StringBuilder();
		boolean hasDot = dname.contains(".");
		while (dname.contains(".")) {
			s.append(dname.charAt(0));
			dname = dname.substring(dname.indexOf(".")+1);
		}
		if (hasDot) {
			s.append(".");
		}
		s.append(dname);
		return s.toString();
	}

}
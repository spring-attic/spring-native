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

package org.springframework.nativex.type;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.hint.AccessBits;

/**
 * Represents a real application of a @type {@link HintDeclaration}. This is different
 * because it includes inferred information from the matched location (for example
 * if the HintDeclaration was about uses of ConditionalOnClass, the HintApplication will
 * include the types scraped from a particular ConditionalOnClass usage).
 * 
 * @author Andy Clement
 */
public class HintApplication {
	
	/* 
	 * This is the annotation 'chain' from the type that got asked about to the entity that actually
	 * has the @NativeHint against it.
	 * This chain may be short: for example if a configuration class has @ConditionalOnClass on it which itself
	 * has a @NativeHint applicable to it: the chain will be [ConditionalOnClass]
	 * or it may be long: for example if a configuration has an @EnableFoo on it which itself is marked
	 * with @ConditionalOnClass which in turn has a @NativeHint applicable to it: the chain will be
	 * [EnableFoo, ConditionalOnClass]
	 */
	private List<Type> annotationChain;
	
	// These are pulled from the particular application of the hint (e.g. @ConditionalOnClass has a hint and when
	// @ConditionalOnClass is seen, these are the types pulled from the @ConditionalOnClass - i.e. the types
	// upon which it is being conditional)
	private Map<String, Integer> inferredTypes;
	
	private HintDeclaration hintDeclaration;

	public HintApplication(List<Type> annotationChain,
			Map<String,Integer> inferredTypes,
			HintDeclaration hintDeclaration) {
		this.annotationChain = annotationChain;
		this.inferredTypes = inferredTypes;
		this.hintDeclaration = hintDeclaration;
	}

	public List<Type> getAnnotationChain() {
		return annotationChain;
	}

	public boolean isSkipIfTypesMissing() {
		return hintDeclaration.skipIfTypesMissing;
	}

	public boolean isFollow() {
		return hintDeclaration.follow;
	}
	
	public Map<String,AccessDescriptor> getSpecificTypes() {
		return hintDeclaration.getDependantTypes();
	}
	
	public Map<String, Integer> getInferredTypes() {
		return inferredTypes;
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Hint{");
		s.append(shortChain());
		if (!getSpecificTypes().isEmpty()) {
			s.append(",specific=").append(shortenWithAD(getSpecificTypes()));
		}
		if (!getInferredTypes().isEmpty()) {
			s.append(",inferred=").append(shorten(inferredTypes));
		}
		if (!getResourceDescriptors().isEmpty()) {
			s.append(",resources=").append(getResourceDescriptors());
		}
		if (!getInitializationDescriptors().isEmpty()) {
			s.append(",initialization=").append(getInitializationDescriptors());
		}
		if (!getProxyDescriptors().isEmpty()) {
			s.append(",proxies=").append(getProxyDescriptors());
		}
//		s.append(",skipIfTypesMissing=").append(skipIfTypesMissing);
//		s.append(",follow=").append(follow);
		s.append("}");
		return s.toString();
	}
	
	private String shortenWithAD(Map<String, AccessDescriptor> types) {
		StringBuilder s = new StringBuilder();
		int i=0;
		s.append("[");
		for (Map.Entry<String, AccessDescriptor> entry: types.entrySet()) {
			if (i>0) {
				s.append(",");
			}
			s.append(shorten(entry.getKey())).append(":").append(entry.getValue().toString());
			i++;
		}
		s.append("]");
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
	 * @return the more compact form
	 */
	public String shortChain() {
		StringBuilder s = new StringBuilder();
		s.append("[");
		// Not including the first entry in the chain because it is always the target on which
		// the hint was identified
		for (int i=1;i<annotationChain.size();i++) {
			Type t = annotationChain.get(i);
			if (i>1) {
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
	
	public List<ResourcesDescriptor> getResourceDescriptors() {
		return hintDeclaration.getResourcesDescriptors();
	}

	public List<JdkProxyDescriptor> getProxyDescriptors() {
		return hintDeclaration.getProxyDescriptors();
	}

	public List<InitializationDescriptor> getInitializationDescriptors() {
		return hintDeclaration.getInitializationDescriptors();
	}
	
	public Set<String> getOptions() {
		return hintDeclaration.getOptions();
	}

	public Set<String> getSerializationTypes() {
		return hintDeclaration.getSerializationTypes();
	}

	public Map<String,AccessDescriptor> getJNITypes() {
		return hintDeclaration.getJNITypes();
	}

}
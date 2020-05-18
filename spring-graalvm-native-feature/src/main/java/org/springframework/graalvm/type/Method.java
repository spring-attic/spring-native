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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.springframework.graalvm.support.SpringFeature;

public class Method {
	
	private MethodNode mn;
	private TypeSystem typeSystem;

	public Method(MethodNode mn, TypeSystem ts) {
		this.mn = mn;
		this.typeSystem = ts;
	}
	
	public String toString() {
		return mn.name+mn.desc;
	}

	public String getName() {
		return mn.name;
	}

	public String getDesc() {
		return mn.desc;
	}

	public List<Hint> getHints() {
		List<Hint> hints = new ArrayList<>();
		if (mn.visibleAnnotations != null) {
			for (AnnotationNode an: mn.visibleAnnotations) {
				Type annotationType = typeSystem.Lresolve(an.desc, true);
				if (annotationType == null) {
					SpringFeature.log("Couldn't resolve "+an.desc+" annotation type whilst searching for hints on "+getName());
				} else {
					Stack<Type> s = new Stack<>();
					// s.push(this);
					annotationType.collectHints(an, hints, new HashSet<>(), s);
				}
			}
		}
		return hints.size()==0?Collections.emptyList():hints;
	}
	
	public List<Type> getAnnotationTypes() {
		List<Type> results = null;
		if (mn.visibleAnnotations!= null) {
			for (AnnotationNode an: mn.visibleAnnotations) {
				Type annotationType = typeSystem.Lresolve(an.desc, true);
				if (annotationType != null) {
					if (results == null) {
						results = new ArrayList<>();
					}
					results.add(annotationType);
				}
			}
		}
		return results == null? Collections.emptyList():results;
	}
	
	static class TypesFromSignatureCollector extends SignatureVisitor {

		Set<String> types = null;
		
		public TypesFromSignatureCollector() {
			super(Opcodes.ASM7);
		}
		
		@Override
		public void visitClassType(String name) {
			if (types == null) {
				types = new HashSet<String>();
			}
			types.add(name);
		}
		
		public Set<String> getTypes() {
			if (types == null) {
				return Collections.emptySet();
			} else {
				return types;
			}
		}
				
	}

	/**
	 * @return full list of types involved in the signature, including those embedded in generics.
	 */
	public Set<Type> getSignatureTypes() {
		Set<Type> signatureTypes = new HashSet<>();
		if (mn.signature == null) {
			org.objectweb.asm.Type methodType = org.objectweb.asm.Type.getMethodType(mn.desc);
			Type t = typeSystem.resolve(methodType.getReturnType(), true);
			if (t == null) {
				SpringFeature.log("Can't resolve the type used in this @Bean method: "+mn.name+mn.desc+": "+methodType.getDescriptor());
			} else {
				signatureTypes.add(t);
			}
			for (org.objectweb.asm.Type at: methodType.getArgumentTypes()) {
				t = typeSystem.resolve(methodType.getReturnType(), true);
				if (t == null) {
					SpringFeature.log("Can't resolve the type used in this @Bean method: "+mn.name+mn.desc+": "+at.getDescriptor());
				} else {
					signatureTypes.add(t);
				}	
			}
		} else {
			SignatureReader reader = new SignatureReader(mn.signature);
			TypesFromSignatureCollector tc = new TypesFromSignatureCollector();
			reader.accept(tc);
			Set<String> collectedTypes = tc.getTypes();
			for (String s: collectedTypes) {
				Type t = typeSystem.resolveDotted(s,true);
				if (t == null) {
					SpringFeature.log("Can't resolve the type used in this @Bean method: "+mn.name+mn.desc+": "+s);
				} else {
					signatureTypes.add(t);
				}
			}
		}
		return signatureTypes;
	}

	public Type getReturnType() {
		org.objectweb.asm.Type methodType = org.objectweb.asm.Type.getMethodType(mn.desc);
		Type returnType = typeSystem.resolve(methodType.getReturnType(), true);	
		return returnType;
	}
	

	public List<Type> getParameterTypes() {
		List<Type> results = null;
		org.objectweb.asm.Type[] parameterTypes = org.objectweb.asm.Type.getArgumentTypes(mn.desc);
		if (parameterTypes!=null) {
			for (org.objectweb.asm.Type t: parameterTypes) {
				if (results == null) {
					results = new ArrayList<>();
				}
				results.add(typeSystem.resolve(t,true));
			}
		}
		return results==null?Collections.emptyList():results;
	}


}
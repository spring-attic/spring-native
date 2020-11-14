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

import java.lang.reflect.Modifier;
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
	
	private static org.objectweb.asm.Type[] NONE = new org.objectweb.asm.Type[0];

	private MethodNode mn;

	private TypeSystem typeSystem;

	private boolean unresolvableParams = false;

	private List<Type> resolvedParameters;

	private org.objectweb.asm.Type[] internalParameterTypes = null;

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

	public List<HintApplication> getHints() {
		List<HintApplication> hints = new ArrayList<>();
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
	
	public List<Type> getParameterAnnotationTypes(int parameterIndex) {
		List<Type> results = null;
		//System.out.println("Method is "+toString()+
		//		" pi = "+parameterIndex+
		//		" total = "+(mn.visibleParameterAnnotations==null?"null":mn.visibleParameterAnnotations.length));
		if (mn.visibleParameterAnnotations!= null) {
			if (parameterIndex < mn.visibleParameterAnnotations.length) {
				List<AnnotationNode> pas = mn.visibleParameterAnnotations[parameterIndex];
				if (pas != null) {
					for (AnnotationNode an: pas) {
						Type annotationType = typeSystem.Lresolve(an.desc, true);
						if (annotationType != null) {
							if (results == null) {
								results = new ArrayList<>();
							}
							results.add(annotationType);
						}
					}
				}
			}
		}
		return results == null? Collections.emptyList():results;
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
		private boolean returnTypeOnly;
		private boolean captureTypes = false;
		
		public TypesFromSignatureCollector(boolean returnTypeOnly) {
			super(Opcodes.ASM9);
			this.returnTypeOnly = returnTypeOnly;
			if (!returnTypeOnly) {
				this.captureTypes=true;
			}
		}
		
		@Override
		public SignatureVisitor visitReturnType() {
			if (this.returnTypeOnly) {
				captureTypes = true;
			}	
			return super.visitReturnType();
		}
		
		@Override
		public SignatureVisitor visitExceptionType() {
			if (this.returnTypeOnly) {
				captureTypes = false;
			}
			return super.visitExceptionType();
		}
		
		@Override
		public SignatureVisitor visitParameterType() {
			if (this.returnTypeOnly) {
				captureTypes = false;
			}
			return super.visitParameterType();
		}
		
		@Override
		public void visitClassType(String name) {
			if (captureTypes) {
				if (types == null) {
					types = new HashSet<String>();
				}
				types.add(name);
			}
		}
		
		public Set<String> getTypes() {
			if (types == null) {
				return Collections.emptySet();
			} else {
				return types;
			}
		}
				
	}

	public Set<Type> getSignatureTypes() {
		return getSignatureTypes(false);
	}

	/**
	 * @return full list of types involved in the signature, including those embedded in generics.
	 */
	public Set<Type> getSignatureTypes(boolean returnTypeOnly) {
		Set<Type> signatureTypes = new HashSet<>();
		if (mn.signature == null) {
			org.objectweb.asm.Type methodType = org.objectweb.asm.Type.getMethodType(mn.desc);
			org.objectweb.asm.Type returnType = methodType.getReturnType();
			Type t = null;
			if (returnType.getDescriptor().length()!=1) {
				t = typeSystem.resolve(methodType.getReturnType(), true);
				if (t == null) {
					SpringFeature.log("Can't resolve the type used in this @Bean method: "+mn.name+mn.desc+": "+methodType.getDescriptor());
				} else {
					signatureTypes.add(t);
				}
			}
			if (!returnTypeOnly) {
				for (org.objectweb.asm.Type at : methodType.getArgumentTypes()) {
					if (at.getDescriptor().length()!=1) {
						t = typeSystem.resolve(methodType.getReturnType(), true);
						if (t == null) {
							SpringFeature.log("Can't resolve the type used in this @Bean method: " + mn.name + mn.desc
									+ ": " + at.getDescriptor());
						} else {
							signatureTypes.add(t);
						}
					}
				}
			}
		} else {
			SignatureReader reader = new SignatureReader(mn.signature);
			TypesFromSignatureCollector tc = new TypesFromSignatureCollector(returnTypeOnly);
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

	public boolean hasUnresolvableTypesInSignature() {
		Set<Type> signatureTypes = new HashSet<>();
		try {
			if (mn.signature == null) {
				org.objectweb.asm.Type methodType = org.objectweb.asm.Type.getMethodType(mn.desc);
				org.objectweb.asm.Type returnType = methodType.getReturnType();
				Type t = null;
				if (returnType.getDescriptor().length() != 1) {
					t = typeSystem.resolve(methodType.getReturnType(),false);
					signatureTypes.add(t);
				}
				for (org.objectweb.asm.Type at : methodType.getArgumentTypes()) {
					if (at.getDescriptor().length() != 1) {
						t = typeSystem.resolve(methodType.getReturnType(),false);
						signatureTypes.add(t);
					}
				}
			} else {
				SignatureReader reader = new SignatureReader(mn.signature);
				TypesFromSignatureCollector tc = new TypesFromSignatureCollector(false);
				reader.accept(tc);
				Set<String> collectedTypes = tc.getTypes();
				for (String s : collectedTypes) {
					Type t = typeSystem.resolveDotted(s, false);
					signatureTypes.add(t);
				}
			}
		} catch (MissingTypeException mte) {
			// System.out.println("Unresolvable: " + mte.getMessage());
			return true;
		}
		return false;
	}

	public Type getReturnType() {
		org.objectweb.asm.Type methodType = org.objectweb.asm.Type.getMethodType(mn.desc);
		Type returnType = typeSystem.resolve(methodType.getReturnType(), true);	
		return returnType;
	}
	
	
	private org.objectweb.asm.Type[] resolveInternalParameterTypes() {
		if (internalParameterTypes == null) {
			internalParameterTypes = org.objectweb.asm.Type.getArgumentTypes(mn.desc);
			if (internalParameterTypes == null) {
				internalParameterTypes = NONE;
			}
		}
		return internalParameterTypes;
	}
	
	public List<Type> getParameterTypes() {
		if (resolvedParameters == null) {
			List<Type> results = null;
			org.objectweb.asm.Type[] parameterTypes = resolveInternalParameterTypes();
			if (parameterTypes != null) {
				for (org.objectweb.asm.Type t : parameterTypes) {
					if (results == null) {
						results = new ArrayList<>();
					}
					Type ptype = typeSystem.resolve(t, true);
					if (ptype == null) {
						SpringFeature.log("WARNING: method has unresolvable parameters: " + mn.name + mn.desc);
						unresolvableParams = true;
					}
					results.add(ptype);
				}
			}
			resolvedParameters = (results == null ? Collections.emptyList() : results);
		}
		return resolvedParameters;
	}

	public boolean isAtMapping() {
		if (mn.visibleAnnotations!= null) {
			for (AnnotationNode an: mn.visibleAnnotations) {
				if (an.desc.equals(Type.AtMapping)) {
					return true;
				}
				Type annotationType = typeSystem.Lresolve(an.desc, true);
				boolean metaUsage = annotationType.isMetaAnnotated(Type.fromLdescriptorToSlashed(Type.AtMapping));
				if (metaUsage) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasAnnotation(String annotationDescriptor, boolean checkMetaUsage) {
		if (mn.visibleAnnotations != null) {
			for (AnnotationNode an: mn.visibleAnnotations) {
				if (an.desc.equals(annotationDescriptor)) {
					return true;
				}
				if (checkMetaUsage) {
					Type annotationType = typeSystem.Lresolve(an.desc, true);
					boolean metaUsage = annotationType.isMetaAnnotated(Type.fromLdescriptorToSlashed(Type.AtMapping));
					if (metaUsage) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public int getParameterCount() {
		return resolveInternalParameterTypes().length;
	}

	public boolean hasAliasForAnnotation() {
		if (mn.visibleAnnotations!= null) {
			for (AnnotationNode an: mn.visibleAnnotations) {
				if (an.desc.equals(Type.AtAliasFor)) {
					return true;
				}
			}
		}
		return false;
	}

	// AliasFor attributes: value(String)==attribute(String), annotation(Class)
	/**
	 * Check for an @AliasFor on this method. If one exists check if it specifies a
	 * value for the 'value' or 'attribute' or 'annotation' fields. The result will be null
	 * if there is no @AliasFor otherwise it will be a pair containing the name of the type
	 * specified for 'annotation' (may be null if not set) and true/false depending on whether
	 * a value was set for 'value' or 'attribute'.
	 */
	public Pair<String,Boolean> getAliasForSummary() {
		AnnotationNode aliasForAnnotation = getAliasForAnnotation();
		if (aliasForAnnotation != null) {
			List<Object> values = aliasForAnnotation.values;
			boolean namedAttribute = false;
			String targetAnnotationType = null;
			for (int i=0;i<values.size();i+=2) {
				switch ((String)values.get(i)) {
				case "value":
				case "attribute":
					namedAttribute = true;
					break;
				case "annotation":
					targetAnnotationType = ((org.objectweb.asm.Type)values.get(i+1)).getClassName();
					break;
				}
			}
			return Pair.of(targetAnnotationType, namedAttribute);
		}
		return null;
	}

	
	
	private AnnotationNode getAliasForAnnotation() {
		if (mn.visibleAnnotations!= null) {
			for (AnnotationNode an: mn.visibleAnnotations) {
				if (an.desc.equals(Type.AtAliasFor)) {
					return an;
				}
			}
		}
		return null;
	}

	public boolean hasAnnotations() {
		return mn.visibleAnnotations!=null;
	}
	
	public boolean hasUnresolvableParams() {
		getParameterTypes();
		return unresolvableParams;
	}

	public String[] asConfigurationArray() {
		int p = -1;
		try {
			List<Type> params = getParameterTypes();
			String[] output = new String[params.size() + 1];
			output[0] = getName();
			for (p = 0; p < params.size(); p++) {
				output[p + 1] = params.get(p).getDottedName();
			}
			return output;
		} catch (NullPointerException npe) {
			throw new IllegalStateException("Problem producing array for " + mn.name + mn.desc + "  (param #" + p + ")");
		}
	}
	
	public boolean isPublic() {
		return Modifier.isPublic(mn.access);
	}

	public boolean markedAtBean() {
		return hasAnnotation(Type.AtBean, false);
	}

	public boolean isPrivate() {
		return Modifier.isPrivate(mn.access);
	}

}
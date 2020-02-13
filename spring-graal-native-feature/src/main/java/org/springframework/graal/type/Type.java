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
package org.springframework.graal.type;

import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.ConfigurationHints;
import org.springframework.graal.support.SpringFeature;

/**
 * @author Andy Clement
 */
public class Type {
	
	public final static String AtBean = "Lorg/springframework/context/annotation/Bean;";
 	public final static String AtConditionalOnClass = "Lorg/springframework/boot/autoconfigure/condition/ConditionalOnClass;";
	public final static String AtConditionalOnMissingBean = "Lorg/springframework/boot/autoconfigure/condition/ConditionalOnMissingBean;";
	public final static String AtConfiguration = "Lorg/springframework/context/annotation/Configuration;";
	public final static String AtEnableConfigurationProperties = "Lorg/springframework/boot/context/properties/EnableConfigurationProperties;";
	public final static String AtImports = "Lorg/springframework/context/annotation/Import;";
	public final static String ImportBeanDefinitionRegistrar ="Lorg/springframework/context/annotation/ImportBeanDefinitionRegistrar;";
	public final static String ImportSelector ="Lorg/springframework/context/annotation/ImportSelector;";
	public final static String Condition = "Lorg/springframework/context/annotation/Condition;";

	
	public final static Type MISSING = new Type(null, null);

	public final static Type[] NO_INTERFACES = new Type[0];

	protected static Set<String> validBoxing = new HashSet<String>();

	private TypeSystem typeSystem;

	private ClassNode node;
	
	private Type[] interfaces;


	public Type(TypeSystem typeSystem, ClassNode node) {
		this.typeSystem = typeSystem;
		this.node = node;
	}

	public static Type forClassNode(TypeSystem typeSystem, ClassNode node) {
		return new Type(typeSystem, node);
	}

	/**
	 * @return typename in slashed form (aaa/bbb/ccc/Ddd$Eee)
	 */
	public String getName() {
		return node.name;
	}

	public String getDottedName() {
		return node.name.replace("/", ".");
	}

	public Type getSuperclass() {
		if (node.superName == null) {
			return null;
		}
		return typeSystem.resolveSlashed(node.superName);
	}
	
	@Override
	public String toString() {
		return "Type:"+getName();
	}

	public Type[] getInterfaces() {
		if (interfaces == null) {
			List<String> itfs = node.interfaces;
			if (itfs.size() == 0) {
				interfaces = NO_INTERFACES;
			} else {
				interfaces = new Type[itfs.size()];
				for (int i = 0; i < itfs.size(); i++) {
					interfaces[i] = typeSystem.resolveSlashed(itfs.get(i));
				}
			}
		}
		return interfaces;
	}
	
	/** @return List of slashed interface types */
	public List<String> getInterfacesStrings() {
		return node.interfaces;
	}

	/** @return slashed supertype name */
	public String getSuperclassString() {
		return node.superName;
	}
	
	public List<String> getTypesInSignature() {
		if (node.signature == null) {
			return Collections.emptyList();
		} else {
			SignatureReader reader = new SignatureReader(node.signature);
			TypeCollector tc = new TypeCollector();
			reader.accept(tc);
			return tc.getTypes();
		}
	}
	
	static class TypeCollector extends SignatureVisitor {

		List<String> types = null;
		
		public TypeCollector() {
			super(Opcodes.ASM7);
		}
		
		@Override
		public void visitClassType(String name) {
			if (types == null) {
				types = new ArrayList<String>();
			}
			types.add(name);
		}
		
		public List<String> getTypes() {
			if (types == null) {
				return Collections.emptyList();
			} else {
				return types;
			}
		}
				
	}
	
	public boolean extendsClass(String clazzname) {
		Type superclass = getSuperclass();
		while (superclass != null) {
			if (superclass.getDescriptor().equals(clazzname)) {
				return true;
			}
			superclass = superclass.getSuperclass();
		}
		return false;
	}

	public boolean implementsInterface(String interfaceName) {
		Type[] interfacesToCheck = getInterfaces();
		for (Type interfaceToCheck : interfacesToCheck) {
			if (interfaceToCheck != null) {
				if (interfaceToCheck.getName().equals(interfaceName)) {
					return true;
				}
				if (interfaceToCheck.implementsInterface(interfaceName)) {
					return true;
				}
			}
		}
		Type superclass = getSuperclass();
		while (superclass != null) {
			if (superclass.implementsInterface(interfaceName)) {
				return true;
			}
			superclass = superclass.getSuperclass();
		}
		return false;
	}

	public List<Method> getMethodsWithAnnotation(String string) {
		return node.methods.stream().filter(m -> hasAnnotation(m, string)).map(m -> wrap(m))
				.collect(Collectors.toList());
	}
	
	public List<Method> getMethodsWithAtBean() {
		return getMethodsWithAnnotation(AtBean);
	}

	public Method wrap(MethodNode mn) {
		return new Method(mn,typeSystem);
	}

	private boolean hasAnnotation(MethodNode m, String string) {
		List<AnnotationNode> visibleAnnotations = m.visibleAnnotations;
		Optional<AnnotationNode> findAny = visibleAnnotations == null ? Optional.empty()
				: visibleAnnotations.stream().filter(a -> a.desc.equals(string)).findFirst();
		return findAny.isPresent();
	}

	static {
		validBoxing.add("Ljava/lang/Byte;B");
		validBoxing.add("Ljava/lang/Character;C");
		validBoxing.add("Ljava/lang/Double;D");
		validBoxing.add("Ljava/lang/Float;F");
		validBoxing.add("Ljava/lang/Integer;I");
		validBoxing.add("Ljava/lang/Long;J");
		validBoxing.add("Ljava/lang/Short;S");
		validBoxing.add("Ljava/lang/Boolean;Z");
		validBoxing.add("BLjava/lang/Byte;");
		validBoxing.add("CLjava/lang/Character;");
		validBoxing.add("DLjava/lang/Double;");
		validBoxing.add("FLjava/lang/Float;");
		validBoxing.add("ILjava/lang/Integer;");
		validBoxing.add("JLjava/lang/Long;");
		validBoxing.add("SLjava/lang/Short;");
		validBoxing.add("ZLjava/lang/Boolean;");
	}

	public boolean isAssignableFrom(Type other) {
		if (other.isPrimitiveType()) {
			if (validBoxing.contains(this.getName() + other.getName())) {
				return true;
			}
		}
		if (this == other) {
			return true;
		}

		if (this.getName().equals("Ljava/lang/Object;")) {
			return true;
		}

//		if (!isTypeVariableReference()
//				&& other.getSignature().equals("Ljava/lang/Object;")) {
//			return false;
//		}

//		boolean thisRaw = this.isRawType();
//		if (thisRaw && other.isParameterizedOrGenericType()) {
//			return isAssignableFrom(other.getRawType());
//		}
//
//		boolean thisGeneric = this.isGenericType();
//		if (thisGeneric && other.isParameterizedOrRawType()) {
//			return isAssignableFrom(other.getGenericType());
//		}
//
//		if (this.isParameterizedType()) {
//			// look at wildcards...
//			if (((ReferenceType) this.getRawType()).isAssignableFrom(other)) {
//				boolean wildcardsAllTheWay = true;
//				ResolvedType[] myParameters = this.getResolvedTypeParameters();
//				for (int i = 0; i < myParameters.length; i++) {
//					if (!myParameters[i].isGenericWildcard()) {
//						wildcardsAllTheWay = false;
//					} else {
//						BoundedReferenceType boundedRT = (BoundedReferenceType) myParameters[i];
//						if (boundedRT.isExtends() || boundedRT.isSuper()) {
//							wildcardsAllTheWay = false;
//						}
//					}
//				}
//				if (wildcardsAllTheWay && !other.isParameterizedType()) {
//					return true;
//				}
//				// we have to match by parameters one at a time
//				ResolvedType[] theirParameters = other
//						.getResolvedTypeParameters();
//				boolean parametersAssignable = true;
//				if (myParameters.length == theirParameters.length) {
//					for (int i = 0; i < myParameters.length
//							&& parametersAssignable; i++) {
//						if (myParameters[i] == theirParameters[i]) {
//							continue;
//						}
//						// dont do this: pr253109
//						// if
//						// (myParameters[i].isAssignableFrom(theirParameters[i],
//						// allowMissing)) {
//						// continue;
//						// }
//						ResolvedType mp = myParameters[i];
//						ResolvedType tp = theirParameters[i];
//						if (mp.isParameterizedType()
//								&& tp.isParameterizedType()) {
//							if (mp.getGenericType().equals(tp.getGenericType())) {
//								UnresolvedType[] mtps = mp.getTypeParameters();
//								UnresolvedType[] ttps = tp.getTypeParameters();
//								for (int ii = 0; ii < mtps.length; ii++) {
//									if (mtps[ii].isTypeVariableReference()
//											&& ttps[ii]
//													.isTypeVariableReference()) {
//										TypeVariable mtv = ((TypeVariableReferenceType) mtps[ii])
//												.getTypeVariable();
//										boolean b = mtv
//												.canBeBoundTo((ResolvedType) ttps[ii]);
//										if (!b) {// TODO incomplete testing here
//													// I think
//											parametersAssignable = false;
//											break;
//										}
//									} else {
//										parametersAssignable = false;
//										break;
//									}
//								}
//								continue;
//							} else {
//								parametersAssignable = false;
//								break;
//							}
//						}
//						if (myParameters[i].isTypeVariableReference()
//								&& theirParameters[i].isTypeVariableReference()) {
//							TypeVariable myTV = ((TypeVariableReferenceType) myParameters[i])
//									.getTypeVariable();
//							// TypeVariable theirTV =
//							// ((TypeVariableReferenceType)
//							// theirParameters[i]).getTypeVariable();
//							boolean b = myTV.canBeBoundTo(theirParameters[i]);
//							if (!b) {// TODO incomplete testing here I think
//								parametersAssignable = false;
//								break;
//							} else {
//								continue;
//							}
//						}
//						if (!myParameters[i].isGenericWildcard()) {
//							parametersAssignable = false;
//							break;
//						} else {
//							BoundedReferenceType wildcardType = (BoundedReferenceType) myParameters[i];
//							if (!wildcardType.alwaysMatches(theirParameters[i])) {
//								parametersAssignable = false;
//								break;
//							}
//						}
//					}
//				} else {
//					parametersAssignable = false;
//				}
//				if (parametersAssignable) {
//					return true;
//				}
//			}
//		}
//
//		// eg this=T other=Ljava/lang/Object;
//		if (isTypeVariableReference() && !other.isTypeVariableReference()) {
//			TypeVariable aVar = ((TypeVariableReference) this)
//					.getTypeVariable();
//			return aVar.resolve(world).canBeBoundTo(other);
//		}
//
//		if (other.isTypeVariableReference()) {
//			TypeVariableReferenceType otherType = (TypeVariableReferenceType) other;
//			if (this instanceof TypeVariableReference) {
//				return ((TypeVariableReference) this)
//						.getTypeVariable()
//						.resolve(world)
//						.canBeBoundTo(
//								otherType.getTypeVariable().getFirstBound()
//										.resolve(world));// pr171952
//				// return
//				// ((TypeVariableReference)this).getTypeVariable()==otherType
//				// .getTypeVariable();
//			} else {
//				// FIXME asc should this say canBeBoundTo??
//				return this.isAssignableFrom(otherType.getTypeVariable()
//						.getFirstBound().resolve(world));
//			}
//		}

		Type[] interfaces = other.getInterfaces();
		for (Type intface : interfaces) {
			boolean b;
//			if (thisRaw && intface.isParameterizedOrGenericType()) {
//				b = this.isAssignableFrom(intface.getRawType(), allowMissing);
//			} else {
			b = this.isAssignableFrom(intface);
//			}
			if (b) {
				return true;
			}
		}
		Type superclass = other.getSuperclass();
		if (superclass != null) {
			boolean b;
//			if (thisRaw && superclass.isParameterizedOrGenericType()) {
//				b = this.isAssignableFrom(superclass.getRawType(), allowMissing);
//			} else {
			b = this.isAssignableFrom(superclass);
//			}
			if (b) {
				return true;
			}
		}
		return false;
	}

	private boolean isPrimitiveType() {
		return false;
	}

	public boolean isInterface() {
		return Modifier.isInterface(node.access);
	}
	
	public Map<String,String> getAnnotationValuesInHierarchy(String LdescriptorLookingFor) {
		Map<String,String> collector = new HashMap<>();
		getAnnotationValuesInHierarchy(LdescriptorLookingFor, new ArrayList<>(), collector);
		return collector;
	}

	public void getAnnotationValuesInHierarchy(String lookingFor, List<String> seen,Map<String,String> collector) {
		if (node.visibleAnnotations != null) {
			for (AnnotationNode anno : node.visibleAnnotations) {
				if (seen.contains(anno.desc))
					continue;
				seen.add(anno.desc);
				//System.out.println("Comparing "+anno.desc+" with "+lookingFor);
				if (anno.desc.equals(lookingFor)) {
					List<Object> os =anno.values;
					for (int i=0;i<os.size();i+=2) {
						collector.put(os.get(i).toString(), os.get(i+1).toString());
					}
				}
				try {
					Type resolve = typeSystem.Lresolve(anno.desc);
					resolve.getAnnotationValuesInHierarchy(lookingFor, seen,collector);
				} catch (MissingTypeException mte) {
					// not on classpath, that's ok
				}
			}
		}
	}

	public boolean hasAnnotationInHierarchy(String lookingFor) {
		return hasAnnotationInHierarchy(lookingFor, new ArrayList<String>());
	}

	public boolean hasAnnotationInHierarchy(String lookingFor, List<String> seen) {
		if (node.visibleAnnotations != null) {
			for (AnnotationNode anno : node.visibleAnnotations) {
				if (seen.contains(anno.desc))
					continue;
				seen.add(anno.desc);
				// System.out.println("Comparing "+anno.desc+" with "+lookingFor);
				if (anno.desc.equals(lookingFor)) {
					return true;
				}
				try {
					Type resolve = typeSystem.Lresolve(anno.desc);
					if (resolve.hasAnnotationInHierarchy(lookingFor, seen)) {
						return true;
					}
				} catch (MissingTypeException mte) {
					// not on classpath, that's ok
				}
			}
		}
		return false;
	}
	
	public boolean isCondition() {
		try {
			return implementsInterface(fromLdescriptorToSlashed(Condition));
		} catch (MissingTypeException mte) {
			return false;
		}
	}
	
	private boolean isEventListener() {
		try {
			return implementsInterface("org/springframework/context/event/EventListenerFactory");
		} catch (MissingTypeException mte) {
			return false;
		}
		
	}

	
	public boolean isAtConfiguration() {
		return isMetaAnnotated(fromLdescriptorToSlashed(AtConfiguration), new HashSet<>());
	}
	
	public boolean isAbstractNestedCondition() {
		return extendsClass("Lorg/springframework/boot/autoconfigure/condition/AbstractNestedCondition;");
	}

	public boolean isMetaAnnotated(String slashedTypeDescriptor) {
		return isMetaAnnotated(slashedTypeDescriptor, new HashSet<>());
	}

	public boolean isMetaAnnotated(String slashedTypeDescriptor, Set<String> seen) {
//		System.out.println("Looking at "+this.getName()+" for "+slashedTypeDescriptor);
		for (Type t : this.getAnnotations()) {
			String tname = t.getName();
			if (tname.equals(slashedTypeDescriptor)) {
				return true;
			}
			if (!seen.contains(tname)) {
				seen.add(tname);
				if (t.isMetaAnnotated(slashedTypeDescriptor, seen)) {
					return true;
				}
			}
		}
		return false;
	}

	List<Type> annotations = null;

	public static final List<Type> NO_ANNOTATIONS = Collections.emptyList();

	
	public List<String> findConditionalOnMissingBeanValue() {
		 List<String> findAnnotationValue = findAnnotationValue(AtConditionalOnMissingBean, false);
		 if (findAnnotationValue==null) {
			 if (node.visibleAnnotations != null) {
					for (AnnotationNode an : node.visibleAnnotations) {
						if (an.desc.equals(AtConditionalOnMissingBean)) {
							System.out.println("??? found nothing on this @COC annotated thing "+this.getName());
						}
					}
				}
		 }
		 return findAnnotationValue;
	}

	public List<String> findConditionalOnClassValue() {
		 List<String> findAnnotationValue = findAnnotationValue(AtConditionalOnClass, false);
		 if (findAnnotationValue==null) {
			 if (node.visibleAnnotations != null) {
					for (AnnotationNode an : node.visibleAnnotations) {
						if (an.desc.equals(AtConditionalOnClass)) {
							System.out.println("??? found nothing on this @COC annotated thing "+this.getName());
						}
					}
				}
		 }
		 return findAnnotationValue;
	}
	
	public List<String> findEnableConfigurationPropertiesValue() {
		 List<String> values = findAnnotationValue(AtEnableConfigurationProperties, false);
		 return values;
	}

	public Map<String,List<String>> findImports() {
		 return findAnnotationValueWithHostAnnotation(AtImports, true, new HashSet<>());
	}
		
	public List<String> findAnnotationValue(String annotationType, boolean searchMeta) {		
		return findAnnotationValue(annotationType, searchMeta, new HashSet<>());
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,List<String>> findAnnotationValueWithHostAnnotation(String annotationType, boolean searchMeta, Set<String> visited) {		
		if (!visited.add(this.getName())) {
			return Collections.emptyMap();
		}
		Map<String,List<String>> collectedResults = new LinkedHashMap<>();
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				if (an.desc.equals(annotationType)) {
					List<Object> values = an.values;
					if (values != null) {
						for (int i=0;i<values.size();i+=2) {
							if (values.get(i).equals("value")) {
								 List<String> importedReferences = ((List<org.objectweb.asm.Type>)values.get(i+1))
										.stream()
										.map(t -> t.getDescriptor())
										.collect(Collectors.toList());
								collectedResults.put(this.getName().replace("/", "."), importedReferences);
							}
						}
					}
				}
			}
			if (searchMeta) {
				for (AnnotationNode an: node.visibleAnnotations) {
					// For example @EnableSomething might have @Import on it
					Type annoType = null;
					try {
						annoType = typeSystem.Lresolve(an.desc);
					} catch (MissingTypeException mte) { 
						System.out.println("SBG: WARNING: Unable to find "+an.desc+" skipping...");
						continue;
					}
					collectedResults.putAll(annoType.findAnnotationValueWithHostAnnotation(annotationType, searchMeta, visited));
				}
			}
		}
		return collectedResults;
	}
	

	@SuppressWarnings("unchecked")
	public List<String> findAnnotationValue(String annotationType, boolean searchMeta, Set<String> visited) {		
		if (!visited.add(this.getName())) {
			return Collections.emptyList();
		}
		List<String> collectedResults = new ArrayList<>();
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				if (an.desc.equals(annotationType)) {
					List<Object> values = an.values;
					if (values != null) {
						for (int i=0;i<values.size();i+=2) {
							if (values.get(i).equals("value")) {
								return ( (List<org.objectweb.asm.Type>)values.get(i+1))
										.stream()
										.map(t -> t.getDescriptor())
										.collect(Collectors.toCollection(() -> collectedResults));
							}
						}
					}
				}
			}
			if (searchMeta) {
				for (AnnotationNode an: node.visibleAnnotations) {
					// For example @EnableSomething might have @Import on it
					Type annoType = typeSystem.Lresolve(an.desc);
					collectedResults.addAll(annoType.findAnnotationValue(annotationType, searchMeta, visited));
				}
			}
		}
		return collectedResults;
	}

	private List<Type> getAnnotations() {
		if (annotations == null) {
			annotations = new ArrayList<>();
			if (node.visibleAnnotations != null) {
				for (AnnotationNode an : node.visibleAnnotations) {
					try {
						annotations.add(this.typeSystem.Lresolve(an.desc));
					} catch (MissingTypeException mte) {
						// that's ok you weren't relying on it anyway!
					}
				}
			}
//			if (node.invisibleAnnotations != null) {
//			for (AnnotationNode an: node.invisibleAnnotations) {
//				try {
//					annotations.add(this.typeSystem.Lresolve(an.desc));
//				} catch (MissingTypeException mte) {
//					// that's ok you weren't relying on it anyway!
//				}
//			}
//			}
			if (annotations.size() == 0) {
				annotations = NO_ANNOTATIONS;
			}
		}
		return annotations;
	}

	public Type findAnnotation(Type searchType) {
		List<Type> annos = getAnnotations();
		for (Type anno : annos) {
			if (anno.equals(searchType)) {
				return anno;
			}
		}
		return null;
	}
	
	/**
	 * Discover any uses of @Indexed or javax annotations, via direct/meta or interface usage.
	 * Example output:
	 * <pre><code>
	 * org.springframework.samples.petclinic.PetClinicApplication=org.springframework.stereotype.Component
     * org.springframework.samples.petclinic.model.Person=javax.persistence.MappedSuperclass
	 * org.springframework.samples.petclinic.owner.JpaOwnerRepositoryImpl=[org.springframework.stereotype.Component,javax.transaction.Transactional]
	 * </code></pre>
	 */
	public Entry<Type,List<Type>> getRelevantStereotypes() {
		List<Type> relevantAnnotations = new ArrayList<>();
		List<Type> indexedTypesInHierachy = getAnnotatedElementsInHierarchy(a -> a.desc.equals("Lorg/springframework/stereotype/Indexed;"));
		relevantAnnotations.addAll(indexedTypesInHierachy);
		List<Type> types = getJavaxAnnotationsInHierarchy();
		relevantAnnotations.addAll(types);
		if (!relevantAnnotations.isEmpty()) {
			return new AbstractMap.SimpleImmutableEntry<Type,List<Type>>(this,relevantAnnotations);
		} else {
			return null;
		}
	}
	
	/**
	 * Find usage of javax annotations in hierarchy (including meta usage).
	 * @return list of types in the hierarchy of this type that are affected by a javax annotation
	 */
	List<Type> getJavaxAnnotationsInHierarchy() {
		return getJavaxAnnotationsInHierarchy(new HashSet<>());
	}

	private boolean isAnnotated(String Ldescriptor) {
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an: node.visibleAnnotations) {
				if (an.desc.equals(Ldescriptor)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private List<Type> getAnnotatedElementsInHierarchy(Predicate<AnnotationNode> p) {
		return getAnnotatedElementsInHierarchy(p,new HashSet<>());
	}

	private List<Type> getAnnotatedElementsInHierarchy(Predicate<AnnotationNode> p, Set<String> seen) {
		List<Type> results = new ArrayList<>();
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an: node.visibleAnnotations) {
				if (seen.add(an.desc)) { 
					if (p.test(an)) {
						results.add(this);
					} else {
						Type annoType = typeSystem.Lresolve(an.desc, true);
						if (annoType != null) {
							List<Type> ts = annoType.getAnnotatedElementsInHierarchy(p, seen);
							results.addAll(ts);
						}
					}
				}
			}
		}
		return results.size()>0?results:Collections.emptyList();
	}
	
	private List<Type> getJavaxAnnotationsInHierarchy(Set<String> seen) {
		List<Type> result = new ArrayList<>();
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an: node.visibleAnnotations) {
				if (seen.add(an.desc)) { 
					Type annoType = typeSystem.Lresolve(an.desc,true);
					if (annoType != null) {
						if (annoType.getDottedName().startsWith("javax.")) {
							result.add(annoType);
						} else {
							List<Type> ts = annoType.getJavaxAnnotationsInHierarchy(seen);
							result.addAll(ts);
						}
					}	
				}
			}
		}
		Type[] intfaces = getInterfaces();
		for (Type intface: intfaces) {
			if (seen.add(intface.getDottedName())) {
				result.addAll(intface.getJavaxAnnotationsInHierarchy(seen));
			}
		}
		return result;
	}

	public List<Type> getNestedTypes() {
		List<Type> result = null;
		List<InnerClassNode> innerClasses = node.innerClasses;
		for (InnerClassNode inner: innerClasses) {	
			if (inner.outerName==null || !inner.outerName.equals(getName())) {
//				System.out.println("SKIPPING "+inner.name+" because outer is "+inner.outerName+" and we are looking at "+getName());
				continue;
			}
			if (inner.name.equals(getName())) {
				continue;
			}
			Type t = typeSystem.resolve(inner.name); // aaa/bbb/ccc/Ddd$Eee
			if (result == null) {
				result = new ArrayList<>();
			}
			result.add(t);
		}
		return result==null?Collections.emptyList():result;
	}

	public String getDescriptor() {
		return "L"+node.name.replace(".", "/")+";";
	}

	/**
	 * Find compilation hints directly on this type or used as a meta-annotation on annotations on this type.
	 */
	public List<Hint> getHints() {
		List<Hint> hints = new ArrayList<>();
		List<CompilationHint> hintx = typeSystem.findHints(getName());//SpringConfiguration.findProposedHints(getName());
		if (hintx.size() != 0) {
			List<Type> s = new ArrayList<>();
			s.add(this);
			for (CompilationHint hintxx: hintx) {
				hints.add(new Hint(s, hintxx.skipIfTypesMissing, hintxx.follow, hintxx.getDependantTypes(), Collections.emptyMap()));
			}
		}
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an: node.visibleAnnotations) {
				Type annotationType = typeSystem.Lresolve(an.desc, true);
				if (annotationType == null) {
					SpringFeature.log("Couldn't resolve "+an.desc+" annotation type whilst searching for hints on "+getName());
				} else {
					Stack<Type> s = new Stack<>();
					s.push(this);
					annotationType.collectHints(an, hints, new HashSet<>(), s);
				}
			}
		}
		if (isImportSelector() && hints.size()==0) {
			// Failing early as this will likely result in a problem later - fix is typically (right now) to
			// add an entry in the Type static initializer.
			throw new IllegalStateException("No @CompilationHint found for import selector: "+getDottedName());
		}
		return hints.size()==0?Collections.emptyList():hints;
	}
	
	// TODO handle repeatable annotations everywhere!
	
	void collectHints(AnnotationNode an, List<Hint> hints, Set<AnnotationNode> visited, Stack<Type> annotationChain) {
		if (!visited.add(an)) {
			return;
		}
		try {
			annotationChain.push(this);
			// Am I a compilation hint?
			List<CompilationHint> hints2 = typeSystem.findHints(an.desc);//;SpringConfiguration.findProposedHints(an.desc);
			if (hints2.size() !=0) {
				List<String> typesCollectedFromAnnotation = collectTypes(an);
				for (CompilationHint hints2a: hints2) {
				hints.add(new Hint(new ArrayList<>(annotationChain), hints2a.skipIfTypesMissing, 
					hints2a.follow, hints2a.getDependantTypes(), asMap(typesCollectedFromAnnotation,hints2a.skipIfTypesMissing)));
				}
			}
			// check for meta annotation
			if (node.visibleAnnotations != null) {
				for (AnnotationNode an2: node.visibleAnnotations) {
					Type annotationType = typeSystem.Lresolve(an2.desc, true);
					if (annotationType == null) {
						SpringFeature.log("Couldn't resolve "+an2.desc+" annotation type whilst searching for hints on "+getName());
					} else {
						annotationType.collectHints(an2, hints, visited, annotationChain);
					}
				}
			}
		} finally {
			annotationChain.pop();
		}
	}

	private Map<String, Integer> asMap(List<String> typesCollectedFromAnnotation, boolean usingForVisibilityCheck) {
		Map<String, Integer> map = new HashMap<>();
		for (String t: typesCollectedFromAnnotation) {
			Type type = typeSystem.Lresolve(t,true);
			int ar = -1;
			if (usingForVisibilityCheck) {
				ar = AccessBits.CLASS;//TypeKind.EXISTENCE_CHECK;
			} else {
				if (type != null && (type.isCondition() || type.isEventListener())) {
					ar = AccessBits.RESOURCE|AccessBits.CLASS|AccessBits.PUBLIC_CONSTRUCTORS;//TypeKind.RESOURCE_AND_INSTANTIATION;//AccessRequired.RESOURCE_CTORS_ONLY;
					if (type.isAbstractNestedCondition()) {
						// Need to pull in member types of this condition
						//Type[] memberTypes = type.getMemberTypes();
						//for (Type memberType: memberTypes) {
						//	// map.put(memberType.getDottedName(), AccessRequired.RESOURCE_ONLY);
						//}
					}
				} else {
					ar = AccessBits.EVERYTHING;//TypeKind.EVERYTHING;
				}
			}
			map.put(fromLdescriptorToDotted(t), ar);
		}
		return map;
	}

	private Type[] getMemberTypes() {
		List<Type> result = new ArrayList<>();
		List<InnerClassNode> nestMembers = node.innerClasses;
		if (nestMembers != null) {
			for (InnerClassNode icn: nestMembers) {
				if (icn.name.startsWith(this.getName()+"$")) {
					result.add(typeSystem.resolveSlashed(icn.name));
				}
			}
			System.out.println(this.getName()+" has inners "+
				nestMembers.stream().map(f -> "oo="+this.getDescriptor()+"::o="+f.outerName+"::n="+f.name+"::in="+f.innerName).collect(Collectors.joining(","))+"  >> "+result);
		}
		return result.toArray(new Type[0]);
	}

	private List<CompilationHint> findCompilationHintHelper(HashSet<Type> visited) {
		if (!visited.add(this)) {
			return null;
		}
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				List<CompilationHint> compilationHints = typeSystem.findHints(an.desc);//SpringConfiguration.findProposedHints(an.desc);
				if (compilationHints.size()!=0) {
					return compilationHints;
				}
				Type resolvedAnnotation = typeSystem.Lresolve(an.desc);
				compilationHints = resolvedAnnotation.findCompilationHintHelper(visited);
				if (compilationHints.size()!=0) {
					return compilationHints;
				}
			}
		}
		return null;
	}

	private List<String> collectTypes(AnnotationNode an) {
		List<Object> values = an.values;
		if (values != null) {
			for (int i=0;i<values.size();i+=2) {
				if (values.get(i).equals("value")) {
					// For some annotations it is a list, for some a single class (e.g. @ConditionalOnSingleCandidate)
					Object object = values.get(i+1);
					List<String> importedReferences = null;
					if (object instanceof List) {
						importedReferences = ((List<org.objectweb.asm.Type>)object)
							.stream()
							.map(t -> t.getDescriptor())
							.collect(Collectors.toList());
					} else {
						importedReferences = new ArrayList<>();
						importedReferences.add(((org.objectweb.asm.Type)object).getDescriptor());
					}
					return importedReferences;
				}
			}
		}
		return Collections.emptyList();
	}

	public boolean isImportSelector() {
		return implementsInterface(fromLdescriptorToSlashed(ImportSelector));
	}
	
	public boolean isImportRegistrar() {
		return implementsInterface(fromLdescriptorToSlashed(ImportBeanDefinitionRegistrar));
	}
	
	private String fromLdescriptorToSlashed(String Ldescriptor) {
		return Ldescriptor.substring(1,Ldescriptor.length()-1);
	}

	private String fromLdescriptorToDotted(String Ldescriptor) {
		return Ldescriptor.substring(1,Ldescriptor.length()-1).replace("/",".");
	}
	
	private List<CompilationHint> findCompilationHint(Type annotationType) {
		String descriptor = "L"+annotationType.getName().replace(".", "/")+";";
		List<CompilationHint> hints = typeSystem.findHints(descriptor);//SpringConfiguration.findProposedHints(descriptor);
		if (hints.size()!=0) {
			return hints;
		} else {
			// check for meta annotation
			return annotationType.findCompilationHintHelper(new HashSet<>());
		}
	}		
	
	public void collectMissingAnnotationTypesHelper(Set<String> missingAnnotationTypes, HashSet<Type> visited) {
		if (!visited.add(this)) {
			return;
		}
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an: node.visibleAnnotations) {
				Type annotationType = typeSystem.Lresolve(an.desc, true);
				if (annotationType == null) {
					missingAnnotationTypes.add(an.desc.substring(0,an.desc.length()-1).replace("/", "."));
				} else {
					annotationType.collectMissingAnnotationTypesHelper(missingAnnotationTypes, visited);
				}
			}
		}
	}

	public int getMethodCount() {
		return node.methods.size();
	}

	public boolean isAnnotation() {
		return (node.access & Opcodes.ACC_ANNOTATION)!=0;
	}

	public List<Type> getAutoConfigureBeforeOrAfter() {
		List<Type> result = new ArrayList<>();
		for (AnnotationNode an : node.visibleAnnotations) {
			if (an.desc.equals("Lorg/springframework/boot/autoconfigure/AutoConfigureAfter;") ||
					an.desc.equals("Lorg/springframework/boot/autoconfigure/AutoConfigureAfter;")) {
				List<Object> values = an.values;
				if (values != null) {
					for (int i=0;i<values.size();i+=2) {
						if (values.get(i).equals("value")) {
							List<org.objectweb.asm.Type> types = (List<org.objectweb.asm.Type>)values.get(i+1);
							for (org.objectweb.asm.Type type: types) {
							  Type t = typeSystem.Lresolve(type.getDescriptor());
							  if (t!= null) {
								  result.add(t);
							  }
							}
						}
					}
				}
			}
		}
		return result;
	}

	public boolean hasOnlySimpleConstructor() {
		boolean hasCtor = false;
		List<MethodNode> methods = node.methods;
		for (MethodNode mn: methods) {
			if (mn.name.equals("<init>")) {
				if (mn.desc.equals("()V")) {
					hasCtor = true;
				} else {
					return false;
				}
			}
		}
		return hasCtor;
	}

	public static boolean shouldBeProcessed(String key, TypeSystem ts) {
		String[] guardTypes = SpringConfiguration.findProposedFactoryGuards(key);//.get(key);
		if (guardTypes == null) {
			return true;
		} else {
			for (String guardType: guardTypes) {
				Type resolvedType = ts.resolveDotted(guardType,true);
				if (resolvedType != null) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Find the @ConfigurationHint annotations on this type (may be more than one)
	 * and from them build CompilationHints, taking care to convert class references
	 * to strings because they may not be resolvable. TODO ok to discard those that
	 * aren't resolvable at this point?
	 * 
	 * @return
	 */
	public List<CompilationHint> unpackConfigurationHints() {
		List<CompilationHint> hints = null;
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				if (fromLdescriptorToDotted(an.desc).equals(ConfigurationHint.class.getName())) {
					CompilationHint hint = fromConfigurationHintToCompilationHint(an);
					if (hints == null) {
						hints = new ArrayList<>();
					}
					hints.add(hint);
				} else if (fromLdescriptorToDotted(an.desc).equals(ConfigurationHints.class.getName())) {
					List<CompilationHint> chints = fromConfigurationHintsToCompilationHints(an);
					if (hints == null) {
						hints = new ArrayList<>();
					}
					hints.addAll(chints);
				}
			}
		}
		// TODO support repeatable version
		return hints==null?Collections.emptyList():hints;
	}

	@SuppressWarnings("unchecked")
	private CompilationHint fromConfigurationHintToCompilationHint(AnnotationNode an) {
		CompilationHint ch = new CompilationHint();
		List<Object> values = an.values;
		for (int i=0;i<values.size();i+=2){ 
			String key = (String)values.get(i);
			Object value = values.get(i+1);
			if (key.equals("value")) {
				// value(String)=Ljava/lang/String;(org.objectweb.asm.Type)
				ch.setTargetType(((org.objectweb.asm.Type)value).getClassName());
				/*
			} else if (key.equals("types")) {
				// types=[Ljava/lang/String;, Ljava/lang/Float;](class java.util.ArrayList)
				ArrayList<org.objectweb.asm.Type> types = (ArrayList<org.objectweb.asm.Type>)value;
			    for (org.objectweb.asm.Type type: types) {
			    		ch.addDependantType(type.getClassName(), inferTypeKind(type));
			    }
			*/
			} else if (key.equals("typeInfos")) {
				List<AnnotationNode> typeInfos = (List<AnnotationNode>)value;
				for (AnnotationNode typeInfo: typeInfos) {
					unpackTypeInfo(typeInfo, ch);
				}
			} else if (key.equals("abortIfTypesMissing")) {
				Boolean b = (Boolean)value;
				ch.setAbortIfTypesMissing(b);
			} else if (key.equals("follow")) {
				Boolean b = (Boolean)value;
				ch.setFollow(b);
			} else {
				System.out.println("annotation "+key+"="+value+"("+value.getClass()+")");
			}
		}
		return ch;
	}
	
	@SuppressWarnings("unchecked")
	private void unpackTypeInfo(AnnotationNode typeInfo, CompilationHint ch) {
		List<Object> values = typeInfo.values;
		List<org.objectweb.asm.Type> types = new ArrayList<>();
		List<String> typeNames = new ArrayList<>();
		int accessRequired = -1;
		for (int i=0;i<values.size();i+=2){ 
			String key = (String)values.get(i);
			Object value = values.get(i+1);
			if (key.equals("types")) {
				types = (ArrayList<org.objectweb.asm.Type>)value;
			} else if (key.equals("access")) {
				accessRequired = (Integer)value;
			} else if (key.equals("typeNames")) {
				typeNames = (ArrayList<String>)value;
			}
		}
		for (org.objectweb.asm.Type type: types) {
			ch.addDependantType(type.getClassName(), accessRequired==-1?inferTypeKind(type):accessRequired);
		}
		for (String typeName: typeNames) {
			Type resolvedType = typeSystem.resolveName(typeName,true);
			if (resolvedType != null) {
				ch.addDependantType(typeName, accessRequired==-1?inferTypeKind(resolvedType):accessRequired);
			}
			
		}
	}

	private int inferTypeKind(Type t) {
		if (t == null) {
			return AccessBits.NONE;
		}
		if (t.isAtConfiguration()) {
			return AccessBits.CONFIGURATION;
		} else {
			return AccessBits.ALL; // TODO this is wrong!
		}
	}

	@SuppressWarnings("unchecked")
	private List<CompilationHint> fromConfigurationHintsToCompilationHints(AnnotationNode an) {
		List<CompilationHint> chs = new ArrayList<>();
		List<Object> values = an.values;
		for (int i=0;i<values.size();i+=2){ 
			String key = (String)values.get(i);
			Object value = values.get(i+1);
			if (key.equals("value")) {
				// value=[org.objectweb.asm.tree.AnnotationNode@63e31ee, org.objectweb.asm.tree.AnnotationNode@68fb2c38]
				List<AnnotationNode> annotationNodes = (List<AnnotationNode>)value;
				for (int j=0;j<annotationNodes.size();j++) {
					chs.add(fromConfigurationHintToCompilationHint(annotationNodes.get(j)));
				}
			}
		}
		return chs;
	}
	
	private int inferTypeKind(org.objectweb.asm.Type type) {
		Type t = typeSystem.resolve(type, true);
		if (t == null) {
			return AccessBits.NONE;
		}
		if (t.isAtConfiguration()) {
			return AccessBits.CONFIGURATION;
		} else {
			return AccessBits.ALL; // TODO this is wrong!
		}
	}

	public List<CompilationHint> getCompilationHints() {
		return unpackConfigurationHints();
	}
	
	
}
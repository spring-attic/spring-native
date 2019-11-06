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
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.springframework.graal.support.SpringFeature;

/**
 * @author Andy Clement
 */
public class Type {
	
	// Spring Security
	public final static String OAuth2ImportSelector = "Lorg/springframework/security/config/annotation/web/configuration/OAuth2ImportSelector;";
	
	public final static String SpringWebMvcImportSelector = "Lorg/springframework/security/config/annotation/web/configuration/SpringWebMvcImportSelector;";
	
	public final static String ImportSelector ="Lorg/springframework/context/annotation/ImportSelector;";
	
	public final static String ImportBeanDefinitionRegistrar ="Lorg/springframework/context/annotation/ImportBeanDefinitionRegistrar;";

	public final static String TransactionManagementConfigurationSelector = "Lorg/springframework/transaction/annotation/TransactionManagementConfigurationSelector;";
	
	public final static String SpringDataWebConfigurationSelector = "Lorg/springframework/data/web/config/EnableSpringDataWebSupport$SpringDataWebConfigurationImportSelector;";

	public final static String SpringDataWebQueryDslSelector = "Lorg/springframework/data/web/config/EnableSpringDataWebSupport$QuerydslActivator;";

	public final static String AdviceModeImportSelector="Lorg/springframework/context/annotation/AdviceModeImportSelector;";

	public final static String EnableConfigurationPropertiesImportSelector = "Lorg/springframework/boot/context/properties/EnableConfigurationPropertiesImportSelector;";
	
	public final static String CacheConfigurationImportSelector = "Lorg/springframework/boot/autoconfigure/cache/CacheAutoConfiguration$CacheConfigurationImportSelector;";
	
	public final static String RabbitConfigurationImportSelector = "Lorg/springframework/amqp/rabbit/annotation/RabbitListenerConfigurationSelector;";
	
	public final static String AtBean = "Lorg/springframework/context/annotation/Bean;";

	public final static String AtImports = "Lorg/springframework/context/annotation/Import;";

	public final static String AtEnableConfigurationProperties = "Lorg/springframework/boot/context/properties/EnableConfigurationProperties;";
	
	public final static String AtConditionalOnClass = "Lorg/springframework/boot/autoconfigure/condition/ConditionalOnClass;";

	public final static String AtConfiguration = "Lorg/springframework/context/annotation/Configuration;";
	
	public final static String AtConditional = "Lorg/springframework/context/annotation/Conditional;";

	public final static String HypermediaConfigurationImportSelector = "Lorg/springframework/hateoas/config/HypermediaConfigurationImportSelector;";

	public final static String WebStackImportSelector = "Lorg/springframework/hateoas/config/WebStackImportSelector;";
	
	public final static String AtConditionalOnMissingBean = "Lorg/springframework/boot/autoconfigure/condition/ConditionalOnMissingBean;";

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

	public boolean hasAnnotationInHierarchy(String lookingFor) {
		return hasAnnotationInHierarchy(lookingFor, new ArrayList<String>());
	}

	public boolean hasAnnotationInHierarchy(String lookingFor, List<String> seen) {
		if (node.visibleAnnotations != null) {
			for (AnnotationNode anno : node.visibleAnnotations) {
				if (seen.contains(anno.desc))
					continue;
				seen.add(anno.desc);
	//			System.out.println("Comparing "+anno.desc+" with "+lookingFor);
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
	
	public boolean isAtConfiguration() {
		return isMetaAnnotated(fromLdescriptorToSlashed(AtConfiguration), new HashSet<>());
	}
	
	public boolean isAbstractNestedCondition() {
		return isAnnotated("Lorg/springframework/boot/autoconfigure/condition/AbstractNestedCondition;");
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
		CompilationHint hint = proposedHints.get(getDescriptor());
		if (hint !=null) {
			List<Type> s = new ArrayList<>();
			s.add(this);
			hints.add(new Hint(s, hint.skipIfTypesMissing, hint.follow, hint.specificTypes, Collections.emptyMap()));
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
			CompilationHint hint = proposedHints.get(an.desc);
			if (hint !=null) {
				List<String> typesCollectedFromAnnotation = collectTypes(an);
				hints.add(new Hint(new ArrayList<>(annotationChain), hint.skipIfTypesMissing, hint.follow, hint.specificTypes,asMap(typesCollectedFromAnnotation,hint.skipIfTypesMissing)));
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

	private Map<String, AccessRequired> asMap(List<String> typesCollectedFromAnnotation, boolean usingForVisibilityCheck) {
		Map<String, AccessRequired> map = new HashMap<>();
		for (String t: typesCollectedFromAnnotation) {
			map.put(fromLdescriptorToDotted(t), usingForVisibilityCheck?AccessRequired.EXISTENCE_CHECK:AccessRequired.ALL);
		}
		return map;
	}

	private CompilationHint findCompilationHintHelper(HashSet<Type> visited) {
		if (!visited.add(this)) {
			return null;
		}
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				CompilationHint compilationHint = proposedHints.get(an.desc);
				if (compilationHint != null) {
					return compilationHint;
				}
				Type resolvedAnnotation = typeSystem.Lresolve(an.desc);
				compilationHint = resolvedAnnotation.findCompilationHintHelper(visited);
				if (compilationHint != null) {
					return compilationHint;
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
					 List<String> importedReferences = ((List<org.objectweb.asm.Type>)values.get(i+1))
							.stream()
							.map(t -> t.getDescriptor())
							.collect(Collectors.toList());
					 return importedReferences;
				}
			}
		}
		return Collections.emptyList();
	}

	static Map<String, CompilationHint> proposedHints = new HashMap<>();
	
	static {
		// @ConditionalOnClass has @CompilationHint(skipIfTypesMissing=true, follow=false)
		proposedHints.put(AtConditionalOnClass, new CompilationHint(true,false));
		
		proposedHints.put("Lorg/springframework/boot/autoconfigure/condition/ConditionalOnMissingBean;",
				new CompilationHint(true, false, new String[] {
				 	"org.springframework.boot.autoconfigure.condition.SearchStrategy",
				}));
		
		// TODO can be {@link Configuration}, {@link ImportSelector}, {@link ImportBeanDefinitionRegistrar}
		// @Imports has @CompilationHint(skipIfTypesMissing=false?, follow=true)
		proposedHints.put(AtImports, new CompilationHint(false, true));
		
		// @Conditional has @CompilationHint(skipIfTypesMissing=false, follow=false)
		proposedHints.put(AtConditional, new CompilationHint(false, false));
		
		// TODO do configuration properties chain?
		// @EnableConfigurationProperties has @CompilationHint(skipIfTypesMissing=false, follow=false)
		proposedHints.put(AtEnableConfigurationProperties, new CompilationHint(false, false));
		
		// @EnableConfigurationPropertiesImportSelector has
		// @CompilationHint(skipIfTypesMissing=false, follow=false, name={
		//   ConfigurationPropertiesBeanRegistrar.class.getName(),
		//   ConfigurationPropertiesBindingPostProcessorRegistrar.class.getName() })
		// proposedAnnotations.put(AtEnableConfigurationProperties, new CompilationHint(false, false));
		
		proposedHints.put(CacheConfigurationImportSelector,
				new CompilationHint(false,false, new String[] {
				 	"org.springframework.boot.autoconfigure.cache.GenericCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.HazelcastCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.InfinispanCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.JCacheCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.SimpleCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.NoOpCacheConfiguration"}
				));
		
		proposedHints.put(RabbitConfigurationImportSelector,
				new CompilationHint(true,true, new String[] {
				 	"org.springframework.amqp.rabbit.annotation.RabbitBootstrapConfiguration"
				}));
		
		proposedHints.put("Lorg/springframework/boot/autoconfigure/condition/OnWebApplicationCondition;", 
				new CompilationHint(false, false, new String[] {
					"org.springframework.web.context.support.GenericWebApplicationContext",
					
				}));
		
		proposedHints.put(TransactionManagementConfigurationSelector,
				new CompilationHint(true, true, new String[] {
					"org.springframework.context.annotation.AutoProxyRegistrar",
					"org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration",
					"org.springframework.transaction.aspectj.AspectJJtaTransactionManagementConfiguration",
					"org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration"
				}));

		proposedHints.put("Lorg/springframework/boot/autoconfigure/session/SessionAutoConfiguration$ReactiveSessionConfigurationImportSelector;",
				new CompilationHint(true, true, new String[] {
						"org.springframework.boot.autoconfigure.session.RedisReactiveSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.MongoReactiveSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.NoOpReactiveSessionConfiguration"
				}));

		proposedHints.put("Lorg/springframework/boot/autoconfigure/session/SessionAutoConfiguration$SessionConfigurationImportSelector;",
				new CompilationHint(true, true, new String[] {
						"org.springframework.boot.autoconfigure.session.RedisSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.RedisReactiveSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.MongoSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.MongoReactiveSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.JdbcSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.HazelcastSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.NoOpSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.NoOpReactiveSessionConfiguration"
				}));

		proposedHints.put("Lorg/springframework/boot/autoconfigure/session/SessionAutoConfiguration$ServletSessionConfigurationImportSelector;",
				new CompilationHint(true, true, new String[] {
						"org.springframework.boot.autoconfigure.session.RedisSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.MongoSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.JdbcSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.HazelcastSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.NoOpSessionConfiguration"
				}));
		
		//  EnableSpringDataWebSupport. TODO: there are others in spring.factories.
		proposedHints.put(SpringDataWebConfigurationSelector,
				new CompilationHint(true, true, new String[] {
					"org.springframework.data.web.config.HateoasAwareSpringDataWebConfiguration",
					"org.springframework.data.web.config.SpringDataWebConfiguration"
				}));
		
		//  EnableSpringDataWebSupport. TODO: there are others in spring.factories.
		proposedHints.put(SpringDataWebQueryDslSelector,
				new CompilationHint(true, true, new String[] {
					"org.springframework.data.web.config.QuerydslWebConfiguration"}
				));
		
		// EnableConfigurationPropertiesImportSelector has
		// @CompilationHint(skipIfTypesMissing=true, follow=false, name={
		//	 	"org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector$ConfigurationPropertiesBeanRegistrar",
		//	 	"org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessorRegistrar"})
		proposedHints.put(EnableConfigurationPropertiesImportSelector,
				new CompilationHint(false,false, new String[] {
				 	"org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector$ConfigurationPropertiesBeanRegistrar:REGISTRAR",
				 	"org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessorRegistrar:REGISTRAR"}
				));
		
				
		// Not quite right... this is a superclass of a selector we've already added...
		proposedHints.put(AdviceModeImportSelector,
				new CompilationHint(true, true, new String[0]
				));

		
		// Spring Security!
		// TODO these should come with the jars themselves really (@CompilationHints on the selectors...)
		proposedHints.put(SpringWebMvcImportSelector,
				new CompilationHint(false, true, new String[] {
					"org.springframework.web.servlet.DispatcherServlet:EXISTENCE_CHECK",
					"org.springframework.security.config.annotation.web.configuration.WebMvcSecurityConfiguration"
				}));
				
		// TODO this one is actually incomplete, finish it
		proposedHints.put(OAuth2ImportSelector,
				new CompilationHint(false, true, new String[] {
					"org.springframework.security.oauth2.client.registration.ClientRegistration:EXISTENCE_CHECK",
					"org.springframework.security.config.annotation.web.configuration.OAuth2ClientConfiguration"
				}));

		// TODO I am not sure the specific entry here is right, but given that the selector references entries loaded via factories - aren't those already handled? 
		proposedHints.put(HypermediaConfigurationImportSelector,
				new CompilationHint(false, true, new String[] {
						"org.springframework.hateoas.config.HypermediaConfigurationImportSelector"
				}));

		proposedHints.put(WebStackImportSelector,
				new CompilationHint(false, true, new String[] {
					//"org.springframework.hateoas.config.WebStackImportSelector" - why was this here???
					"org.springframework.hateoas.config.WebMvcHateoasConfiguration",
					"org.springframework.hateoas.config.WebFluxHateoasConfiguration"
				}));
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
	
	private CompilationHint findCompilationHint(Type annotationType) {
		String descriptor = "L"+annotationType.getName().replace(".", "/")+";";
		CompilationHint hint = proposedHints.get(descriptor);
		if (hint !=null) {
			return hint;
		} else {
			// check for meta annotation
			return annotationType.findCompilationHintHelper(new HashSet<>());
		}
	}		
	
	// TODO what about AliasFor usage in spring annotations themselves? does that trip this code up when we start looking at particular fields?

	static class CompilationHint {
		boolean follow;
		boolean skipIfTypesMissing;
		private Map<String, AccessRequired> specificTypes;
		
		public CompilationHint(boolean skipIfTypesMissing, boolean follow) {
			this(skipIfTypesMissing,follow,new String[] {});
		}
		
		public CompilationHint(boolean skipIfTypesMissing, boolean follow, String[] specificTypes) {
			this.skipIfTypesMissing = skipIfTypesMissing;
			this.follow = follow;
			if (specificTypes != null) {
				this.specificTypes = new LinkedHashMap<>();
				for (String specificType: specificTypes) {
					AccessRequired access = AccessRequired.ALL;
					StringTokenizer t = new StringTokenizer(specificType,":");
					String type = t.nextToken(); // the type name
					if (t.hasMoreTokens()) { // possible access specified otherwise default to ALL
						access = AccessRequired.valueOf(t.nextToken());
					}
					this.specificTypes.put(type, access);
				}
			} else {
				this.specificTypes = Collections.emptyMap();
			}
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
	
//	@SuppressWarnings("unchecked")
//	public void findCompilationHints(String annotationType, Map<String,List<String>> hintCollector, Set<String> visited) {		
//		if (!visited.add(this.getName())) {
//			return Collections.emptyMap();
//		}
//		Map<String,List<String>> collectedResults = new LinkedHashMap<>();
//		if (node.visibleAnnotations != null) {
//			for (AnnotationNode an : node.visibleAnnotations) {
//				if (an.desc.equals(annotationType)) {
//					List<Object> values = an.values;
//					if (values != null) {
//						for (int i=0;i<values.size();i+=2) {
//							if (values.get(i).equals("value")) {
//								 List<String> importedReferences = ((List<org.objectweb.asm.Type>)values.get(i+1))
//										.stream()
//										.map(t -> t.getDescriptor())
//										.collect(Collectors.toList());
//								collectedResults.put(this.getName().replace("/", "."), importedReferences);
//							}
//						}
//					}
//				}
//			}
//			if (searchMeta) {
//				for (AnnotationNode an: node.visibleAnnotations) {
//					// For example @EnableSomething might have @Import on it
//					Type annoType = null;
//					try {
//						annoType = typeSystem.Lresolve(an.desc);
//					} catch (MissingTypeException mte) { 
//						System.out.println("SBG: WARNING: Unable to find "+an.desc+" skipping...");
//						continue;
//					}
//					collectedResults.putAll(annoType.findCompilationHints(annotationType, visited));
//				}
//			}
//		}
//		return collectedResults;
//	}

	
	// Assume @ConditionalOnClass has @CompilationHint(skipIfTypesMissing=true) and both value() and name() in
	// the annotation would have @CompilationTypeList

	

}
/*
 * Copyright 2019-2020 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.springframework.graalvm.domain.init.InitializationDescriptor;
import org.springframework.graalvm.extension.InitializationInfo;
import org.springframework.graalvm.extension.InitializationInfos;
import org.springframework.graalvm.extension.InitializationTime;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.NativeImageHints;
import org.springframework.graalvm.extension.ProxyInfo;
import org.springframework.graalvm.extension.ProxyInfos;
import org.springframework.graalvm.extension.ResourcesInfo;
import org.springframework.graalvm.extension.ResourcesInfos;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.extension.TypeInfos;
import org.springframework.graalvm.support.ConfigOptions;
import org.springframework.graalvm.support.SpringFeature;
import org.springframework.util.ClassUtils;

/**
 * @author Andy Clement
 * @author Christoph Strobl
 */
@SuppressWarnings("unchecked")
public class Type {

	public final static String AtResponseBody = "Lorg/springframework/web/bind/annotation/ResponseBody;";
	public final static String AtMapping = "Lorg/springframework/web/bind/annotation/Mapping;";
	public final static String AtTransactional = "Lorg/springframework/transaction/annotation/Transactional;";
	public final static String AtEndpoint = "Lorg/springframework/boot/actuate/endpoint/annotation/Endpoint;";
	public final static String AtJavaxTransactional = "Ljavax/transaction/Transactional;";
	public final static String AtBean = "Lorg/springframework/context/annotation/Bean;";
	public final static String AtConditionalOnClass = "Lorg/springframework/boot/autoconfigure/condition/ConditionalOnClass;";
	public final static String AtConditionalOnProperty = "Lorg/springframework/boot/autoconfigure/condition/ConditionalOnProperty;";
	public final static String AtConditionalOnEnabledMetricsExport = "Lorg/springframework/boot/actuate/autoconfigure/metrics/export/ConditionalOnEnabledMetricsExport;";
	public final static String AtConditionalOnCloudPlatform = "Lorg/springframework/boot/autoconfigure/condition/ConditionalOnCloudPlatform;";
	public final static String AtConditionalOnAvailableEndpoint = "Lorg/springframework/boot/actuate/autoconfigure/endpoint/condition/ConditionalOnAvailableEndpoint;";
	public final static String AtConditionalOnEnabledHealthIndicator = "Lorg/springframework/boot/actuate/autoconfigure/health/ConditionalOnEnabledHealthIndicator;";
	public final static String AtConditionalOnMissingBean = "Lorg/springframework/boot/autoconfigure/condition/ConditionalOnMissingBean;";
	public final static String AtConditionalOnBean = "Lorg/springframework/boot/autoconfigure/condition/ConditionalOnBean;";
	public final static String AtConfiguration = "Lorg/springframework/context/annotation/Configuration;";
	public final static String AtImportAutoConfiguration = "Lorg/springframework/boot/autoconfigure/ImportAutoConfiguration;";
	public final static String AtConfigurationProperties = "Lorg/springframework/boot/context/properties/ConfigurationProperties;";
	public final static String AtSpringBootApplication = "Lorg/springframework/boot/autoconfigure/SpringBootApplication;";
	public final static String AtController = "Lorg/springframework/stereotype/Controller;";
	public final static String AtRepository = "Lorg/springframework/stereotype/Repository;";
	public final static String AtEnableConfigurationProperties = "Lorg/springframework/boot/context/properties/EnableConfigurationProperties;";
	public final static String AtImports = "Lorg/springframework/context/annotation/Import;";
	public final static String AtValidated = "Lorg/springframework/validation/annotation/Validated;";
	public final static String AtComponent = "Lorg/springframework/stereotype/Component;";
	public final static String BeanFactoryPostProcessor = "Lorg/springframework/beans/factory/config/BeanFactoryPostProcessor;";
	public final static String ImportBeanDefinitionRegistrar = "Lorg/springframework/context/annotation/ImportBeanDefinitionRegistrar;";
	public final static String ImportSelector = "Lorg/springframework/context/annotation/ImportSelector;";
	public final static String ApplicationListener = "Lorg/springframework/context/ApplicationListener;";
	public final static String AtAliasFor = "Lorg/springframework/core/annotation/AliasFor;";
	public final static String Condition = "Lorg/springframework/context/annotation/Condition;";
	public final static String EnvironmentPostProcessor = "Lorg/springframework/boot/env/EnvironmentPostProcessor";

	public final static Type MISSING = new Type(null, null, 0);

	public final static Type[] NO_INTERFACES = new Type[0];

	protected static Set<String> validBoxing = new HashSet<String>();

	private TypeSystem typeSystem;

	private ClassNode node;

	private Type[] interfaces;

	private String name;
	private String dottedName;

	private int dimensions = 0; // >0 for array types

	private final Lazy<List<Field>> fields;
	private final Lazy<List<Method>> methods;
	private final Lazy<List<Type>> annotations;
	
	private boolean isPrimitive;

	private Type(TypeSystem typeSystem, ClassNode node, int dimensions) {
		this.typeSystem = typeSystem;
		this.node = node;
		this.dimensions = dimensions;
		if (node != null) {
			this.name = node.name;
			for (int i = 0; i < dimensions; i++) {
				this.name += "[]";
			}
			this.dottedName = name.replace("/", ".");
			this.fields = Lazy.of(this::resolveFields);
			this.methods = Lazy.of(this::resolveMethods);
			this.annotations = Lazy.of(this::resolveAnnotations);
		} else {

			this.fields = Lazy.empty();
			this.methods = Lazy.empty();
			this.annotations = Lazy.empty();
		}
	}

	public static Type forClassNode(TypeSystem typeSystem, ClassNode node, int dimensions) {
		return new Type(typeSystem, node, dimensions);
	}

	/**
	 * @return typename in slashed form (aaa/bbb/ccc/Ddd$Eee)
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return short version of the typename (with packages collapsed) to add with readability
	 */
	public String getShortName() {
		String dname = getDottedName();
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

	public String getDottedName() {
		return dottedName;
	}

	public Type getSuperclass() {
		if (dimensions > 0) {
			return typeSystem.resolveSlashed("java/lang/Object");
		}
		if (node.superName == null) {
			return null;
		}
		return typeSystem.resolveSlashed(node.superName);
	}

	@Override
	public String toString() {
		return "Type:" + getName();
	}

	public Type[] getInterfaces() {
		if (interfaces == null) {
			if (dimensions != 0) {
				interfaces = new Type[] { typeSystem.resolveSlashed("java/lang/Cloneable"),
						typeSystem.resolveSlashed("java/io/Serializable") };
			} else {
				List<String> itfs = node.interfaces;
				if (itfs.size() == 0) {
					interfaces = NO_INTERFACES;
				} else {
					List<Type> interfacesOnClasspath = new ArrayList<>();
					for (int i = 0; i < itfs.size(); i++) {
						Type t = typeSystem.resolveSlashed(itfs.get(i));
						interfacesOnClasspath.add(t);
					}
					interfaces = interfacesOnClasspath.toArray(new Type[interfacesOnClasspath.size()]);
				}
			}
		}
		return interfaces;
	}

	public boolean isPartOfDomain(String packageName) {
		return belongsToPackage(packageName, true);
	}

	public boolean belongsToPackage(String packageName, boolean orPartOfSubPackage) {
		return orPartOfSubPackage ? dottedName.startsWith(packageName) : getPackageName().equals(packageName);
	}

	public String getPackageName() {
		return dottedName.substring(0, dottedName.indexOf('.'));
	}

	/**
	 * @return List of slashed interface types
	 */
	public List<String> getInterfacesStrings() {
		if (dimensions != 0) {
			List<String> itfs = new ArrayList<>();
			itfs.add("java/lang/Cloneable");
			itfs.add("java/io/Serializable");
			return itfs;
		} else {
			return node.interfaces;
		}
	}

	/**
	 * @return slashed supertype name
	 */
	public String getSuperclassString() {
		return dimensions > 0 ? "java/lang/Object" : node.superName;
	}

	/**
	 * Compute all the types referenced in the signature of this type.
	 *
	 * @return
	 */
	public List<String> getTypesInSignature() {
		if (dimensions > 0) {
			return Collections.emptyList();
		} else if (node.signature == null) {
			// With no generic signature it is just superclass and interfaces
			List<String> ls = new ArrayList<>();
			if (node.superName != null) {
				ls.add(node.superName);
			}
			if (node.interfaces != null) {
				ls.addAll(node.interfaces);
			}
			return ls;
		} else {
			// Pull out all the types from the generic signature
			SignatureReader reader = new SignatureReader(node.signature);
			TypeCollector tc = new TypeCollector();
			reader.accept(tc);
			return tc.getTypes();
		}
	}

	static class TypeCollector extends SignatureVisitor {

		List<String> types = null;

		public TypeCollector() {
			super(Opcodes.ASM9);
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
		if (this.getName().equals(interfaceName)) {
			return true;
		}
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
		// System.out.println("looking through methods "+node.methods+" for "+string);
		return dimensions > 0 ? Collections.emptyList()
				: node.methods.stream().filter(m -> hasAnnotation(m, string)).map(m -> wrap(m))
						.collect(Collectors.toList());
	}

	public List<Method> getMethodsWithAnnotationName(String string, boolean checkMetaUsage) {
		return getMethodsWithAnnotation("L" + string.replace(".", "/") + ";", checkMetaUsage);
	}

	public List<Method> getMethodsWithAnnotation(String string, boolean checkMetaUsage) {
		if (dimensions > 0) {
			return Collections.emptyList();
		}
		List<Method> results = new ArrayList<>();
		if (node.methods != null) {
			for (MethodNode mn : node.methods) {
				if (hasAnnotation(mn, string, checkMetaUsage)) {
					if (results == null) {
						results = new ArrayList<>();
					}
					results.add(wrap(mn));
				}
			}
		}
		return results == null ? Collections.emptyList() : results;
	}

	public List<Method> getMethods() {
		return methods.get();
	}

	private List<Method> resolveMethods() {
		return dimensions > 0 ? Collections.emptyList()
				: node.methods.stream().map(m -> wrap(m)).collect(Collectors.toList());
	}

	public List<Field> getFields() {
		return fields.get();
	}

	private List<Field> resolveFields() {
		return dimensions > 0 ? Collections.emptyList()
				: node.fields.stream().map(it -> new Field(it, typeSystem)).collect(Collectors.toList());
	}

	public List<Method> getMethodsWithAtBean() {
		return getMethodsWithAnnotation(AtBean);
	}

	private Method wrap(MethodNode mn) {
		return new Method(mn, typeSystem);
	}

	private boolean hasAnnotation(MethodNode m, String string) {
		List<AnnotationNode> visibleAnnotations = m.visibleAnnotations;
		Optional<AnnotationNode> findAny = visibleAnnotations == null ? Optional.empty()
				: visibleAnnotations.stream().filter(a -> a.desc.equals(string)).findFirst();
		return findAny.isPresent();
	}

	public boolean hasAnnotation(String lAnnotationDescriptor, boolean checkMetaUsage) {
		Set<String> seen = new HashSet<>();
		return hasAnnotationHelper(lAnnotationDescriptor, checkMetaUsage, seen);
	}

	private boolean hasAnnotationHelper(String lAnnotationDescriptor, boolean checkMetaUsage, Set<String> seen) {
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				if (an.desc.equals(lAnnotationDescriptor)) {
					return true;
				}
				if (checkMetaUsage && seen.add(lAnnotationDescriptor)) {
					Type t = typeSystem.Lresolve(an.desc);
					if (t != null) {
						boolean b = t.hasAnnotationHelper(lAnnotationDescriptor, checkMetaUsage, seen);
						if (b) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private boolean hasAnnotation(MethodNode mn, String lAnnotationDescriptor, boolean checkMetaUsage) {
		if (checkMetaUsage) {
			return findMetaAnnotationUsage(toAnnotations(mn.visibleAnnotations), lAnnotationDescriptor);
		} else {
			List<AnnotationNode> vAnnotations = mn.visibleAnnotations;
			if (vAnnotations != null) {
				for (AnnotationNode an : vAnnotations) {
					if (an.desc.equals(lAnnotationDescriptor)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private List<Type> toAnnotations(List<AnnotationNode> visibleAnnotations) {
		if (visibleAnnotations == null) {
			return Collections.emptyList();
		}
		List<Type> result = new ArrayList<>();
		for (AnnotationNode an : visibleAnnotations) {
			result.add(typeSystem.Lresolve(an.desc));
		}
		return result;
	}

	private boolean findMetaAnnotationUsage(List<Type> toSearch, String lAnnotationDescriptor) {
		return findMetaAnnotationUsageHelper(toSearch, lAnnotationDescriptor, new HashSet<>());
	}

	private boolean findMetaAnnotationUsageHelper(List<Type> toSearch, String lAnnotationDescriptor, Set<String> seen) {
		if (toSearch == null) {
			return false;
		}
		for (Type an : toSearch) {
			if (an.getDescriptor().equals(lAnnotationDescriptor)) {
				return true;
			}
			if (seen.add(an.getName())) {
				boolean isMetaAnnotated = findMetaAnnotationUsageHelper(an.getAnnotations(), lAnnotationDescriptor,
						seen);
				if (isMetaAnnotated) {
					return true;
				}
			}
		}
		return false;
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

		// if (!isTypeVariableReference()
		// && other.getSignature().equals("Ljava/lang/Object;")) {
		// return false;
		// }

		// boolean thisRaw = this.isRawType();
		// if (thisRaw && other.isParameterizedOrGenericType()) {
		// return isAssignableFrom(other.getRawType());
		// }
		//
		// boolean thisGeneric = this.isGenericType();
		// if (thisGeneric && other.isParameterizedOrRawType()) {
		// return isAssignableFrom(other.getGenericType());
		// }
		//
		// if (this.isParameterizedType()) {
		// // look at wildcards...
		// if (((ReferenceType) this.getRawType()).isAssignableFrom(other)) {
		// boolean wildcardsAllTheWay = true;
		// ResolvedType[] myParameters = this.getResolvedTypeParameters();
		// for (int i = 0; i < myParameters.length; i++) {
		// if (!myParameters[i].isGenericWildcard()) {
		// wildcardsAllTheWay = false;
		// } else {
		// BoundedReferenceType boundedRT = (BoundedReferenceType) myParameters[i];
		// if (boundedRT.isExtends() || boundedRT.isSuper()) {
		// wildcardsAllTheWay = false;
		// }
		// }
		// }
		// if (wildcardsAllTheWay && !other.isParameterizedType()) {
		// return true;
		// }
		// // we have to match by parameters one at a time
		// ResolvedType[] theirParameters = other
		// .getResolvedTypeParameters();
		// boolean parametersAssignable = true;
		// if (myParameters.length == theirParameters.length) {
		// for (int i = 0; i < myParameters.length
		// && parametersAssignable; i++) {
		// if (myParameters[i] == theirParameters[i]) {
		// continue;
		// }
		// // dont do this: pr253109
		// // if
		// // (myParameters[i].isAssignableFrom(theirParameters[i],
		// // allowMissing)) {
		// // continue;
		// // }
		// ResolvedType mp = myParameters[i];
		// ResolvedType tp = theirParameters[i];
		// if (mp.isParameterizedType()
		// && tp.isParameterizedType()) {
		// if (mp.getGenericType().equals(tp.getGenericType())) {
		// UnresolvedType[] mtps = mp.getTypeParameters();
		// UnresolvedType[] ttps = tp.getTypeParameters();
		// for (int ii = 0; ii < mtps.length; ii++) {
		// if (mtps[ii].isTypeVariableReference()
		// && ttps[ii]
		// .isTypeVariableReference()) {
		// TypeVariable mtv = ((TypeVariableReferenceType) mtps[ii])
		// .getTypeVariable();
		// boolean b = mtv
		// .canBeBoundTo((ResolvedType) ttps[ii]);
		// if (!b) {// TODO incomplete testing here
		// // I think
		// parametersAssignable = false;
		// break;
		// }
		// } else {
		// parametersAssignable = false;
		// break;
		// }
		// }
		// continue;
		// } else {
		// parametersAssignable = false;
		// break;
		// }
		// }
		// if (myParameters[i].isTypeVariableReference()
		// && theirParameters[i].isTypeVariableReference()) {
		// TypeVariable myTV = ((TypeVariableReferenceType) myParameters[i])
		// .getTypeVariable();
		// // TypeVariable theirTV =
		// // ((TypeVariableReferenceType)
		// // theirParameters[i]).getTypeVariable();
		// boolean b = myTV.canBeBoundTo(theirParameters[i]);
		// if (!b) {// TODO incomplete testing here I think
		// parametersAssignable = false;
		// break;
		// } else {
		// continue;
		// }
		// }
		// if (!myParameters[i].isGenericWildcard()) {
		// parametersAssignable = false;
		// break;
		// } else {
		// BoundedReferenceType wildcardType = (BoundedReferenceType) myParameters[i];
		// if (!wildcardType.alwaysMatches(theirParameters[i])) {
		// parametersAssignable = false;
		// break;
		// }
		// }
		// }
		// } else {
		// parametersAssignable = false;
		// }
		// if (parametersAssignable) {
		// return true;
		// }
		// }
		// }
		//
		// // eg this=T other=Ljava/lang/Object;
		// if (isTypeVariableReference() && !other.isTypeVariableReference()) {
		// TypeVariable aVar = ((TypeVariableReference) this)
		// .getTypeVariable();
		// return aVar.resolve(world).canBeBoundTo(other);
		// }
		//
		// if (other.isTypeVariableReference()) {
		// TypeVariableReferenceType otherType = (TypeVariableReferenceType) other;
		// if (this instanceof TypeVariableReference) {
		// return ((TypeVariableReference) this)
		// .getTypeVariable()
		// .resolve(world)
		// .canBeBoundTo(
		// otherType.getTypeVariable().getFirstBound()
		// .resolve(world));// pr171952
		// // return
		// // ((TypeVariableReference)this).getTypeVariable()==otherType
		// // .getTypeVariable();
		// } else {
		// // FIXME asc should this say canBeBoundTo??
		// return this.isAssignableFrom(otherType.getTypeVariable()
		// .getFirstBound().resolve(world));
		// }
		// }

		Type[] interfaces = other.getInterfaces();
		for (Type intface : interfaces) {
			boolean b;
			// if (thisRaw && intface.isParameterizedOrGenericType()) {
			// b = this.isAssignableFrom(intface.getRawType(), allowMissing);
			// } else {
			b = this.isAssignableFrom(intface);
			// }
			if (b) {
				return true;
			}
		}
		Type superclass = other.getSuperclass();
		if (superclass != null) {
			boolean b;
			// if (thisRaw && superclass.isParameterizedOrGenericType()) {
			// b = this.isAssignableFrom(superclass.getRawType(), allowMissing);
			// } else {
			b = this.isAssignableFrom(superclass);
			// }
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
		return dimensions > 0 ? false : Modifier.isInterface(node.access);
	}

	public Map<String, String> getAnnotationValuesInHierarchy(String LdescriptorLookingFor) {
		if (dimensions > 0) {
			return Collections.emptyMap();
		}
		Map<String, String> collector = new HashMap<>();
		getAnnotationValuesInHierarchy(LdescriptorLookingFor, new ArrayList<>(), collector);
		return collector;
	}

	public void getAnnotationValuesInHierarchy(String lookingFor, List<String> seen, Map<String, String> collector) {
		if (dimensions > 0) {
			return;
		}
		if (node.visibleAnnotations != null) {
			for (AnnotationNode anno : node.visibleAnnotations) {
				if (seen.contains(anno.desc))
					continue;
				seen.add(anno.desc);
				// System.out.println("Comparing "+anno.desc+" with "+lookingFor);
				if (anno.desc.equals(lookingFor)) {
					List<Object> os = anno.values;
					for (int i = 0; i < os.size(); i += 2) {
						collector.put(os.get(i).toString(), os.get(i + 1).toString());
					}
				}
				try {
					Type resolve = typeSystem.Lresolve(anno.desc);
					resolve.getAnnotationValuesInHierarchy(lookingFor, seen, collector);
				} catch (MissingTypeException mte) {
					// not on classpath, that's ok
				}
			}
		}
	}

	public boolean hasAnnotationInHierarchy(String lookingFor) {
		if (dimensions > 0) {
			return false;
		}
		return hasAnnotationInHierarchy(lookingFor, new ArrayList<String>());
	}

	public boolean hasAnnotationInHierarchy(String lookingFor, List<String> seen) {
		if (dimensions > 0) {
			return false;
		}
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
		if (dimensions > 0) {
			return false;
		}
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

	public boolean isAtImport() {
		return (dimensions > 0) ? false : isMetaAnnotated(fromLdescriptorToSlashed(AtImports));
	}

	public boolean isAtConfiguration() {
		if (dimensions > 0) {
			return false;
		}
		return isMetaAnnotated(fromLdescriptorToSlashed(AtConfiguration), new HashSet<>());
	}
	
	public boolean isAtComponent() {
		return dimensions>0?false:isMetaAnnotated(fromLdescriptorToSlashed(AtComponent), new HashSet<>());
	}

	public boolean isAtSpringBootApplication() {
		return (dimensions > 0) ? false
				: isMetaAnnotated(fromLdescriptorToSlashed(AtSpringBootApplication), new HashSet<>());
	}

	public boolean isAtController() {
		return (dimensions > 0) ? false : isMetaAnnotated(fromLdescriptorToSlashed(AtController), new HashSet<>());
	}

	public boolean isAbstractNestedCondition() {
		if (dimensions > 0) {
			return false;
		}
		return extendsClass("Lorg/springframework/boot/autoconfigure/condition/AbstractNestedCondition;");
	}

	public boolean isMetaAnnotated(String slashedTypeDescriptor) {
		if (dimensions > 0) {
			return false;
		}
		return isMetaAnnotated(slashedTypeDescriptor, new HashSet<>());
	}

	private boolean isMetaAnnotated(String slashedTypeDescriptor, Set<String> seen) {
		if (dimensions > 0) {
			return false;
		}
		for (Type t : this.getAnnotations()) {
			if (t == null) {
				continue;
			}
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

	public static final List<Type> NO_ANNOTATIONS = Collections.emptyList();


	public List<String> findConditionalOnBeanValue() {
		if (dimensions > 0) {
			return Collections.emptyList();
		}
		List<String> findAnnotationValue = findAnnotationValue(AtConditionalOnBean, false);
		if (findAnnotationValue == null) {
			if (node.visibleAnnotations != null) {
				for (AnnotationNode an : node.visibleAnnotations) {
					if (an.desc.equals(AtConditionalOnBean)) {
						System.out.println("??? found nothing on this @COB annotated thing " + this.getName());
					}
				}
			}
		}
		return findAnnotationValue;
	}

	public List<String> findConditionalOnMissingBeanValue() {
		if (dimensions > 0) {
			return Collections.emptyList();
		}
		List<String> findAnnotationValue = findAnnotationValue(AtConditionalOnMissingBean, false);
		if (findAnnotationValue == null) {
			if (node.visibleAnnotations != null) {
				for (AnnotationNode an : node.visibleAnnotations) {
					if (an.desc.equals(AtConditionalOnMissingBean)) {
						System.out.println("??? found nothing on this @COMB annotated thing " + this.getName());
					}
				}
			}
		}
		return findAnnotationValue;
	}

	public List<String> findConditionalOnClassValue() {
		if (dimensions > 0) {
			return Collections.emptyList();
		}
		List<String> findAnnotationValue = findAnnotationValue(AtConditionalOnClass, false);
		if (findAnnotationValue == null) {
			if (node.visibleAnnotations != null) {
				for (AnnotationNode an : node.visibleAnnotations) {
					if (an.desc.equals(AtConditionalOnClass)) {
						System.out.println("??? found nothing on this @COC annotated thing " + this.getName());
					}
				}
			}
		}
		return findAnnotationValue;
	}

	public List<String> findEnableConfigurationPropertiesValue() {
		if (dimensions > 0) {
			return Collections.emptyList();
		}
		List<String> values = findAnnotationValue(AtEnableConfigurationProperties, false);
		return values;
	}

	public Map<String, List<String>> findImports() {
		if (dimensions > 0) {
			return Collections.emptyMap();
		}
		return findAnnotationValueWithHostAnnotation(AtImports, true, new HashSet<>());
	}

	public List<String> findAnnotationValue(String annotationType, boolean searchMeta) {
		if (dimensions > 0) {
			return Collections.emptyList();
		}
		return findAnnotationValue(annotationType, searchMeta, new HashSet<>());
	}

	public Map<String, List<String>> findAnnotationValueWithHostAnnotation(String annotationType, boolean searchMeta,
			Set<String> visited) {
		if (dimensions > 0) {
			return Collections.emptyMap();
		}
		if (!visited.add(this.getName())) {
			return Collections.emptyMap();
		}
		Map<String, List<String>> collectedResults = new LinkedHashMap<>();
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				if (an.desc.equals(annotationType)) {
					List<Object> values = an.values;
					if (values != null) {
						for (int i = 0; i < values.size(); i += 2) {
							if (values.get(i).equals("value")) {
								List<String> importedReferences = ((List<org.objectweb.asm.Type>) values.get(i + 1))
										.stream().map(t -> t.getDescriptor()).collect(Collectors.toList());
								collectedResults.put(this.getName().replace("/", "."), importedReferences);
							}
						}
					}
				}
			}
			if (searchMeta) {
				for (AnnotationNode an : node.visibleAnnotations) {
					// For example @EnableSomething might have @Import on it
					Type annoType = null;
					try {
						annoType = typeSystem.Lresolve(an.desc);
					} catch (MissingTypeException mte) {
						System.out.println("SBG: WARNING: Unable to find " + an.desc + " skipping...");
						continue;
					}
					collectedResults.putAll(
							annoType.findAnnotationValueWithHostAnnotation(annotationType, searchMeta, visited));
				}
			}
		}
		return collectedResults;
	}

	public List<String> findAnnotationValue(String annotationType, boolean searchMeta, Set<String> visited) {
		if (dimensions > 0) {
			return Collections.emptyList();
		}
		if (!visited.add(this.getName())) {
			return Collections.emptyList();
		}
		List<String> collectedResults = new ArrayList<>();
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				if (an.desc.equals(annotationType)) {
					List<Object> values = an.values;
					if (values != null) {
						for (int i = 0; i < values.size(); i += 2) {
							if (values.get(i).equals("value")) {
								return ((List<org.objectweb.asm.Type>) values.get(i + 1)).stream()
										.map(t -> t.getDescriptor())
										.collect(Collectors.toCollection(() -> collectedResults));
							}
						}
					}
				}
			}
			if (searchMeta) {
				for (AnnotationNode an : node.visibleAnnotations) {
					// For example @EnableSomething might have @Import on it
					Type annoType = typeSystem.Lresolve(an.desc);
					collectedResults.addAll(annoType.findAnnotationValue(annotationType, searchMeta, visited));
				}
			}
		}
		return collectedResults;
	}

	public List<Type> getAnnotations() {
		return annotations.get();
	}

	private List<Type> resolveAnnotations() {
		if (dimensions > 0) {
			return Collections.emptyList();
		}
		List<Type> annotations = new ArrayList<>();
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				try {
					annotations.add(this.typeSystem.Lresolve(an.desc));
				} catch (MissingTypeException mte) {
					// that's ok you weren't relying on it anyway!
				}
			}
		}
		// if (node.invisibleAnnotations != null) {
		// for (AnnotationNode an: node.invisibleAnnotations) {
		// try {
		// annotations.add(this.typeSystem.Lresolve(an.desc));
		// } catch (MissingTypeException mte) {
		// // that's ok you weren't relying on it anyway!
		// }
		// }
		// }
		if (annotations.size() == 0) {
			annotations = NO_ANNOTATIONS;
		}
		return annotations;
	}

	public Type findAnnotation(Type searchType) {
		if (dimensions > 0) {
			return null;
		}
		List<Type> annos = getAnnotations();
		for (Type anno : annos) {
			if (anno.equals(searchType)) {
				return anno;
			}
		}
		return null;
	}

	/**
	 * Discover any uses of @Indexed or javax annotations, via direct/meta or
	 * interface usage. Example output:
	 * 
	 * <pre>
	 * <code>
	 * org.springframework.samples.petclinic.PetClinicApplication=org.springframework.stereotype.Component
	 * org.springframework.samples.petclinic.model.Person=javax.persistence.MappedSuperclass
	 * org.springframework.samples.petclinic.owner.JpaOwnerRepositoryImpl=[org.springframework.stereotype.Component,javax.transaction.Transactional]
	 * </code>
	 * </pre>
	 */
	public Entry<Type, List<Type>> getRelevantStereotypes() {
		if (dimensions > 0) {
			return null;
		}
		List<Type> relevantAnnotations = new ArrayList<>();
		collectRelevantStereotypes(this, new HashSet<>(), relevantAnnotations);
		List<Type> types = getJavaxAnnotations();
		for (Type t : types) {
			if (!relevantAnnotations.contains(t)) {
				relevantAnnotations.add(t);
			}
		}
		if (!relevantAnnotations.isEmpty()) {
			return new AbstractMap.SimpleImmutableEntry<Type, List<Type>>(this, relevantAnnotations);
		} else {
			return null;
		}
	}

	private void collectRelevantStereotypes(Type type, Set<Type> seen, List<Type> collector) {
		if (type == null || type.dimensions > 0 || !seen.add(type)) {
			return;
		}
		List<Type> ts = type
				.getAnnotatedElementsInHierarchy(a -> a.desc.equals("Lorg/springframework/stereotype/Indexed;"));
		for (Type t : ts) {
			if (!collector.contains(t)) {
				collector.add(t);
			}
		}
		for (Type inttype : type.getInterfaces()) {
			collectRelevantStereotypes(inttype, seen, collector);
		}
		collectRelevantStereotypes(type.getSuperclass(), seen, collector);
	}

	public Entry<Type, List<Type>> getMetaComponentTaggedAnnotations() {
		if (dimensions > 0)
			return null;
		List<Type> relevantAnnotations = new ArrayList<>();
		List<Type> indexedTypesInHierachy = getAnnotatedElementsInHierarchy(
				a -> a.desc.equals("Lorg/springframework/stereotype/Component;"), true);
		for (Type t : indexedTypesInHierachy) {
			if (t.isAnnotation()) {
				relevantAnnotations.add(t);
			}
		}
		if (!relevantAnnotations.isEmpty()) {
			return new AbstractMap.SimpleImmutableEntry<Type, List<Type>>(this, relevantAnnotations);
		} else {
			return null;
		}
	}

	/**
	 * @return list of javax.* annotations on this type, directly specified or
	 *         meta-specified.
	 */
	List<Type> getJavaxAnnotations() {
		return getJavaxAnnotations(new HashSet<>());
	}

	private boolean isAnnotated(String Ldescriptor) {
		if (dimensions > 0) {
			return false;
		}
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				if (an.desc.equals(Ldescriptor)) {
					return true;
				}
			}
		}
		return false;
	}

	// // TODO this is broken, don't use!
	// public boolean isAnnotated(String Ldescriptor, boolean checkMetaUsage) {
	// if (dimensions > 0) {
	// return false;
	// }
	// if (checkMetaUsage) {
	// return isMetaAnnotated(Ldescriptor);
	// }
	// if (node.visibleAnnotations != null) {
	// for (AnnotationNode an : node.visibleAnnotations) {
	// if (an.desc.equals(Ldescriptor)) {
	// return true;
	// }
	// }
	// }
	// return false;
	// }

	private List<Type> getAnnotatedElementsInHierarchy(Predicate<AnnotationNode> p) {
		return getAnnotatedElementsInHierarchy(p, new HashSet<>(), false);
	}

	private List<Type> getAnnotatedElementsInHierarchy(Predicate<AnnotationNode> p, boolean includeInterim) {
		return getAnnotatedElementsInHierarchy(p, new HashSet<>(), includeInterim);
	}

	private List<Type> getAnnotatedElementsInHierarchy(Predicate<AnnotationNode> p, Set<String> seen,
			boolean includeInterim) {
		if (dimensions > 0)
			return Collections.emptyList();
		List<Type> results = new ArrayList<>();
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				boolean match = p.test(an);
				if (match || seen.add(an.desc)) {
					if (match) {
						results.add(this);
					} else {
						Type annoType = typeSystem.Lresolve(an.desc, true);
						if (annoType != null) {
							List<Type> ts = annoType.getAnnotatedElementsInHierarchy(p, seen, includeInterim);
							if (ts.size() != 0 && includeInterim) {
								// Include interim enables us to catch intermediate annotations on the route to
								// the one that passes the predicate test.
								results.add(this);
							}
							results.addAll(ts);
						}
					}
				}
			}
		}
		return results.size() > 0 ? results : Collections.emptyList();
	}

	private List<Type> getJavaxAnnotations(Set<String> seen) {
		if (dimensions > 0)
			return Collections.emptyList();
		List<Type> result = new ArrayList<>();
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				if (seen.add(an.desc)) {
					Type annoType = typeSystem.Lresolve(an.desc, true);
					if (annoType != null) {
						if (annoType.getDottedName().startsWith("javax.")) {
							result.add(annoType);
						} else {
							List<Type> ts = annoType.getJavaxAnnotations(seen);
							result.addAll(ts);
						}
					}
				}
			}
		}
		return result;
	}

	public List<Type> getNestedTypes() {
		if (dimensions > 0)
			return Collections.emptyList();
		List<Type> result = null;
		List<InnerClassNode> innerClasses = node.innerClasses;
		for (InnerClassNode inner : innerClasses) {
			if (inner.outerName == null || !inner.outerName.equals(getName())) {
				// System.out.println("SKIPPING "+inner.name+" because outer is
				// "+inner.outerName+" and we are looking at "+getName());
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
		return result == null ? Collections.emptyList() : result;
	}

	public String getDescriptor() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < dimensions; i++) {
			s.append("[");
		}
		s.append("L").append(node.name.replace(".", "/")).append(";");
		return s.toString();
	}

	/**
	 * Find compilation hints directly on this type or used as a meta-annotation on
	 * annotations on this type.
	 */
	public List<HintApplication> getHints() {
		if (dimensions > 0) {
			return Collections.emptyList();
		}
		List<HintApplication> hints = new ArrayList<>();
		List<HintDeclaration> hintDeclarations = typeSystem.findHints(getName());
		if (hintDeclarations.size() != 0) {
			List<Type> s = new ArrayList<>();
			s.add(this);
			for (HintDeclaration hintDeclaration : hintDeclarations) {
				hints.add(new HintApplication(s, Collections.emptyMap(), hintDeclaration));
			}
		}
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				Type annotationType = typeSystem.Lresolve(an.desc, true);
				if (annotationType == null) {
					SpringFeature.log("Couldn't resolve " + an.desc + " annotation type whilst searching for hints on "
							+ getName());
				} else {
					Stack<Type> s = new Stack<>();
					s.push(this);
					annotationType.collectHints(an, hints, new HashSet<>(), s);
				}
			}
		}
		try {
			if (isImportSelector() && hints.size() == 0) {
				// Failing early as this will likely result in a problem later - fix is
				// typically (right now) to add a new Hint in the configuration module
				if (ConfigOptions.areMissingSelectorHintsAnError()) {
					throw new IllegalStateException("No access hint found for import selector: " + getDottedName());
				} else {
					System.out.println("WARNING: No access hint found for import selector: " + getDottedName());
				}
			}
		} catch (MissingTypeException mte) {
			System.out.println("Unable to determine if type " + getName()
					+ " is import selector - can't fully resolve hierarchy - ignoring");
		}
		return hints.size() == 0 ? Collections.emptyList() : hints;
	}

	// TODO handle repeatable annotations everywhere!

	void collectHints(AnnotationNode an, List<HintApplication> hints, Set<AnnotationNode> visited, Stack<Type> annotationChain) {
		if (!visited.add(an)) {
			return;
		}
		try {
			annotationChain.push(this);
			List<HintDeclaration> hintsOnAnnotation = typeSystem.findHints(an.desc);
			if (hintsOnAnnotation.size() != 0) {
				List<String> extractAttributeNames = hintsOnAnnotation.stream().map(hd -> hd.getExtractAttributeNames()).filter(x->x!=null).flatMap(List::stream).collect(Collectors.toList());
				List<String> typesCollectedFromAnnotation = collectTypeReferencesInAnnotation(an, extractAttributeNames);
				if (an.desc.equals(Type.AtEnableConfigurationProperties) && !ConfigOptions.isFunctionalMode()) {
					// TODO special handling here for @EnableConfigurationProperties - should we
					// promote this to a hint annotation value or truly a special case?
					addInners(typesCollectedFromAnnotation);
				}
				for (HintDeclaration hintOnAnnotation : hintsOnAnnotation) {
					hints.add(new HintApplication(new ArrayList<>(annotationChain),
							asMap(typesCollectedFromAnnotation, hintOnAnnotation.skipIfTypesMissing),
							hintOnAnnotation));
				}
			}
			// check for hints on meta annotation
			if (node.visibleAnnotations != null) {
				for (AnnotationNode visibleAnnotation : node.visibleAnnotations) {
					Type annotationType = typeSystem.Lresolve(visibleAnnotation.desc, true);
					if (annotationType == null) {
						SpringFeature.log("Couldn't resolve " + visibleAnnotation.desc
								+ " annotation type whilst searching for hints on " + getName());
					} else {
						annotationType.collectHints(visibleAnnotation, hints, visited, annotationChain);
					}
				}
			}
		} finally {
			annotationChain.pop();
		}
	}
	

	private void addInners(List<String> propertiesTypes) {
		Set<String> collector = new HashSet<>();
		for (String propertiesType : propertiesTypes) {
			Type type = typeSystem.Lresolve(propertiesType, true);
			if (type != null) {
				collectInners(type, collector);
			}
		}
		propertiesTypes.addAll(collector);
	}
	
	private void collectInners(Type type, Set<String> collector) {
		List<Type> nestedTypes = type.getNestedTypes();
		for (Type nestedType: nestedTypes) {
			String name = nestedType.getDescriptor();
			if (collector.add(name)) {
				collectInners(nestedType, collector);
			}
		}
	}

	private Map<String, Integer> asMap(List<String> typesCollectedFromAnnotation, boolean usingForVisibilityCheck) {
		Map<String, Integer> map = new HashMap<>();
		for (String t : typesCollectedFromAnnotation) {
			Type type = typeSystem.Lresolve(t, true);
			int ar = -1;
			if (usingForVisibilityCheck) {
				ar = AccessBits.CLASS;
			} else {
				ar = inferAccessRequired(type);
			}
			if (ar!=AccessBits.NONE) {
				map.put(fromLdescriptorToDotted(t), ar);
			}
		}
		return map;
	}

	private Type[] getMemberTypes() {
		if (dimensions > 0)
			return new Type[0];
		List<Type> result = new ArrayList<>();
		List<InnerClassNode> nestMembers = node.innerClasses;
		if (nestMembers != null) {
			for (InnerClassNode icn : nestMembers) {
				if (icn.name.startsWith(this.getName() + "$")) {
					result.add(typeSystem.resolveSlashed(icn.name));
				}
			}
			System.out.println(this.getName()
					+ " has inners " + nestMembers.stream().map(f -> "oo=" + this.getDescriptor() + "::o=" + f.outerName
							+ "::n=" + f.name + "::in=" + f.innerName).collect(Collectors.joining(","))
					+ "  >> " + result);
		}
		return result.toArray(new Type[0]);
	}
	
	public List<String> getMethodsInvokingGetBean() {
		byte[] bytes = typeSystem.find(getName());
		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
			return GetBeanDetectionVisitor.run(bais);
		} catch (IOException e) {
			throw new IllegalStateException("Unexpected IOException processing bytes for "+this.getName());
		}
	}

	public List<String> getMethodsInvokingAtBeanMethods() {
		byte[] bytes = typeSystem.find(getName());
		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
			return AtBeanMethodInvocationDetectionVisitor.run(typeSystem, bais);
		} catch (IOException e) {
			throw new IllegalStateException("Unexpected IOException processing bytes for "+this.getName());
		}
	}


	/*
	private List<CompilationHint> findCompilationHintHelper(HashSet<Type> visited) {
		if (!visited.add(this)) {
			return null;
		}
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				List<CompilationHint> compilationHints = typeSystem.findHints(an.desc);// SpringConfiguration.findProposedHints(an.desc);
				if (compilationHints.size() != 0) {
					return compilationHints;
				}
				Type resolvedAnnotation = typeSystem.Lresolve(an.desc);
				compilationHints = resolvedAnnotation.findCompilationHintHelper(visited);
				if (compilationHints.size() != 0) {
					return compilationHints;
				}
			}
		}
		return null;
	}
	*/

	private List<String> collectTypeReferencesInAnnotation(AnnotationNode an, List<String> extractAttributeNames) {
		List<Object> values = an.values;
		List<String> importedReferences = new ArrayList<>();
		if (values != null) {
			for (int i = 0; i < values.size(); i += 2) {
				if (values.get(i).equals("value")) {
					// For some annotations it is a list, for some a single class (e.g.
					// @ConditionalOnSingleCandidate)
					Object object = values.get(i + 1);
					if (object instanceof List) {
						List<String> toAdd = ((List<org.objectweb.asm.Type>) object).stream()
								.map(t -> t.getDescriptor()).collect(Collectors.toList());
						importedReferences.addAll(toAdd);
					} else {
						importedReferences.add(((org.objectweb.asm.Type) object).getDescriptor());
					}
				} else if (
				// TODO 'other things to dig type names out of' should be driven by the hint
				// annotation members
				// For now we hard code this to pull conditional types out of
				// ConditionalOnClass.name
				an.desc.equals("Lorg/springframework/boot/autoconfigure/condition/ConditionalOnClass;")
						&& values.get(i).equals("name")) {
					Object object = values.get(i + 1);
					if (object instanceof List) {
						for (String s : (List<String>) object) {
							importedReferences.add("L" + s.replace(".", "/") + ";");
						}
					} else {
						importedReferences.add("L" + ((String) object).replace(".", "/") + ";");
					}
				}
			}
		}
		return importedReferences.size() == 0 ? Collections.emptyList() : importedReferences;
	}

	public boolean isImportSelector() {
		try {
			return implementsInterface(fromLdescriptorToSlashed(ImportSelector));
		} catch (MissingTypeException mte) {
			return false;
		}
	}
	
	public boolean isAtValidated() {
		return (dimensions > 0) ? false : isMetaAnnotated(fromLdescriptorToSlashed(AtValidated));	
	}

	public boolean isApplicationListener() {
		try {
			return implementsInterface(fromLdescriptorToSlashed(ApplicationListener));
		} catch (MissingTypeException mte) {
			return false;
		}
	}

	private boolean isEnvironmentPostProcessor() {
		return implementsInterface(fromLdescriptorToSlashed(EnvironmentPostProcessor),true);
	}

	public boolean isBeanFactoryPostProcessor() {
		try {
			return implementsInterface(fromLdescriptorToSlashed(BeanFactoryPostProcessor));
		} catch (MissingTypeException mte) {
			return false;
		}
	}

	public boolean isImportRegistrar() {
		try {
			return implementsInterface(fromLdescriptorToSlashed(ImportBeanDefinitionRegistrar));
		} catch (MissingTypeException mte) {
			return false;
		}
	}

	public static String fromLdescriptorToSlashed(String Ldescriptor) {
		int dims = 0;
		int p = 0;
		if (Ldescriptor.startsWith("[")) {
			while (Ldescriptor.charAt(p) == '[') {
				p++;
				dims++;
			}
		}
		StringBuilder r = new StringBuilder();
		r.append(Ldescriptor.substring(p + 1, Ldescriptor.length() - 1));
		for (int i = 0; i < dims; i++) {
			r.append("[]");
		}
		return r.toString();
	}

	public static String fromLdescriptorToDotted(String Ldescriptor) {
		return fromLdescriptorToSlashed(Ldescriptor).replace("/", ".");
	}

	/*
	private List<CompilationHint> findCompilationHint(Type annotationType) {
		String descriptor = "L" + annotationType.getName().replace(".", "/") + ";";
		List<CompilationHint> hints = typeSystem.findHints(descriptor);// SpringConfiguration.findProposedHints(descriptor);
		if (hints.size() != 0) {
			return hints;
		} else {
			// check for meta annotation
			return annotationType.findCompilationHintHelper(new HashSet<>());
		}
	}
	*/

	public void collectMissingAnnotationTypesHelper(Set<String> missingAnnotationTypes, HashSet<Type> visited) {
		if (dimensions > 0)
			return;
		if (!visited.add(this)) {
			return;
		}
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				Type annotationType = typeSystem.Lresolve(an.desc, true);
				if (annotationType == null) {
					missingAnnotationTypes.add(an.desc.substring(0, an.desc.length() - 1).replace("/", "."));
				} else {
					annotationType.collectMissingAnnotationTypesHelper(missingAnnotationTypes, visited);
				}
			}
		}
	}

	public int getMethodCount(boolean includeConstructors) {
		if (includeConstructors) {
			return node.methods.size();
		} else {
			int m = 0;
			for (MethodNode methodNode : node.methods) {
				if (!methodNode.name.equals("<init>")) {
					m++;
				}
			}
			return m;
		}
	}

	public boolean isAnnotation() {
		if (dimensions > 0)
			return false;
		return (node.access & Opcodes.ACC_ANNOTATION) != 0;
	}

	public List<Type> getAutoConfigureBeforeOrAfter() {
		if (dimensions > 0)
			return Collections.emptyList();
		List<Type> result = new ArrayList<>();
		if (node.visibleAnnotations!=null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				if (an.desc.equals("Lorg/springframework/boot/autoconfigure/AutoConfigureAfter;")
						|| an.desc.equals("Lorg/springframework/boot/autoconfigure/AutoConfigureBefore;")) {
					List<Object> values = an.values;
					if (values != null) {
						for (int i = 0; i < values.size(); i += 2) {
							if (values.get(i).equals("value")) {
								List<org.objectweb.asm.Type> types = (List<org.objectweb.asm.Type>) values.get(i + 1);
								for (org.objectweb.asm.Type type : types) {
									Type t = typeSystem.Lresolve(type.getDescriptor(), true);
									if (t != null) {
										result.add(t);
									}
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
		if (dimensions > 0)
			return false;
		boolean hasCtor = false;
		List<MethodNode> methods = node.methods;
		for (MethodNode mn : methods) {
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

	/**
	 * Find the @ConfigurationHint annotations on this type (may be more than one)
	 * and from them build CompilationHints, taking care to convert class references
	 * to strings because they may not be resolvable. TODO ok to discard those that
	 * aren't resolvable at this point?
	 *
	 * @return
	 */
	public List<HintDeclaration> unpackConfigurationHints() {
		if (dimensions > 0)
			return Collections.emptyList();
		List<HintDeclaration> hints = null;
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				if (fromLdescriptorToDotted(an.desc).equals(NativeImageHint.class.getName())) {
					HintDeclaration hint = fromConfigurationHintToHintDeclaration(an);
					if (hints == null) {
						hints = new ArrayList<>();
					}
					hints.add(hint);
				} else if (fromLdescriptorToDotted(an.desc).equals(NativeImageHints.class.getName())) {
					List<HintDeclaration> chints = fromConfigurationHintsToCompilationHints(an);
					if (hints == null) {
						hints = new ArrayList<>();
					}
					hints.addAll(chints);
				}
			}
		}
		// TODO support repeatable version
		return hints == null ? Collections.emptyList() : hints;
	}

	private HintDeclaration fromConfigurationHintToHintDeclaration(AnnotationNode an) {
		HintDeclaration ch = new HintDeclaration();
		List<Object> values = an.values;
		if (values != null) {
			for (int i = 0; i < values.size(); i += 2) {
				String key = (String) values.get(i);
				Object value = values.get(i + 1);
				if (key.equals("trigger")) {
					ch.setTriggerTypename(((org.objectweb.asm.Type) value).getClassName());
				} else if (key.equals("applyToFunctional")) {
					ch.setApplyToFunctional((Boolean) value);
				} else if (key.equals("typeInfos")) {
					processTypeInfoList(ch, value);
				} else if (key.equals("importInfos")) {
					processImportInfos(ch, value);
				} else if (key.equals("proxyInfos")) {
					processProxyInfo(ch, value);
				} else if (key.equals("resourcesInfos")) {
					processResourcesInfos(ch, value);
				} else if (key.equals("initializationInfos")) {
					processInitializationInfos(ch, value);
				} else if (key.equals("abortIfTypesMissing")) {
					Boolean b = (Boolean) value;
					ch.setAbortIfTypesMissing(b);
				} else if (key.equals("follow")) {
					Boolean b = (Boolean) value;
					ch.setFollow(b);
				} else if (key.equals("extractTypesFromAttributes")) {
					ch.setAttributesToExtract((List<String>)value);
				} else {
					System.out.println("annotation " + key + "=" + value + "(" + value.getClass() + ")");
				}
			}
		}
		if (ch.getTriggerTypename() == null) {
			ch.setTriggerTypename("java.lang.Object");
		}
		return ch;
	}

	private void processResourcesInfos(HintDeclaration ch, Object value) {
		List<AnnotationNode> resourcesInfos = (List<AnnotationNode>) value;
		for (AnnotationNode resourcesInfo : resourcesInfos) {
			unpackResourcesInfo(resourcesInfo, ch);
		}
	}

	private void processInitializationInfos(HintDeclaration ch, Object value) {
		List<AnnotationNode> initializationInfos = (List<AnnotationNode>) value;
		for (AnnotationNode initializationInfo : initializationInfos) {
			unpackInitializationInfo(initializationInfo, ch);
		}
	}

	private void processTypeInfoList(HintDeclaration ch, Object value) {
		List<AnnotationNode> typeInfos = (List<AnnotationNode>) value;
		for (AnnotationNode typeInfo : typeInfos) {
			unpackTypeInfo(typeInfo, ch);
		}
	}

	private void processFieldInfoList(List<FieldDescriptor> fds, Object value) {
		List<AnnotationNode> fieldInfos = (List<AnnotationNode>) value;
		for (AnnotationNode fieldInfo : fieldInfos) {
			unpackFieldInfo(fieldInfo, fds);
		}
	}

	private void processMethodInfoList(List<MethodDescriptor> mds, Object value) {
		List<AnnotationNode> methodInfos = asList(value, AnnotationNode.class);// (List<AnnotationNode>) value;
		for (AnnotationNode methodInfo : methodInfos) {
			unpackMethodInfo(methodInfo, mds);
		}
	}

	private <T> List<T> asList(Object value, Class<T> type) {
		return (List<T>) value;
	}

	private void processProxyInfo(HintDeclaration ch, Object value) {
		List<AnnotationNode> proxyInfos = (List<AnnotationNode>) value;
		for (AnnotationNode proxyInfo : proxyInfos) {
			unpackProxyInfo(proxyInfo, ch);
		}
	}

	private void processImportInfos(HintDeclaration ch, Object value) {
		List<org.objectweb.asm.Type> importInfos = (ArrayList<org.objectweb.asm.Type>) value;
		for (org.objectweb.asm.Type importInfo : importInfos) {
			String className = importInfo.getClassName();
			Type resolvedImportInfo = typeSystem.resolveDotted(className, true);
			if (resolvedImportInfo == null) {
				throw new IllegalStateException("Cannot find importInfos referenced type: " + className);
			}
			ClassNode node = resolvedImportInfo.getClassNode();
			if (node.visibleAnnotations != null) {
				for (AnnotationNode an : node.visibleAnnotations) {
					String annotationClassname = fromLdescriptorToDotted(an.desc);
					if (annotationClassname.equals(TypeInfo.class.getName())) {
						unpackTypeInfo(an, ch);
					} else if (annotationClassname.equals(TypeInfos.class.getName())) {
						processTypeInfosList(ch, an);
					} else if (annotationClassname.equals(ResourcesInfo.class.getName())) {
						unpackResourcesInfo(an, ch);
					} else if (annotationClassname.equals(ResourcesInfos.class.getName())) {
						processRepeatableInfosList(an, anno -> unpackResourcesInfo(anno, ch));
					} else if (annotationClassname.equals(ProxyInfo.class.getName())) {
						unpackProxyInfo(an, ch);
					} else if (annotationClassname.equals(ProxyInfos.class.getName())) {
						processRepeatableInfosList(an, anno -> unpackProxyInfo(anno, ch));
					} else if (annotationClassname.equals(InitializationInfo.class.getName())) {
						unpackInitializationInfo(an, ch);
					} else if (annotationClassname.equals(InitializationInfos.class.getName())) {
						processRepeatableInfosList(an, anno -> unpackInitializationInfo(anno, ch));
					}
				}
			}

		}
	}

	ClassNode getClassNode() {
		return node;
	}

	private void unpackTypeInfo(AnnotationNode typeInfo, HintDeclaration ch) {
		List<Object> values = typeInfo.values;
		List<org.objectweb.asm.Type> types = new ArrayList<>();
		List<String> typeNames = new ArrayList<>();
		int accessRequired = -1;
		List<MethodDescriptor> mds = new ArrayList<>();
		List<FieldDescriptor> fds = new ArrayList<>();
		for (int i = 0; i < values.size(); i += 2) {
			String key = (String) values.get(i);
			Object value = values.get(i + 1);
			if (key.equals("types")) {
				types = (ArrayList<org.objectweb.asm.Type>) value;
			} else if (key.equals("access")) {
				accessRequired = (Integer) value;
			} else if (key.equals("typeNames")) {
				typeNames = (ArrayList<String>) value;
			} else if (key.equals("methods")) {
				processMethodInfoList(mds, value);
			} else if (key.equals("fields")) {
				processFieldInfoList(fds, value);
			}
		}
		for (org.objectweb.asm.Type type : types) {
			AccessDescriptor ad = null;
			if (accessRequired == -1) {
				ad = new AccessDescriptor(inferAccessRequired(type, mds, fds), mds, fds, true);
			} else {
				if ((MethodDescriptor.includesConstructors(mds) || MethodDescriptor.includesStaticInitializers(mds)) && 
						AccessBits.isSet(accessRequired, AccessBits.DECLARED_METHODS|AccessBits.PUBLIC_METHODS)) {
					throw new IllegalStateException("Do not include global method reflection access when specifying individual methods");
				}
				ad = new AccessDescriptor(accessRequired, mds, fds, false);
			}
			ch.addDependantType(type.getClassName(), ad);
		}
		for (String typeName : typeNames) {
			Type resolvedType = typeSystem.resolveName(typeName, true);
			if (resolvedType != null) {
				AccessDescriptor ad = null;
				if (accessRequired == -1) {
					ad = new AccessDescriptor(inferAccessRequired(resolvedType), mds, fds, true);
				} else {
					if ((MethodDescriptor.includesConstructors(mds) || MethodDescriptor.includesStaticInitializers(mds)) && 
							AccessBits.isSet(accessRequired, AccessBits.DECLARED_METHODS|AccessBits.PUBLIC_METHODS)) {
						throw new IllegalStateException("Do not include global method reflection access when specifying individual methods");
					}
					ad = new AccessDescriptor(accessRequired, mds, fds, false);
				}
				ch.addDependantType(typeName, ad);
			}
		}
	}

	private Integer inferAccessRequired(org.objectweb.asm.Type type, List<MethodDescriptor> mds,
			List<FieldDescriptor> fds) {
		int inferredAccess = inferAccessRequired(type);
		int originalAccess = inferredAccess;
		// Adjust inferred access if explicit method/constructor/field references specified
		boolean includesConstructors = MethodDescriptor.includesConstructors(mds);
		boolean includesMethods = MethodDescriptor.includesMethods(mds);
		if (includesMethods) {
			if ((inferredAccess & AccessBits.DECLARED_METHODS)!= 0) {
				inferredAccess-=AccessBits.DECLARED_METHODS;
			}
			if ((inferredAccess & AccessBits.PUBLIC_METHODS)!= 0) {
				inferredAccess-=AccessBits.PUBLIC_METHODS;
			}
		}
		if (includesConstructors) {
			if ((inferredAccess & AccessBits.DECLARED_CONSTRUCTORS) ==0) {
				inferredAccess-=AccessBits.DECLARED_CONSTRUCTORS;
			}
		}
		if (fds!=null && !fds.isEmpty()) {
			if ((inferredAccess & AccessBits.DECLARED_FIELDS)!= 0) {
				inferredAccess-=AccessBits.DECLARED_FIELDS;
			}
		}
		if (inferredAccess != originalAccess) {
			SpringFeature.log("Modifying default inferred access to "+type.getClassName()+" from "+
					AccessBits.toString(originalAccess)+" to "+AccessBits.toString(inferredAccess));
		}
		return inferredAccess;
	}

	private void unpackFieldInfo(AnnotationNode fieldInfo, List<FieldDescriptor> fds) {
		List<Object> values = fieldInfo.values;
		String name = null;
		boolean allowUnsafeAccess = false; // default is false
		for (int i = 0; i < values.size(); i += 2) {
			String key = (String) values.get(i);
			Object value = values.get(i + 1);
			if (key.equals("allowUnsafeAccess")) {
				allowUnsafeAccess = (Boolean) value;
			} else if (key.equals("name")) {
				name = (String) value;
			}
		}
		fds.add(new FieldDescriptor(name, allowUnsafeAccess));
	}

	private void unpackMethodInfo(AnnotationNode methodInfo, List<MethodDescriptor> mds) {
		List<Object> values = methodInfo.values;
		List<org.objectweb.asm.Type> parameterTypes = new ArrayList<>();
		String name = null;
		for (int i = 0; i < values.size(); i += 2) {
			String key = (String) values.get(i);
			Object value = values.get(i + 1);
			if (key.equals("parameterTypes")) {
				parameterTypes = (ArrayList<org.objectweb.asm.Type>) value;
			} else if (key.equals("name")) {
				name = (String) value;
			}
		}
		boolean unresolvable = false;
		List<String> resolvedParameterTypes = new ArrayList<>();
		for (org.objectweb.asm.Type ptype : parameterTypes) {
			String typeName = ptype.getClassName();
			Type resolvedType = typeSystem.resolveName(typeName, true);
			if (resolvedType != null) {
				resolvedParameterTypes.add(typeName);
			} else {
				unresolvable = true;
			}
		}
		if (unresolvable) {
			StringBuilder message = new StringBuilder();
			for (org.objectweb.asm.Type ptype : parameterTypes) {
				message.append(ptype.getClassName()).append(" ");
			}
			SpringFeature.log("Unable to fully resolve method " + name + "(" + message.toString().trim() + ")");
		} else {
			mds.add(new MethodDescriptor(name, resolvedParameterTypes));
		}
	}

	private void unpackProxyInfo(AnnotationNode typeInfo, HintDeclaration ch) {
		List<Object> values = typeInfo.values;
		List<org.objectweb.asm.Type> types = new ArrayList<>();
		List<String> typeNames = new ArrayList<>();
		for (int i = 0; i < values.size(); i += 2) {
			String key = (String) values.get(i);
			Object value = values.get(i + 1);
			if (key.equals("types")) {
				types = (ArrayList<org.objectweb.asm.Type>) value;
			} else if (key.equals("typeNames")) {
				typeNames = (ArrayList<String>) value;
			}
		}
		// Note: Proxies hints will get discarded immediately if types are not around
		List<String> proxyTypes = new ArrayList<>();
		boolean typeMissing = false;
		for (org.objectweb.asm.Type type : types) {
			String typeName = type.getClassName();
			Type resolvedType = typeSystem.resolveName(typeName, true);
			if (resolvedType != null) {
				proxyTypes.add(typeName);
			} else {
				typeMissing = true;
			}
		}
		for (String typeName : typeNames) {
			Type resolvedType = typeSystem.resolveName(typeName, true);
			if (resolvedType != null) {
				proxyTypes.add(typeName);
			} else {
				typeMissing = true;
			}
		}
		if (!typeMissing) {
			ch.addProxyDescriptor(new ProxyDescriptor(proxyTypes.toArray(new String[0])));
		}
	}

	private void unpackResourcesInfo(AnnotationNode typeInfo, HintDeclaration ch) {
		List<Object> values = typeInfo.values;
		List<String> patterns = null;
		Boolean isBundle = null;
		for (int i = 0; i < values.size(); i += 2) {
			String key = (String) values.get(i);
			Object value = values.get(i + 1);
			if (key.equals("patterns")) {
				patterns = (ArrayList<String>) value;
			} else if (key.equals("isBundle")) {
				isBundle = (Boolean) value;
			}
		}
		ch.addResourcesDescriptor(
				new ResourcesDescriptor(patterns.toArray(new String[0]), isBundle == null ? false : isBundle));
	}

	private void unpackInitializationInfo(AnnotationNode typeInfo, HintDeclaration ch) {
		List<Object> values = typeInfo.values;
		List<org.objectweb.asm.Type> types = new ArrayList<>();
		List<String> typeNames = new ArrayList<>();
		List<String> packageNames = new ArrayList<>();
		InitializationTime initTime = null;
		for (int i = 0; i < values.size(); i += 2) {
			String key = (String) values.get(i);
			Object value = values.get(i + 1);
			if (key.equals("types")) {
				types = (ArrayList<org.objectweb.asm.Type>) value;
			} else if (key.equals("typeNames")) {
				typeNames = (ArrayList<String>) value;
			} else if (key.equals("packageNames")) {
				packageNames = (ArrayList<String>) value;
			} else if (key.equals("initTime")) {
				initTime = InitializationTime.valueOf(((String[]) value)[1]);
			}
		}
		for (org.objectweb.asm.Type type : types) {
			String typeName = type.getClassName();
			typeNames.add(typeName);
		}
		InitializationDescriptor id = new InitializationDescriptor();
		if (initTime == InitializationTime.BUILD) {
			for (String typeName : typeNames) {
				id.addBuildtimeClass(typeName);
			}
			for (String packageName : packageNames) {
				id.addBuildtimePackage(packageName);
			}
		} else {
			for (String typeName : typeNames) {
				id.addRuntimeClass(typeName);
			}
			for (String packageName : packageNames) {
				id.addRuntimePackage(packageName);
			}
		}
		ch.addInitializationDescriptor(id);
	}

	private int inferAccessRequired(org.objectweb.asm.Type type) {
		Type t = typeSystem.resolve(type, true);
		return inferAccessRequired(t);
	}
	
	public boolean isConfigurationProperties() {
		return (dimensions > 0) ? false
				: isMetaAnnotated(fromLdescriptorToSlashed(AtConfigurationProperties), new HashSet<>());
	}

	public static int inferAccessRequired(Type t) {
		if (t == null) {
			return AccessBits.FULL_REFLECTION;
		}
		if (t.isAtConfiguration() || t.isMetaImportAnnotated()) {
			return AccessBits.CONFIGURATION;
		} else if (t.isImportSelector()) {
			return AccessBits.LOAD_AND_CONSTRUCT | AccessBits.RESOURCE;
		} else if (t.isImportRegistrar()) {
			return AccessBits.LOAD_AND_CONSTRUCT | AccessBits.RESOURCE; // Including resource because of KafkaBootstrapConfiguration
		} else if (t.isBeanFactoryPostProcessor()) {
			// vanilla-jpa demos these needing accessing a a resource *sigh*
			// TODO investigate if deeper pattern can tell us why certain things need
			// RESOURCE
			return AccessBits.LOAD_AND_CONSTRUCT | AccessBits.DECLARED_METHODS | AccessBits.RESOURCE;
		} else if (t.isArray()) {
			return AccessBits.CLASS;
		} else if (t.isConfigurationProperties()) {
			if (!ConfigOptions.isFunctionalMode()) {
				if (t.isAtValidated()) {
					return AccessBits.CLASS | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_CONSTRUCTORS | AccessBits.DECLARED_FIELDS;
				} else {
					return AccessBits.CLASS | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_CONSTRUCTORS;
				}
			} else {
				SpringFeature.log("Skipping registration of reflective access to configuration properties: "+t.getDottedName());
				return AccessBits.NONE;
			}
		} else if (t.isCondition()) {
			return AccessBits.CLASS | AccessBits.DECLARED_CONSTRUCTORS;
		} else if (t.isComponent() || t.isApplicationListener()) {
			return AccessBits.ALL;
		} else if (t.isEnvironmentPostProcessor()) {
			return AccessBits.LOAD_AND_CONSTRUCT;
		} else {
			return AccessBits.FULL_REFLECTION;//-AccessBits.DECLARED_FIELDS;
		}
	}

	private List<HintDeclaration> fromConfigurationHintsToCompilationHints(AnnotationNode an) {
		List<HintDeclaration> chs = new ArrayList<>();
		List<Object> values = an.values;
		for (int i = 0; i < values.size(); i += 2) {
			String key = (String) values.get(i);
			Object value = values.get(i + 1);
			if (key.equals("value")) {
				// value=[org.objectweb.asm.tree.AnnotationNode@63e31ee,
				// org.objectweb.asm.tree.AnnotationNode@68fb2c38]
				List<AnnotationNode> annotationNodes = (List<AnnotationNode>) value;
				for (int j = 0; j < annotationNodes.size(); j++) {
					chs.add(fromConfigurationHintToHintDeclaration(annotationNodes.get(j)));
				}
			}
		}
		return chs;
	}

	private void processTypeInfosList(HintDeclaration ch, AnnotationNode an) {
		List<Object> values = an.values;
		for (int i = 0; i < values.size(); i += 2) {
			String key = (String) values.get(i);
			Object value = values.get(i + 1);
			if (key.equals("value")) {
				// value=[org.objectweb.asm.tree.AnnotationNode@63e31ee,
				// org.objectweb.asm.tree.AnnotationNode@68fb2c38]
				List<AnnotationNode> annotationNodes = (List<AnnotationNode>) value;
				for (int j = 0; j < annotationNodes.size(); j++) {
					unpackTypeInfo(annotationNodes.get(j), ch);
				}
			}
		}
	}

	private void processRepeatableInfosList(AnnotationNode an, Consumer<AnnotationNode> c) {
		List<Object> values = an.values;
		for (int i = 0; i < values.size(); i += 2) {
			String key = (String) values.get(i);
			Object value = values.get(i + 1);
			if (key.equals("value")) {
				// value=[org.objectweb.asm.tree.AnnotationNode@63e31ee,
				// org.objectweb.asm.tree.AnnotationNode@68fb2c38]
				List<AnnotationNode> annotationNodes = (List<AnnotationNode>) value;
				for (int j = 0; j < annotationNodes.size(); j++) {
					c.accept(annotationNodes.get(j));
				}
			}
		}
	}

	public List<HintDeclaration> getCompilationHints() {
		if (dimensions > 0)
			return Collections.emptyList();
		return unpackConfigurationHints();
	}

	public int getDimensions() {
		return dimensions;
	}

	public boolean isArray() {
		return dimensions > 0;
	}

	/**
	 * @return true if type or a method inside is marked @Transactional
	 */
	public boolean isTransactional() {
		// TODO are these usable as meta annotations?
		return isAnnotated(AtTransactional) || isAnnotated(AtJavaxTransactional);
	}

	public boolean hasTransactionalMethods() {
		// TODO meta annotation usage?
		List<Method> methodsWithAtTransactional = getMethodsWithAnnotation(AtTransactional);
		if (methodsWithAtTransactional.size() > 0) {
			return true;
		}
		List<Method> methodsWithAtJavaxTransactional = getMethodsWithAnnotation(AtJavaxTransactional);
		if (methodsWithAtJavaxTransactional.size() > 0) {
			return true;
		}
		return false;
	}

	public boolean isAnnotatedInHierarchy(String anno) {
		if (isAnnotated(AtTransactional)) {
			return true;
		}
		List<Method> methodsWithAnnotation = getMethodsWithAnnotation(anno);
		if (methodsWithAnnotation.size() != 0) {
			return true;
		}
		Type superclass = this.getSuperclass();
		if (superclass != null) {
			if (superclass.isAnnotatedInHierarchy(anno)) {
				return true;
			}
		}
		Type[] intfaces = this.getInterfaces();
		if (intfaces != null) {
			for (Type intface : intfaces) {
				if (intface.isAnnotatedInHierarchy(anno)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<String> collectTypeParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isAtRepository() {
		return isAnnotated(AtRepository);
	}

	public boolean isAtResponseBody() {
		boolean b = hasAnnotation(AtResponseBody, true);
		// System.out.println("Checking if " + getName() + " is @ResponseBody meta
		// annotated: " + b);
		return b;
	}

	public Collection<Type> collectAtMappingMarkedReturnTypes() {
		Set<Type> returnTypes = new LinkedHashSet<>();
		List<Method> methodsWithAnnotation = getMethodsWithAnnotation(AtMapping, true);
		for (Method m : methodsWithAnnotation) {
			returnTypes.add(m.getReturnType());
		}
		return returnTypes;
	}

	public String findTypeParameterInSupertype(String supertype, int typeParameterNumber) {
		if (node.signature == null) {
			return null;
		}
		SignatureReader reader = new SignatureReader(node.signature);
		TypeParamFinder tpm = new TypeParamFinder(supertype);
		reader.accept(tpm);
		return tpm.getTypeParameter(typeParameterNumber);
	}

	static class TypeParamFinder extends SignatureVisitor {

		String typeparameter;

		List<String> types = null;

		private String supertype;

		private boolean nextIsBoundType = false;

		private boolean collectTypeParams = false;

		private List<String> typeParams = new ArrayList<>();

		public TypeParamFinder(String supertype) {
			super(Opcodes.ASM9);
			this.supertype = supertype.replace(".", "/");
		}

		public String getTypeParameter(int typeParameterNumber) {
			if (typeParameterNumber >= typeParams.size()) {
				return null;
			}
			return typeParams.get(typeParameterNumber).replace("/", ".");
		}

		@Override
		public SignatureVisitor visitSuperclass() {
			nextIsBoundType = true;
			collectTypeParams = false;
			return super.visitSuperclass();
		}

		@Override
		public SignatureVisitor visitInterface() {
			nextIsBoundType = true;
			collectTypeParams = false;
			return super.visitInterface();
		}

		@Override
		public void visitClassType(String name) {
			if (nextIsBoundType && name.equals(supertype)) {
				// Hit the bound type of interest, the next few types are what we should collect
				collectTypeParams = true;
			} else if (collectTypeParams) {
				typeParams.add(name);
			}
			super.visitClassType(name);
			nextIsBoundType = false;
		}
	}

	public String fetchReactiveCrudRepositoryType() {
		return findTypeParameterInSupertype("org.springframework.data.repository.reactive.ReactiveCrudRepository", 0);
	}

	public String fetchCrudRepositoryType() {
		return findTypeParameterInSupertype("org.springframework.data.repository.CrudRepository", 0);
	}

	public String fetchJpaRepositoryType() {
		return findTypeParameterInSupertype("org.springframework.data.jpa.repository.JpaRepository", 0);
	}

	/**
	 * Verify this type as a component, checking everything is set correctly for
	 * native-image construction to succeed.
	 *
	 */
	public void verifyComponent() {
		List<String> methodsInvokingAtBeanMethods = null;
		try {
			methodsInvokingAtBeanMethods = getMethodsInvokingAtBeanMethods();
		} catch (Exception e) {
			// Probably a MissingTypeException trying to resolve something not on the classpath -
			// in this case we can't correctly verify something, but it *probably* isn't getting used anyway
		}
		if (methodsInvokingAtBeanMethods != null) {
			throw new IllegalStateException("ERROR: in '"+getDottedName()+"' these methods are directly invoking methods marked @Bean: "+
					methodsInvokingAtBeanMethods+" - due to the enforced proxyBeanMethods=false for components in a native-image, please consider "+
					"refactoring to use instance injection.");
		}
	}

	private void verifyProxyBeanMethodsSetting() {
		List<Method> methodsWithAtBean = getMethodsWithAtBean();
		if (methodsWithAtBean.size() != 0) {
			List<AnnotationNode> annos = collectAnnotations();
			annos = filterAnnotations(annos, an -> {
				Type annotationType = typeSystem.Lresolve(an.desc);
				return annotationType.hasMethod("proxyBeanMethods");
			});
			// Rule:
			// At least one annotation in the list has to be setting proxyBeanMethods=false.
			// Some may not supply it if they are being aliased by other values.
			// TODO this does not check the default value for proxyBeanMethods
			// TODO this does not verify/follow AliasFor usage
			boolean atLeastSetFalseSomewhere = false;
			if (ClassUtils.isPresent(getDottedName().replace("$", "_") + "Cached", null)) {
				return;
			}
			for (AnnotationNode anode : annos) {
				if (hasValue(anode, "proxyBeanMethods")) {
					Boolean value = (Boolean) getValue(anode, "proxyBeanMethods");
					if (!value) {
						atLeastSetFalseSomewhere = true;
					}
				}
			}
			if (!atLeastSetFalseSomewhere) {
				System.out.println("[verification] Warning: component " + this.getDottedName()
						+ " does not specify annotation value proxyBeanMethods=false to avoid CGLIB proxies");
			}
		}
	}

	/**
	 * For an {@link AnnotationNode} retrieve the value for a particular attribute,
	 * will throw an exception if no value is set for that attribute.
	 *
	 * @param anode the annotation node whose values should be checked
	 * @param name  the annotation attribute name being searched for
	 * @return the value of that attribute if set on that annotation node
	 * @throws IllegalStateException if that attribute name is not set
	 */
	private Object getValue(AnnotationNode anode, String name) {
		List<Object> values = anode.values;
		for (int i = 0; i < values.size(); i += 2) {
			if (values.get(i).toString().equals(name)) {
				return values.get(i + 1);
			}
		}
		return new IllegalStateException("Attribute " + name
				+ " not set on the specified annotation, precede this call to getValue() with a hasValue() check");
	}

	/**
	 * For an {@link AnnotationNode} check if it specifies a value for a particular
	 * attribute.
	 *
	 * @param node the annotation node whose values should be checked
	 * @param name the annotation attribute name being searched for
	 * @return true if the annotation did specify a value for that attribute
	 */
	private boolean hasValue(AnnotationNode node, String name) {
		List<Object> values = node.values;
		if (values != null) {
			for (int i = 0; i < values.size(); i += 2) {
				if (values.get(i).toString().equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasMethod(String methodName) {
		List<MethodNode> methods = node.methods;
		for (MethodNode method : methods) {
			if (method.name.equals(methodName)) {
				return true;
			}
		}
		return false;
	}

	private List<AnnotationNode> filterAnnotations(List<AnnotationNode> input, Predicate<AnnotationNode> filter) {
		return input.stream().filter(filter).collect(Collectors.toList());
	}

	private List<AnnotationNode> collectAnnotations() {
		List<AnnotationNode> resultsHolder = new ArrayList<>();
		collectAnnotationsHelper(resultsHolder, new HashSet<String>());
		return resultsHolder;
	}

	private void collectAnnotationsHelper(List<AnnotationNode> collector, Set<String> seen) {
		if (node.visibleAnnotations != null) {
			for (AnnotationNode anno : node.visibleAnnotations) {
				if (!seen.add(anno.desc)) {
					continue;
				}
				collector.add(anno);
				Type annotationType = typeSystem.Lresolve(anno.desc);
				annotationType.collectAnnotationsHelper(collector, seen);
			}
		}
	}

	public List<Method> getMethods(Predicate<Method> predicate) {
		return dimensions > 0 ? Collections.emptyList()
				: node.methods.stream().map(m -> wrap(m)).filter(m -> predicate.test(m)).collect(Collectors.toList());
	}
	
	public List<Method> getMethod(String name) {
		List<Method> results = new ArrayList<>();
		for (int i = 0; i < node.methods.size(); i++) {
			MethodNode methodNode = node.methods.get(i);
			if (methodNode.name.equals(name)) {
				results.add(wrap(methodNode));
			}
		}
		return results;
	}

	public boolean equals(Object that) {
		return (that instanceof Type) && (((Type) that).name.equals(this.name))
				&& (((Type) that).dimensions == this.dimensions) && (((Type) that).node.equals(this.node));
	}

	public int hashCode() {
		return node.hashCode() * 37;
	}

	/**
	 * TODO: This is a little crude, relying on patterns rather than classfile
	 * encoded data.
	 * 
	 * @return the guessed enclosing type, if resolvable
	 */
	public Type getEnclosingType() {
		String n = this.getDottedName();
		int idx = n.lastIndexOf("$");
		if (idx == -1) {
			return null;
		}
		Type t = typeSystem.resolveDotted(n.substring(0, idx), true);
		if (t == null) {
			return null;
		} else {
			return t;
		}
	}

	/**
	 * Check if there is a @ConditionalOnWebApplication annotation on this type. If
	 * there is, check that web application condition immediately (which is related
	 * to whether a certain type is on the classpath).
	 * 
	 * @return false if there is an @COWA and the specified web application type
	 *         requirement cannot be met, otherwise true
	 */
	public boolean checkConditionalOnWebApplication() {
		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				if (an.desc.equals("Lorg/springframework/boot/autoconfigure/condition/ConditionalOnWebApplication;")) {
					List<Object> values = an.values;
					if (values != null) {
						for (int i = 0; i < values.size(); i += 2) {
							if (values.get(i).equals("type")) {
								// [Lorg/springframework/boot/autoconfigure/condition/ConditionalOnWebApplication$Type;,
								// SERVLET]
								String webApplicationType = ((String[]) values.get(i + 1))[1];
								// SERVLET, REACTIVE, ANY
								if (webApplicationType.equals("SERVLET")) {
									// If GenericWebApplicationContext not around this check on SERVLET cannot pass
									return typeSystem.resolveDotted(
											"org.springframework.web.context.support.GenericWebApplicationContext",
											true) != null;
								}
								if (webApplicationType.equals("REACTIVE")) {
									// If HandlerResult not around this check on REACTIVE cannot pass
									return typeSystem.resolveDotted("org.springframework.web.reactive.HandlerResult",
											true) != null;
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * For annotation types this will return true if any of the members of the
	 * annotation are using @AliasFor (implying they need a proxy at runtime) For
	 * example:
	 * 
	 * <pre>
	 * <code>
	 * &#64;AliasFor(annotation = RequestMapping.class)
	 * String name() default "";
	 * </code>
	 * </pre>
	 */
	public boolean hasAliasForMarkedMembers() {
		for (Method m : getMethods()) {
			if (m.hasAliasForAnnotation()) {
				return true;
			}
		}
		return false;
	}

	public void collectAliasReferencedMetas(Set<String> collector) {
		// If this gets set the type is using @AliasFor amongst its own
		// attributes, not specifying one of them is an alias for
		// an attribute in a seperate meta annotation.
		boolean usesLocalAliases = false;
		for (Method m : getMethods()) {
			Pair<String, Boolean> aliasForInfo = m.getAliasForSummary();
			if (aliasForInfo != null) {
				String relatedMetaAnnotation = aliasForInfo.getA();
				if (relatedMetaAnnotation == null) {
					usesLocalAliases |= aliasForInfo.getB();
				} else {
					collector.add(relatedMetaAnnotation);
				}
			}
		}
		if (usesLocalAliases) {
			collector.add(getDottedName());
		}
	}

	/**
	 * Example: see @EnableR2dbcRepositories in
	 * R2dbcRepositoresAutoConfigureRegistrar:
	 * 
	 * <pre>
	 * <code>
	 * &#64;EnableR2dbcRepositories 
	 * private static class EnableR2dbcRepositoriesConfiguration { }
	 * </code>
	 * </pre>
	 * 
	 * The @Enable... thing looks like this:
	 * 
	 * <pre>
	 * <code>
	 * &#64;Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) @Documented @Inherited
	 * &#64;Import(R2dbcRepositoriesRegistrar.class) 
	 * public @interface EnableR2dbcRepositories {
	 * </code>
	 * </pre>
	 * 
	 * So this method is checking if a class is meta annotated with @Import
	 */
	public boolean isMetaImportAnnotated() {
		return isMetaAnnotated(fromLdescriptorToSlashed(AtImports));
	}

	public boolean isComponent() {
		return isMetaAnnotated(fromLdescriptorToSlashed(AtComponent));
	}
	
	public boolean implementsInterface(String interfaceDescriptor, boolean silent) {
		try {
			return implementsInterface(interfaceDescriptor);
		} catch (MissingTypeException mte) {
			if (silent) {
				return false;
			} else {
				throw mte;
			}
		}
	}

	public boolean isConditional() {
		// Extends Condition or has @Conditional related annotation on it
		if (implementsInterface("org/springframework/context/annotation/Condition",true)
				|| isMetaAnnotated("org/springframework/context/annotation/Conditional")) {
			return true;
		} else {
			return false;
		}
	}

	public void collectAnnotations(List<Type> collector, Predicate<Type> filter) {
		if (!this.isAnnotation()) {
			throw new IllegalStateException(this.getDottedName() + " is not an annotation");
		}
		walkAnnotation(collector, filter, new HashSet<>());
	}

	private void walkAnnotation(List<Type> collector, Predicate<Type> filter, Set<Type> seen) {
		if (!seen.add(this)) {
			return;
		}
		if (filter.test(this)) {
			collector.add(this);
		}
		List<Type> annotations = getAnnotations();
		for (Type annotation : annotations) {
			annotation.walkAnnotation(collector, filter, seen);
		}
	}

	public Field getField(String name) {
		for (Field field : fields.get()) {
			if (field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}

	public List<String> getImportedConfigurations() {
		List<String> importedConfigurations = findAnnotationValueWithHostAnnotation(AtImportAutoConfiguration, false,
				new HashSet<>()).get(this.getDottedName());
		return (importedConfigurations == null ? Collections.emptyList() : importedConfigurations);
	}

	private AnnotationNode getAnnotation(String Ldescriptor) {
		if (node.visibleAnnotations !=null) {
			for (AnnotationNode annotationNode : node.visibleAnnotations) {
				if (annotationNode.desc.equals(Ldescriptor)) {
					return annotationNode;
				}
			}
		}
		return null;
	}
	
	/**
	 * Find any @ConditionalOnProperty and see if the specified property should be checked at build time. If it should be and
	 * fails the check, return the property name in question.
	 * 
	 * @return the property name if it fails a check, otherwise null (meaning everything is OK)
	 */
	public String testAnyConditionalOnProperty() {
		// Examples:
		//	found COP on org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
		//	copDescriptor: @COP(names=[spring.flyway.enabled],matchIfMissing=true
		//  found COP on org.springframework.boot.autoconfigure.h2.H2ConsoleAutoConfiguration
		//	copDescriptor @COP(names=[spring.h2.console.enabled],havingValue=true,matchIfMissing=false
		AnnotationNode annotation = getAnnotation(AtConditionalOnProperty);
		if (annotation != null) {
			ConditionalOnPropertyDescriptor copDescriptor = unpackConditionalOnPropertyAnnotation(annotation);
			Map<String,String> activeProperties = typeSystem.getActiveProperties();
			return copDescriptor.test(activeProperties);
		}
		return null;
	}
	
	private ConditionalOnEnabledMetricsExportDescriptor unpackConditionalOnEnabledMetricsExportDescriptor(AnnotationNode annotation) {
		List<Object> values = annotation.values;
		String name = null;
		for (int i=0;i<values.size();i++) {
			String attributeName = (String)values.get(i);
			if (attributeName.equals("name") || attributeName.equals("value")) {
				name = (String)values.get(++i);
			}
		}
		return new ConditionalOnEnabledMetricsExportDescriptor(name);
	}

	static class ConditionalOnEnabledMetricsExportDescriptor implements TestableDescriptor {

		private String metricsExporter;

		public ConditionalOnEnabledMetricsExportDescriptor(String metricsExporter) {
			this.metricsExporter = metricsExporter;
		}

		public String test(Map<String, String> properties) {
			String key = "management.metrics.export."+metricsExporter+".enabled";
			if (!ConfigOptions.buildTimeCheckableProperty(key)) {
				return null;
			}
			String value = properties.get(key);
			if (value!=null && !value.equalsIgnoreCase("true")) {
				return null;
			}
			String defaultKey = "management.metrics.export.defaults.enabled";
			if (!ConfigOptions.buildTimeCheckableProperty(defaultKey)) {
				return null;
			}
			String defaultValue = properties.get(defaultKey);
			if (defaultValue!=null && !defaultValue.equalsIgnoreCase("true")) {
				return null;
			}
			return "neither "+key+" nor "+defaultKey+" are true";
		}

		public String toString() {
			return "@COEME("+metricsExporter+")";
		}

	}

	
	private ConditionalOnPropertyDescriptor unpackConditionalOnPropertyAnnotation(AnnotationNode annotation) {
		List<Object> values = annotation.values;
		List<String> names = new ArrayList<>();
		String prefix = null;
		String havingValue = null;
		boolean matchIfMissing = false;;
		for (int i=0;i<values.size();i++) {
			String attributeName = (String)values.get(i);
			if (attributeName.equals("name") || attributeName.equals("value")) {
				names.addAll((List<String>)values.get(++i));
			} else if (attributeName.equals("prefix")) {
				prefix = (String)values.get(++i);
			} else if (attributeName.equals("matchIfMissing")) {
				matchIfMissing = (Boolean)values.get(++i);
			} else if (attributeName.equals("havingValue")) {
				havingValue = (String)values.get(++i);
			} else {
				throw new IllegalStateException("Not expecting attribute named '"+attributeName+"' in ConditionalOnProperty annotation");
			}
		}
		if (prefix != null) {
			if (!prefix.endsWith(".")) {
				prefix = prefix + ".";
			}
			for (int i=0;i<names.size();i++) {
				names.set(i, prefix+names.get(i));
			}
		}
		return new ConditionalOnPropertyDescriptor(names, havingValue, matchIfMissing);
	}
	
	
	private String fetchAnnotationAttributeValueAsString(AnnotationNode annotation, String attributeName) {
		List<Object> values = annotation.values;
		for (int i=0;i<values.size();i+=2) {
			String n = (String)values.get(i);
			if (n.equals(attributeName)) {
				return (String)values.get(i+1);
			}
		}
		return null;
	}

	private Type fetchAnnotationAttributeValueAsClass(AnnotationNode annotation, String attributeName) {
		List<Object> values = annotation.values;
		for (int i=0;i<values.size();i+=2) {
			String n = (String)values.get(i);
			if (n.equals(attributeName)) {
				String cn = ((org.objectweb.asm.Type)values.get(i+1)).getClassName();
				return typeSystem.resolveDotted(cn,true);
			}
		}
		return null;
	}
	
	// @ConditionalOnEnabledHealthIndicator("cassandra")
	private ConditionalOnEnabledHealthIndicatorDescriptor unpackConditionalOnEnabledHealthIndicatorAnnotation(AnnotationNode annotation) {
		String value = fetchAnnotationAttributeValueAsString(annotation, "value");
		return new ConditionalOnEnabledHealthIndicatorDescriptor(value);
	}

	private ConditionalOnAvailableEndpointDescriptor unpackConditionalOnAvailableEndpointAnnotation(AnnotationNode annotation) {
		Type endpointClass = fetchAnnotationAttributeValueAsClass(annotation, "endpoint");
		AnnotationNode endpointAnnotation = endpointClass.getAnnotation(AtEndpoint);
		if (endpointAnnotation == null) {
			System.out.println("Couldn't find @Endpoint on "+endpointClass.getName());
			// We are seeing meta usage of the endpoint annotation
			endpointAnnotation = endpointClass.getAnnotationMetaAnnotatedWith(AtEndpoint);
			if (endpointAnnotation == null) {
				System.out.println("Couldn't find meta usage of @Endpoint on "+endpointClass.getName());
			}
		}
		String endpointId = fetchAnnotationAttributeValueAsString(endpointAnnotation,"id");
		// TODO pull in enableByDefault option from endpointAnnotation
		return new ConditionalOnAvailableEndpointDescriptor(endpointId);
	}

	private AnnotationNode getAnnotationMetaAnnotatedWith(String Ldescriptor) {
		if (node.visibleAnnotations !=null) {
			for (AnnotationNode annotationNode : node.visibleAnnotations) {
				if (annotationNode.desc.equals(Ldescriptor)) {
					return annotationNode;
				}
				Type resolvedAnnotationType = typeSystem.Lresolve(annotationNode.desc,true);
				if (resolvedAnnotationType != null) {
					String slashedDesc = Ldescriptor.substring(1,Ldescriptor.length()-1);
					if (resolvedAnnotationType.isMetaAnnotated(slashedDesc)) {
						return annotationNode;
					}
				}
			}
		}
		return null;
	}


	interface TestableDescriptor {
		String test(Map<String, String> properties);
	}

	static class ConditionalOnEnabledHealthIndicatorDescriptor implements TestableDescriptor {

		private String value;

		public ConditionalOnEnabledHealthIndicatorDescriptor(String value) {
			this.value = value;
		}

		public String test(Map<String, String> properties) {
			String key = "management.health."+value+".enabled";
			if (!ConfigOptions.buildTimeCheckableProperty(key)) {
				return null;
			}
			String value = properties.get(key);
			if (value!=null && !value.equalsIgnoreCase("true")) {
				return null;
			}
			String defaultKey = "management.health.defaults.enabled";
			if (!ConfigOptions.buildTimeCheckableProperty(defaultKey)) {
				return null;
			}
			String defaultValue = properties.get(defaultKey);
			if (defaultValue!=null && !defaultValue.equalsIgnoreCase("true")) {
				return null;
			}
			return "neither "+key+" nor "+defaultKey+" are true";
		}

		public String toString() {
			return "@COEHI("+value+")";
		}

	}
	
	static class ConditionalOnAvailableEndpointDescriptor implements TestableDescriptor {

		private String endpointId;

		public ConditionalOnAvailableEndpointDescriptor(String endpointId) {
			this.endpointId = endpointId;
		}

		// https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html
		// Crude first pass - ignore JMX exposure and only consider web exposure include (not exclude)
		public String test(Map<String, String> properties) {
			String key = "management.endpoints.web.exposure.include";
			if (!ConfigOptions.buildTimeCheckableProperty(key)) {
				return null;
			}
			String webExposedEndpoints = properties.get(key);
			if (webExposedEndpoints==null) {
				webExposedEndpoints="";
			}
			webExposedEndpoints+=(webExposedEndpoints.length()==0?"info,health":",info,health");
			String[] exposedEndpointIds = webExposedEndpoints.split(",");
			for (String exposedEndpointId: exposedEndpointIds) {
				if (exposedEndpointId.equals(endpointId)) {
					SpringFeature.log("COAE check: endpoint "+endpointId+" *is* exposed via management.endpoints.web.exposed.include");
					return null;
				}
			}
			SpringFeature.log("COAE check: endpoint "+endpointId+" *is not* exposed via management.endpoints.web.exposed.include");
			return "management.endpoints.web.exposed.include="+webExposedEndpoints+" does not contain endpoint "+endpointId;
		}

		public String toString() {
			return "@COAE(id="+endpointId+")";
		}

	}

	static class ConditionalOnPropertyDescriptor implements TestableDescriptor {

		private List<String> propertyNames;
		private String havingValue;
		private boolean matchIfMissing;

		public ConditionalOnPropertyDescriptor(List<String> names, String havingValue, boolean matchIfMissing) {
			this.propertyNames = names;
			this.havingValue = havingValue;
			this.matchIfMissing = matchIfMissing;
		}
		
		public String test(Map<String, String> properties) {
			for (String name: propertyNames) {
				if (!ConfigOptions.buildTimeCheckableProperty(name)) {
					return null;
				}
				String definedValue = properties.get(name);
				if ((havingValue == null && !(definedValue==null || definedValue.toLowerCase().equals("false"))) ||
					(havingValue != null && definedValue!=null && definedValue.toLowerCase().equals(havingValue.toLowerCase()))) {
					// all is well!
				} else if (matchIfMissing && definedValue == null) {
					if (ConfigOptions.shouldRespectMatchIfMissing()) {
						// all is well
					} else {
						return name+(havingValue==null?"":"="+havingValue)+" property check failed because configuration indicated matchIfMissing should be ignored";
					}
				} else {
					return name+(havingValue==null?"":"="+havingValue)+" property check failed "+(definedValue==null?"":" against the discovered value "+definedValue);
				}
			}
			return null;
		}

		public String toString() {
			return "@COP(names="+propertyNames+","+(havingValue==null?"":"havingValue="+havingValue+",")+"matchIfMissing="+matchIfMissing;
		}
		
	}
	
	// Examples:
	// @ConditionalOnEnabledMetricsExport("simple")
	public String testAnyConditionalOnEnabledMetricsExport() {
		AnnotationNode annotation = getAnnotation(AtConditionalOnEnabledMetricsExport);
		if (annotation != null) {
			ConditionalOnEnabledMetricsExportDescriptor coemedDescriptor = unpackConditionalOnEnabledMetricsExportDescriptor(annotation);
			Map<String,String> activeProperties = typeSystem.getActiveProperties();
			return coemedDescriptor.test(activeProperties);
		}
		return null;
	}

	// Example:
	// @ConditionalOnAvailableEndpoint(endpoint = FlywayEndpoint.class) public class FlywayEndpointAutoConfiguration {
	// @Endpoint(id = "flyway") public class FlywayEndpoint {
	public String testAnyConditionalOnAvailableEndpoint() {
		AnnotationNode annotation = getAnnotation(AtConditionalOnAvailableEndpoint);
		if (annotation != null) {
			ConditionalOnAvailableEndpointDescriptor coaeDescriptor = unpackConditionalOnAvailableEndpointAnnotation(annotation);
			Map<String,String> activeProperties = typeSystem.getActiveProperties();
			return coaeDescriptor.test(activeProperties);
		}
		return null;
	}
	
	// Example:
	// @ConditionalOnEnabledHealthIndicator("diskspace")
	// @AutoConfigureBefore(HealthContributorAutoConfiguration.class)
	// @EnableConfigurationProperties(DiskSpaceHealthIndicatorProperties.class)
	// public class DiskSpaceHealthContributorAutoConfiguration {
	public String testAnyConditionalOnEnabledHealthIndicator() {
		AnnotationNode annotation = getAnnotation(AtConditionalOnEnabledHealthIndicator);
		if (annotation != null) {
			ConditionalOnEnabledHealthIndicatorDescriptor coehiDescriptor = unpackConditionalOnEnabledHealthIndicatorAnnotation(annotation);
			Map<String,String> activeProperties = typeSystem.getActiveProperties();
			return coehiDescriptor.test(activeProperties);
		}
		return null;
	}

	public TypeSystem getTypeSystem() {
		return this.typeSystem;
	}

	public boolean hasAnnotatedField(Predicate<String> annotationCheck) {
		if (node.fields != null) {
			for (FieldNode fieldNode : node.fields) {
				List<AnnotationNode> vAnnotations = fieldNode.visibleAnnotations;
				if (vAnnotations!=null) {
					if (annotationCheck == null) {
						return true;
					}
					for (AnnotationNode vAnnotation : vAnnotations) {
						String d = vAnnotation.desc;
						if (annotationCheck.test(d.substring(1,d.length()-1).replace("/", "."))) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Does the specified method on this type have the @Bean annotation on it.
	 */
	public boolean isAtBeanMethod(String methodName, String descriptor) {
		List<MethodNode> ms = this.node.methods;
		for (MethodNode m : ms) {
			if (m.name.equals(methodName) && m.desc.equals(descriptor)) {
				List<AnnotationNode> vas = m.visibleAnnotations;
				if (vas != null) {
					for (AnnotationNode va : vas) {
						if (va.desc.equals(AtBean)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}

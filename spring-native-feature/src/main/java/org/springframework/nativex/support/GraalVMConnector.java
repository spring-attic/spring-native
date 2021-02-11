/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.nativex.support;

import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;

import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.impl.RuntimeReflectionSupport;
import org.graalvm.util.GuardedAnnotationAccess;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.domain.reflect.Flag;
import org.springframework.nativex.domain.reflect.MethodDescriptor;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;
import org.springframework.nativex.type.Type;

import com.oracle.svm.core.configure.ResourcesRegistry;
import com.oracle.svm.core.hub.ClassForNameSupport;
import com.oracle.svm.core.jdk.Resources;
import com.oracle.svm.core.jdk.proxy.DynamicProxyRegistry;
import com.oracle.svm.hosted.ImageClassLoader;
import com.oracle.svm.hosted.config.ReflectionRegistryAdapter;

/**
 * Acts as a bridge from the ConfigurationCollector to the Graal build system. Enables the general processing not to be aware
 * of the destination for configuration (pure json from the collector, or runtime API configuration if running as a feature).
 * 
 * @author Andy Clement
 */
public class GraalVMConnector implements Connector {

	private ImageClassLoader imageClassLoader;

	private ReflectionRegistryAdapter rra;

	private ResourcesRegistry resourcesRegistry;

	public GraalVMConnector(ImageClassLoader imageClassLoader) {
		this.imageClassLoader = imageClassLoader;
		RuntimeReflectionSupport rrs = ImageSingletons.lookup(RuntimeReflectionSupport.class);
		this.rra = new ReflectionRegistryAdapter(rrs, imageClassLoader);
		resourcesRegistry = ImageSingletons.lookup(ResourcesRegistry.class);
	}

	public void addProxy(List<String> interfaceNames) {
		Class<?>[] interfaces = new Class<?>[interfaceNames.size()];
		for (int i = 0; i < interfaceNames.size(); i++) {
			String className = interfaceNames.get(i);
			Class<?> clazz = imageClassLoader.findClassByName(className, false);
			if (clazz == null) {
				return;
			}
			if (!clazz.isInterface()) {
				return;
			}
			interfaces[i] = clazz;
		}
		addProxy(interfaces);
	}
	
	public void addProxy(Class<?>[] interfaces) {
		DynamicProxyRegistry dynamicProxySupport = ImageSingletons.lookup(DynamicProxyRegistry.class);
		dynamicProxySupport.addProxyClass(interfaces);
	}
	
	public void addReflectionDescriptor(ReflectionDescriptor reflectionDescriptor) {
		for (ClassDescriptor classDescriptor : reflectionDescriptor.getClassDescriptors()) {
			addClassDescriptor(classDescriptor);
		}
	}
	
	public void addClassDescriptor(ClassDescriptor classDescriptor) {
		// The call on this next line and the need to guard with checkType on the
		// register call feel dirty
		// They are here because otherwise we start getting warnings to system.out -
		// need graal bug to tidy this up
		// Feels crude...
		String name2 = classDescriptor.getName();
		int dims = 0;
		while (name2.endsWith("[]")) {
			name2 = name2.substring(0,name2.length()-2);
			dims++;
		}
		Class<?> type = imageClassLoader.findClassByName(name2,false);
		if (type == null) {
			return;
		}
		while (dims!=0) {
			type = Array.newInstance(type, 0).getClass();
			dims--;
		}
		ClassForNameSupport.registerClass(type);
		// TODO need a checkType() kinda guard on here? (to avoid rogue printouts from graal)
		boolean specificConstructorsSpecified = false;
		boolean specificMethodsSpecified = false;
		boolean specificFieldsSpecified = false;
		if (classDescriptor.getMethods()!=null) {
		for (MethodDescriptor md: classDescriptor.getMethods()) {
			String name = md.getName();
			List<Class<?>> params = new ArrayList<>();
			List<String> paramTypes = md.getParameterTypes();
			for (String paramType: paramTypes) {
				// TODO should use type system and catch problems?
				params.add(rra.resolveType(paramType));
			}
			try {
				if (name.equals("<init>")) {
					specificConstructorsSpecified=true;
					rra.registerConstructor(type, params);
				} else {
					specificMethodsSpecified=true;
					try {
						rra.registerMethod(type, name, params);
					} catch (NoClassDefFoundError ncdfe) {
						SpringFeature.log("skipping problematic registration of method: "+name+" missing class: "+ncdfe.getMessage());
					}
				}
			} catch (NoSuchMethodException nsme) {
				throw new IllegalStateException(
						"Problem registering member " + name + " for reflective access on type " + type, nsme);
			}
		}
		}
		if (classDescriptor.getFields()!=null) {
		for (FieldDescriptor fd: classDescriptor.getFields()) {
			try {
				specificFieldsSpecified=true;
				rra.registerField(type, fd.getName(), fd.isAllowWrite(), fd.isAllowUnsafeAccess());
			} catch (NoSuchFieldException nsfe) {
				throw new IllegalStateException(
						"Problem registering field " + fd.getName() + " for reflective access on type " + type, nsfe);
			}
		}
		}
		if (checkType(type)) {
			rra.registerType(type);
			Set<Flag> flags = classDescriptor.getFlags();
			if (flags !=null) {
			for (Flag flag : flags) {
				try {
					switch (flag) {
					case allDeclaredClasses:
						if (verify(type.getDeclaredClasses())) {
							rra.registerDeclaredClasses(type);
						}
						break;
					case allDeclaredFields:
						if (specificFieldsSpecified) {
							// TODO when there is time sort out to ensure flags for fields not set if specific fields specified (we do something like this already for methods)
							break;
//							throw new IllegalStateException("Why allDeclaredFields when specific fields set for "+type);
						}
						if (verify(type.getDeclaredFields())) {
							rra.registerDeclaredFields(type);
						}
						break;
					case allPublicFields:
						if (specificFieldsSpecified) {
							break;
//							throw new IllegalStateException();
						}
						if (verify(type.getFields())) {
							rra.registerPublicFields(type);
						}
						break;
					case allDeclaredConstructors:
						if (specificConstructorsSpecified) {
							throw new IllegalStateException();
						}
						if (verify(type.getDeclaredConstructors())) {
							rra.registerDeclaredConstructors(type);
						}
						break;
					case allPublicConstructors:
						if (specificConstructorsSpecified) {
							throw new IllegalStateException();
						}
						if (verify(type.getConstructors())) {
							rra.registerPublicConstructors(type);
						}
						break;
					case allDeclaredMethods:
						if (specificMethodsSpecified) {
							throw new IllegalStateException();
						}
						if (verify(type.getDeclaredMethods())) {
							rra.registerDeclaredMethods(type);
						}
						break;
					case allPublicMethods:
						if (specificMethodsSpecified) {
							throw new IllegalStateException();
						}
						if (verify(type.getMethods())) {
							rra.registerPublicMethods(type);
						}
						break;
					case allPublicClasses:
						if (verify(type.getClasses())) {
							rra.registerPublicClasses(type);
						}
						break;
					}
				} catch (NoClassDefFoundError ncdfe) {
					SpringFeature.log("WARNING: problem handling flag: " + flag + " for " + type.getName()
							+ " because of missing " + ncdfe.getMessage());
				}
			}
			}
		}
	}
		
		// static file handling one..
//		int missingFromClasspathCount = 0;
//		int flagHandlingCount = 0;
//		Class<?> type = null;
//		String n2 = classDescriptor.getName();
//		if (n2.endsWith("[]")) {
//			type = rra.resolveType(n2.substring(0, n2.length() - 2));
//			if (type != null) {
//				Object o = Array.newInstance(type, 1);
//				type = o.getClass();
//			}
//		} else {
//			type = rra.resolveType(classDescriptor.getName());
//		}
//		if (checkType(type)) {
//			activeClassDescriptors.add(classDescriptor);
//			rra.registerType(type);
//			Set<Flag> flags = classDescriptor.getFlags();
//			if (flags != null) {
//				for (Flag flag : flags) {
//					try {
//						switch (flag) {
//						case allDeclaredClasses:
//							rra.registerDeclaredClasses(type);
//							break;
//						case allDeclaredFields:
//							rra.registerDeclaredFields(type);
//							break;
//						case allPublicFields:
//							rra.registerPublicFields(type);
//							break;
//						case allDeclaredConstructors:
//							rra.registerDeclaredConstructors(type);
//							break;
//						case allPublicConstructors:
//							rra.registerPublicConstructors(type);
//							break;
//						case allDeclaredMethods:
//							rra.registerDeclaredMethods(type);
//							break;
//						case allPublicMethods:
//							rra.registerPublicMethods(type);
//							break;
//						case allPublicClasses:
//							rra.registerPublicClasses(type);
//							break;
//						}
//					} catch (NoClassDefFoundError ncdfe) {
//						flagHandlingCount++;
//						SpringFeature.log(RESOURCE_FILE + " problem handling flag: " + flag + " for "
//								+ type.getName() + " because of missing " + ncdfe.getMessage());
//					}
//				}
//			}
//		}
//
//		// Process all specific methods defined in the input class descriptor (including
//		// constructors)
//		List<MethodDescriptor> methods = classDescriptor.getMethods();
//		if (methods != null) {
//			for (MethodDescriptor methodDescriptor : methods) {
//				String n = methodDescriptor.getName();
//				List<String> parameterTypes = methodDescriptor.getParameterTypes();
//				if (parameterTypes == null) {
//					if (n.equals("<init>")) {
//						rra.registerAllConstructors(type);
//					} else {
//						rra.registerAllMethodsWithName(type, n);
//					}
//				} else {
//					List<Class<?>> collect = parameterTypes.stream().map(pname -> rra.resolveType(pname))
//							.collect(Collectors.toList());
//					try {
//						if (n.equals("<init>")) {
//							rra.registerConstructor(type, collect);
//						} else {
//							rra.registerMethod(type, n, collect);
//						}
//					} catch (NoSuchMethodException nsme) {
//						throw new IllegalStateException("Couldn't find: " + methodDescriptor.toString(), nsme);
//					}
//				}
//			}
//		}
//
//		// Process all specific fields defined in the input class descriptor
//		List<FieldDescriptor> fields = classDescriptor.getFields();
//		if (fields != null) {
//			for (FieldDescriptor fieldDescriptor : fields) {
//				try {
//					rra.registerField(type, fieldDescriptor.getName(), fieldDescriptor.isAllowWrite(),
//							fieldDescriptor.isAllowUnsafeAccess());
//				} catch (NoSuchFieldException nsfe) {
//					throw new IllegalStateException(
//							"Couldn't find field: " + type.getName() + "." + fieldDescriptor.getName(), nsfe);
////					System.out.println("SBG: WARNING: skipping reflection registration of field "+type.getName()+"."+fieldDescriptor.getName()+": field not found");
//				}
//			}
//		}
//	}
	
	public boolean checkType(Type t) {
		Class<?> findClassByName = imageClassLoader.findClassByName(t.getDottedName(),false);
		if (findClassByName == null) {
			return false;
		} else {
			return checkType(findClassByName);
		}
	}

	// TODO verify should look at the difference between declared vs non-declared (since it includes super members in the latter case)
			private boolean checkType(Class<?> clazz) {
				try {
				clazz.getDeclaredFields();
				clazz.getFields();
				clazz.getDeclaredMethods();
				clazz.getMethods();
				clazz.getDeclaredConstructors();
				clazz.getConstructors();
				clazz.getDeclaredClasses();
				clazz.getClasses();
				} catch (NoClassDefFoundError e) {
				return false;
			}
				return true;
			}
			

			private boolean verify(Object[] things) {
				for (Object o : things) {
					try {
						if (o instanceof Method) {
							((Method) o).getGenericReturnType();
						}
						if (o instanceof Field) {
							((Field) o).getGenericType();
						}
						if (o instanceof AccessibleObject) {
							AccessibleObject accessibleObject = (AccessibleObject) o;
							GuardedAnnotationAccess.getDeclaredAnnotations(accessibleObject);
						}
		
						if (o instanceof Parameter) {
							Parameter parameter = (Parameter) o;
							parameter.getType();
						}
						if (o instanceof Executable) {
							Executable e = (Executable) o;
							e.getGenericParameterTypes();
							e.getGenericExceptionTypes();
							e.getParameters();
						}
					} catch (Exception e) {
						SpringFeature.log("WARNING: Possible reflection problem later due to (generics related) reference from "
								+ o + " to " + e.getMessage());
						return false;
					}
				}
				return true;
			}

	public void addResourcesDescriptor(ResourcesDescriptor resourcesDescriptor) {
		
	}

	public void registerResource(String resourceName, InputStream bais) {
		Resources.registerResource(resourceName, bais);
	}

	public void addResource(String pattern, boolean isBundle) {
		if (isBundle) {
			try {
				resourcesRegistry.addResourceBundles(pattern);
			} catch (MissingResourceException mre) {
				SpringFeature.log("WARNING: resource bundle "+pattern+" could not be registered");
			}
		} else {
			resourcesRegistry.addResources(pattern);
		}
	}

	public void initializeAtBuildTime(String... typenames) {
		for (String typename: typenames) {
			try {
				RuntimeClassInitialization.initializeAtBuildTime(imageClassLoader.findClassByName(typename));
			} catch (Throwable e) {
//				throw new IllegalStateException("Unexpected - type " + typename +" cannot be found!",e);
			}
		}
	}

	public void initializeAtRunTime(String[] typenames) {
	for (String typename: typenames) {
		try {
			RuntimeClassInitialization.initializeAtRunTime(imageClassLoader.findClassByName(typename));
		} catch (Throwable e ) {
//			throw new IllegalStateException("Unexpected - type " + typename +" cannot be found!",e);
		}
	}
	}

	public void initializeAtBuildTimePackages(String[] packageNames) {
		RuntimeClassInitialization.initializeAtBuildTime(packageNames);
	}

	public void initializeAtRunTimePackages(String[] packageNames) {
		RuntimeClassInitialization.initializeAtRunTime(packageNames);
	}

	public void initializeAtRunTime(List<Type> types) {
		for (Type type: types) {
			try {
			Class<?> clazz = imageClassLoader.findClassByName(type.getDottedName());
			RuntimeClassInitialization.initializeAtRunTime(clazz);
			} catch (Throwable t) {
				
			}
		}
	}

}

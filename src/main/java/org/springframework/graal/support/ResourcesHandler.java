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
package org.springframework.graal.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature.BeforeAnalysisAccess;
import org.springframework.graal.domain.reflect.ClassDescriptor.Flag;
import org.springframework.graal.domain.resources.ResourcesDescriptor;
import org.springframework.graal.domain.resources.ResourcesJsonMarshaller;
import org.springframework.graal.type.HintDescriptor;
import org.springframework.graal.type.MissingTypeException;
import org.springframework.graal.type.Type;
import org.springframework.graal.type.TypeSystem;

import com.oracle.svm.core.jdk.Resources;
import com.oracle.svm.hosted.FeatureImpl.BeforeAnalysisAccessImpl;
import com.oracle.svm.hosted.ImageClassLoader;
import com.oracle.svm.core.configure.ResourcesRegistry;

public class ResourcesHandler {

	private TypeSystem ts;
	
	private ImageClassLoader cl;
	
	private ReflectionHandler reflectionHandler;
	
	private static boolean REMOVE_UNNECESSARY_CONFIGURATIONS;
	
	static {
		REMOVE_UNNECESSARY_CONFIGURATIONS = Boolean.valueOf(System.getProperty("removeUnusedAutoconfig","false"));
		System.out.println("Remove unused config = "+REMOVE_UNNECESSARY_CONFIGURATIONS);
	}

	public ResourcesHandler(ReflectionHandler reflectionHandler) {
		this.reflectionHandler = reflectionHandler;
	}

	public ResourcesDescriptor compute() {
		try {
			InputStream s = this.getClass().getResourceAsStream("/resources.json");
			ResourcesDescriptor read = ResourcesJsonMarshaller.read(s);
			return read;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void register(BeforeAnalysisAccess access) {
		cl = ((BeforeAnalysisAccessImpl) access).getImageClassLoader();
		ts = TypeSystem.get(cl.getClasspath());
		ResourcesDescriptor rd = compute();
		ResourcesRegistry resourcesRegistry = ImageSingletons.lookup(ResourcesRegistry.class);
		// Patterns can be added to the registry, resources can be directly registered
		// against Resources
		// resourcesRegistry.addResources("*");
		// Resources.registerResource(relativePath, inputstream);
		System.out.println("SBG: adding resources - #" + rd.getPatterns().size()+" patterns");

		for (String pattern : rd.getPatterns()) {
			if (pattern.equals("META-INF/spring.factories")) {
				continue; // leave to special handling...
			}
//			if (pattern.contains("logging.properties")) {
//				URL resource = cl.getClassLoader().getResource(pattern);
//				System.out.println("Can I find "+pattern+"?  "+resource);
//			}
			resourcesRegistry.addResources(pattern);
		}
		processSpringFactories();
		processSpringComponents();
	}
	
	public void processSpringComponents() {
		Enumeration<URL> springComponents = fetchResources("META-INF/spring.components");
		if (springComponents.hasMoreElements()) {
			log("Processing META-INF/spring.components files...");
			while (springComponents.hasMoreElements()) {
				URL springFactory = springComponents.nextElement();
				processSpringComponents(ts, springFactory);
			}
		} else {
//			System.out.println("No META-INF/spring.components found");
			System.out.println("Found no META-INF/spring.components -> generating one...");
			List<Entry<String, String>> components = scanClasspathForIndexedStereotypes();
			List<Entry<String,String>> filteredComponents = filterComponents(components);
			Properties p = new Properties();
			for (Entry<String,String> filteredComponent: filteredComponents) {
				String k = filteredComponent.getKey();
				System.out.println("- "+k);
				p.put(k, filteredComponent.getValue());
				reflectionHandler.addAccess(k,Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allDeclaredClasses);
				ResourcesRegistry resourcesRegistry = ImageSingletons.lookup(ResourcesRegistry.class);
				resourcesRegistry.addResources(k.replace(".", "/")+".class");
				processComponent(k, new HashSet<>());
            }
            System.out.println("Computed spring.components is ");
			System.out.println("vvv");
			p.list(System.out);
			System.out.println("^^^");
			System.out.println(">>> "+p.toString());
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				p.store(baos,"");
				baos.close();
				byte[] bs = baos.toByteArray();
				ByteArrayInputStream bais = new ByteArrayInputStream(bs);
				Resources.registerResource("META-INF/spring.components", bais);
				System.out.println("BAOS: "+new String(bs));
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	private void processComponent(String typename, Set<String> visited) {
		if (!visited.add(typename)) {
			return;
		}
		ResourcesRegistry resourcesRegistry = ImageSingletons.lookup(ResourcesRegistry.class);
		Type componentType = ts.resolveDotted(typename);
		System.out.println("> Component processing: "+typename);
		List<String> conditionalTypes = componentType.findConditionalOnClassValue();
		if (conditionalTypes != null) {
			for (String lDescriptor : conditionalTypes) {
				Type t = ts.Lresolve(lDescriptor, true);
				boolean exists = (t != null);
				if (!exists) {
					return;
				} else {
					try {
						reflectionHandler.addAccess(lDescriptor.substring(1,lDescriptor.length()-1).replace("/", "."),Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
						resourcesRegistry.addResources(lDescriptor.substring(1,lDescriptor.length()-1)+".class");
					} catch (NoClassDefFoundError e) {
						System.out.println("Conditional type "+fromLtoDotted(lDescriptor)+" not found for component "+componentType.getName());
					}
					
				}
			}
		}
		try {
			// String configNameDotted = configType.getName().replace("/",".");
			System.out.println("Including auto-configuration "+typename);
			reflectionHandler.addAccess(typename,Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
			resourcesRegistry.addResources(typename.replace(".", "/")+".class");
		} catch (NoClassDefFoundError e) {
			// Example:
			// PROBLEM? Can't register Type:org/springframework/boot/autoconfigure/web/servlet/HttpEncodingAutoConfiguration because cannot find javax/servlet/Filter
			// java.lang.NoClassDefFoundError: javax/servlet/Filter
			// ... at com.oracle.svm.hosted.config.ReflectionRegistryAdapter.registerDeclaredConstructors(ReflectionRegistryAdapter.java:97)
			System.out.println("PROBLEM? Can't register "+typename+" because cannot find "+e.getMessage());
		}
		
		Map<String,List<String>> imports = componentType.findImports();
		if (imports != null) {
			System.out.println("Imports found on "+typename+" are "+imports);
			for (Map.Entry<String,List<String>> importsEntry: imports.entrySet()) {
				reflectionHandler.addAccess(importsEntry.getKey(),Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
				for (String imported: importsEntry.getValue()) {
					String importedName = fromLtoDotted(imported);
					try {
						Type t = ts.resolveDotted(importedName);
						processComponent( t.getName().replace("/", "."), visited);
					} catch (MissingTypeException mte) {
						System.out.println("Cannot find imported "+importedName+" so skipping processing that");
					}
				}
			}
		}
		
		// Without this code, error at:
		// java.lang.ClassNotFoundException cannot be cast to java.lang.Class[]
		// at org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector$ConfigurationPropertiesBeanRegistrar.lambda$collectClasses$1(EnableConfigurationPropertiesImportSelector.java:80)
		List<String> ecProperties = componentType.findEnableConfigurationPropertiesValue();
		if (ecProperties != null) {
			for (String ecPropertyDescriptor: ecProperties) {
				String ecPropertyName = fromLtoDotted(ecPropertyDescriptor);
				System.out.println("ECP "+ecPropertyName);
				try {
					reflectionHandler.addAccess(ecPropertyName,Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
					resourcesRegistry.addResources(ecPropertyName.replace(".", "/")+".class");
				} catch (NoClassDefFoundError e) {
					System.out.println("Not found for registration: "+ecPropertyName);
				}
			}
		}
		
		// Find @Bean methods and add them
//		List<Method> methodsWithAtBean = configType.getMethodsWithAtBean();
//		if (methodsWithAtBean.size() != 0) {
//			System.out.println(configType+" here they are: "+
//			methodsWithAtBean.stream().map(m -> m.getName()+m.getDesc()).collect(Collectors.toList()));
//			for (Method m: methodsWithAtBean) {
//				String desc = m.getDesc();
//				String retType = desc.substring(desc.lastIndexOf(")")+1); //Lorg/springframework/boot/task/TaskExecutorBuilder;
//				System.out.println("@Bean return type "+retType);
//				reflectionHandler.addAccess(fromLtoDotted(retType), Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
//			}
//		}
		
		List<Type> nestedTypes = componentType.getNestedTypes();
		for (Type t: nestedTypes) {
			if (visited.add(t.getName())) {
				processComponent(t.getName().replace("/", "."), visited);
			}
		}
	}

	private void processSpringComponents(TypeSystem ts, URL springComponentsFile) {	
		Properties p = new Properties();
		loadSpringFactoryFile(springComponentsFile, p);
		// Example:
		// com.example.demo.Foobar=org.springframework.stereotype.Component
		// com.example.demo.DemoApplication=org.springframework.stereotype.Component
		Enumeration<Object> keys = p.keys();
		ResourcesRegistry resourcesRegistry = ImageSingletons.lookup(ResourcesRegistry.class);
		while (keys.hasMoreElements()) {
			String k = (String)keys.nextElement();
			System.out.println("Registering Spring Component: "+k);
			reflectionHandler.addAccess(k,Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allDeclaredClasses);
			resourcesRegistry.addResources(k.replace(".", "/")+".class");
			// Register nested types of the component
			Type baseType = ts.resolveDotted(k);
			for (Type t: baseType.getNestedTypes()) {
				String n = t.getName().replace("/", ".");
				reflectionHandler.addAccess(n,Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allDeclaredClasses);
				resourcesRegistry.addResources(t.getName()+".class");
			}
			registerHierarchy(baseType, new HashSet<>(), resourcesRegistry);
		}
	}
	
	public void registerHierarchy(Type t, Set<Type> visited, ResourcesRegistry resourcesRegistry) {
		if (t == null || t.getName().equals("java/lang/Object") || !visited.add(t)) {
			return;
		}
		String desc = t.getName();
		System.out.println("Hierarchy registration of "+t.getName());
		reflectionHandler.addAccess(desc.replace("/", "."),Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allDeclaredClasses);
		resourcesRegistry.addResources(desc.replace("$", ".")+".class");			
		Type s = t.getSuperclass();
		registerHierarchy(s, visited, resourcesRegistry);
		Type[] is = t.getInterfaces();
		for (Type i: is) { 
			registerHierarchy(i, visited, resourcesRegistry);
		}
		// TODO inners of those supertypes/interfaces?
	}

	/**
	 * Find all META-INF/spring.factories - for any configurations listed in each, check if those configurations use ConditionalOnClass.
	 * If the classes listed in ConditionalOnClass can't be found, discard the configuration from spring.factories. Register either
	 * the unchanged or modified spring.factories files with the system.
	 */
	public void processSpringFactories() {
		log("Processing META-INF/spring.factories files...");
		Enumeration<URL> springFactories = fetchResources("META-INF/spring.factories");
		while (springFactories.hasMoreElements()) {
			URL springFactory = springFactories.nextElement();
			processSpringFactory(ts, springFactory);
		}
	}
	
	
	private List<Entry<String, String>> filterComponents(List<Entry<String, String>> as) {
		List<Entry<String,String>> filtered = new ArrayList<>();
		List<Entry<String,String>> subtypesToRemove = new ArrayList<>();
		for (Entry<String,String> a: as) {
			String type = a.getKey();
			subtypesToRemove.addAll(as.stream().filter(e -> e.getKey().startsWith(type+"$")).collect(Collectors.toList()));
		}
		filtered.addAll(as);
		filtered.removeAll(subtypesToRemove);
		return filtered;
	}

	private List<Entry<String,String>> scanClasspathForIndexedStereotypes() {
		return findDirectories(ts.getClasspath())
			.flatMap(this::findClasses)
			.map(this::typenameOfClass)
			.map(this::isIndexedOrEntity)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	
// app.main.SampleTransactionManagementConfiguration=org.springframework.stereotype.Component
// app.main.model.Foo=javax.persistence.Entity
// app.main.model.FooRepository=org.springframework.data.repository.Repository
// app.main.SampleApplication=org.springframework.stereotype.Component
	private Entry<String,String> isIndexedOrEntity(String slashedClassname) {
		Entry<String,String> entry = ts.resolveSlashed(slashedClassname).isIndexedOrEntity();
//		if (entry != null) {
//			System.out.println("isIndexed for "+slashedClassname+" returned "+entry);
//		}
		return entry;
	}
	
	private String typenameOfClass(File f) {
		return Utils.scanClass(f).getClassname();
	}

	private Stream<File> findClasses(File dir) {
		ArrayList<File> classfiles = new ArrayList<>();
		walk(dir,classfiles);
		return classfiles.stream();
	}

	private void walk(File dir, ArrayList<File> classfiles) {
		File[] fs = dir.listFiles();
		for (File f: fs) {
			if (f.isDirectory()) {
				walk(f,classfiles);
			} else if (f.getName().endsWith(".class")) {
				classfiles.add(f);
			}
		}
	}

	private Stream<File> findDirectories(List<String> classpath) {
		List<File> directories = new ArrayList<>();
		for (String classpathEntry: classpath) {
			File f = new File(classpathEntry);
			if (f.isDirectory()) {
				directories.add(f);
			}
		}
		return directories.stream();
	}
	
	private void processSpringFactory(TypeSystem ts, URL springFactory) {
		List<String> forRemoval = new ArrayList<>();
		Properties p = new Properties();
		loadSpringFactoryFile(springFactory, p);
		
		Enumeration<Object> keyz = p.keys();
		// Handle all keys except EnableAutoConfiguration
		while (keyz.hasMoreElements()) {
			String k = (String)keyz.nextElement();
			if (!k.equals("org.springframework.boot.autoconfigure.EnableAutoConfiguration")) {
				String classesList = p.getProperty(k);
				for (String s: classesList.split(",")) {
					try {
						reflectionHandler.addAccess(s,Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
						System.out.println("NEEDS ADDING TO RESOURCE LIST? "+s);
					} catch (NoClassDefFoundError ncdfe) {
						System.out.println("SBG: WARNING: Whilst processing "+k+" problem adding access for type: "+s+" because of missing "+ncdfe.getMessage());
					}
				}				
			}
		}
		
		String configsString = (String) p.get("org.springframework.boot.autoconfigure.EnableAutoConfiguration");
		if (configsString != null) {
			List<String> configs = new ArrayList<>();
			for (String s: configsString.split(",")) {
				configs.add(s);
			}
			// TODO what about ConditionalOnResource?
			System.out.println(
					"Spring.factories processing: looking at #" + configs.size() + " configuration references");
			for (Iterator<String> iterator = configs.iterator(); iterator.hasNext();) {
				String config = iterator.next();
				boolean needToAddThem = true;
				if (!verifyType(config)) {
					System.out.println("Excluding auto-configuration " + config);
					System.out.println("= COC failed so just adding class forname access (no methods/ctors)");
					if (REMOVE_UNNECESSARY_CONFIGURATIONS) {
						forRemoval.add(config);
						needToAddThem = false;
					}
				}
				if (needToAddThem) {
					System.out.println("Resource Adding: "+config);	
					reflectionHandler.addAccess(config); // no flags as it isn't going to trigger
					ResourcesRegistry resourcesRegistry = ImageSingletons.lookup(ResourcesRegistry.class);
					resourcesRegistry.addResources(config.replace(".", "/").replace("$", ".")+".class");
				}
			}
			configs.removeAll(forRemoval);
			p.put("org.springframework.boot.autoconfigure.EnableAutoConfiguration", String.join(",", configs));
		}
		try {
			if (forRemoval.size() == 0) {
				Resources.registerResource("META-INF/spring.factories", springFactory.openStream());
			} else {
				System.out.println("  removed " + forRemoval.size() + " configurations");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				p.store(baos,"");
				baos.close();
				byte[] bs = baos.toByteArray();
				ByteArrayInputStream bais = new ByteArrayInputStream(bs);
				Resources.registerResource("META-INF/spring.factories", bais);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void loadSpringFactoryFile(URL springFactory, Properties p) {
		try (InputStream is = springFactory.openStream()) {
			p.load(is);
		} catch (IOException e) {
			throw new IllegalStateException("Unable to load spring.factories", e);
		}
	}
	
	/**
	 * For the specified type (dotted name) determine which types must be reflectable at runtime. This means
	 * looking at annotations and following any type references within those.
	 */
	private boolean verifyType(String name) {
		return processType(name, new HashSet<>());
	}

	private boolean processType(String config, Set<String> visited) {
		return processType(ts.resolveDotted(config), visited, 0);
	}

	private boolean processType(Type configType, Set<String> visited, int depth) {	
		System.out.println(spaces(depth)+"Processing type "+configType.getName());
		ResourcesRegistry resourcesRegistry = ImageSingletons.lookup(ResourcesRegistry.class);

		// This would fetch 'things we care about from a graal point of view'
		// a list
		// ConditionalOnClass (annotation instance)
		// configType.getRelevantAnnotations();
		// Then for each of those give me the relevant 'types i have to look around for'
		
		
		Set<String> missing = ts.resolveCompleteFindMissingTypes(configType);
		if (!missing.isEmpty()) {
			// No point continuing with this type, it cannot be resolved against current classpath
			// The assumption is it will never need to be accessed anyway
			System.out.println(spaces(depth)+"for "+configType.getName()+" missing types are "+missing);
			return false;
		}
		
		Set<String> missingAnnotationTypes = ts.resolveCompleteFindMissingAnnotationTypes(configType);
		if (!missingAnnotationTypes.isEmpty()) {
			// If only the annotations are missing, it is ok to reflect on the existence of the type, it is
			// just not safe to reflect on the annotations on that type.
			System.out.println(spaces(depth)+"for "+configType.getName()+" missing annotation types are "+missingAnnotationTypes);
		}
		boolean passesTests = true;
		Set<String> toMakeAccessible = new HashSet<>();
		Map<HintDescriptor, List<String>> hints = configType.getHints();
		if (!hints.isEmpty()) {
			int h=1;
			for (Map.Entry<HintDescriptor, List<String>> hint: hints.entrySet()) {
				HintDescriptor hintDescriptor = hint.getKey();
				List<String> typeReferences = hint.getValue();
				System.out.println(spaces(depth)+"checking @CompilationHint "+h+"/"+hints.size()+" "+hintDescriptor.getAnnotationChain());
				
				String[] name = hintDescriptor.getName();
				if (name != null) {
					// The CollectionHint included a list of types to worry about in the annotation
					// itself (e.g. as used on import selector to specify.					
					for (String n: name) {
						resourcesRegistry.addResources(n.replace(".", "/")+".class");
						reflectionHandler.addAccess(n,Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
					}
				}

				if (h==1) { // only do this once all hints should have this in common, TODO polish this up...
					// This handles the case for something like:
					// ReactiveWebServerFactoryConfiguration$EmbeddedTomcat with ConditionalOnClass
					// TODO is this too much repetition for certain types?
					for (Type annotatedType : hintDescriptor.getAnnotationChain()) {
						try {
							System.out.println("Handling annotated thingy: "+annotatedType.getName());
							String t = annotatedType.getDescriptor();
							reflectionHandler.addAccess(t.substring(1,t.length()-1).replace("/", "."),Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
							resourcesRegistry.addResources(t.substring(1,t.length()-1).replace("$", ".")+".class");
						} catch (NoClassDefFoundError e) {
							System.out.println(spaces(depth)+annotatedType.getName()+" not found for configuration "+configType.getName());
						}
						
					}
				}
				
				if (typeReferences != null) {
					for (String typeReference: typeReferences) { // La/b/C;
						Type t = ts.Lresolve(typeReference, true);
						boolean exists = (t != null);
						System.out.println(spaces(depth)+" does "+fromLtoDotted(typeReference)+" exist? "+exists);
						if (exists) {
							// TODO should this specify what aspects of reflection are required (methods/fields/ctors/annotations)
							toMakeAccessible.add(typeReference);
							if (hintDescriptor.isFollow()) {
								processType(t, visited, depth+1);
							}
						} else if (hintDescriptor.isSkipIfTypesMissing()) {
							passesTests = false;
						}
					}
				}
				h++;
			}
		}
		
		if (passesTests || !REMOVE_UNNECESSARY_CONFIGURATIONS) {
			for (String t: toMakeAccessible) {
				try {
					reflectionHandler.addAccess(t.substring(1,t.length()-1).replace("/", "."),Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
					resourcesRegistry.addResources(t.substring(1,t.length()-1).replace("$", ".")+".class");
				} catch (NoClassDefFoundError e) {
					System.out.println(spaces(depth)+"Conditional type "+fromLtoDotted(t)+" not 	found for configuration "+configType.getName());
				}
			}
		}
		
		if (passesTests) {
			try {
				String configNameDotted = configType.getName().replace("/",".");
				System.out.println(spaces(depth)+"including reflective/resource access to "+configNameDotted);
				visited.add(configType.getName());
				reflectionHandler.addAccess(configNameDotted,Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
				System.out.println("res: "+configType.getName().replace("$", ".")+".class");
				resourcesRegistry.addResources(configType.getName().replace("$", ".")+".class");
				// In some cases the superclass of the config needs to be accessible
				// TODO need this guard? if (isConfiguration(configType)) {
				registerHierarchy(configType, new HashSet<>(), resourcesRegistry);
			} catch (NoClassDefFoundError e) {
				// Example:
				// PROBLEM? Can't register Type:org/springframework/boot/autoconfigure/web/servlet/HttpEncodingAutoConfiguration because cannot find javax/servlet/Filter
				// java.lang.NoClassDefFoundError: javax/servlet/Filter
				// ... at com.oracle.svm.hosted.config.ReflectionRegistryAdapter.registerDeclaredConstructors(ReflectionRegistryAdapter.java:97)
				System.out.println("PROBLEM? Can't register "+configType.getName()+" because cannot find "+e.getMessage());
			}
		}
		
		// HibernateJpaConfiguration has a supertype also covered with @Configuration - so more than just registering
		// the hierarchy as accessible, it may contain more config to chase down
		Type s = configType.getSuperclass();
		while (s!= null) {
			processType(s, visited, depth+1);
			s = s.getSuperclass();
		}

		// If the outer type is failing a test, we don't need to recurse...
		if (passesTests) {
			List<Type> nestedTypes = configType.getNestedTypes();
			for (Type t: nestedTypes) {
				if (visited.add(t.getName())) {
					processType(t, visited, depth+1);
				}
			}
		} else {
			System.out.println("INFO: tests failed on "+configType.getName()+" so not going into nested types");
		}
		return passesTests;
	}
	
	String fromLtoDotted(String lDescriptor) {
		return lDescriptor.substring(1,lDescriptor.length()-1).replace("/", ".");
	}

	private Enumeration<URL> fetchResources(String resource) {
		try {
			Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(resource);
			return resources;
		} catch (IOException e1) {
			return Collections.enumeration(Collections.emptyList());
		}
	}

	private void log(String msg) {
		System.out.println(msg);
	}

	private String spaces(int depth) {
		return "                                                  ".substring(0,depth*2);
	}
	
}

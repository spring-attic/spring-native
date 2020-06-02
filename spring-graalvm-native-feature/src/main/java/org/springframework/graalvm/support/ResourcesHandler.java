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
package org.springframework.graalvm.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature.BeforeAnalysisAccess;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.springframework.graalvm.domain.reflect.Flag;
import org.springframework.graalvm.domain.resources.ResourcesDescriptor;
import org.springframework.graalvm.domain.resources.ResourcesJsonMarshaller;
import org.springframework.graalvm.extension.ComponentProcessor;
import org.springframework.graalvm.extension.NativeImageContext;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.graalvm.type.CompilationHint;
import org.springframework.graalvm.type.Hint;
import org.springframework.graalvm.type.Method;
import org.springframework.graalvm.type.MissingTypeException;
import org.springframework.graalvm.type.Type;
import org.springframework.graalvm.type.TypeSystem;

import com.oracle.svm.core.configure.ResourcesRegistry;
import com.oracle.svm.core.jdk.Resources;
import com.oracle.svm.hosted.FeatureImpl.BeforeAnalysisAccessImpl;
import com.oracle.svm.hosted.ImageClassLoader;

public class ResourcesHandler {
	
	private final static String enableAutoconfigurationKey = "org.springframework.boot.autoconfigure.EnableAutoConfiguration";

	private final static String propertySourceLoaderKey = "org.springframework.boot.env.PropertySourceLoader";

	private final static String applicationListenerKey = "org.springframework.context.ApplicationListener";

	private TypeSystem ts;

	private ImageClassLoader cl;

	private ReflectionHandler reflectionHandler;

	private ResourcesRegistry resourcesRegistry;

	private DynamicProxiesHandler dynamicProxiesHandler;

	public ResourcesHandler(ReflectionHandler reflectionHandler, DynamicProxiesHandler dynamicProxiesHandler) {
		this.reflectionHandler = reflectionHandler;
		this.dynamicProxiesHandler = dynamicProxiesHandler;
	}

	/**
	 * Read in the static resource list from inside this project. Common resources for all Spring applications.
	 * @return a ResourcesDescriptor describing the resources from the file
	 */
	public ResourcesDescriptor readStaticResourcesConfiguration() {
		try {
			InputStream s = this.getClass().getResourceAsStream("/resources.json");
			ResourcesDescriptor resourcesDescriptor = ResourcesJsonMarshaller.read(s);
			return resourcesDescriptor;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Callback from native-image. Determine resources related to Spring applications that need to be added to the image.
	 * @param access provides API access to native image construction information
	 */
	public void register(BeforeAnalysisAccess access) {
		cl = ((BeforeAnalysisAccessImpl) access).getImageClassLoader();
		ts = TypeSystem.get(cl.getClasspath());
		resourcesRegistry = ImageSingletons.lookup(ResourcesRegistry.class);
		ResourcesDescriptor rd = readStaticResourcesConfiguration();
		System.out.println("Registering resources - #" + rd.getPatterns().size() + " patterns");
		// Patterns can be added to the registry, resources can be directly registered
		// against the Resources object:
		// resourcesRegistry.addResources("*");
		// Resources.registerResource(relativePath, inputstream);
		if (ConfigOptions.isFunctionalMode() || ConfigOptions.isFeatureMode()) {
			registerPatterns(rd);
			registerResourceBundles(rd);
		}
		if (ConfigOptions.isFeatureMode()) {
			processSpringFactories();
			handleSpringConstantHints();
			processExistingOrSynthesizedSpringComponentsFiles();
		}
	}

	private void registerPatterns(ResourcesDescriptor rd) {
		for (String pattern : rd.getPatterns()) {
			if (pattern.equals("META-INF/spring.factories")) {
				continue; // leave to special handling which may trim these files...
			}
			resourcesRegistry.addResources(pattern);
		}
	}

	private void registerResourceBundles(ResourcesDescriptor rd) {
		System.out.println("Registering resources - #" + rd.getBundles().size() + " bundles");
		for (String bundle : rd.getBundles()) {
			try {
				ResourceBundle.getBundle(bundle);
				resourcesRegistry.addResourceBundles(bundle);
			} catch (MissingResourceException e) {
				//bundle not available. don't load it
			}
		}
	}

	/**
	 * Some types need reflective access in every Spring Boot application. When hints are scanned
	 * these 'constants' are registered against the java.lang.Object key. Because they won't have been 
	 * registered in regular analysis, here we 
	 */
	private void handleSpringConstantHints() {
		List<CompilationHint> constantHints = ts.findHints("java.lang.Object");
		SpringFeature.log("Registering fixed entries: " + constantHints);
		for (CompilationHint ch : constantHints) {
			Map<String, Integer> dependantTypes = ch.getDependantTypes();
			for (Map.Entry<String, Integer> dependantType : dependantTypes.entrySet()) {
				reflectionHandler.addAccess(dependantType.getKey(), null, true,
						AccessBits.getFlags(dependantType.getValue()));
			}
		}
	}

	public void processExistingOrSynthesizedSpringComponentsFiles() {
		Enumeration<URL> springComponents = fetchResources("META-INF/spring.components");
		if (springComponents.hasMoreElements()) {
			log("Processing existing META-INF/spring.components files...");
			while (springComponents.hasMoreElements()) {
				URL springFactory = springComponents.nextElement();
				Properties p = new Properties();
				loadSpringFactoryFile(springFactory, p);
				processSpringComponents(p);
			}
		} else {
			log("Found no META-INF/spring.components -> synthesizing one...");
			Properties p = synthesizeSpringComponents();
			processSpringComponents(p);
		}
	}

	private Properties synthesizeSpringComponents() {
		Properties p = new Properties();
		List<Entry<Type, List<Type>>> components = scanForSpringComponents();
		List<Entry<Type, List<Type>>> filteredComponents = filterOutNestedTypes(components);
		for (Entry<Type, List<Type>> filteredComponent : filteredComponents) {
			String k = filteredComponent.getKey().getDottedName();
			p.put(k, filteredComponent.getValue().stream().map(t -> t.getDottedName())
					.collect(Collectors.joining(",")));
		}
		System.out.println("Computed spring.components is ");
		System.out.println("vvv");
		for (Object k : p.keySet()) {
			System.out.println(k + "=" + p.getProperty((String) k));
		}
		System.out.println("^^^");
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			p.store(baos, "");
			baos.close();
			byte[] bs = baos.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(bs);
			Resources.registerResource("META-INF/spring.components", bais);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return p;
	}

	// TODO shouldn't this code just call into processType like we do for discovered
	// configuration?
	// (Then we have consistent processing across library and user code - without it
	// I think this code below will prove insufficient when we get more
	// sophisticated
	// samples - such as one using @Imported configuration - write a testcase for
	// that)
	/**
	 * Process a spring components properties object. The data within will look like:
	 * <pre><code>
	 * app.main.SampleApplication=org.springframework.stereotype.Component
	 * app.main.model.Foo=javax.persistence.Entity
	 * app.main.model.FooRepository=org.springframework.data.repository.Repository
	 * </code></pre>
	 * @param p the properties object containing spring components
	 */
	private void processSpringComponents(Properties p) {
		Enumeration<Object> keys = p.keys();
		int registeredComponents = 0;
		ResourcesRegistry resourcesRegistry = ImageSingletons.lookup(ResourcesRegistry.class);
		while (keys.hasMoreElements()) {
			boolean isRepository = false;
			boolean isComponent = false;
			String k = (String) keys.nextElement();
			String vs = (String) p.get(k);
			if (vs.equals("package-info")) {
				continue;
			}
			Type kType = ts.resolveDotted(k);
			SpringFeature.log("Registering Spring Component: " + k);
			registeredComponents++;
			
			// Ensure if usage of @Component is meta-usage, the annotations that are meta-annotated are
			// exposed
			Entry<Type, List<Type>> metaAnnotated = kType.getMetaComponentTaggedAnnotations();
			if (metaAnnotated != null) {
				for (Type t: metaAnnotated.getValue()) {
					String name = t.getDottedName();
					reflectionHandler.addAccess(name, Flag.allDeclaredMethods);
					resourcesRegistry.addResources(name.replace(".", "/")+".class");
				}
			}

//			if (kType.isAtConfiguration()) {
//				checkAndRegisterConfigurationType(k);
//			} else {
			try {
				// reflectionHandler.addAccess(k,Flag.allDeclaredConstructors,
				// Flag.allDeclaredMethods, Flag.allDeclaredClasses);
				// reflectionHandler.addAccess(k,Flag.allPublicConstructors,
				// Flag.allPublicMethods, Flag.allDeclaredClasses);
				// TODO assess which kinds of thing requiring what kind of access - here we see an Entity might require field reflective access where others don't
				// I think as a component may have autowired fields (and an entity may have interesting fields) - you kind of always need to expose fields
				// There is a type in vanilla-orm called Bootstrap that shows this need
				boolean includeFields = true;
				if (includeFields) {
					reflectionHandler.addAccess(k, Flag.allDeclaredConstructors, Flag.allDeclaredMethods,
							Flag.allDeclaredClasses, Flag.allDeclaredFields);
				} else {
					reflectionHandler.addAccess(k, Flag.allDeclaredConstructors, Flag.allDeclaredMethods,
							Flag.allDeclaredClasses);

				}
				resourcesRegistry.addResources(k.replace(".", "/") + ".class");
				// Register nested types of the component
				Type baseType = ts.resolveDotted(k);
				
				if (baseType != null) {
					// System.out.println("Checking if type "+baseType+" has transactional methods: "+baseType.hasTransactionalMethods());
					if (baseType.isTransactional() || baseType.hasTransactionalMethods()) { // TODO should this check the values against this key or for the annotation presence?
						processTransactionalTarget(baseType);
					}
				}

				for (Type t : baseType.getNestedTypes()) {
					String n = t.getName().replace("/", ".");
					reflectionHandler.addAccess(n, Flag.allDeclaredConstructors, Flag.allDeclaredMethods,
							Flag.allDeclaredClasses);
					resourcesRegistry.addResources(t.getName() + ".class");
				}
				registerHierarchy(baseType, new HashSet<>(), null);
			} catch (Throwable t) {
				SpringFeature.log("WHAT?"+t.toString());
				t.printStackTrace();
				// assuming ok for now - see
				// SBG: ERROR: CANNOT RESOLVE org.springframework.samples.petclinic.model ???
				// for petclinic spring.components
			}
//		}
		if (kType!=null && kType.isAtRepository()) { // See JpaVisitRepositoryImpl in petclinic sample
			processRepository2(kType);
		}
		if (kType != null && kType.isAtResponseBody()) {
			processResponseBodyComponent(kType);
		}
			List<String> values = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(vs, ",");
			// org.springframework.samples.petclinic.visit.JpaVisitRepositoryImpl=org.springframework.stereotype.Component,javax.transaction.Transactional
			while (st.hasMoreElements()) {
				String tt = st.nextToken();
				values.add(tt);
				if (tt.equals("org.springframework.stereotype.Component")) {
					isComponent = true;
				}
				if (tt.equals("org.springframework.data.repository.Repository")) {
					isRepository = true;
				}
				try {
					Type baseType = ts.resolveDotted(tt);

					// reflectionHandler.addAccess(tt,Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allDeclaredClasses);
					// reflectionHandler.addAccess(tt,Flag.allPublicConstructors, Flag.allPublicMethods, Flag.allDeclaredClasses);
//					reg(tt);
					reflectionHandler.addAccess(tt, Flag.allDeclaredMethods);
					resourcesRegistry.addResources(tt.replace(".", "/") + ".class");
					// Register nested types of the component
					for (Type t : baseType.getNestedTypes()) {
						String n = t.getName().replace("/", ".");
						reflectionHandler.addAccess(n, Flag.allDeclaredMethods);
//						reflectionHandler.addAccess(n, Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allDeclaredClasses);
						resourcesRegistry.addResources(t.getName() + ".class");
					}
					registerHierarchy(baseType, new HashSet<>(), null);
				} catch (Throwable t) {
					t.printStackTrace();
					System.out.println("Problems with value " + tt);
				}
				if (isRepository) {
					processRepository(k);
				}
			}
			if (isComponent && ConfigOptions.isVerifierOn()) {
				kType.verifyComponent();
			}
			List<ComponentProcessor> componentProcessors = ts.getComponentProcessors();
			for (ComponentProcessor componentProcessor: componentProcessors) {
				if (componentProcessor.handle(k,values)) {
					componentProcessor.process(new NativeImageContextImpl(), k, values);
				}
			}
		}
		System.out.println("Registered " + registeredComponents + " entries");
	}
	
	class NativeImageContextImpl implements NativeImageContext {

		final HashMap<String, Flag[]> reflectiveFlags = new LinkedHashMap<>();

		@Override
		public boolean addProxy(List<String> interfaces) {
			dynamicProxiesHandler.addProxy(interfaces);
			return true;
		}

		@Override
		public boolean addProxy(String... interfaces) {
			if (interfaces != null) {
				dynamicProxiesHandler.addProxy(Arrays.asList(interfaces));
			}
			return true;
		}

		@Override
		public TypeSystem getTypeSystem() {
			return ts;
		}

		@Override
		public void addReflectiveAccess(String key, Flag... flags) {

			reflectionHandler.addAccess(key, flags);

			// TODO: is there a way to ask the ReflectionRegistry? If not maye keep track of flag changes.
			reflectiveFlags.put(key, flags);
		}

		@Override
		public boolean hasReflectionConfigFor(String key) {
			return reflectiveFlags.containsKey(key);
		}

		@Override
		public void addReflectiveAccessHierarchy(Type type, Flag... flags) {
			registerHierarchy(type, new HashSet<>(), flags);
		}
		
		private void registerHierarchy(Type type, Set<String> visited, Flag... flags) {
			String typename = type.getDottedName();
			if (visited.add(typename)) {
				addReflectiveAccess(typename, flags);
				List<String> relatedTypes = type.getTypesInSignature();
				for (String relatedType: relatedTypes) {
					Type t = ts.resolveSlashed(relatedType,true);
					if (t!=null) {
						registerHierarchy(t, visited, flags);
					}
				}
			}
		}
		
	}
	
	private void processResponseBodyComponent(Type t) {
	  // If a controller is marked up @ResponseBody (possibly via @RestController), need to register reflective access to
	  // the return types of the methods marked @Mapping (meta marked) 
	  Collection<Type> returnTypes = t.collectAtMappingMarkedReturnTypes();
	  SpringFeature.log("Found these return types from Mapped methods in "+t.getName()+" > "+returnTypes);
	  for (Type returnType: returnTypes ) {
		  reflectionHandler.addAccess(returnType.getDottedName(), Flag.allDeclaredMethods, Flag.allDeclaredConstructors,Flag.allDeclaredFields);
	  }
	}

	// Code from petclinic that ends us up in here:
	// public interface VisitRepository { ... }
	// @org.springframework.stereotype.Repository @Transactional public class JpaVisitRepositoryImpl implements VisitRepository { ... }
	// Need proxy: [org.springframework.samples.petclinic.visit.VisitRepository,org.springframework.aop.SpringProxy,
	//              org.springframework.aop.framework.Advised,org.springframework.core.DecoratingProxy] 
	// And entering here with r = JpaVisitRepositoryImpl
	private void processRepository2(Type r) {
		SpringFeature.log("Processing @oss.Repository annotated "+r.getDottedName());
		List<String> repositoryInterfaces = new ArrayList<>();
		for (String s: r.getInterfacesStrings()) {
			repositoryInterfaces.add(s.replace("/", "."));
		}
		repositoryInterfaces.add("org.springframework.aop.SpringProxy");
		repositoryInterfaces.add("org.springframework.aop.framework.Advised");
		repositoryInterfaces.add("org.springframework.core.DecoratingProxy");
		dynamicProxiesHandler.addProxy(repositoryInterfaces);
	}

	/**
	 * Crude basic repository processing - needs more analysis to only add the
	 * interfaces the runtime will be asking for when requesting the proxy.
	 * 
	 * @param repositoryName type name of the repository
	 */
	private void processRepository(String repositoryName) {
		SpringFeature.log("Processing repository: "+repositoryName+" - adding proxy implementing Repository, TransactionalProxy, Advised, DecoratingProxy");
		Type type = ts.resolveDotted(repositoryName);
		// Example proxy:
		// ["app.main.model.FooRepository",
		// "org.springframework.data.repository.Repository",
		// "org.springframework.transaction.interceptor.TransactionalProxy",
		// "org.springframework.aop.framework.Advised",
		// "org.springframework.core.DecoratingProxy"]
		List<String> repositoryInterfaces = new ArrayList<>();
		repositoryInterfaces.add(type.getDottedName());
		repositoryInterfaces.add("org.springframework.data.repository.Repository");
		repositoryInterfaces.add("org.springframework.transaction.interceptor.TransactionalProxy");
		repositoryInterfaces.add("org.springframework.aop.framework.Advised");
		repositoryInterfaces.add("org.springframework.core.DecoratingProxy");
		dynamicProxiesHandler.addProxy(repositoryInterfaces);
		
		// TODO why do we need two proxies here? (vanilla-jpa seems to need both when upgraded from boot 2.2 to 2.3)
		repositoryInterfaces.clear();
		repositoryInterfaces.add(type.getDottedName());
		repositoryInterfaces.add("org.springframework.aop.SpringProxy");
		repositoryInterfaces.add("org.springframework.aop.framework.Advised");
		repositoryInterfaces.add("org.springframework.core.DecoratingProxy");
		dynamicProxiesHandler.addProxy(repositoryInterfaces);
		
		
		// webflux-r2dbc:
		// Example:
		// interface ReservationRepository extends ReactiveCrudRepository<Reservation, Integer> {
		// For this we seem to need:
		// {"name":"com.example.traditional.Reservation",
		//  "allDeclaredFields":true,"allDeclaredConstructors":true,"allDeclaredMethods":true,"allPublicMethods":true},
		// {"name":"com.example.traditional.ReservationRepository",
		//  "allDeclaredMethods":true,"allPublicMethods":true},
		// and register the Reservation type 
		// We don't need this - optimization to have it?
		// {"name":"com.example.traditional.Reservation_Instantiator_c7cq6j",
		//  "methods":[{"name":"<init>","parameterTypes":[]}]}
		if (type.implementsInterface("org/springframework/data/repository/reactive/ReactiveCrudRepository")) {
			try {
				RuntimeClassInitialization.initializeAtBuildTime(Class.forName(type.getDottedName()));
			} catch (ClassNotFoundException cnfe) {
				throw new IllegalStateException("Unexpected - why can inferred repository "+type.getDottedName()+" not be found?",cnfe);
			}
			reflectionHandler.addAccess(type.getDottedName(), Flag.allDeclaredMethods,Flag.allPublicMethods);
			String typeOfThingsInRepository = type.fetchReactiveCrudRepositoryType(); // For our example this will be Reservation
			reflectionHandler.addAccess(typeOfThingsInRepository, Flag.allDeclaredConstructors, Flag.allDeclaredMethods,Flag.allDeclaredFields);
			SpringFeature.log("Dealing with a ReactiveCrudRepository for "+typeOfThingsInRepository);
		}
	}

	private void processTransactionalTarget(Type type) {
		List<String> transactionalInterfaces = new ArrayList<>();
		for (Type intface: type.getInterfaces()) {
			transactionalInterfaces.add(intface.getDottedName());
		}
		transactionalInterfaces.add("org.springframework.aop.SpringProxy");
		transactionalInterfaces.add("org.springframework.aop.framework.Advised");
		transactionalInterfaces.add("org.springframework.core.DecoratingProxy");
		dynamicProxiesHandler.addProxy(transactionalInterfaces);	
		SpringFeature.log("Created transaction related proxy for interfaces: "+transactionalInterfaces);
	}

	/**
	 * Walk a type hierarchy and register them all for reflective access.
	 * 
	 * @param type the type whose hierarchy to register
	 * @param visited used to remember what has already been visited
	 * @param typesToMakeAccessible if non null required accesses are collected here rather than recorded directly on the runtime
	 */
	public void registerHierarchy(Type type, Set<Type> visited, TypeAccessRequestor typesToMakeAccessible) {
		if (type == null || !visited.add(type)) {
			return;
		}
		// SpringFeature.log("> registerHierarchy "+type.getName());
		String desc = type.getName();
		if (type.isCondition()) {
			if (type.hasOnlySimpleConstructor()) {
				if (typesToMakeAccessible != null) {
					typesToMakeAccessible.request(type.getDottedName(),
							AccessBits.RESOURCE | AccessBits.DECLARED_CONSTRUCTORS);
				} else {
					reflectionHandler.addAccess(desc.replace("/", "."), Flag.allDeclaredConstructors,
							Flag.allDeclaredMethods, Flag.allDeclaredClasses);
					resourcesRegistry.addResources(desc.replace("$", ".") + ".class");
				}
			} else {
				if (typesToMakeAccessible != null) {
					typesToMakeAccessible.request(type.getDottedName(),
							AccessBits.RESOURCE | AccessBits.DECLARED_CONSTRUCTORS);
				} else {
					reflectionHandler.addAccess(desc.replace("/", "."), Flag.allDeclaredConstructors);
					resourcesRegistry.addResources(desc.replace("$", ".") + ".class");
				}
			}
		} else {
			if (typesToMakeAccessible != null) {
				// TODO why no CLASS here?
				typesToMakeAccessible.request(type.getDottedName(),
						AccessBits.DECLARED_CONSTRUCTORS | AccessBits.DECLARED_METHODS | AccessBits.RESOURCE);
			} else {
				reflectionHandler.addAccess(desc.replace("/", "."), Flag.allDeclaredConstructors,
						Flag.allDeclaredMethods);// , Flag.allDeclaredClasses);
				resourcesRegistry.addResources(desc.replace("$", ".") + ".class");
			}
			// reflectionHandler.addAccess(configNameDotted, Flag.allDeclaredConstructors,
			// Flag.allDeclaredMethods);
		}
		// Rather than just looking at superclass and interfaces, this will dig into everything including
		// parameterized type references so nothing is missed
		List<String> relatedTypes = type.getTypesInSignature();
		for (String relatedType: relatedTypes) {
			Type t = ts.resolveSlashed(relatedType,true);
			if (t!=null) {
			registerHierarchy(t, visited, typesToMakeAccessible);
			}
		}
	}

	/**
	 * Find all META-INF/spring.factories - for any configurations listed in each,
	 * check if those configurations use ConditionalOnClass. If the classes listed
	 * in ConditionalOnClass can't be found, discard the configuration from
	 * spring.factories. Register either the unchanged or modified spring.factories
	 * files with the system.
	 */
	public void processSpringFactories() {
		log("Processing META-INF/spring.factories files...");
		Enumeration<URL> springFactories = fetchResources("META-INF/spring.factories");
		while (springFactories.hasMoreElements()) {
			URL springFactory = springFactories.nextElement();
			processSpringFactory(ts, springFactory);
		}
	}

	private List<Entry<Type, List<Type>>> filterOutNestedTypes(List<Entry<Type, List<Type>>> springComponents) {
		List<Entry<Type, List<Type>>> filtered = new ArrayList<>();
		List<Entry<Type, List<Type>>> subtypesToRemove = new ArrayList<>();
		for (Entry<Type, List<Type>> a : springComponents) {
			String type = a.getKey().getDottedName();
			subtypesToRemove.addAll(springComponents.stream()
					.filter(e -> e.getKey().getDottedName().startsWith(type + "$")).collect(Collectors.toList()));
		}
		filtered.addAll(springComponents);
		filtered.removeAll(subtypesToRemove);
		return filtered;
	}

	private List<Entry<Type, List<Type>>> scanForSpringComponents() {
		return findDirectories(ts.getClasspath()).flatMap(this::findClasses).map(this::typenameOfClass)
				.map(this::getStereoTypesOnType).filter(Objects::nonNull).collect(Collectors.toList());
	}

	/**
	 * Search for any relevant stereotypes on the specified type. Return entries of
	 * the form:
	 * "com.foo.MyType=org.springframework.stereotype.Component,javax.transaction.Transactional"
	 */
	private Entry<Type, List<Type>> getStereoTypesOnType(String slashedClassname) {
		return ts.resolveSlashed(slashedClassname).getRelevantStereotypes();
	}

	private String typenameOfClass(File f) {
		return Utils.scanClass(f).getClassname();
	}

	private Stream<File> findClasses(File dir) {
		ArrayList<File> classfiles = new ArrayList<>();
		walk(dir, classfiles);
		return classfiles.stream();
	}

	private void walk(File dir, ArrayList<File> classfiles) {
		File[] fs = dir.listFiles();
		for (File f : fs) {
			if (f.isDirectory()) {
				walk(f, classfiles);
			} else if (f.getName().endsWith(".class")) {
				classfiles.add(f);
			}
		}
	}

	private Stream<File> findDirectories(List<String> classpath) {
		List<File> directories = new ArrayList<>();
		for (String classpathEntry : classpath) {
			File f = new File(classpathEntry);
			if (f.isDirectory()) {
				directories.add(f);
			}
		}
		return directories.stream();
	}

	/**
	 * A key in a spring.factories file has a value that is a list of types. These
	 * will be accessed at runtime through an interface but must be reflectively
	 * instantiated. Hence reflective access to constructors but not to methods.
	 */
	private void registerTypeReferencedBySpringFactoriesKey(String s) {
		try {
			Type t = ts.resolveDotted(s, true);
			if (t != null) {
				if (t.hasOnlySimpleConstructor()) {
					reflectionHandler.addAccess(s, new String[][] { { "<init>" } }, false);
				} else {
					reflectionHandler.addAccess(s, Flag.allDeclaredConstructors);
				}
			}
		} catch (NoClassDefFoundError ncdfe) {
			System.out.println(
					"spring.factories processing, problem adding access for key " + s + ": " + ncdfe.getMessage());
		}
	}

	private void processSpringFactory(TypeSystem ts, URL springFactory) {
		List<String> forRemoval = new ArrayList<>();
		Properties p = new Properties();
		loadSpringFactoryFile(springFactory, p);
		int excludedAutoConfigCount = 0;
		Enumeration<Object> factoryKeys = p.keys();

		// Handle all keys other than EnableAutoConfiguration and PropertySourceLoader
		while (factoryKeys.hasMoreElements()) {
			String k = (String) factoryKeys.nextElement();
			SpringFeature.log("Adding all the classes for this key: " + k);
			if (!k.equals(enableAutoconfigurationKey) && !k.equals(propertySourceLoaderKey) && !k.equals(applicationListenerKey)) {
				if (ts.shouldBeProcessed(k)) {
					for (String v : p.getProperty(k).split(",")) {
						registerTypeReferencedBySpringFactoriesKey(v);

					}
				} else {
					SpringFeature.log("Skipping processing spring.factories key " + k + " due to missing guard types");
				}
			}
		}

		// Handle ApplicationListener
		String applicationListenerValues = (String) p.get(applicationListenerKey);
		if (applicationListenerValues != null) {
			List<String> applicationListeners = new ArrayList<>();
			for (String s : applicationListenerValues.split(",")) {
				// BackgroundPreinitializer is REALLY not a good fit with native images (load eagerly lot of classes)
				if (!s.equals("org.springframework.boot.autoconfigure.BackgroundPreinitializer")) {
					registerTypeReferencedBySpringFactoriesKey(s);
					applicationListeners.add(s);
				} else {
					forRemoval.add(s);
				}
			}
			System.out.println("Processing spring.factories - ApplicationListener lists #"
					+ applicationListeners.size() + " application listeners");
			SpringFeature.log("These application listeners are remaining in the ApplicationListener key value:");
			for (int c = 0; c < applicationListeners.size(); c++) {
				SpringFeature.log((c + 1) + ") " + applicationListeners.get(c));
			}
			p.put(applicationListenerKey, String.join(",", applicationListeners));

		}

		// Handle PropertySourceLoader
		String propertySourceLoaderValues = (String) p.get(propertySourceLoaderKey);
		if (propertySourceLoaderValues != null) {
			List<String> propertySourceLoaders = new ArrayList<>();
			for (String s : propertySourceLoaderValues.split(",")) {
				if (!s.equals("org.springframework.boot.env.YamlPropertySourceLoader")
						|| !ConfigOptions.shouldRemoveYamlSupport()) {
					registerTypeReferencedBySpringFactoriesKey(s);
					propertySourceLoaders.add(s);
				} else {
					forRemoval.add(s);
				}
			}
			System.out.println("Processing spring.factories - PropertySourceLoader lists #"
					+ propertySourceLoaders.size() + " property source loaders");
			SpringFeature.log("These property source loaders are remaining in the PropertySourceLoader key value:");
			for (int c = 0; c < propertySourceLoaders.size(); c++) {
				SpringFeature.log((c + 1) + ") " + propertySourceLoaders.get(c));
			}
			p.put(propertySourceLoaderKey, String.join(",", propertySourceLoaders));

		}

		// Handle EnableAutoConfiguration
		String enableAutoConfigurationValues = (String) p.get(enableAutoconfigurationKey);
		if (enableAutoConfigurationValues != null) {
			List<String> configurations = new ArrayList<>();
			for (String s : enableAutoConfigurationValues.split(",")) {
				configurations.add(s);
			}
			System.out.println("Processing spring.factories - EnableAutoConfiguration lists #" + configurations.size()
					+ " configurations");
			for (String config : configurations) {
				if (!checkAndRegisterConfigurationType(config)) {
					if (ConfigOptions.shouldRemoveUnusedAutoconfig()) {
						excludedAutoConfigCount++;
						SpringFeature.log("Excluding auto-configuration " + config);
						forRemoval.add(config);
					}
				}
			}
			if (ConfigOptions.shouldRemoveUnusedAutoconfig()) {
				System.out.println(
						"Excluding " + excludedAutoConfigCount + " auto-configurations from spring.factories file");
				configurations.removeAll(forRemoval);
				p.put(enableAutoconfigurationKey, String.join(",", configurations));
				SpringFeature.log("These configurations are remaining in the EnableAutoConfiguration key value:");
				for (int c = 0; c < configurations.size(); c++) {
					SpringFeature.log((c + 1) + ") " + configurations.get(c));
				}
			}
		}

		if (forRemoval.size() > 0) {
			String existingRC = ts.findAnyResourceConfigIncludingSpringFactoriesPattern();
			if (existingRC != null) {
				System.out.println("WARNING: unable to trim META-INF/spring.factories (for example to disable unused auto configurations)"+
					" because an existing resource-config is directly including it: "+existingRC);
				return;
			}
		}	

		// Filter spring.factories if necessary
		try {
			if (forRemoval.size() == 0) {
				Resources.registerResource("META-INF/spring.factories", springFactory.openStream());
			} else {
				SpringFeature.log("  removed " + forRemoval.size() + " classes");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				p.store(baos, "");
				baos.close();
				byte[] bs = baos.toByteArray();
				SpringFeature.log("The new spring.factories is: vvvvvvvvv");
				SpringFeature.log(new String(bs));
				SpringFeature.log("^^^^^^^^");
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
	 * For the specified type (dotted name) determine which types must be
	 * reflectable at runtime. This means looking at annotations and following any
	 * type references within those. These types will be registered. If there are
	 * any issues with accessibility of required types this will return false
	 * indicating it can't be used at runtime.
	 */
	private boolean checkAndRegisterConfigurationType(String name) {
		return processType(name, new HashSet<>());
	}

	private boolean processType(String config, Set<String> visited) {
		SpringFeature.log("\n\nProcessing configuration type " + config);
		boolean b = processType(ts.resolveDotted(config), visited, 0);
		SpringFeature.log("Configuration type " + config + " has " + (b ? "passed" : "failed") + " validation");
		return b;
	}

	/**
	 * Specific type references are used when registering types not easily identifiable from the
	 * bytecode that we are simply capturing as specific references in the Hints defined
	 * in the configuration module. Used for import selectors, import registrars, configuration.
	 * For @Configuration types here, need only bean method access (not all methods), for 
	 * other types (import selectors/etc) may not need any method reflective access at all
	 * (and no resource access in that case).
	 */
	private boolean registerSpecific(String typename, Integer typeKind, TypeAccessRequestor tar) {
		Type t = ts.resolveDotted(typename, true);
		if (t == null) {
			SpringFeature.log("WARNING: Unable to resolve specific type: " + typename);
			return false;
		} else {
			boolean importRegistrarOrSelector = false;
			try {
				importRegistrarOrSelector = t.isImportRegistrar() || t.isImportSelector();
			} catch (MissingTypeException mte) {
				// something is missing, reflective access not going to work here!
				return false;
			}
			if (importRegistrarOrSelector) {
				tar.request(typename, AccessBits.CLASS | AccessBits.DECLARED_CONSTRUCTORS);
			} else {
				if (AccessBits.isResourceAccessRequired(typeKind)) {
					tar.request(typename, AccessBits.RESOURCE);
					tar.request(typename, typeKind);
				} else {
					// TODO worth limiting it solely to @Bean methods? Need to check how many
					// configuration classes typically have methods that are not @Bean
					tar.request(typename, typeKind);
				}
				if (t.isAtConfiguration()) {
					// This is because of cases like Transaction auto configuration where the
					// specific type names types like ProxyTransactionManagementConfiguration
					// are referred to from the AutoProxyRegistrar CompilationHint.
					// There is a conditional on bean later on the supertype
					// (AbstractTransactionConfiguration)
					// and so we must register proxyXXX and its supertypes as visible.
					registerHierarchy(t, new HashSet<>(), tar);
				}
			}
			return true;
		}
	}

	private boolean processType(Type type, Set<String> visited, int depth) {
		SpringFeature.log(spaces(depth) + "Analyzing " + type.getDottedName());

		if (ConfigOptions.shouldRemoveJmxSupport()) {
			if (type.getDottedName().toLowerCase().contains("jmx")) {
				return false;
			}
		}

		// Check the hierarchy of the type, if bits are missing resolution of this
		// type at runtime will not work - that suggests that in this particular
		// run the types are not on the classpath and so this type isn't being used.
		Set<String> missingTypes = ts.findMissingTypesInHierarchyOfThisType(type);
		if (!missingTypes.isEmpty()) {
			SpringFeature.log(spaces(depth) + "for " + type.getName() + " missing types are " + missingTypes);
			if (ConfigOptions.shouldRemoveUnusedAutoconfig()) {
				return false;
			}
		}

		Set<String> missingAnnotationTypes = ts.resolveCompleteFindMissingAnnotationTypes(type);
		if (!missingAnnotationTypes.isEmpty()) {
			// If only the annotations are missing, it is ok to reflect on the existence of
			// the type, it is just not safe to reflect on the annotations on that type.
			SpringFeature.log(spaces(depth) + "for " + type.getName() + " missing annotation types are "
					+ missingAnnotationTypes);
		}

		boolean passesTests = true;
		
		TypeAccessRequestor tar = new TypeAccessRequestor();
		List<Hint> hints = passesTests ? type.getHints() : Collections.emptyList();
		if (hints.size() != 0) {
			SpringFeature.log(spaces(depth) + hints.size() + " hints on " + type.getDottedName() + " are: ");
			for (int h = 0; h < hints.size(); h++) {
				SpringFeature.log(spaces(depth) + (h + 1) + ") " + hints.get(h));
			}
		} else {
			SpringFeature.log(spaces(depth) + "no hints on " + type.getName());
		}
		List<Type> toFollow = new ArrayList<>();
		if (!hints.isEmpty()) {
			hints: for (Hint hint : hints) {
				SpringFeature.log(spaces(depth) + "processing hint " + hint);

				// This is used for hints that didn't gather data from the bytecode but had them
				// directly encoded. For example when a CompilationHint on an ImportSelector
				// encodes
				// the types that might be returned from it.
				Map<String, Integer> specificNames = hint.getSpecificTypes();
				if (specificNames.size() > 0) {
					SpringFeature.log(spaces(depth) + "attempting registration of " + specificNames.size() + " specific types");
					for (Map.Entry<String, Integer> specificNameEntry : specificNames.entrySet()) {
						String specificTypeName = specificNameEntry.getKey();
						if (!registerSpecific(specificTypeName, specificNameEntry.getValue(), tar)) {
							if (hint.isSkipIfTypesMissing()) {
								passesTests = false;
								if (ConfigOptions.shouldRemoveUnusedAutoconfig()) {
									break;
								}
							}
						} else {
							if (hint.isFollow()) {
								// TODO I suspect only certain things need following, specific types lists could
								// specify that in a suffix (like access required)
								SpringFeature.log(spaces(depth) + "will follow specific type reference " + specificTypeName);
								toFollow.add(ts.resolveDotted(specificTypeName));
							}
						}
					}
				}

				Map<String, Integer> inferredTypes = hint.getInferredTypes();
				if (inferredTypes.size() > 0) {
					SpringFeature.log(
							spaces(depth) + "attempting registration of " + inferredTypes.size() + " inferred types");
					for (Map.Entry<String, Integer> inferredType : inferredTypes.entrySet()) {
						String s = inferredType.getKey();
						Type t = ts.resolveDotted(s, true);
						boolean exists = (t != null);
						if (!exists) {
							SpringFeature.log(spaces(depth) + "inferred type " + s + " not found");
						}
						if (exists) {
							// TODO if already there, should we merge access required values?
							tar.request(s, inferredType.getValue());
							if (hint.isFollow()) {
								SpringFeature.log(spaces(depth) + "will follow " + t);
								toFollow.add(t);
							}
						} else if (hint.isSkipIfTypesMissing() && depth == 0) {
							// TODO If processing secondary type (depth>0) we can't skip things as we don't
							// know if the top level type that refers to us is going to fail or not. Ideally we should
							// pass in the tar and accumulate types in secondary type processing and leave it to the
							// outermost processing to decide if they need registration.
							passesTests = false;
							// Once failing, no need to process other hints
							if (ConfigOptions.shouldRemoveUnusedAutoconfig()) {
								break hints;
							}
						}
					}
				}

				List<Type> annotationChain = hint.getAnnotationChain();
				registerAnnotationChain(depth, tar, annotationChain);
			}
		}

		// TODO this should be pushed earlier and access requests put into tar
		String configNameDotted = type.getDottedName();
		visited.add(type.getName());
		if (passesTests || !ConfigOptions.shouldRemoveUnusedAutoconfig()) {
			if (type.isCondition()) {
				if (type.hasOnlySimpleConstructor()) {
					reflectionHandler.addAccess(configNameDotted, new String[][] { { "<init>" } }, true);
				} else {
					reflectionHandler.addAccess(configNameDotted, null, true, Flag.allDeclaredConstructors);
				}
			} else {
				reflectionHandler.addAccess(configNameDotted, Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
			}
			resourcesRegistry.addResources(type.getName().replace("$", ".") + ".class");
			// In some cases the superclass of the config needs to be accessible
			// TODO need this guard? if (isConfiguration(configType)) {
			// }
			// if (type.isAtConfiguration()) {
			registerHierarchy(type, new HashSet<>(), tar);
			// }

			Type s = type.getSuperclass();
			while (s != null) {
				if (s.getName().equals("java/lang/Object")) {
					break;
				}
				if (visited.add(s.getName())) {
					boolean b = processType(s, visited, depth + 1);
					if (!b) {
						SpringFeature.log(spaces(depth) + "WARNING: whilst processing type " + type.getName()
								+ " superclass " + s.getName() + " verification failed");
					}
				} else {
					break;
				}
				s = s.getSuperclass();
			}
		}

		if (passesTests || !ConfigOptions.shouldRemoveUnusedAutoconfig()) {
			if (type.isAtConfiguration()) {
				// This type might have @AutoConfigureAfter/@AutoConfigureBefore references to
				// other configurations.
				// Those may be getting discarded in this run but need to be class accessible
				// because this configuration needs to refer to them.
				List<Type> boaTypes = type.getAutoConfigureBeforeOrAfter();
				if (boaTypes.size() != 0) {
					SpringFeature.log(spaces(depth) + "registering " + boaTypes.size()
							+ " @AutoConfigureBefore/After references");
				}
				for (Type t : boaTypes) {
					tar.request(t.getDottedName(), AccessBits.CLASS);
				}

				// This is computing how many methods we are exposing unnecessarily via
				// reflection by specifying allDeclaredMethods
				// rather than individually specifying them. A high number indicates we should
				// perhaps do more to be selective.
				int c = type.getMethodCount();
				List<Method> methodsWithAtBean = type.getMethodsWithAtBean();
				int rogue = (c - methodsWithAtBean.size());
				if (rogue != 0) {
					SpringFeature.log(spaces(depth)
							+ "WARNING: Methods unnecessarily being exposed by reflection on this config type "
							+ type.getName() + " = " + rogue + " (total methods including @Bean ones:" + c + ")");
				}

				List<Method> atBeanMethods = type.getMethodsWithAtBean();
				if (atBeanMethods.size() != 0) {
					SpringFeature.log(spaces(depth) + "processing " + atBeanMethods.size() + " @Bean methods");
				}
				for (Method atBeanMethod : atBeanMethods) {
					Type returnType = atBeanMethod.getReturnType();
					if (returnType == null) {
						// I believe null means that type is not on the classpath so skip further
						// analysis
						continue;
					} else {


						// We will need access to Supplier and Flux because of this return type
						Set<Type> ts = atBeanMethod.getSignatureTypes();
						for (Type t: ts) {
							SpringFeature.log("Processing @Bean method "+atBeanMethod.getName()+"(): adding "+t.getDottedName());
							tar.request(t.getDottedName(),
									AccessBits.CLASS | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_CONSTRUCTORS);
						}
					}

					// Processing this kind of thing, parameter types need to be exposed
					// @Bean
					// TomcatReactiveWebServerFactory tomcatReactiveWebServerFactory(
					// ObjectProvider<TomcatConnectorCustomizer> connectorCustomizers,
					// ObjectProvider<TomcatContextCustomizer> contextCustomizers,
					// ObjectProvider<TomcatProtocolHandlerCustomizer<?>>
					// protocolHandlerCustomizers) {
					// atBeanMethod.getSignatureTypes();

					// This code would cover adding parameter types - but do we really need to?
					// If these 'beans' are being built the return types of the bean factory
					// methods would ensure the registration has occurred.
					/*
					 * Set<Type> signatureTypes = atBeanMethod.getSignatureTypes(); for (Type
					 * signatureType: signatureTypes) {
					 * System.out.println("Flibble: "+signatureType.getDottedName());
					 * tar.request(signatureType.getDottedName(), AccessRequired.EXISTENCE_MC); }
					 */

					// Processing, for example:
					// @ConditionalOnResource(resources =
					// "${spring.info.build.location:classpath:META-INF/build-info.properties}")
					// @ConditionalOnMissingBean
					// @Bean
					// public BuildProperties buildProperties() throws Exception {
					List<Hint> methodHints = atBeanMethod.getHints();
					SpringFeature.log(spaces(depth) + "hints on method " + atBeanMethod + ":\n" + methodHints);
					for (Hint hint : methodHints) {
						SpringFeature.log(spaces(depth) + "processing hint " + hint);

						// This is used for hints that didn't gather data from the bytecode but had them
						// directly encoded. For example
						// when a CompilationHint on an ImportSelector encodes the types that might be
						// returned from it.
						// (see the static initializer in Type for examples)
						Map<String, Integer> specificNames = hint.getSpecificTypes();
						SpringFeature.log(spaces(depth) + "attempting registration of " + specificNames.size()
								+ " specific types");
						for (Map.Entry<String, Integer> specificNameEntry : specificNames.entrySet()) {
							registerSpecific(specificNameEntry.getKey(), specificNameEntry.getValue(), tar);
						}

						Map<String, Integer> inferredTypes = hint.getInferredTypes();
						SpringFeature.log(spaces(depth) + "attempting registration of " + inferredTypes.size()
								+ " inferred types");
						for (Map.Entry<String, Integer> inferredType : inferredTypes.entrySet()) {
							String s = inferredType.getKey();
							Type t = ts.resolveDotted(s, true);
							boolean exists = (t != null);
							if (!exists) {
								SpringFeature.log(spaces(depth) + "inferred type " + s
										+ " not found (whilst processing @Bean method " + atBeanMethod + ")");
							} else {
								SpringFeature.log(spaces(depth) + "inferred type " + s
										+ " found, will get accessibility " + inferredType.getValue()
										+ " (whilst processing @Bean method " + atBeanMethod + ")");
							}
							if (exists) {
								// TODO if already there, should we merge access required values?
								tar.request(s, inferredType.getValue());
								if (hint.isFollow()) {
									toFollow.add(t);
								}
							} else if (hint.isSkipIfTypesMissing()) {
								passesTests = false;
								// Once failing, no need to process other hints
							}
						}

						List<Type> annotationChain = hint.getAnnotationChain();
						registerAnnotationChain(depth, tar, annotationChain);
					}

					// Register other runtime visible annotations from the @Bean method. For example
					// this ensures @Role is visible on:
					// @Bean
					// @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
					// @ConditionalOnMissingBean(Validator.class)
					// public static LocalValidatorFactoryBean defaultValidator() {
					List<Type> annotationsOnMethod = atBeanMethod.getAnnotationTypes();
					for (Type annotationOnMethod : annotationsOnMethod) {
						tar.request(annotationOnMethod.getDottedName(), AccessBits.ANNOTATION);
					}
				}
			}
			// Follow transitively included inferred types only if necessary:
			for (Type t : toFollow) {
				try {
					boolean b = processType(t, visited, depth + 1);
					if (!b) {
						SpringFeature.log(spaces(depth) + "followed " + t.getName() + " and it failed validation");
					}
				} catch (MissingTypeException mte) {
					// Failed to follow that type because some element involved is not on the classpath 
					// (Typically happens when not specifying discard-unused-autconfiguration)
					SpringFeature.log("Unable to completely process followed type "+t.getName()+": "+mte.getMessage());
				}
			}
			for (Map.Entry<String, Integer> t : tar.entrySet()) {
				String dname = t.getKey();

				// Let's produce a message if this computed value is also in reflect.json
				// This is a sign we can probably remove that entry from reflect.json (maybe
				// depend if inferred access required matches declared)
				if (reflectionHandler.getConstantData().hasClassDescriptor(dname)) {
					System.out.println("This is in the constant data, does it need to stay in there? " + dname
							+ "  (dynamically requested access is " + t.getValue() + ")");
				}

				SpringFeature.log(spaces(depth) + "making this accessible: " + dname + "   " + AccessBits.toString(t.getValue()));
				Flag[] flags = AccessBits.getFlags(t.getValue());
				if (flags != null && flags.length == 1 && flags[0] == Flag.allDeclaredConstructors) {
					Type resolvedType = ts.resolveDotted(dname, true);
					if (resolvedType != null && resolvedType.hasOnlySimpleConstructor()) {
						reflectionHandler.addAccess(dname, new String[][] { { "<init>" } }, true);
					} else {
						reflectionHandler.addAccess(dname, null, true, flags);
					}
				} else {
					reflectionHandler.addAccess(dname, null, true, flags);
				}
				if (AccessBits.isResourceAccessRequired(t.getValue())) {
					resourcesRegistry.addResources(
							dname.replace(".", "/").replace("$", ".").replace("[", "\\[").replace("]", "\\]")
									+ ".class");
				}
			}
		}

		// If the outer type is failing a test, we don't need to go into nested types...
		if (passesTests || !ConfigOptions.shouldRemoveUnusedAutoconfig()) {
			// if (type.isAtConfiguration() || type.isAbstractNestedCondition()) {
			List<Type> nestedTypes = type.getNestedTypes();
			for (Type t : nestedTypes) {
				if (visited.add(t.getName())) {
					try {
						boolean b = processType(t, visited, depth + 1);
						if (!b) {
							SpringFeature.log(spaces(depth) + "verification of nested type " + t.getName() + " failed");
						}
					} catch (MissingTypeException mte) {
						// Failed to process that type because some element involved is not on the classpath 
						// (Typically happens when not specifying discard-unused-autoconfiguration)
						SpringFeature.log("Unable to completely process nested type "+t.getName()+": "+mte.getMessage());
					}
				}
			}
		}
		return passesTests;
	}
	
	private void registerAnnotationChain(int depth, TypeAccessRequestor tar, List<Type> annotationChain) {
		SpringFeature.log(spaces(depth) + "attempting registration of " + annotationChain.size()
				+ " elements of annotation chain");
		for (int i = 0; i < annotationChain.size(); i++) {
			// i=0 is the annotated type, i>0 are all annotation types
			Type t = annotationChain.get(i);
			tar.request(t.getDottedName(), t.isAnnotation() ? AccessBits.ANNOTATION : AccessBits.EVERYTHING);
		}
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
		return "                                                  ".substring(0, depth * 2);
	}

}

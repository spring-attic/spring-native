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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
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
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature.BeforeAnalysisAccess;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.springframework.graalvm.domain.init.InitializationDescriptor;
import org.springframework.graalvm.domain.reflect.Flag;
import org.springframework.graalvm.domain.reflect.ReflectionDescriptor;
import org.springframework.graalvm.domain.resources.ResourcesDescriptor;
import org.springframework.graalvm.domain.resources.ResourcesJsonMarshaller;
import org.springframework.graalvm.extension.ComponentProcessor;
import org.springframework.graalvm.extension.NativeImageContext;
import org.springframework.graalvm.extension.SpringFactoriesProcessor;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.graalvm.type.AccessDescriptor;
import org.springframework.graalvm.type.CompilationHint;
import org.springframework.graalvm.type.Hint;
import org.springframework.graalvm.type.Method;
import org.springframework.graalvm.type.MissingTypeException;
import org.springframework.graalvm.type.ProxyDescriptor;
import org.springframework.graalvm.type.Type;
import org.springframework.graalvm.type.TypeSystem;

import com.oracle.svm.core.configure.ResourcesRegistry;
import com.oracle.svm.core.jdk.Resources;
import com.oracle.svm.hosted.FeatureImpl.BeforeAnalysisAccessImpl;
import com.oracle.svm.hosted.ImageClassLoader;

public class ResourcesHandler {
	
	private final static String enableAutoconfigurationKey = "org.springframework.boot.autoconfigure.EnableAutoConfiguration";

	private final static String propertySourceLoaderKey = "org.springframework.boot.env.PropertySourceLoader";

	private final static String managementContextConfigurationKey = "org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration";

	private TypeSystem ts;

	private ImageClassLoader cl;

	private ReflectionHandler reflectionHandler;

	private ResourcesRegistry resourcesRegistry;

	private DynamicProxiesHandler dynamicProxiesHandler;

	private InitializationHandler initializationHandler;

	public ResourcesHandler(ReflectionHandler reflectionHandler, DynamicProxiesHandler dynamicProxiesHandler, InitializationHandler initializationHandler) {
		this.reflectionHandler = reflectionHandler;
		this.dynamicProxiesHandler = dynamicProxiesHandler;
		this.initializationHandler = initializationHandler;
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
		if (ConfigOptions.isFunctionalMode() ||
			ConfigOptions.isAnnotationMode() ||
			ConfigOptions.isAgentMode()) {
			System.out.println("Registering statically declared resources - #" + rd.getPatterns().size() + " patterns");
			registerPatterns(rd);
			registerResourceBundles(rd);
		}
		if (ConfigOptions.isAnnotationMode() ||
			ConfigOptions.isAgentMode()) {
			processSpringFactories();
		}
		if (!ConfigOptions.isInitMode()) {
			handleSpringConstantHints();
		}
		handleSpringConstantInitialiationHints();
		if (ConfigOptions.isAnnotationMode() ||
			ConfigOptions.isAgentMode() ||
			ConfigOptions.isFunctionalMode()) {
			handleSpringComponents();
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
	 * registered in regular analysis, here we explicitly register those. Initialization hints are handled
	 * separately.
	 */
	private void handleSpringConstantHints() {
		List<CompilationHint> constantHints = ts.findActiveDefaultHints();
		SpringFeature.log("Registering fixed hints: " + constantHints);
		for (CompilationHint ch : constantHints) {
			if (!isHintValidForCurrentMode(ch)) {
				continue;
			}
			Map<String, AccessDescriptor> dependantTypes = ch.getDependantTypes();
			for (Map.Entry<String, AccessDescriptor> dependantType : dependantTypes.entrySet()) {
				SpringFeature.log("  fixed type registered "+dependantType.getKey());
				reflectionHandler.addAccess(dependantType.getKey(), null, null, true,
						AccessBits.getFlags(dependantType.getValue().getAccessBits()));
			}
			List<ProxyDescriptor> proxyDescriptors = ch.getProxyDescriptors();
			for (ProxyDescriptor pd: proxyDescriptors) {
				dynamicProxiesHandler.addProxy(pd);
			}
			List<org.springframework.graalvm.type.ResourcesDescriptor> resourcesDescriptors = ch.getResourcesDescriptors();
			for (org.springframework.graalvm.type.ResourcesDescriptor rd: resourcesDescriptors) {
				registerResourcesDescriptor(rd);
			}
		}
	}
	
	private void handleSpringConstantInitialiationHints() {
		List<CompilationHint> constantHints = ts.findHints("java.lang.Object");
		SpringFeature.log("Registering fixed initialization entries: ");
		for (CompilationHint ch : constantHints) {
			List<InitializationDescriptor> ids = ch.getInitializationDescriptors();
			for (InitializationDescriptor id: ids) {
				SpringFeature.log(" registering initialization descriptor: "+id);
				initializationHandler.registerInitializationDescriptor(id);
			}
		}
	}
	
	public void registerResourcesDescriptor(org.springframework.graalvm.type.ResourcesDescriptor rd) {
		String[] patterns = rd.getPatterns();
		for (String pattern: patterns) {
			if (rd.isBundle()) {
				try {
					resourcesRegistry.addResourceBundles(pattern);
				} catch (MissingResourceException mre) {
					SpringFeature.log("WARNING: resource bundle "+pattern+" could not be registered");
				}
			} else {
				resourcesRegistry.addResources(pattern);
			}
		}	
	}

	/**
	 * Discover existing spring.components or synthesize one if none are found. If not running
	 * in hybrid mode then process the spring.components entries.
	 */
	public void handleSpringComponents() {
		NativeImageContext context = new NativeImageContextImpl();
		Enumeration<URL> springComponents = fetchResources("META-INF/spring.components");
		List<String> alreadyProcessed = new ArrayList<>();
		if (springComponents.hasMoreElements()) {
			log("Processing existing META-INF/spring.components files...");
			while (springComponents.hasMoreElements()) {
				URL springFactory = springComponents.nextElement();
				Properties p = new Properties();
				loadSpringFactoryFile(springFactory, p);
				if (ConfigOptions.isAgentMode()) {
					processSpringComponentsHybrid(p, context);
				} else if (ConfigOptions.isFunctionalMode()) {
					processSpringComponentsFunc(p, context, alreadyProcessed);
				} else {
					processSpringComponents(p, context, alreadyProcessed);
				}
			}
		} else {
			log("Found no META-INF/spring.components -> synthesizing one...");
			Properties p = synthesizeSpringComponents();
			if (ConfigOptions.isAgentMode()) {
				processSpringComponentsHybrid(p, context);
			} else if (ConfigOptions.isFunctionalMode()) {
				processSpringComponentsFunc(p, context, alreadyProcessed);
			} else {
				processSpringComponents(p, context, alreadyProcessed);
			}
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
	
	private void processSpringComponentsFunc(Properties p, NativeImageContext context,List<String> alreadyProcessed) {
		Enumeration<Object> keys = p.keys();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			String valueString = (String)p.get(key);
			if (valueString.equals("package-info")) {
				continue;
			}
			Type keyType = ts.resolveDotted(key);
			if (keyType.isAtConfiguration()) {
				checkAndRegisterConfigurationType(key);
				//	[Lorg/springframework/boot/autoconfigure/context/PropertyPlaceholderAutoConfiguration;, Lorg/springframework/boot/autoconfigure/context/ConfigurationPropertiesAutoConfiguration;, Lorg/springframework/boot/autoconfigure/web/servlet/ServletWebServerFactoryAutoConfiguration;
				

			}
			/*
			Type keyType = ts.resolveDotted(key);
			// The context start/stop test may not exercise the @SpringBootApplication class
			if (keyType.isAtSpringBootApplication()) {
				System.out.println("hybrid: adding access to "+keyType+" since @SpringBootApplication");
				reflectionHandler.addAccess(key,  Flag.allDeclaredMethods, Flag.allDeclaredFields, Flag.allDeclaredConstructors);
				resourcesRegistry.addResources(key.replace(".", "/")+".class");
			}
			if (keyType.isAtController()) {
				System.out.println("hybrid: Processing controller "+key);
				List<Method> mappings = keyType.getMethods(m -> m.isAtMapping());
				// Example:
				// @GetMapping("/greeting")
				// public String greeting( @RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) {
				for (Method mapping: mappings) {
					for (int pi=0;pi<mapping.getParameterCount();pi++) {
						List<Type> parameterAnnotationTypes = mapping.getParameterAnnotationTypes(pi);
						for (Type parameterAnnotationType: parameterAnnotationTypes) {
							if (parameterAnnotationType.hasAliasForMarkedMembers()) {
								List<String> interfaces = new ArrayList<>();
								interfaces.add(parameterAnnotationType.getDottedName());
								interfaces.add("org.springframework.core.annotation.SynthesizedAnnotation");
								System.out.println("Adding dynamic proxy for "+interfaces);
								dynamicProxiesHandler.addProxy(interfaces);
							}
						}
						
					}
				}
			}
			*/
		}
		
	}
	
	private void processSpringComponentsHybrid(Properties p, NativeImageContext context) {
		Enumeration<Object> keys = p.keys();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			String valueString = (String)p.get(key);
			if (valueString.equals("package-info")) {
				continue;
			}
			Type keyType = ts.resolveDotted(key);
			// The context start/stop test may not exercise the @SpringBootApplication class
			if (keyType.isAtSpringBootApplication()) {
				System.out.println("hybrid: adding access to "+keyType+" since @SpringBootApplication");
				reflectionHandler.addAccess(key,  Flag.allDeclaredMethods, Flag.allDeclaredFields, Flag.allDeclaredConstructors);
				resourcesRegistry.addResources(key.replace(".", "/")+".class");
			}
			if (keyType.isAtController()) {
				System.out.println("hybrid: Processing controller "+key);
				List<Method> mappings = keyType.getMethods(m -> m.isAtMapping());
				// Example:
				// @GetMapping("/greeting")
				// public String greeting( @RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) {
				for (Method mapping: mappings) {
					for (int pi=0;pi<mapping.getParameterCount();pi++) {
						List<Type> parameterAnnotationTypes = mapping.getParameterAnnotationTypes(pi);
						for (Type parameterAnnotationType: parameterAnnotationTypes) {
							if (parameterAnnotationType.hasAliasForMarkedMembers()) {
								List<String> interfaces = new ArrayList<>();
								interfaces.add(parameterAnnotationType.getDottedName());
								interfaces.add("org.springframework.core.annotation.SynthesizedAnnotation");
								System.out.println("Adding dynamic proxy for "+interfaces);
								dynamicProxiesHandler.addProxy(interfaces);
							}
						}
						
					}
				}
			}
		}
	}

	/**
	 * Process a spring components properties object. The data within will look like:
	 * <pre><code>
	 * app.main.SampleApplication=org.springframework.stereotype.Component
	 * app.main.model.Foo=javax.persistence.Entity
	 * app.main.model.FooRepository=org.springframework.data.repository.Repository
	 * </code></pre>
	 * @param p the properties object containing spring components
	 */
	private void processSpringComponents(Properties p, NativeImageContext context,List<String> alreadyProcessed) {
		List<ComponentProcessor> componentProcessors = ts.getComponentProcessors();
		Enumeration<Object> keys = p.keys();
		int registeredComponents = 0;
		RequestedConfigurationManager requestor = new RequestedConfigurationManager();
		ResourcesRegistry resourcesRegistry = ImageSingletons.lookup(ResourcesRegistry.class);
		while (keys.hasMoreElements()) {
			boolean isComponent = false;
			String k = (String) keys.nextElement();
			String vs = (String) p.get(k);
			if (vs.equals("package-info")) {
				continue;
			}
			if (alreadyProcessed.contains(k+":"+vs)) {
				continue;
			}
			alreadyProcessed.add(k+":"+vs);
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

			if (kType.isAtConfiguration()) {
				// Treat user configuration (from spring.components) the same as configuration
				// discovered via spring.factories
				checkAndRegisterConfigurationType(k);
			} else {
				try {
					// TODO assess which kinds of thing requiring what kind of access - here we see
					// an Entity might require field reflective access where others don't
					// I think as a component may have autowired fields (and an entity may have
					// interesting fields) - you kind of always need to expose fields
					// There is a type in vanilla-orm called Bootstrap that shows this need
					reflectionHandler.addAccess(k, Flag.allDeclaredConstructors, Flag.allDeclaredMethods,
						Flag.allDeclaredClasses, Flag.allDeclaredFields);
					resourcesRegistry.addResources(k.replace(".", "/") + ".class");
					// Register nested types of the component
//					Type baseType = ts.resolveDotted(k);
					for (Type t : kType.getNestedTypes()) {
						String n = t.getName().replace("/", ".");
						reflectionHandler.addAccess(n, Flag.allDeclaredConstructors, Flag.allDeclaredMethods,
								Flag.allDeclaredClasses);
						resourcesRegistry.addResources(t.getName() + ".class");
					}
					registerHierarchy(kType, requestor);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			if (kType != null && kType.isAtRepository()) { // See JpaVisitRepositoryImpl in petclinic sample
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
				try {
					Type baseType = ts.resolveDotted(tt);

					// reflectionHandler.addAccess(tt,Flag.allDeclaredConstructors,
					// Flag.allDeclaredMethods, Flag.allDeclaredClasses);
					// reflectionHandler.addAccess(tt,Flag.allPublicConstructors,
					// Flag.allPublicMethods, Flag.allDeclaredClasses);
					reflectionHandler.addAccess(tt, Flag.allDeclaredMethods);
					resourcesRegistry.addResources(tt.replace(".", "/") + ".class");
					// Register nested types of the component
					for (Type t : baseType.getNestedTypes()) {
						String n = t.getName().replace("/", ".");
						reflectionHandler.addAccess(n, Flag.allDeclaredMethods);
//						reflectionHandler.addAccess(n, Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allDeclaredClasses);
						resourcesRegistry.addResources(t.getName() + ".class");
					}
					registerHierarchy(baseType, requestor);
				} catch (Throwable t) {
					t.printStackTrace();
					System.out.println("Problems with value " + tt);
				}
			}
			if (isComponent && ConfigOptions.isVerifierOn()) {
				kType.verifyComponent();
			}
			for (ComponentProcessor componentProcessor: componentProcessors) {
				if (componentProcessor.handle(context, k, values)) {
					componentProcessor.process(context, k, values);
				}
			}
		}
		registerAllRequested(0, requestor);
		componentProcessors.forEach(ComponentProcessor::printSummary);
		System.out.println("Registered " + registeredComponents + " entries");
	}
	
	/**
	 * This is the type passed to the 'plugins' that process spring components or spring factories entries.
	 */
	class NativeImageContextImpl implements NativeImageContext {

		private final HashMap<String, Flag[]> reflectiveFlags = new LinkedHashMap<>();

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

			// TODO: is there a way to ask the ReflectionRegistry? If not may keep track of flag changes.
			reflectiveFlags.put(key, flags);
		}

		@Override
		public boolean hasReflectionConfigFor(String key) {
			return reflectiveFlags.containsKey(key);
		}

		@Override
		public void initializeAtBuildTime(Type type) {

			try {
				RuntimeClassInitialization.initializeAtBuildTime(Class.forName(type.getDottedName()));
			} catch (ClassNotFoundException cnfe) {
				throw new IllegalStateException("Unexpected - type " + type.getDottedName() +" cannot be found!",cnfe);
			}
		}

		@Override
		public Set<String> addReflectiveAccessHierarchy(String typename, int accessBits) {
			Type type = ts.resolveDotted(typename, true);
			Set<String> added = new TreeSet<>();
			registerHierarchy(type, added, accessBits);
			return added;
		}
		
		private void registerHierarchy(Type type, Set<String> visited, int accessBits) {
			String typename = type.getDottedName();
			if (visited.add(typename)) {
				addReflectiveAccess(typename, AccessBits.getFlags(accessBits));
				List<String> relatedTypes = type.getTypesInSignature();
				for (String relatedType: relatedTypes) {
					Type t = ts.resolveSlashed(relatedType, true);
					if (t!=null) {
						registerHierarchy(t, visited, accessBits);
					}
				}
			}
		}

		@Override
		public void log(String message) {
			SpringFeature.log(message);
		}
		
	}
	
	private void processResponseBodyComponent(Type t) {
	  // If a controller is marked up @ResponseBody (possibly via @RestController), need to register reflective access to
	  // the return types of the methods marked @Mapping (meta marked) 
	  Collection<Type> returnTypes = t.collectAtMappingMarkedReturnTypes();
	  SpringFeature.log("Found these return types from Mapped methods in "+t.getName()+" > "+returnTypes);
	  for (Type returnType: returnTypes ) {
		  if (returnType==null) {
			  continue;
		  }
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
	 * Walk a type hierarchy and register them all for reflective access.
	 * 
	 * @param type the type whose hierarchy to register
	 * @param visited used to remember what has already been visited
	 * @param typesToMakeAccessible if non null required accesses are collected here rather than recorded directly on the runtime
	 */
	public void registerHierarchy(Type type, RequestedConfigurationManager typesToMakeAccessible) {
		AccessBits accessRequired = AccessBits.forValue(Type.inferAccessRequired(type));
		registerHierarchyHelper(type, new HashSet<>(), typesToMakeAccessible, accessRequired);
	}
	
	private void registerHierarchyHelper(Type type, Set<Type> visited, RequestedConfigurationManager typesToMakeAccessible, AccessBits inferredRequiredAccess) {
		if (typesToMakeAccessible == null) {
			throw new IllegalStateException();
		}
		if (type == null || !visited.add(type)) {
			return;
		}
		if (type.isCondition()) {
			if (type.hasOnlySimpleConstructor()) {
				typesToMakeAccessible.requestTypeAccess(type.getDottedName(),inferredRequiredAccess.getValue());
			} else {
				typesToMakeAccessible.requestTypeAccess(type.getDottedName(),inferredRequiredAccess.getValue());
			}
		} else {
			// TODO we can do better here, why can we not use the inferredRequiredAccess -
			// it looks like we aren't adding RESOURCE to something when inferring.
			typesToMakeAccessible.requestTypeAccess(type.getDottedName(),
					AccessBits.DECLARED_CONSTRUCTORS|AccessBits.DECLARED_METHODS|AccessBits.RESOURCE);
//					inferredRequiredAccess.getValue());
			// reflectionHandler.addAccess(configNameDotted, Flag.allDeclaredConstructors,
			// Flag.allDeclaredMethods);
		}
		// Rather than just looking at superclass and interfaces, this will dig into everything including
		// parameterized type references so nothing is missed
//		if (type.getSuperclass()!=null) {
//			System.out.println("RH>SC "+type.getSuperclass());
//		registerHierarchyHelper(type.getSuperclass(),visited, typesToMakeAccessible,inferredRequiredAccess);
//		}
//		Type[] intfaces = type.getInterfaces();
//		for (Type intface: intfaces) {
//			System.out.println("RH>IF "+intface);
//			registerHierarchyHelper(intface,visited, typesToMakeAccessible,inferredRequiredAccess);
//		}
/*		
		List<String> supers = new ArrayList<>();
		if (type.getSuperclass()!=null) {
			supers.add(type.getSuperclass().getDottedName());
		}
		Type[] intfaces = type.getInterfaces();
		for (Type intface: intfaces) {
			supers.add(intface.getDottedName());
		}
		List<String> lst = new ArrayList<>();
*/	
		
		List<String> relatedTypes = type.getTypesInSignature();
		for (String relatedType: relatedTypes) {
			Type t = ts.resolveSlashed(relatedType,true);
			if (t!=null) {
//				lst.add(t.getDottedName());
				registerHierarchyHelper(t, visited, typesToMakeAccessible, inferredRequiredAccess);
			}
		}
//		lst.removeAll(supers);
//		if (lst.size()!=0) {
//		System.out.println("MISSED THESE ("+type.getDottedName()+"): "+lst);
//		}
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
		return findDirectoriesOrTargetDirJar(ts.getClasspath()).flatMap(this::findClasses).map(this::typenameOfClass)
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

	private String typenameOfClass(Path p) {
		return Utils.scanClass(p).getClassname();
	}

	private Stream<Path> findClasses(Path path) {
		ArrayList<Path> classfiles = new ArrayList<>();
		if (Files.isDirectory(path)) {
			walk(path, classfiles);
		} else {
			walkJar(path, classfiles);
		}
		return classfiles.stream();
	}

	static class ClassCollectorFileVisitor implements FileVisitor<Path> {
		
		private List<Path> collector = new ArrayList<>();

		List<Path> getClassFiles() {
			return collector;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (file.getFileName().toString().endsWith(".class")) {
				collector.add(file);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		
	}

	private void walkJar(Path jarfile, ArrayList<Path> classfiles) {
		try {
			FileSystem jarfs = FileSystems.newFileSystem(jarfile,null);
			Iterable<Path> rootDirectories = jarfs.getRootDirectories();
			ClassCollectorFileVisitor x = new ClassCollectorFileVisitor();
			for (Path path: rootDirectories) {
				Files.walkFileTree(path, x);
			}
			classfiles.addAll(x.getClassFiles());
		} catch (IOException e) {
			throw new IllegalStateException("Problem opening "+jarfile,e);
		}
	}

	private void walk(Path dir, ArrayList<Path> classfiles) {
		try {
			ClassCollectorFileVisitor x = new ClassCollectorFileVisitor();
			Files.walkFileTree(dir, x);
			classfiles.addAll(x.getClassFiles());
		} catch (IOException e) {
			throw new IllegalStateException("Problem walking directory "+dir, e);
			
		}
	}
	
	private Stream<Path> findDirectoriesOrTargetDirJar(List<String> classpath) {
		List<Path> result = new ArrayList<>();
		for (String classpathEntry : classpath) {
			File f = new File(classpathEntry);
			if (f.isDirectory()) {
				result.add(Paths.get(f.toURI()));
			} else if (f.isFile() && f.getName().endsWith(".jar") && f.getParent().endsWith(File.separator+"target")) {
				result.add(Paths.get(f.toURI()));
			}
		}
		return result.stream();
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
				// This 'name' may not be the same as 's' if 's' referred to an inner type -
				// 'name' will include the right '$' characters.
				String name = t.getDottedName();
				if (t.hasOnlySimpleConstructor()) {
					reflectionHandler.addAccess(name, new String[][] { { "<init>" } },null, false);
				} else {
					reflectionHandler.addAccess(name, Flag.allDeclaredConstructors);
				}
			}
		} catch (NoClassDefFoundError ncdfe) {
			System.out.println(
					"spring.factories processing, problem adding access for key " + s + ": " + ncdfe.getMessage());
		}
	}

	private void processSpringFactory(TypeSystem ts, URL springFactory) {
		List<SpringFactoriesProcessor> springFactoriesProcessors = ts.getSpringFactoryProcessors();
		List<String> forRemoval = new ArrayList<>();
		Properties p = new Properties();
		loadSpringFactoryFile(springFactory, p);
		int excludedAutoConfigCount = 0;
		Enumeration<Object> factoryKeys = p.keys();
		boolean modified = false;
		

		if (!ConfigOptions.isAgentMode()) {
			Properties filteredProperties = new Properties();
			for (Map.Entry<Object, Object> factoriesEntry : p.entrySet()) {
				String key = (String) factoriesEntry.getKey();
				String valueString = (String) factoriesEntry.getValue();
				List<String> values = new ArrayList<>();
				for (String value : valueString.split(",")) {
					values.add(value);
				}
				for (SpringFactoriesProcessor springFactoriesProcessor : springFactoriesProcessors) {
					if (springFactoriesProcessor.filter(springFactory, key, values)) {
						modified = true;
					}
				}
				if (modified) {
					filteredProperties.put(key, String.join(",", values));
				} else {
					filteredProperties.put(key, valueString);
				}
			}
			p = filteredProperties;
		}

		factoryKeys = p.keys();
		// Handle all keys other than EnableAutoConfiguration and PropertySourceLoader
		if (!ConfigOptions.isAgentMode()) {
			while (factoryKeys.hasMoreElements()) {
				String k = (String) factoryKeys.nextElement();
				SpringFeature.log("Adding all the classes for this key: " + k);
				if (!k.equals(enableAutoconfigurationKey) 
						&& !k.equals(propertySourceLoaderKey)
						&& !k.equals(managementContextConfigurationKey) 
						) {
					if (ts.shouldBeProcessed(k)) {
						for (String v : p.getProperty(k).split(",")) {
							registerTypeReferencedBySpringFactoriesKey(v);
						}
					} else {
						SpringFeature
								.log("Skipping processing spring.factories key " + k + " due to missing guard types");
					}
				}
			}
		}

		if (!ConfigOptions.isAgentMode()) {
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
		}

		modified = processConfigurationsWithKey(p, managementContextConfigurationKey) || modified;
		
		// TODO refactor this chunk to call processConfigurationsWithKey()
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
			if (forRemoval.size() == 0 && !modified) {
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

	/**
	 * Find the configurations referred to by the specified key in the specified properties object.
	 * Process each configuration and if it fails conditional checks (e.g. @ConditionalOnClass)
	 * then it is considered inactive and removed. The key is rewritten with a new list of configurations
	 * with inactive ones removed.
	 * @param p the properties object
	 * @param configurationsKey the key into the properties object whose value is a configurations list
	 * 
	 * @return true if inactive configurations were discovered and removed
	 */
	private boolean processConfigurationsWithKey(Properties p, String configurationsKey) {
		boolean modified = false;
		List<String> inactiveConfigurations = new ArrayList<>();
		String configurationsValue = (String)p.get(configurationsKey);
		if (configurationsValue != null) {
			List<String> configurations = Stream.of(configurationsValue.split(",")).collect(Collectors.toList());
			for (String configuration: configurations) {
				if (!checkAndRegisterConfigurationType(configuration)) {
					if (ConfigOptions.shouldRemoveUnusedAutoconfig()) {
						SpringFeature.log("Excluding auto-configuration (key="+configurationsKey+") =" +configuration);
						inactiveConfigurations.add(configuration);
					}
				}
			}
			if (ConfigOptions.shouldRemoveUnusedAutoconfig() && inactiveConfigurations.size()>0) {
				int totalConfigurations = configurations.size();
				configurations.removeAll(inactiveConfigurations);
				p.put(configurationsKey, String.join(",", configurations));
				SpringFeature.log("Removed "+inactiveConfigurations.size()+" of the "+totalConfigurations+" configurations specified for the key "+configurationsKey);
				modified = true;
			}
		}
		return modified;
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
		Type resolvedConfigType = ts.resolveDotted(config,true);
		if (resolvedConfigType==null) {
			SpringFeature.log("Configuration type " + config + " is missing - presuming stripped out - considered failed validation");
			return false;
		} else {
			boolean b = processType(resolvedConfigType, visited, 0);
			SpringFeature.log("Configuration type " + config + " has " + (b ? "passed" : "failed") + " validation");
			return b;
		}
	}

	/**
	 * Specific type references are used when registering types not easily identifiable from the
	 * bytecode that we are simply capturing as specific references in the Hints defined
	 * in the configuration module. Used for import selectors, import registrars, configuration.
	 * For @Configuration types here, need only bean method access (not all methods), for 
	 * other types (import selectors/etc) may not need any method reflective access at all
	 * (and no resource access in that case).
	 */
	private boolean registerSpecific(String typename, Integer typeKind, RequestedConfigurationManager rcm) {
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
				rcm.requestTypeAccess(typename, AccessBits.CLASS | AccessBits.DECLARED_CONSTRUCTORS);
			} else {
				if (AccessBits.isResourceAccessRequired(typeKind)) {
					rcm.requestTypeAccess(typename, AccessBits.RESOURCE);
					rcm.requestTypeAccess(typename, typeKind);
				} else {
					// TODO worth limiting it solely to @Bean methods? Need to check how many
					// configuration classes typically have methods that are not @Bean
					rcm.requestTypeAccess(typename, typeKind);
				}
				if (t.isAtConfiguration()) {
					// This is because of cases like Transaction auto configuration where the
					// specific type names types like ProxyTransactionManagementConfiguration
					// are referred to from the AutoProxyRegistrar CompilationHint.
					// There is a conditional on bean later on the supertype
					// (AbstractTransactionConfiguration)
					// and so we must register proxyXXX and its supertypes as visible.
					registerHierarchy(t, rcm);
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
		
		if (ConfigOptions.isIgnoreHintsOnExcludedConfig() && type.isAtConfiguration()) {
			if (isIgnored(type)) {
				SpringFeature.log("INFO: skipping hints on "+type.getName()+" because it is excluded in this application");
				// You may wonder why this is not false? That is because if we return false it will be deleted from
				// spring.factories. Then later when Spring processes the spring exclude autoconfig key that contains this
				// name - it will fail with an error that it doesn't refer to a valid configuration. So here we return true,
				// which isn't optimal but we do skip all the hint processing and further chasing from this configuration.
				return true;
			}
		}

		boolean passesTests = true;
		
		RequestedConfigurationManager accessRequestor = new RequestedConfigurationManager();
		
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
				boolean hintExplicitReferencesValidInCurrentMode = isHintValidForCurrentMode(hint);

				// This is used for hints that didn't gather data from the bytecode but had them
				// directly encoded. For example when a CompilationHint on an ImportSelector
				// encodes the types that might be returned from it.
				if (hintExplicitReferencesValidInCurrentMode) {
					Map<String, AccessDescriptor> specificNames = hint.getSpecificTypes();
					if (specificNames.size() > 0) {
						SpringFeature.log(spaces(depth) + "attempting registration of " + specificNames.size()
								+ " specific types");
						for (Map.Entry<String, AccessDescriptor> specificNameEntry : specificNames.entrySet()) {
							String specificTypeName = specificNameEntry.getKey();
							if (!registerSpecific(specificTypeName, specificNameEntry.getValue().getAccessBits(), accessRequestor)) {
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
									SpringFeature.log(
											spaces(depth) + "will follow specific type reference " + specificTypeName);
									toFollow.add(ts.resolveDotted(specificTypeName));
								}
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
							// TODO
							// Inferred types are how we follow configuration classes, we don't need to add these - 
							// we could rely on the existing config (from the surefire run) to tell us whether to follow some
							// things here..
							// Do we not follow if it is @Configuration and missing from the existing other file? 
							
							//if (!ConfigOptions.isFunctionalMode()) {
								accessRequestor.requestTypeAccess(s, inferredType.getValue());
							//}
							
							if (hint.isFollow()) {
								SpringFeature.log(spaces(depth) + "will follow " + t);
								toFollow.add(t);
							}
						} else if (hint.isSkipIfTypesMissing()  && (depth == 0 || isNestedConfiguration(type))) {
							// TODO If processing secondary type (depth>0) we can't skip things as we don't
							// know if the top level type that refers to us is going to fail or not. Ideally we should
							// pass in the tar and accumulate types in secondary type processing and leave it to the
							// outermost processing to decide if they need registration.
							// Update: the isNestedConfiguration() clause allows us to discard nested configurations that are failing a COC check.
							// This works if they are simply included in a setup due to being lexically inside an outer configuration - if they
							// are being explicitly referenced via some other mechanism (e.g. @Import) this will need a bit of rework (the outer
							// call into here should tell us how this configuration is being made so we can make a smarter decision).
							passesTests = false;
							// Once failing, no need to process other hints
							if (ConfigOptions.shouldRemoveUnusedAutoconfig()) {
								break hints;
							}
						}
					}
				}

				List<Type> annotationChain = hint.getAnnotationChain();
				registerAnnotationChain(depth, accessRequestor, annotationChain);
				if (hintExplicitReferencesValidInCurrentMode) {
					accessRequestor.requestProxyDescriptors(hint.getProxyDescriptors());
					accessRequestor.requestResourcesDescriptors(hint.getResourceDescriptors());
					accessRequestor.requestInitializationDescriptors(hint.getInitializationDescriptors());
				}
			}
		}

		// TODO think about pulling out into extension mechanism for condition evaluators
		// Special handling for @ConditionalOnWebApplication
		if (!type.checkConditionalOnWebApplication() && (depth == 0 || isNestedConfiguration(type))) {
			passesTests = false;
		}

		// TODO this should be pushed earlier and access requests put into tar
		String configNameDotted = type.getDottedName();
		visited.add(type.getName());
		if (passesTests || !ConfigOptions.shouldRemoveUnusedAutoconfig()) {
			if (type.isImportSelector()) {
				accessRequestor.requestTypeAccess(configNameDotted, Type.inferAccessRequired(type)|AccessBits.RESOURCE);
			} else {
			if (type.isCondition()) {
				accessRequestor.requestTypeAccess(configNameDotted, AccessBits.LOAD_AND_CONSTRUCT|AccessBits.RESOURCE);//Flag.allDeclaredConstructors);
//				if (type.hasOnlySimpleConstructor()) {
//					reflectionHandler.addAccess(configNameDotted, new String[][] { { "<init>" } },null, true);
//				} else {
//					reflectionHandler.addAccess(configNameDotted, null, null, true, Flag.allDeclaredConstructors);
//				}
			} else {
				accessRequestor.requestTypeAccess(configNameDotted, AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS|AccessBits.DECLARED_METHODS|AccessBits.RESOURCE);//Flag.allDeclaredConstructors);
//				reflectionHandler.addAccess(configNameDotted, Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
			}
//			resourcesRegistry.addResources(type.getName().replace("$", ".") + ".class");
			}
			// In some cases the superclass of the config needs to be accessible
			// TODO need this guard? if (isConfiguration(configType)) {
			// }
			// if (type.isAtConfiguration()) {
			registerHierarchy(type, accessRequestor);
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
		
		List<String> importedConfigurations = type.getImportedConfigurations();
		for (String importedConfiguration: importedConfigurations) {
			toFollow.add(ts.resolveSlashed(Type.fromLdescriptorToSlashed(importedConfiguration)));
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
					accessRequestor.requestTypeAccess(t.getDottedName(), AccessBits.CLASS);
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
						accessRequestor.requestTypeAccess(returnType.getDottedName(), AccessBits.CLASS | AccessBits.DECLARED_CONSTRUCTORS);
						/*
						Set<Type> ts = atBeanMethod.getSignatureTypes();
						for (Type t: ts) {
							SpringFeature.log("Processing @Bean method "+atBeanMethod.getName()+"(): adding "+t.getDottedName());
							tar.request(t.getDottedName(),
									AccessBits.CLASS | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_CONSTRUCTORS);
						}
						*/
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
					
					if (!ConfigOptions.isSkipAtBeanHintProcessing()) {
						List<Hint> methodHints = atBeanMethod.getHints();
						SpringFeature.log(spaces(depth) + "hints on method " + atBeanMethod + ":\n" + methodHints);
						for (Hint hint : methodHints) {
							SpringFeature.log(spaces(depth) + "processing hint " + hint);

							// This is used for hints that didn't gather data from the bytecode but had them
							// directly encoded. For example when a CompilationHint on an ImportSelector encodes
							// the types that might be returned from it.
							// (see the static initializer in Type for examples)
							Map<String, AccessDescriptor> specificNames = hint.getSpecificTypes();
							SpringFeature.log(spaces(depth) + "attempting registration of " + specificNames.size()
									+ " specific types");
							for (Map.Entry<String, AccessDescriptor> specificNameEntry : specificNames.entrySet()) {
								registerSpecific(specificNameEntry.getKey(),
										specificNameEntry.getValue().getAccessBits(), accessRequestor);
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
									accessRequestor.requestTypeAccess(s, inferredType.getValue());
									if (hint.isFollow()) {
										toFollow.add(t);
									}
								} else if (hint.isSkipIfTypesMissing()) {
									passesTests = false;
									// Once failing, no need to process other hints
								}
							}
							if (!ConfigOptions.isFunctionalMode()) {
								List<Type> annotationChain = hint.getAnnotationChain();
								registerAnnotationChain(depth, accessRequestor, annotationChain);
							}
						}
					}

					// Register other runtime visible annotations from the @Bean method. For example
					// this ensures @Role is visible on:
					// @Bean
					// @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
					// @ConditionalOnMissingBean(Validator.class)
					// public static LocalValidatorFactoryBean defaultValidator() {
					List<Type> annotationsOnMethod = atBeanMethod.getAnnotationTypes();
					for (Type annotationOnMethod : annotationsOnMethod) {
						accessRequestor.requestTypeAccess(annotationOnMethod.getDottedName(), AccessBits.ANNOTATION);
					}
				}
			}
			// Follow transitively included inferred types only if necessary:
			for (Type t : toFollow) {
				boolean shouldFollow = existingReflectionConfigContains(t.getDottedName()); // Only worth following if this config is active...
				if (ConfigOptions.isAgentMode() && t.isAtConfiguration() && !shouldFollow) {
					System.out.println("hybrid: Not following "+t.getDottedName()+" from "+type.getName()+" - not mentioned in existing reflect configuration");
					continue;
				}
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
			for (InitializationDescriptor initializationDescriptor : accessRequestor.getRequestedInitializations()) {
				initializationHandler.registerInitializationDescriptor(initializationDescriptor);
			}
			for (ProxyDescriptor proxyDescriptor : accessRequestor.getRequestedProxies()) {
				dynamicProxiesHandler.addProxy(proxyDescriptor);
			}
			for (org.springframework.graalvm.type.ResourcesDescriptor rd : accessRequestor.getRequestedResources()) {
				registerResourcesDescriptor(rd);
			}
			registerAllRequested(depth, accessRequestor);
		}

		// If the outer type is failing a test, we don't need to go into nested types...
		if (passesTests || !ConfigOptions.shouldRemoveUnusedAutoconfig()) {
			// if (type.isAtConfiguration() || type.isAbstractNestedCondition()) {
//			SpringFeature.log(spaces(depth)+" processing nested types of "+type.getName());
			List<Type> nestedTypes = type.getNestedTypes();
			for (Type t : nestedTypes) {
				if (visited.add(t.getName())) {
					if (!(t.isAtConfiguration() || t.isConditional() || t.isMetaImportAnnotated() || t.isComponent())) {
						continue;
					}
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

	private void registerAllRequested(int depth, RequestedConfigurationManager accessRequestor) {
		for (Map.Entry<String, Integer> accessRequest : accessRequestor.getRequestedTypeAccesses()) {
			String dname = accessRequest.getKey();
			int requestedAccess = accessRequest.getValue();
			
			// TODO promote this approach to a plugin if becomes a little more common...
			if (dname.equals("org.springframework.boot.autoconfigure.web.ServerProperties$Jetty")) { // See EmbeddedJetty @COC check
				if (!ts.canResolve("org/eclipse/jetty/webapp/WebAppContext")) {
					System.out.println("Reducing access on "+dname+" because WebAppContext not around");
					requestedAccess = AccessBits.CLASS;
				}
			}

			if (dname.equals("org.springframework.boot.autoconfigure.web.ServerProperties$Undertow")) { // See EmbeddedUndertow @COC check
				if (!ts.canResolve("io/undertow/Undertow")) {
					System.out.println("Reducing access on "+dname+" because Undertow not around");
					requestedAccess = AccessBits.CLASS;
				}
			}

			if (dname.equals("org.springframework.boot.autoconfigure.web.ServerProperties$Tomcat")) { // See EmbeddedTomcat @COC check
				if (!ts.canResolve("org/apache/catalina/startup/Tomcat")) {
					System.out.println("Reducing access on "+dname+" because Tomcat not around");
					requestedAccess = AccessBits.CLASS;
				}
			}

			if (dname.equals("org.springframework.boot.autoconfigure.web.ServerProperties$Netty")) { // See EmbeddedNetty @COC check
				if (!ts.canResolve("reactor/netty/http/server/HttpServer")) {
					System.out.println("Reducing access on "+dname+" because HttpServer not around");
					requestedAccess = AccessBits.CLASS;
				}
			}

			// Let's produce a message if this computed value is also in reflect.json
			// This is a sign we can probably remove that entry from reflect.json (maybe
			// depend if inferred access required matches declared)
			if (reflectionHandler.getConstantData().hasClassDescriptor(dname)) {
				System.out.println("This is in the constant data, does it need to stay in there? " + dname
						+ "  (dynamically requested access is " + requestedAccess + ")");
			}

			SpringFeature.log(spaces(depth) + "making this accessible: " + dname + "   " + AccessBits.toString(requestedAccess));
			Flag[] flags = AccessBits.getFlags(requestedAccess);
			Type rt = ts.resolveDotted(dname, true);
			if (ConfigOptions.isFunctionalMode()) {
				if (rt.isAtConfiguration() || rt.isConditional() || rt.isCondition() ||
						rt.isImportSelector() || rt.isImportRegistrar()) {
				continue;
				}
			}
			if (flags != null && flags.length == 1 && flags[0] == Flag.allDeclaredConstructors) {
				Type resolvedType = ts.resolveDotted(dname, true);
				if (resolvedType != null && resolvedType.hasOnlySimpleConstructor()) {
					reflectionHandler.addAccess(dname, new String[][] { { "<init>" } },null, true);
				} else {
					reflectionHandler.addAccess(dname, null, null, true, flags);
				}
			} else {
				reflectionHandler.addAccess(dname, null, null, true, flags);
			}
			if (AccessBits.isResourceAccessRequired(requestedAccess)) {
				resourcesRegistry.addResources(
						dname.replace(".", "/").replace("$", ".").replace("[", "\\[").replace("]", "\\]")
								+ ".class");
			}
		}
	}
	
	private boolean isHintValidForCurrentMode(Hint hint) {
		Mode currentMode = ConfigOptions.getMode();
		if (!hint.applyToFunctional() && currentMode == Mode.FUNCTIONAL) {
			return false;
		}
		return true;
	}
	
	private boolean isHintValidForCurrentMode(CompilationHint hint) {
		Mode currentMode = ConfigOptions.getMode();
		if (!hint.applyToFunctional() && currentMode==Mode.FUNCTIONAL) {
			return false;
		}
		return true;
	}

	private boolean isIgnored(Type configurationType) {
		List<String> excludedAutoConfig = ts.getExcludedAutoConfigurations();
		return excludedAutoConfig.contains(configurationType.getDottedName());
	}

	private boolean existingReflectionConfigContains(String s) {
		Map<String, ReflectionDescriptor> reflectionConfigurationsOnClasspath = ts.getReflectionConfigurationsOnClasspath();
		for (ReflectionDescriptor rd: reflectionConfigurationsOnClasspath.values()) {
			if (rd.hasClassDescriptor(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Crude guess at nested configuration.
	 */
	private boolean isNestedConfiguration(Type type) {
		boolean b = type.isAtConfiguration() && type.getEnclosingType()!=null;
		return b;
	}

	private void registerAnnotationChain(int depth, RequestedConfigurationManager tar, List<Type> annotationChain) {
		SpringFeature.log(spaces(depth) + "attempting registration of " + annotationChain.size()
				+ " elements of annotation chain");
		for (int i = 0; i < annotationChain.size(); i++) {
			// i=0 is the annotated type, i>0 are all annotation types
			Type t = annotationChain.get(i);
			if (i==0 && ConfigOptions.isAgentMode()) {
				boolean beingReflectedUponInIncomingConfiguration = existingReflectionConfigContains(t.getDottedName());
				if (!beingReflectedUponInIncomingConfiguration) {
					System.out.println("HYBRID: IGNORE: We could skip this "+t.getDottedName());
					break;
				}
			}
			tar.requestTypeAccess(t.getDottedName(), Type.inferAccessRequired(t));
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

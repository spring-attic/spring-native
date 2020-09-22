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
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature.BeforeAnalysisAccess;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.springframework.graalvm.domain.init.InitializationDescriptor;
import org.springframework.graalvm.domain.reflect.Flag;
import org.springframework.graalvm.domain.reflect.MethodDescriptor;
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

	public TypeSystem ts;

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
			return ResourcesJsonMarshaller.read(s);
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
			SpringFeature.log("Registering statically declared resources - #" + rd.getPatterns().size() + " patterns");
			registerPatterns(rd);
			registerResourceBundles(rd);
		}
		if (ConfigOptions.isAnnotationMode() ||
			ConfigOptions.isAgentMode()) {
			processSpringFactories();
		}
		if (!ConfigOptions.isInitMode()) {
			handleConstantHints();
		}
		handleConstantInitializationHints();
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
	private void handleConstantHints() {
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
	
	private void handleConstantInitializationHints() {
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
					processSpringComponentsAgent(p, context);
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
				processSpringComponentsAgent(p, context);
			} else if (ConfigOptions.isFunctionalMode()) {
				processSpringComponentsFunc(p, context, alreadyProcessed);
			} else {
				processSpringComponents(p, context, alreadyProcessed);
			}
		}
	}

	private Properties synthesizeSpringComponents() {
		Properties p = new Properties();
		List<Entry<Type, List<Type>>> components = ts.scanForSpringComponents();
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
	
	// TODO [0.9.0] rationalize duplicate processSpringComponentXXXX methods
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
				checkAndRegisterConfigurationType(key,ReachedBy.FromSpringComponent);
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
	
	private void processSpringComponentsAgent(Properties p, NativeImageContext context) {
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
	 * app.main.model.Foo=javax.persistence.Entity,something.Else
	 * app.main.model.FooRepository=org.springframework.data.repository.Repository
	 * </code></pre>
	 * @param p the properties object containing spring components
	 */
	private void processSpringComponents(Properties p, NativeImageContext context, List<String> alreadyProcessed) {
		int registeredComponents = 0;
		RequestedConfigurationManager requestor = new RequestedConfigurationManager();
		for (Entry<Object, Object> entry : p.entrySet()) {
			boolean processedOK = processSpringComponent((String)entry.getKey(), (String)entry.getValue(), context, requestor, alreadyProcessed);
			if (processedOK) {
				registeredComponents++;
			}
		}
		registerAllRequested(requestor);
		ts.getComponentProcessors().forEach(ComponentProcessor::printSummary);
		SpringFeature.log("Registered " + registeredComponents + " entries");
	}
	
	private boolean processSpringComponent(String componentTypename, String classifiers, NativeImageContext context, RequestedConfigurationManager requestor, List<String> alreadyProcessed) {
		List<ComponentProcessor> componentProcessors = ts.getComponentProcessors();
		boolean isComponent = false;
		if (classifiers.equals("package-info")) {
			return false;
		}
		if (alreadyProcessed.contains(componentTypename+":"+classifiers)) {
			return false;
		}
		alreadyProcessed.add(componentTypename+":"+classifiers);
		Type kType = ts.resolveDotted(componentTypename);
		SpringFeature.log("Registering Spring Component: " + componentTypename);

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
			checkAndRegisterConfigurationType(componentTypename,ReachedBy.FromSpringComponent);
		} else {
			try {
				// TODO assess which kinds of thing requiring what kind of access - here we see
				// an Entity might require field reflective access where others don't
				// I think as a component may have autowired fields (and an entity may have
				// interesting fields) - you kind of always need to expose fields
				// There is a type in vanilla-orm called Bootstrap that shows this need
				reflectionHandler.addAccess(componentTypename, Flag.allDeclaredConstructors, Flag.allDeclaredMethods,
					Flag.allDeclaredClasses, Flag.allDeclaredFields);
				resourcesRegistry.addResources(componentTypename.replace(".", "/") + ".class");
				// Register nested types of the component
				for (Type t : kType.getNestedTypes()) {
					reflectionHandler.addAccess(t.getDottedName(), Flag.allDeclaredConstructors, Flag.allDeclaredMethods,
							Flag.allDeclaredClasses);
					resourcesRegistry.addResources(t.getName() + ".class");
				}
				registerHierarchy(kType, requestor);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		if (kType != null && kType.isAtRepository()) { // See JpaVisitRepositoryImpl in petclinic sample
		    // TODO [0.9.0] is this all handled by SpringDataComponentProcessor now?
			processRepository2(kType);
		}
		if (kType != null && kType.isAtResponseBody()) {
			// TODO [0.9.0] move into WebComponentProcessor?
			processResponseBodyComponent(kType);
		}
		List<String> values = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(classifiers, ",");
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
//					reflectionHandler.addAccess(n, Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allDeclaredClasses);
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
		for (Type type : kType.getNestedTypes()) {
			if (type.isComponent()) {
				// TODO do we need to fill in the classifiers list here (second param) correctly?
				// (We could do it, inferring like we infer spring.components in general)
				processSpringComponent(type.getDottedName(),"",context,requestor,alreadyProcessed);
			}
		}
		for (ComponentProcessor componentProcessor: componentProcessors) {
			if (componentProcessor.handle(context, componentTypename, values)) {
				componentProcessor.process(context, componentTypename, values);
			}
		}	
		return true;
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
					AccessBits.DECLARED_CONSTRUCTORS|
					AccessBits.PUBLIC_METHODS|//AccessBits.DECLARED_METHODS|
					AccessBits.RESOURCE);
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
				if (!checkAndRegisterConfigurationType(config,ReachedBy.FromSpringFactoriesKey)) {
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
				if (!checkAndRegisterConfigurationType(configuration,ReachedBy.FromSpringFactoriesKey)) {
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
	private boolean checkAndRegisterConfigurationType(String name,ReachedBy reachedBy) {
		return processType(name, new HashSet<>(),reachedBy);
	}

	private boolean processType(String config, Set<String> visited, ReachedBy reachedBy) {
		SpringFeature.log("\n\nProcessing configuration type " + config);
		Type resolvedConfigType = ts.resolveDotted(config,true);
		if (resolvedConfigType==null) {
			SpringFeature.log("Configuration type " + config + " is missing - presuming stripped out - considered failed validation");
			return false;
		} else {
			boolean b = processType(resolvedConfigType, visited, 0, reachedBy);
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
	private boolean registerSpecific(String typename, AccessDescriptor ad, RequestedConfigurationManager rcm) {
		int accessBits = ad.getAccessBits();
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
				rcm.requestTypeAccess(typename, AccessBits.CLASS | AccessBits.DECLARED_CONSTRUCTORS,ad.getMethodDescriptors(),ad.getFieldDescriptors());
			} else {
				if (AccessBits.isResourceAccessRequired(accessBits)) {
					rcm.requestTypeAccess(typename, AccessBits.RESOURCE);
					rcm.requestTypeAccess(typename, accessBits);
				} else {
					// TODO worth limiting it solely to @Bean methods? Need to check how many
					// configuration classes typically have methods that are not @Bean
					rcm.requestTypeAccess(typename, accessBits);
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
	
	/**
	 * Captures the route taken when processing a type - we can be more aggressive about
	 * discarding information depending on the route taken. For example it is easy
	 * to modify spring.factories to no longer to refer to an autoconfiguration, but
	 * you can't throw away a configuration if referenced from @Import in another type.
	 * (You can create a shell of a representation for the unnecessary config but
	 * you cannot discard entirely because @Import processing will break).
	 */
	enum ReachedBy {
		FromRoot,
		Import,
		Other,
		FromSpringFactoriesKey, // the type reference was discovered from a spring.factories entry
		FromSpringComponent, // the type reference was discovered when reviewing spring.components
		NestedReference, // This was discovered as a nested type within some type currently being processed
		HierarchyProcessing, // This was discovered whilst going up the hierarchy from some type currently being processed
		Inferred, // This type was 'inferred' whilst processing a hint (e.g. the class in a @COC usage)
		Specific // This type was explicitly listed in a hint that was processed
	}

	private boolean processType(Type type, Set<String> visited, int depth, ReachedBy reachedBy) {
		SpringFeature.log(spaces(depth) + "Analyzing " + type.getDottedName()+" reached by "+reachedBy);

		if (ConfigOptions.shouldRemoveJmxSupport()) {
			if (type.getDottedName().toLowerCase().contains("jmx") && 
					!(reachedBy==ReachedBy.Import || reachedBy==ReachedBy.NestedReference)) {
				SpringFeature.log(depth,type.getDottedName()+" FAILED validation - it has 'jmx' in it - returning FALSE");
				return false;
			}
		}

		// Check the hierarchy of the type, if bits are missing resolution of this
		// type at runtime will not work - that suggests that in this particular
		// run the types are not on the classpath and so this type isn't being used.
		Set<String> missingTypes = ts.findMissingTypesInHierarchyOfThisType(type);
		if (!missingTypes.isEmpty()) {
			SpringFeature.log(depth,"for " + type.getName() + " missing types in hierarchy are " + missingTypes );
			if (ConfigOptions.shouldRemoveUnusedAutoconfig()) {
				return false;
			}
		}

		Set<String> missingAnnotationTypes = ts.resolveCompleteFindMissingAnnotationTypes(type);
		if (!missingAnnotationTypes.isEmpty()) {
			// If only the annotations are missing, it is ok to reflect on the existence of
			// the type, it is just not safe to reflect on the annotations on that type.
			SpringFeature.log(depth,"for " + type.getName() + " missing annotation types are "
					+ missingAnnotationTypes);
		}
		
		if (ConfigOptions.isIgnoreHintsOnExcludedConfig() && type.isAtConfiguration()) {
			if (isIgnored(type)) {
				SpringFeature.log(depth, "skipping hints on "+type.getName()+" because it is excluded in this application");
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
		printHintSummary(type, depth, hints);
		Map<Type,ReachedBy> toFollow = new HashMap<>();
		if (!hints.isEmpty()) {
			hints: for (Hint hint : hints) {
				SpringFeature.log(depth, "processing hint " + hint);
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
							if (!registerSpecific(specificTypeName, specificNameEntry.getValue(), accessRequestor)) {
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
									toFollow.put(ts.resolveDotted(specificTypeName),ReachedBy.Specific);
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
								ReachedBy reason = isImportHint(hint)?ReachedBy.Import:ReachedBy.Inferred;
								toFollow.put(t,reason);
							}
							// 'reachedBy==ReachedBySpecific' - trying to do the right thing for (in this example) EhCacheCacheConfiguration.
							// It is a specific reference in the hint on the cache selector - but what does this rule break?
							// The problem is 'conditions' - spring will break with this kind of thing:
							// org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration$ConfigAvailableCondition
//							org.springframework.beans.factory.BeanDefinitionStoreException: Failed to process import candidates for configuration class [org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration]; nested exception is java.lang.IllegalArgumentException: Could not find class [org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration$ConfigAvailableCondition]
//									at org.springframework.context.annotation.ConfigurationClassParser.processImports(ConfigurationClassParser.java:610) ~[na:na]
//									at org.springframework.context.annotation.ConfigurationClassParser.processImports(ConfigurationClassParser.java:583) ~[na:na]
//									at org.springframework.context.annotation.ConfigurationClassParser.doProcessConfigurationClass(ConfigurationClassParser.java:311) ~[na:na]
						} else if (hint.isSkipIfTypesMissing() && (depth == 0 || isNestedConfiguration(type) /*|| reachedBy==ReachedBy.Specific*/ || reachedBy==ReachedBy.Import)) {
							if (depth>0) {
								System.out.println("inferred type missing: "+s+" (processing type: "+type.getDottedName()+" reached by "+reachedBy+") - discarding "+type.getDottedName());
							}
							// Notes: if an inferred type is missing, we have to be careful. Although it should suggest we discard
							// the type being processed, that is not always possible depending on how the type being processed
							// was reached.  If the type being processed came from a spring.factories file, fine, we can throw it away
							// and modify spring.factories. But if we started processing this type because it was listed in an @Import
							// reference, we can't throw it away completely because Spring will fail when it can't class load something
							// listed in @Import. What we can do is create a minimal shell of a type (not allow member reflection) but
							// we can't discard it completely. So the checks here are trying to be careful about what we can throw away.
							// Because nested configuration is discovered solely by reflecting on outer configuration, we can discard
							// any types being processed that were reached by digging into nested types.
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

		String configNameDotted = type.getDottedName();
		visited.add(type.getName());
		if (passesTests || !ConfigOptions.shouldRemoveUnusedAutoconfig()) {
			if (type.isImportSelector()) {
				accessRequestor.requestTypeAccess(configNameDotted, Type.inferAccessRequired(type)|AccessBits.RESOURCE);
			} else {
				if (type.isCondition()) {
					accessRequestor.requestTypeAccess(configNameDotted, AccessBits.LOAD_AND_CONSTRUCT|AccessBits.RESOURCE);
				} else {
					accessRequestor.requestTypeAccess(configNameDotted, AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS|AccessBits.DECLARED_METHODS|AccessBits.RESOURCE);//Flag.allDeclaredConstructors);
				}
			}
			// TODO need this guard? if (isConfiguration(configType)) {
			registerHierarchy(type, accessRequestor);
			recursivelyCallProcessTypeForHierarchyOfType(type, visited, depth);
		}
		
		List<String> importedConfigurations = type.getImportedConfigurations();
		for (String importedConfiguration: importedConfigurations) {
			toFollow.put(ts.resolveSlashed(Type.fromLdescriptorToSlashed(importedConfiguration)),ReachedBy.Import);
		}

		if (passesTests || !ConfigOptions.shouldRemoveUnusedAutoconfig()) {
			if (type.isImportSelector()) {
				
			}
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
				String[][] validMethodsSubset = processTypeAtBeanMethods(type, depth, accessRequestor, toFollow);
				if (validMethodsSubset != null) {
					printMemberSummary(validMethodsSubset);
					// System.out.println("What is the current request level for this config? "+accessRequestor.getTypeAccessRequestedFor(type.getDottedName()));
				}
				configureMethodAccess(type, accessRequestor, validMethodsSubset);
			}
			// Follow transitively included inferred types only if necessary:
			for (Map.Entry<Type,ReachedBy> entry : toFollow.entrySet()) {
				Type t = entry.getKey();
				if (ConfigOptions.isAgentMode() && t.isAtConfiguration()) {
					boolean existsInVisibleConfig = existingReflectionConfigContains(t.getDottedName()); // Only worth following if this config is active...
					if (!existsInVisibleConfig) {
						SpringFeature.log(spaces(depth)+"in agent mode not following "+t.getDottedName()+" from "+type.getName()+" - it is not mentioned in existing reflect configuration");
						continue;
					}
				}
				try {
					boolean b = processType(t, visited, depth + 1, entry.getValue());
					if (!b) {
						SpringFeature.log(spaces(depth) + "followed " + t.getName() + " and it failed validation (whilst processing "+type.getDottedName()+" reached by "+reachedBy+")");
						accessRequestor.reduceTypeAccess(t.getDottedName(),AccessBits.DECLARED_CONSTRUCTORS|AccessBits.CLASS|AccessBits.RESOURCE);
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
						boolean b = processType(t, visited, depth + 1, ReachedBy.NestedReference);
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

	/**
	 * This handles when some @Bean methods on type do not need runtime access (because they have conditions
	 * on them that failed when checked). In this case the full list of methods should be spelled out
	 * excluding the unnecessary ones. If validMethodsSubset is null then all @Bean methods are valid.
	 */
	private void configureMethodAccess(Type type, RequestedConfigurationManager accessRequestor,
			String[][] validMethodsSubset) {
		SpringFeature.log("computing full method list for "+type.getDottedName());
//		boolean onlyPublicMethods = (accessRequestor.getTypeAccessRequestedFor(type.getDottedName())&AccessBits.PUBLIC_METHODS)!=0;
		boolean onlyNonPrivateMethods = true;
		List<String> toMatchAgainst = new ArrayList<>();
		if (validMethodsSubset!=null) {
			for (String[] validMethod: validMethodsSubset) {
				toMatchAgainst.add(String.join("::",validMethod));
			}
		}
		List<String[]> allRelevantMethods = new ArrayList<>();
		for (Method method : type.getMethods()) {
			System.out.println("Checking "+method);
			if (!method.getName().equals("<init>") && !method.getName().equals("<clinit>")) { // ignore constructors
				if (onlyNonPrivateMethods && method.isPrivate()) {
					System.out.println("private - skipping");
					continue;
				}
//				if (onlyPublicMethods && !method.isPublic()) {
//					continue;
//				}
				if (method.hasUnresolvableParams()) {
					SpringFeature.log("ignoring method "+method.getName()+method.getDesc()+" - unresolvable parameters");
					continue;
				}
				String[] candidate = method.asConfigurationArray();
				if (method.markedAtBean()) {
					if (validMethodsSubset != null && !toMatchAgainst.contains(String.join("::", candidate))) {
						continue;
					}
				} 
				allRelevantMethods.add(candidate);
			}
		}
		String[][] methods = allRelevantMethods.toArray(new String[0][]);
		printMemberSummary(methods);
		accessRequestor.addMethodDescriptors(type.getDottedName(), methods);
	}

	private void printMemberSummary(String[][] processTypeAtBeanMethods) {
		if (processTypeAtBeanMethods != null) {
			SpringFeature.log("member summary: " + processTypeAtBeanMethods.length);
			for (int i = 0; i < processTypeAtBeanMethods.length; i++) {
				String[] member = processTypeAtBeanMethods[i];
				StringBuilder s = new StringBuilder();
				s.append(member[0]).append("(");
				for (int p = 1; p < member.length; p++) {
					if (p > 1) {
						s.append(",");
					}
					s.append(member[p]);
				}
				s.append(")");
				SpringFeature.log(s.toString());
			}
		}
	}

	private boolean isImportHint(Hint hint) {
		List<Type> annotationChain = hint.getAnnotationChain();
		return annotationChain.get(annotationChain.size()-1).equals(ts.getType_Import());
	}

	private void printHintSummary(Type type, int depth, List<Hint> hints) {
		if (hints.size() != 0) {
			SpringFeature.log(depth, "found "+ hints.size() + " hints on " + type.getDottedName()+":");
			for (int h = 0; h < hints.size(); h++) {
				SpringFeature.log(spaces(depth) + (h + 1) + ") " + hints.get(h));
			}
		} else {
			SpringFeature.log(spaces(depth) + "no hints on " + type.getName());
		}
	}

	/**
	 * Process any @Bean methods in a configuration type. For these methods we need to ensure
	 * reflective access to the return types involved as well as any annotations on the
	 * method. It is also important to check any Conditional annotations on the method as
	 * a ConditionalOnClass may fail at build time and the whole @Bean method can be ignored.
	 */
	private String[][] processTypeAtBeanMethods(Type type, int depth, 
			RequestedConfigurationManager rcm, Map<Type,ReachedBy> toFollow) {
		List<String[]> passingMethodsSubset = new ArrayList<>();
		boolean anyMethodFailedValidation = false;
		// This is computing how many methods we are exposing unnecessarily via reflection by 
		// specifying allDeclaredMethods for this type rather than individually specifying them.
		// A high number indicates we should perhaps do more to be selective.
		int totalMethodCount = type.getMethodCount(false);
		List<Method> atBeanMethods = type.getMethodsWithAtBean();
		int rogue = (totalMethodCount - atBeanMethods.size());
		if (rogue != 0) {
			SpringFeature.log(depth,
					"WARNING: Methods unnecessarily being exposed by reflection on this config type "
					+ type.getName() + " = " + rogue + " (total methods including @Bean ones:" + totalMethodCount + ")");
		}

		if (atBeanMethods.size() != 0) {
			SpringFeature.log(spaces(depth) + "processing " + atBeanMethods.size() + " @Bean methods on type "+type.getDottedName());
		}

		for (Method atBeanMethod : atBeanMethods) {
			RequestedConfigurationManager methodRCM = new RequestedConfigurationManager();
			Map<Type, ReachedBy> additionalFollows = new HashMap<>();
			boolean passesTests = true;
			Type returnType = atBeanMethod.getReturnType();
			if (returnType == null) {
				// null means that type is not on the classpath so skip further analysis of this method...
				continue;
			} else {
				methodRCM.requestTypeAccess(returnType.getDottedName(), AccessBits.CLASS | AccessBits.DECLARED_CONSTRUCTORS);
			}

			// Example method being handled here:
			//	@Bean
			//	@ConditionalOnClass(name = "org.springframework.security.authentication.event.AbstractAuthenticationEvent")
			//	@ConditionalOnMissingBean(AbstractAuthenticationAuditListener.class)
			//	public AuthenticationAuditListener authenticationAuditListener() throws Exception {
			//		return new AuthenticationAuditListener();
			//	}
			if (!ConfigOptions.isSkipAtBeanHintProcessing()) {
				List<Hint> methodHints = atBeanMethod.getHints();
				SpringFeature.log(spaces(depth) + "@Bean method "+atBeanMethod + " hints: #"+methodHints.size());
				for (int i=0;i<methodHints.size();i++) {
					SpringFeature.log(spaces(depth+1)+(i+1)+") "+methodHints.get(i));
				}
				for (int h=0;h<methodHints.size() && passesTests;h++) {
					Hint hint = methodHints.get(h);
					SpringFeature.log(spaces(depth+1) + "processing hint " + hint);

					Map<String, AccessDescriptor> specificNames = hint.getSpecificTypes();
					if (specificNames.size() != 0) {
						SpringFeature.log(spaces(depth+1) + "handling " + specificNames.size() + " specific types");
						for (Map.Entry<String, AccessDescriptor> specificNameEntry : specificNames.entrySet()) {
							registerSpecific(specificNameEntry.getKey(),
									specificNameEntry.getValue(), methodRCM);
						}
					}

					Map<String, Integer> inferredTypes = hint.getInferredTypes();
					if (inferredTypes.size()!=0) {
						SpringFeature.log(spaces(depth+1) + "handling " + inferredTypes.size() + " inferred types");
						for (Map.Entry<String, Integer> inferredType : inferredTypes.entrySet()) {
							String s = inferredType.getKey();
							Type t = ts.resolveDotted(s, true);
							boolean exists = (t != null);
							if (!exists) {
								SpringFeature.log(spaces(depth+1) + "inferred type " + s + " not found");
							} else {
								SpringFeature.log(spaces(depth+1) + "inferred type " + s + " found, will get accessibility " + AccessBits.toString(inferredType.getValue()));
							}
							if (exists) {
								// TODO if already there, should we merge access required values?
								methodRCM.requestTypeAccess(s, inferredType.getValue());
								if (hint.isFollow()) {
									additionalFollows.put(t,ReachedBy.Other);
								}
							} else if (hint.isSkipIfTypesMissing()) {
								passesTests = false;
								break;
							}
						}
					}
					if (passesTests && !ConfigOptions.isFunctionalMode()) {
						List<Type> annotationChain = hint.getAnnotationChain();
						registerAnnotationChain(depth+1, methodRCM, annotationChain);
					}
				}
			}

			// Register other runtime visible annotations from the @Bean method. For example
			// this ensures @Role is visible on:
			// @Bean
			// @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
			// @ConditionalOnMissingBean(Validator.class)
			// public static LocalValidatorFactoryBean defaultValidator() {
			if (passesTests) {
				List<Type> annotationsOnMethod = atBeanMethod.getAnnotationTypes();
				for (Type annotationOnMethod : annotationsOnMethod) {
					methodRCM.requestTypeAccess(annotationOnMethod.getDottedName(), AccessBits.ANNOTATION);
				}
			} 
			if (passesTests) {
				try {
					passingMethodsSubset.add(atBeanMethod.asConfigurationArray());
					toFollow.putAll(additionalFollows);
					rcm.mergeIn(methodRCM);
					SpringFeature.log(spaces(depth)+"method passed checks - adding configuration for it");
				} catch (IllegalStateException ise) {
					// usually if asConfigurationArray() fails - due to an unresolvable type - it indicates
					// this method is no use
					SpringFeature.log(depth,"method failed checks - ise on "+atBeanMethod.getName()+" so ignoring");
					anyMethodFailedValidation = true;
				}
			} else {
				anyMethodFailedValidation = true;
				SpringFeature.log(spaces(depth)+"method failed checks - not adding configuration for it");
			}
		}
		if (anyMethodFailedValidation) {
			// Return the subset that passed
			return passingMethodsSubset.toArray(new String[0][]);
		} else {
			return null;
		}
	}

	private void recursivelyCallProcessTypeForHierarchyOfType(Type type, Set<String> visited, int depth) {
		Type s = type.getSuperclass();
		while (s != null) {
			if (s.getName().equals("java/lang/Object")) {
				break;
			}
			if (visited.add(s.getName())) {
				boolean b = processType(s, visited, depth + 1, ReachedBy.HierarchyProcessing);
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

	private void registerAllRequested(RequestedConfigurationManager accessRequestor) {
		registerAllRequested(0, accessRequestor);
	}
	
	// In an attempt to reduce verbosity helps avoid reporting identical messages over and over
	private static Map<String, Integer> reflectionConfigurationAlreadyAdded = new HashMap<>();

	private void registerAllRequested(int depth, RequestedConfigurationManager accessRequestor) {
		for (Map.Entry<String, Integer> accessRequest : accessRequestor.getRequestedTypeAccesses()) {
			String dname = accessRequest.getKey();
			int requestedAccess = accessRequest.getValue();
			List<org.springframework.graalvm.type.MethodDescriptor> methods = accessRequestor.getMethodAccessRequestedFor(dname);
			
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

			// Only log new info that is being added at this stage to keep logging down
			Integer access = reflectionConfigurationAlreadyAdded.get(dname);
			if (access == null) {
				SpringFeature.log(spaces(depth) + "configuring reflective access to " + dname + "   " + AccessBits.toString(requestedAccess));
				reflectionConfigurationAlreadyAdded.put(dname, requestedAccess);
			} else {
				int extraAccess = AccessBits.compareAccess(access,requestedAccess);
				if (extraAccess>0) {
					SpringFeature.log(spaces(depth) + "configuring reflective access, adding access for " + dname + " of " + 
							AccessBits.toString(extraAccess)+" (total now: "+AccessBits.toString(requestedAccess)+")");
					reflectionConfigurationAlreadyAdded.put(dname, access);
				}
			}
			Flag[] flags = AccessBits.getFlags(requestedAccess);
			Type rt = ts.resolveDotted(dname, true);
			if (ConfigOptions.isFunctionalMode()) {
				if (rt.isAtConfiguration() || rt.isConditional() || rt.isCondition() ||
						rt.isImportSelector() || rt.isImportRegistrar()) {
				continue;
				}
			}

			if (methods != null) {
				// methods are explicitly specified, remove them from flags
//				SpringFeature.log("Stripping out method flags for "+dname);
				flags = filterFlags(flags, Flag.allDeclaredMethods, Flag.allPublicMethods);
			}
//			SpringFeature.log(spaces(depth) + "fixed flags? "+Flag.toString(flags));
//			SpringFeature.log(depth, "ms: "+methods);
			reflectionHandler.addAccess(dname, MethodDescriptor.toStringArray(methods), null, true, flags);
			/*
			if (flags != null && flags.length == 1 && flags[0] == Flag.allDeclaredConstructors) {
				Type resolvedType = ts.resolveDotted(dname, true);
//				if (resolvedType != null && resolvedType.hasOnlySimpleConstructor()) {
//					reflectionHandler.addAccess(dname, new String[][] { { "<init>" } },null, true);
//				} else {
//				}
			} else {
				reflectionHandler.addAccess(dname, null, null, true, flags);
			}
			*/
			if (AccessBits.isResourceAccessRequired(requestedAccess)) {
				resourcesRegistry.addResources(
						dname.replace(".", "/").replace("$", ".").replace("[", "\\[").replace("]", "\\]")
								+ ".class");
			}
		}
	}
	
	private Flag[] filterFlags(Flag[] flags, Flag... toFilter) {
		List<Flag> ok = new ArrayList<>();
		for (Flag flag: flags) {
			boolean skip  =false;
			for (Flag f: toFilter) {
				if (f==flag) {
					skip = true;
				}
			}
			if (!skip) {
				ok.add(flag);
			}
		}
		return ok.toArray(new Flag[0]);
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
		SpringFeature.log(depth, "attempting registration of " + annotationChain.size()
				+ " elements of annotation hint chain");
		for (int i = 0; i < annotationChain.size(); i++) {
			// i=0 is the annotated type, i>0 are all annotation types
			Type t = annotationChain.get(i);
			if (i==0 && ConfigOptions.isAgentMode()) {
				boolean beingReflectedUponInIncomingConfiguration = existingReflectionConfigContains(t.getDottedName());
				if (!beingReflectedUponInIncomingConfiguration) {
					SpringFeature.log("In agent mode skipping "+t.getDottedName()+" because in already existing configuration");
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

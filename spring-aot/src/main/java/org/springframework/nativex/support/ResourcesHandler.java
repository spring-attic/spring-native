/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.nativex.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.nativex.AotOptions;
import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.domain.reflect.MethodDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.Method;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;

public class ResourcesHandler extends Handler {

	private static Log logger = LogFactory.getLog(ResourcesHandler.class);	

	private final ReflectionHandler reflectionHandler;

	private final DynamicProxiesHandler dynamicProxiesHandler;

	private final InitializationHandler initializationHandler;
	
	private SerializationHandler serializationHandler;

	private JNIReflectionHandler jniReflectionHandler;

	private final OptionHandler optionHandler;

	private final AotOptions aotOptions;

	public ResourcesHandler(ConfigurationCollector collector, ReflectionHandler reflectionHandler, 
			DynamicProxiesHandler dynamicProxiesHandler, InitializationHandler initializationHandler,
			SerializationHandler serializationHandler, JNIReflectionHandler jniReflectionHandler,
			OptionHandler optionHandler, AotOptions aotOptions) {
		super(collector);
		this.reflectionHandler = reflectionHandler;
		this.dynamicProxiesHandler = dynamicProxiesHandler;
		this.initializationHandler = initializationHandler;
		this.serializationHandler = serializationHandler;
		this.jniReflectionHandler = jniReflectionHandler;
		this.optionHandler = optionHandler;
		this.aotOptions = aotOptions;
	}

	/**
	 * Callback from native-image. Determine resources related to Spring applications that need to be added to the image.
	 */
	public void register() {
		if (aotOptions.toMode() == Mode.NATIVE) {
			processSpringFactories();
		}
		handleConstantHints(aotOptions.toMode() == Mode.NATIVE_AGENT);
		if (aotOptions.toMode() == Mode.NATIVE) {
			handleSpringComponents();
		}
	}

	private void registerResourceBundles(ResourcesDescriptor rd) {
		logger.debug("Registering resources - #" + rd.getBundles().size() + " bundles");
		for (String bundle : rd.getBundles()) {
			try {
				ResourceBundle.getBundle(bundle);
				collector.addResource(bundle, true);
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
	private void handleConstantHints(boolean isAgentMode) {
		List<HintDeclaration> constantHints = ts.findActiveDefaultHints();
		logger.debug("> Registering fixed hints: " + constantHints);
		for (HintDeclaration ch : constantHints) {
			if (!isAgentMode) {
				Map<String, AccessDescriptor> dependantTypes = ch.getDependantTypes();
				for (Map.Entry<String, AccessDescriptor> dependantType : dependantTypes.entrySet()) {
					String typename = dependantType.getKey();
					AccessDescriptor ad = dependantType.getValue();
					logger.debug("  fixed type registered " + typename + " with " + ad);
					if (AccessBits.isResourceAccessRequired(ad.getAccessBits()) && !typename.contains("[]")) {
						org.springframework.nativex.type.ResourcesDescriptor resourcesDescriptor = org.springframework.nativex.type.ResourcesDescriptor.ofType(typename);
						registerResourcesDescriptor(resourcesDescriptor);
					}
					Flag[] accessFlags = AccessBits.getFlags(ad.getAccessBits());
					List<org.springframework.nativex.type.MethodDescriptor> mds = ad.getMethodDescriptors();
					if (mds != null && mds.size() != 0 && AccessBits.isSet(ad.getAccessBits(),
							AccessBits.DECLARED_METHODS | AccessBits.PUBLIC_METHODS)) {
						logger.debug("  type has #" + mds.size()
								+ " members specified, removing typewide method access flags");
						accessFlags = filterFlags(accessFlags, Flag.allDeclaredMethods, Flag.allPublicMethods);
					}
					List<org.springframework.nativex.type.MethodDescriptor> qmds = ad.getQueriedMethodDescriptors();
					if (qmds != null && qmds.size() != 0 && AccessBits.isSet(ad.getAccessBits(),
							AccessBits.QUERY_DECLARED_METHODS | AccessBits.QUERY_PUBLIC_METHODS)) {
						logger.debug("  type has #" + qmds.size()
								+ " queried members specified, removing typewide method access flags");
						accessFlags = filterFlags(accessFlags, Flag.queryAllDeclaredMethods, Flag.queryAllPublicMethods);
					}
					List<FieldDescriptor> fds = ad.getFieldDescriptors();
					reflectionHandler.addAccess(typename, ch.getTriggerTypename(), MethodDescriptor.toStringArray(mds),
							MethodDescriptor.toStringArray(qmds), FieldDescriptor.toStringArray(fds), true, accessFlags);
				}
				for (Map.Entry<String, AccessDescriptor> dependantType : ch.getJNITypes().entrySet()) {
					String typename = dependantType.getKey();
					AccessDescriptor ad = dependantType.getValue();
					logger.debug("  fixed JNI access type registered " + typename + " with " + ad);
					List<org.springframework.nativex.type.MethodDescriptor> mds = ad.getMethodDescriptors();
					Flag[] accessFlags = AccessBits.getFlags(ad.getAccessBits());
					if (mds != null && mds.size() != 0 && AccessBits.isSet(ad.getAccessBits(),
							AccessBits.DECLARED_METHODS | AccessBits.PUBLIC_METHODS)) {
						logger.debug("  type has #" + mds.size()
								+ " members specified, removing typewide method access flags");
						accessFlags = filterFlags(accessFlags, Flag.allDeclaredMethods, Flag.allPublicMethods);
					}
					List<FieldDescriptor> fds = ad.getFieldDescriptors();
					jniReflectionHandler.addAccess(typename, MethodDescriptor.toStringArray(mds),
							FieldDescriptor.toStringArray(fds), true, accessFlags);
				}
				List<JdkProxyDescriptor> proxyDescriptors = ch.getProxyDescriptors();
				for (JdkProxyDescriptor pd : proxyDescriptors) {
					logger.debug("Registering proxy descriptor: " + pd);
					dynamicProxiesHandler.addProxy(pd);
				}
				Set<String> serializationTypes = ch.getSerializationTypes();
				if (!serializationTypes.isEmpty()) {
					logger.debug("Registering types as serializable: "+serializationTypes);
					for (String st: serializationTypes) {
						serializationHandler.addType(st);
					}
				}
				List<org.springframework.nativex.type.ResourcesDescriptor> resourcesDescriptors = ch
						.getResourcesDescriptors();
				for (org.springframework.nativex.type.ResourcesDescriptor rd : resourcesDescriptors) {
					logger.debug("Registering resource descriptor: " + rd);
					registerResourcesDescriptor(rd);
				}
			}
			for (InitializationDescriptor initializationDescriptor : ch.getInitializationDescriptors()) {
				logger.debug("Registering initialization descriptor: " + initializationDescriptor);
				initializationHandler.registerInitializationDescriptor(initializationDescriptor);
			}
			if (!ch.getOptions().isEmpty()) {
				logger.debug("Registering options: "+ch.getOptions());
				optionHandler.addOptions(ch.getOptions());
			}
		}
		logger.debug("< Registering fixed hints");
	}
	
	public void registerResourcesDescriptor(org.springframework.nativex.type.ResourcesDescriptor rd) {
		String[] patterns = rd.getPatterns();
		for (String pattern: patterns) {
			collector.addResource(pattern,rd.isBundle());
			
		}	
	}

	/**
	 * Discover existing spring.components or synthesize one if none are found. If not running
	 * in hybrid mode then process the spring.components entries.
	 */
	public void handleSpringComponents() {
		NativeContext context = new NativeContextImpl();
//		Enumeration<URL> springComponents = fetchResources("META-INF/spring.components");
		Collection<byte[]> springComponents = ts.getResources("META-INF/spring.components");
		List<String> alreadyProcessed = new ArrayList<>();
		if (springComponents.size()!=0) {
//		if (springComponents.hasMoreElements()) {
			logger.debug("Processing existing META-INF/spring.components files...");
			for (byte[] springComponentsFile: springComponents) {
//			while (springComponents.hasMoreElements()) {
//				URL springFactory = springComponents.nextElement();
				Properties p = new Properties();
				try (ByteArrayInputStream bais = new ByteArrayInputStream(springComponentsFile)) {
					p.load(bais);
				} catch (IOException e) {
					throw new IllegalStateException("Unable to load spring.factories", e);
				}
//				loadSpringFactoryFile(springFactory, p);
				if (aotOptions.toMode() == Mode.NATIVE_AGENT) {
					processSpringComponentsAgent(p, context);
				} else {
					processSpringComponents(p, context, alreadyProcessed);
				}
			}
		} else {
			logger.debug("Found no META-INF/spring.components -> synthesizing one...");
			Properties p = synthesizeSpringComponents();
			if (aotOptions.toMode() == Mode.NATIVE_AGENT) {
				processSpringComponentsAgent(p, context);
			} else {
				processSpringComponents(p, context, alreadyProcessed);
			}
		}
	}

	private Properties synthesizeSpringComponents() {
		Properties p = new Properties();
		List<Entry<Type, List<Type>>> components = ts.scanForSpringComponents();
		List<Entry<Type, List<Type>>> filteredComponents = filterOutNestedConfigurationTypes(components);
		for (Entry<Type, List<Type>> filteredComponent : filteredComponents) {
			String k = filteredComponent.getKey().getDottedName();
			p.put(k, filteredComponent.getValue().stream().map(t -> t.getDottedName())
					.collect(Collectors.joining(",")));
		}
		logger.debug("Computed spring.components is ");
		logger.debug("vvv");
		for (Object k : p.keySet()) {
			logger.debug(k + "=" + p.getProperty((String) k));
		}
		logger.debug("^^^");
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			p.store(baos, "");
			baos.close();
			byte[] bs = baos.toByteArray();
			collector.registerResource("META-INF/spring.components", bs);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return p;
	}
	
	private void processSpringComponentsAgent(Properties p, NativeContext context) {
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
				logger.debug("hybrid: adding access to "+keyType+" since @SpringBootApplication");
				reflectionHandler.addAccess(key,  Flag.allDeclaredMethods, Flag.allDeclaredFields, Flag.allDeclaredConstructors);
//				resourcesRegistry.addResources(key.replace(".", "/")+".class");
				collector.addResource(key.replace(".", "/")+".class", false);
			}
			if (keyType.isAtController()) {
				logger.debug("hybrid: Processing controller "+key);
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
								logger.debug("Adding dynamic proxy for "+interfaces);
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
	private void processSpringComponents(Properties p, NativeContext context, List<String> alreadyProcessed) {
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
		logger.debug("Registered " + registeredComponents + " entries");
	}
	
	private boolean processSpringComponent(String componentTypename, String classifiers, NativeContext context, RequestedConfigurationManager requestor, List<String> alreadyProcessed) {
		ProcessingContext pc = ProcessingContext.of(componentTypename, ReachedBy.FromSpringComponent);
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
		logger.debug("Registering Spring Component: " + componentTypename);

		// Ensure if usage of @Component is meta-usage, the annotations that are meta-annotated are
		// exposed
		Entry<Type, List<Type>> metaAnnotated = kType.getMetaComponentTaggedAnnotations();
		if (metaAnnotated != null) {
			for (Type t: metaAnnotated.getValue()) {
				String name = t.getDottedName();
				reflectionHandler.addAccess(name, Flag.allDeclaredMethods);
				collector.addResource(name.replace(".", "/")+".class", false);
//				resourcesRegistry.addResources(name.replace(".", "/")+".class");
			}
		}

		if (kType.isAtConfiguration()) {
			// Treat user configuration (from spring.components) the same as configuration
			// discovered via spring.factories
//			checkAndRegisterConfigurationType(componentTypename,ReachedBy.FromSpringComponent);
		} else {
			try {
				// TODO assess which kinds of thing requiring what kind of access - here we see
				// an Entity might require field reflective access where others don't
				// I think as a component may have autowired fields (and an entity may have
				// interesting fields) - you kind of always need to expose fields
				// There is a type in vanilla-orm called Bootstrap that shows this need
				reflectionHandler.addAccess(componentTypename, Flag.allDeclaredConstructors, Flag.allDeclaredMethods,
					Flag.allDeclaredClasses, Flag.allDeclaredFields);
//				resourcesRegistry.addResources(componentTypename.replace(".", "/") + ".class");
				collector.addResource(componentTypename.replace(".", "/")+".class", false);
				// Register nested types of the component
				for (Type t : kType.getNestedTypes()) {
					reflectionHandler.addAccess(t.getDottedName(), Flag.allDeclaredConstructors, Flag.allDeclaredMethods,
							Flag.allDeclaredClasses);
//					resourcesRegistry.addResources(t.getName() + ".class");
					collector.addResource(t.getName()+".class", false);
				}
				registerHierarchy(pc, kType, requestor);
			} catch (Throwable t) {
				t.printStackTrace();
			}
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
//				resourcesRegistry.addResources(tt.replace(".", "/") + ".class");
				collector.addResource(tt.replace(".", "/")+".class", false);
				// Register nested types of the component
				for (Type t : baseType.getNestedTypes()) {
					String n = t.getName().replace("/", ".");
					reflectionHandler.addAccess(n, Flag.allDeclaredMethods);
//					reflectionHandler.addAccess(n, Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allDeclaredClasses);
//					resourcesRegistry.addResources(t.getName() + ".class");
					collector.addResource(t.getName() + ".class", false);
				}
				registerHierarchy(pc, baseType, requestor);
			} catch (Throwable t) {
				t.printStackTrace();
				logger.debug("Problems with value " + tt);
			}
		}
		if (isComponent && aotOptions.isVerify()) {
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
	class NativeContextImpl implements NativeContext {

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
		public void addAotProxy(AotProxyDescriptor proxyDescriptor) {
			dynamicProxiesHandler.addClassProxy(proxyDescriptor.getTargetClassType(), proxyDescriptor.getInterfaceTypes(), proxyDescriptor.getProxyFeatures());
		}

		@Override
		public TypeSystem getTypeSystem() {
			return ts;
		}

		@Override
		public void addReflectiveAccess(String key, Flag... flags) {
			reflectionHandler.addAccess(key, flags);
		}

		@Override
		public void addReflectiveAccess(String typeName, AccessDescriptor descriptor) {
			reflectionHandler.addAccess(typeName, true, descriptor);
		}

		@Override
		public boolean hasReflectionConfigFor(String typename) {
			return collector.getClassDescriptorFor(typename)!=null;
		}

		@Override
		public void initializeAtBuildTime(Type type) {
			initializationHandler.initializeClassesAtBuildTime(type.getDottedName());
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
				Set<String> relatedTypes = type.getTypesInSignature();
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
			logger.debug(message);
		}

		@Override
		public void addResourceBundle(String bundleName) {
			registerResourceBundles(ResourcesDescriptor.ofBundle(bundleName));
		}
		
	}
	
	private void processResponseBodyComponent(Type t) {
	  // If a controller is marked up @ResponseBody (possibly via @RestController), need to register reflective access to
	  // the return types of the methods marked @Mapping (meta marked) 
	  Collection<Type> returnTypes = t.collectAtMappingMarkedReturnTypes();
	  logger.debug("Found these return types from Mapped methods in "+t.getName()+" > "+returnTypes);
	  for (Type returnType: returnTypes ) {
		  if (returnType==null) {
			  continue;
		  }
		  if (returnType.getDottedName().startsWith("java.lang")) {
			  continue;
		  }
		  reflectionHandler.addAccess(returnType.getDottedName(), Flag.allDeclaredMethods, Flag.allDeclaredConstructors,Flag.allDeclaredFields);
	  }
	}

	/**
	 * Walk a type hierarchy and register them all for reflective access.
	 * @param pc Processing context
	 * @param type the type whose hierarchy to register
	 * @param typesToMakeAccessible if non null required accesses are collected here rather than recorded directly on the runtime
	 */
	public void registerHierarchy(ProcessingContext pc, Type type, RequestedConfigurationManager typesToMakeAccessible) {
		AccessBits accessRequired = AccessBits.forValue(Type.inferAccessRequired(type));
		boolean isConfiguration = type.isAtConfiguration();
		if (!isConfiguration) {
			// Double check are we here because we are a parent of some configuration being processed
			// For example: 
			// Analyzing org.springframework.web.reactive.config.WebFluxConfigurationSupport 
			//   reached by 
			// [[Ctx:org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration-FromSpringFactoriesKey], 
			// [Ctx:org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration$EnableWebFluxConfiguration-NestedReference], 
			// [Ctx:org.springframework.web.reactive.config.DelegatingWebFluxConfiguration-HierarchyProcessing], 
			// [Ctx:org.springframework.web.reactive.config.WebFluxConfigurationSupport-HierarchyProcessing]]
			// TODO [0.9.0] tidyup
			String s2 = pc.getHierarchyProcessingTopMostTypename();
			TypeSystem typeSystem = type.getTypeSystem();
			Type resolve = typeSystem.resolveDotted(s2,true);
			isConfiguration = resolve.isAtConfiguration();
		}
		registerHierarchyHelper(type, new HashSet<>(), typesToMakeAccessible, accessRequired, isConfiguration);
	}
	
	private void registerHierarchyHelper(Type type, Set<Type> visited, RequestedConfigurationManager typesToMakeAccessible,
			AccessBits inferredRequiredAccess, boolean rootTypeWasConfiguration) {
		if (typesToMakeAccessible == null) {
			throw new IllegalStateException();
		}
		if (type == null || !visited.add(type)) {
			return;
		}
			if (inferredRequiredAccess.getValue() != 0) {
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
						AccessBits.DECLARED_CONSTRUCTORS | AccessBits.RESOURCE
								| (rootTypeWasConfiguration ? AccessBits.DECLARED_METHODS : AccessBits.PUBLIC_METHODS));
				// inferredRequiredAccess.getValue());
				// reflectionHandler.addAccess(configNameDotted, Flag.allDeclaredConstructors,
				// Flag.allDeclaredMethods);
		}
			}
		
		if (rootTypeWasConfiguration && !type.isAtConfiguration()) {
			// Processing a superclass of a configuration (so may contain @Bean methods)
		}
		// Rather than just looking at superclass and interfaces, this will dig into everything including
		// parameterized type references so nothing is missed
//		if (type.getSuperclass()!=null) {
//			logger.debug("RH>SC "+type.getSuperclass());
//		registerHierarchyHelper(type.getSuperclass(),visited, typesToMakeAccessible,inferredRequiredAccess);
//		}
//		Type[] intfaces = type.getInterfaces();
//		for (Type intface: intfaces) {
//			logger.debug("RH>IF "+intface);
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
		
		Type superclass = type.getSuperclass();
		registerHierarchyHelper(superclass, visited, typesToMakeAccessible, inferredRequiredAccess, true);
		
		Set<String> relatedTypes = type.getTypesInSignature();
		for (String relatedType: relatedTypes) {
			Type t = ts.resolveSlashed(relatedType,true);
			if (t!=null) {
//				lst.add(t.getDottedName());
				registerHierarchyHelper(t, visited, typesToMakeAccessible, inferredRequiredAccess, false);
			}
		}
//		lst.removeAll(supers);
//		if (lst.size()!=0) {
//		logger.debug("MISSED THESE ("+type.getDottedName()+"): "+lst);
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
		logger.debug("Processing META-INF/spring.factories files...");
		for (byte[] springFactory: ts.getResources("META-INF/spring.factories")) {
			Properties p = new Properties();
			try (ByteArrayInputStream bais = new ByteArrayInputStream(springFactory)) {
				p.load(bais);
			} catch (IOException e) {
				throw new IllegalStateException("Unable to load bytes from spring factory file", e);
			}
//			loadSpringFactoryFile(springFactory, p);
			processSpringFactory(ts, p);
		}
//		Enumeration<URL> springFactories = fetchResources("META-INF/spring.factories");
//		while (springFactories.hasMoreElements()) {
//			URL springFactory = springFactories.nextElement();
//			processSpringFactory(ts, springFactory);
//		}
	}

	private List<Entry<Type, List<Type>>> filterOutNestedConfigurationTypes(List<Entry<Type, List<Type>>> indexedComponents) {
		List<Entry<Type, List<Type>>> filtered = new ArrayList<>();
		List<Entry<Type, List<Type>>> subtypesToRemove = new ArrayList<>();
		for (Entry<Type, List<Type>> indexedComponent : indexedComponents) {
			Type componentKey = indexedComponent.getKey();
			String type = componentKey.getDottedName();
			if (componentKey.isAtConfiguration()) {
				subtypesToRemove.addAll(indexedComponents.stream()
						.filter(e -> e.getKey().getDottedName().startsWith(type + "$")).collect(Collectors.toList()));
			}
		}
		filtered.addAll(indexedComponents);
		filtered.removeAll(subtypesToRemove);
		return filtered;
	}
		
	private void processSpringFactory(TypeSystem ts, Properties p) {
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
		AtBeanReturnType, // the type reference is the return type of an @Bean method
		NestedReference, // This was discovered as a nested type within some type currently being processed
		HierarchyProcessing, // This was discovered whilst going up the hierarchy from some type currently being processed
		Inferred, // This type was 'inferred' whilst processing a hint (e.g. the class in a @COC usage)
		Specific, // This type was explicitly listed in a hint that was processed
		InnerOfNestedCondition // it is the inner type of a class implementing AbstractNestedCondition
	}

//	private boolean checkConditionalOnEnabledMetricsExport(Type type) {
//		boolean isOK = type.testAnyConditionalOnEnabledMetricsExport();
//		if (!isOK) {
//			logger.debug(type.getDottedName()+" FAILED ConditionalOnEnabledMetricsExport check - returning FALSE");
//			return false;
//		}
//		return true;
//	}
	
	List<String> failedPropertyChecks = new ArrayList<>();
	
	static class ContextEntry {
		private String typename;
		private ReachedBy reachedBy;
		ContextEntry(String typename, ReachedBy reachedBy) {
			this.typename = typename;
			this.reachedBy = reachedBy;
		}
		public String toString() {
			return "[Ctx:"+typename+"-"+reachedBy+"]";
		}
	}

	/**
	 * The ProcessingContext keeps track of the route taken through chasing hints/configurations.
	 * At any point this means we can know why we are processing a type, processing of some types
	 * may need to know about why it is being looked at. For example the superclass of a configuration
	 * may not have @ Configuration on it and yet have @ Bean methods in it.
	 */
	@SuppressWarnings("serial")
	static class ProcessingContext extends Stack<ContextEntry> {
		
		// Keep track of everything seen during use of this ProcessingContext
		private Set<String> visited = new HashSet<>();

		public static ProcessingContext of(String typename, ReachedBy reachedBy) {
			ProcessingContext pc = new ProcessingContext();
			pc.push(new ContextEntry(typename, reachedBy));
			return pc;
		}

		public ReachedBy peekReachedBy() {
			return peek().reachedBy;
		}

		public ContextEntry push(Type type, ReachedBy reachedBy) {
			return push(new ContextEntry(type.getDottedName(), reachedBy));
		}

		public boolean recordVisit(String name) {
			return visited.add(name);
		}

		public int depth() {
			return size();
		}
		
		public String getHierarchyProcessingTopMostTypename() {
			// Double check are we here because we are a parent of some configuration being processed
						// For example: 
						// Analyzing org.springframework.web.reactive.config.WebFluxConfigurationSupport 
						//   reached by 
						// [[Ctx:org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration-FromSpringFactoriesKey], 
						// [Ctx:org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration$EnableWebFluxConfiguration-NestedReference], 
						// [Ctx:org.springframework.web.reactive.config.DelegatingWebFluxConfiguration-HierarchyProcessing], 
						// [Ctx:org.springframework.web.reactive.config.WebFluxConfigurationSupport-HierarchyProcessing]]
			int i = size()-1;
			ContextEntry entry = get(i);
			while (entry.reachedBy==ReachedBy.HierarchyProcessing) {
				if (i==0) {
					break;
				}
				entry = get(--i);
			}
			// Now entry points to the first non HierarchyProcessing case
			return entry.typename;
		}

		public boolean isFromSpringFactoriesKey() {
			ContextEntry contextEntry = get(0);
			return contextEntry.reachedBy==ReachedBy.FromSpringFactoriesKey;
		}
		
	}

	private void registerAllRequested(RequestedConfigurationManager accessRequestor) {
		registerAllRequested(0, accessRequestor);
	}
	
	// In an attempt to reduce verbosity helps avoid reporting identical messages over and over
	private static Map<String, Integer> reflectionConfigurationAlreadyAdded = new HashMap<>();

	private void registerAllRequested(int depth, RequestedConfigurationManager accessRequestor) {
		for (InitializationDescriptor initializationDescriptor : accessRequestor.getRequestedInitializations()) {
			initializationHandler.registerInitializationDescriptor(initializationDescriptor);
		}
		optionHandler.addOptions(accessRequestor.getRequestedOptions());
		for (JdkProxyDescriptor proxyDescriptor : accessRequestor.getRequestedProxies()) {
			dynamicProxiesHandler.addProxy(proxyDescriptor);
		}
		for (org.springframework.nativex.type.ResourcesDescriptor rd : accessRequestor.getRequestedResources()) {
			registerResourcesDescriptor(rd);
		}
		for (String serializationType: accessRequestor.getRequestedSerializableTypes()) {
			serializationHandler.addType(serializationType);
		}
		for (Entry<String, AccessDescriptor> jniType : accessRequestor.getRequestedJNITypes().entrySet()) {
			jniReflectionHandler.addAccess(jniType.getKey(), jniType.getValue());
		}
		for (Map.Entry<String, Integer> accessRequest : accessRequestor.getRequestedTypeAccesses()) {
			String dname = accessRequest.getKey();
			int requestedAccess = accessRequest.getValue();
			List<org.springframework.nativex.type.MethodDescriptor> methods = accessRequestor.getMethodAccessRequestedFor(dname);

			// TODO promote this approach to a plugin if becomes a little more common...
			if (dname.equals("org.springframework.boot.autoconfigure.web.ServerProperties$Jetty")) { // See EmbeddedJetty @COC check
				if (!ts.canResolve("org/eclipse/jetty/webapp/WebAppContext")) {
					logger.debug("Reducing access on "+dname+" because WebAppContext not around");
					requestedAccess = AccessBits.CLASS;
				}
			}

			if (dname.equals("org.springframework.boot.autoconfigure.web.ServerProperties$Undertow")) { // See EmbeddedUndertow @COC check
				if (!ts.canResolve("io/undertow/Undertow")) {
					logger.debug("Reducing access on "+dname+" because Undertow not around");
					requestedAccess = AccessBits.CLASS;
				}
			}

			if (dname.equals("org.springframework.boot.autoconfigure.web.ServerProperties$Tomcat")) { // See EmbeddedTomcat @COC check
				if (!ts.canResolve("org/apache/catalina/startup/Tomcat")) {
					logger.debug("Reducing access on "+dname+" because Tomcat not around");
					requestedAccess = AccessBits.CLASS;
				}
			}

			if (dname.equals("org.springframework.boot.autoconfigure.web.ServerProperties$Netty")) { // See EmbeddedNetty @COC check
				if (!ts.canResolve("reactor/netty/http/server/HttpServer")) {
					logger.debug("Reducing access on "+dname+" because HttpServer not around");
					requestedAccess = AccessBits.CLASS;
				}
			}

			// Only log new info that is being added at this stage to keep logging down
			Integer access = reflectionConfigurationAlreadyAdded.get(dname);
			if (access == null) {
				logger.debug(spaces(depth) + "configuring reflective access to " + dname + "   " + AccessBits.toString(requestedAccess)+
						(methods==null?"(NO EXPLICIT METHODS)":" mds="+methods));
				reflectionConfigurationAlreadyAdded.put(dname, requestedAccess);
			} else {
				int extraAccess = AccessBits.compareAccess(access,requestedAccess);
				if (extraAccess>0) {
					logger.debug(spaces(depth) + "configuring reflective access, adding access for " + dname + " of " + 
							AccessBits.toString(extraAccess)+" (total now: "+AccessBits.toString(requestedAccess)+")");
					reflectionConfigurationAlreadyAdded.put(dname, access);
				}
			}
			Flag[] flags = AccessBits.getFlags(requestedAccess);
			Type rt = ts.resolveDotted(dname, true);

			if (methods != null) {
				// methods are explicitly specified, remove them from flags
				logger.debug(dname+" has #"+methods.size()+" methods directly specified so removing any general method access needs");
				flags = filterFlags(flags, Flag.allDeclaredMethods, Flag.allPublicMethods);
			}
//			logger.debug(spaces(depth) + "fixed flags? "+Flag.toString(flags));
//			logger.debug(depth, "ms: "+methods);

			String typeReachable = accessRequestor.getTypeReachableFor(dname);

			// TODO See if query method configuration is needed here
			reflectionHandler.addAccess(dname, typeReachable, MethodDescriptor.toStringArray(methods), null, FieldDescriptor.toStringArray(accessRequestor.getFieldAccessRequestedFor(dname)), true, flags);
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
				collector.addResource(fromTypenameToClassResource(dname), false);
			}
		}
	}
	
	private String fromTypenameToClassResource(String name) {
		return name.replace(".", "/").replace("$", ".").replace("[", "\\[").replace("]", "\\]") + ".class";
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

	private String spaces(int depth) {
		return "                                                  ".substring(0, depth * 2);
	}

}

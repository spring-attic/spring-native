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
import org.springframework.graal.domain.reflect.Flag;
import org.springframework.graal.domain.resources.ResourcesDescriptor;
import org.springframework.graal.domain.resources.ResourcesJsonMarshaller;
import org.springframework.graal.type.AccessRequired;
import org.springframework.graal.type.Hint;
import org.springframework.graal.type.Method;
import org.springframework.graal.type.MissingTypeException;
import org.springframework.graal.type.Type;
import org.springframework.graal.type.TypeSystem;

import com.oracle.svm.core.configure.ResourcesRegistry;
import com.oracle.svm.core.jdk.Resources;
import com.oracle.svm.hosted.FeatureImpl.BeforeAnalysisAccessImpl;
import com.oracle.svm.hosted.ImageClassLoader;

public class ResourcesHandler {

	private final static String EnableAutoconfigurationKey = "org.springframework.boot.autoconfigure.EnableAutoConfiguration";

	private final static String PropertySourceLoaderKey = "org.springframework.boot.env.PropertySourceLoader";

	private TypeSystem ts;

	private ImageClassLoader cl;

	private ReflectionHandler reflectionHandler;

	private ResourcesRegistry resourcesRegistry;

	private DynamicProxiesHandler dynamicProxiesHandler;

	public ResourcesHandler(ReflectionHandler reflectionHandler, DynamicProxiesHandler dynamicProxiesHandler) {
		this.reflectionHandler = reflectionHandler;
		this.dynamicProxiesHandler = dynamicProxiesHandler;
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
		resourcesRegistry = ImageSingletons.lookup(ResourcesRegistry.class);
		// Patterns can be added to the registry, resources can be directly registered
		// against Resources
		// resourcesRegistry.addResources("*");
		// Resources.registerResource(relativePath, inputstream);
		System.out.println("Registering resources - #" + rd.getPatterns().size() + " patterns");

		for (String pattern : rd.getPatterns()) {
			if (pattern.equals("META-INF/spring.factories")) {
				continue; // leave to special handling...
			}
			resourcesRegistry.addResources(pattern);
		}
		registerResourceBundles(rd);
		processSpringFactories();
		handleSpringComponents();
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

	public void handleSpringComponents() {
		Enumeration<URL> springComponents = fetchResources("META-INF/spring.components");
		if (springComponents.hasMoreElements()) {
			log("Processing META-INF/spring.components files...");
			while (springComponents.hasMoreElements()) {
				URL springFactory = springComponents.nextElement();
				Properties p = new Properties();
				loadSpringFactoryFile(springFactory, p);
				processSpringComponents(p);
			}
		} else {
			System.out.println("Found no META-INF/spring.components -> synthesizing one...");
			List<Entry<Type, List<Type>>> components = scanForSpringComponents();
			List<Entry<Type, List<Type>>> filteredComponents = filterOutNestedTypes(components);
			Properties p = new Properties();
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
			processSpringComponents(p);
		}
	}

	// TODO verify how much access is needed for a type @COC reference
	private void registerConditionalOnClassTypeReference(Type componentType, String slashedTypeReference) {
		try {
			reflectionHandler.addAccess(slashedTypeReference.replace("/", "."), Flag.allDeclaredConstructors,
					Flag.allDeclaredMethods);
			resourcesRegistry.addResources(slashedTypeReference + ".class");
		} catch (NoClassDefFoundError e) {
			System.out.println(
					"ERROR: @COC type " + slashedTypeReference + " not found for component " + componentType.getName());
		}
	}

	public void reg(String s) {
		reflectionHandler.addAccess(s, Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allDeclaredClasses);
	}

	// TODO shouldn't this code just call into processType like we do for discovered
	// configuration?
	// (Then we have consistent processing across library and user code - without it
	// I think this code below will prove insufficient when we get more sophisticated
	// samples - such as one using @Imported configuration - write a testcase for that)
	private void processSpringComponents(Properties p) {
		// Example:
		// app.main.SampleApplication=org.springframework.stereotype.Component
		// app.main.model.Foo=javax.persistence.Entity
		// app.main.model.FooRepository=org.springframework.data.repository.Repository
		Enumeration<Object> keys = p.keys();
		int registeredComponents = 0;
		ResourcesRegistry resourcesRegistry = ImageSingletons.lookup(ResourcesRegistry.class);
		while (keys.hasMoreElements()) {
			boolean isRepository = false;
			String k = (String) keys.nextElement();
			SpringFeature.log("Registering Spring Component: " + k);
			registeredComponents++;
			try {
				// reflectionHandler.addAccess(k,Flag.allDeclaredConstructors,
				// Flag.allDeclaredMethods, Flag.allDeclaredClasses);
				// reflectionHandler.addAccess(k,Flag.allPublicConstructors,
				// Flag.allPublicMethods, Flag.allDeclaredClasses);
				reg(k);
				resourcesRegistry.addResources(k.replace(".", "/") + ".class");
				// Register nested types of the component
				Type baseType = ts.resolveDotted(k);
				for (Type t : baseType.getNestedTypes()) {
					String n = t.getName().replace("/", ".");
					reflectionHandler.addAccess(n, Flag.allDeclaredConstructors, Flag.allDeclaredMethods,
							Flag.allDeclaredClasses);
					resourcesRegistry.addResources(t.getName() + ".class");
				}
				registerHierarchy(baseType, new HashSet<>(), null);
			} catch (Throwable t) {
				t.printStackTrace();
				// assuming ok for now - see
				// SBG: ERROR: CANNOT RESOLVE org.springframework.samples.petclinic.model ???
				// for petclinic spring.components
			}
			String vs = (String) p.get(k);
			StringTokenizer st = new StringTokenizer(vs, ",");
			// org.springframework.samples.petclinic.visit.JpaVisitRepositoryImpl=org.springframework.stereotype.Component,javax.transaction.Transactional
			while (st.hasMoreElements()) {
				String tt = st.nextToken();
				if (tt.equals("org.springframework.data.repository.Repository")) {
					isRepository = true;
				}
				try {
					// reflectionHandler.addAccess(tt,Flag.allDeclaredConstructors,
					// Flag.allDeclaredMethods, Flag.allDeclaredClasses);
					// reflectionHandler.addAccess(tt,Flag.allPublicConstructors,
					// Flag.allPublicMethods, Flag.allDeclaredClasses);
//					reg(tt);
					reflectionHandler.addAccess(tt, Flag.allDeclaredMethods);
					resourcesRegistry.addResources(tt.replace(".", "/") + ".class");
					// Register nested types of the component
					Type baseType = ts.resolveDotted(tt);
					for (Type t : baseType.getNestedTypes()) {
						String n = t.getName().replace("/", ".");
						reflectionHandler.addAccess(n, Flag.allDeclaredMethods);
//						reflectionHandler.addAccess(n, Flag.allDeclaredConstructors, Flag.allDeclaredMethods,
//								Flag.allDeclaredClasses);
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
		}
		System.out.println("Registered " + registeredComponents + " entries");
	}

	/**
	 * Crude basic repository processing - needs more analysis to only add the
	 * interfaces the runtime will be asking for when requesting the proxy.
	 * 
	 * @param repositoryName type name of the repository
	 */
	private void processRepository(String repositoryName) {
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
	}

	public void registerHierarchy(Type type, Set<Type> visited, TypeAccessRequestor typesToMakeAccessible) {
		if (type == null || !visited.add(type)) {
			return;
		}
		// System.out.println("> registerHierarchy "+type.getName());
		String desc = type.getName();
		if (type.isCondition()) {
			if (type.hasOnlySimpleConstructor()) {
				if (typesToMakeAccessible != null) {
					typesToMakeAccessible.request(type.getDottedName(), AccessRequired.RESOURCE_CTORS_ONLY);
				} else {
					reflectionHandler.addAccess(desc.replace("/", "."), Flag.allDeclaredConstructors,
							Flag.allDeclaredMethods, Flag.allDeclaredClasses);
					resourcesRegistry.addResources(desc.replace("$", ".") + ".class");
				}
			} else {
				if (typesToMakeAccessible != null) {
					typesToMakeAccessible.request(type.getDottedName(), AccessRequired.RESOURCE_CTORS_ONLY);
				} else {
					reflectionHandler.addAccess(desc.replace("/", "."), Flag.allDeclaredConstructors);
					resourcesRegistry.addResources(desc.replace("$", ".") + ".class");
				}
			}
		} else {
			if (typesToMakeAccessible != null) {
				typesToMakeAccessible.request(type.getDottedName(), AccessRequired.RESOURCE_CMC);
			} else {
				reflectionHandler.addAccess(desc.replace("/", "."), Flag.allDeclaredConstructors,
						Flag.allDeclaredMethods);// , Flag.allDeclaredClasses);
				resourcesRegistry.addResources(desc.replace("$", ".") + ".class");
			}
			// reflectionHandler.addAccess(configNameDotted, Flag.allDeclaredConstructors,
			// Flag.allDeclaredMethods);
		}
		Type superclass = type.getSuperclass();
		registerHierarchy(superclass, visited, typesToMakeAccessible);
		Type[] intfaces = type.getInterfaces();
		for (Type intface : intfaces) {
			registerHierarchy(intface, visited, typesToMakeAccessible);
		}
		// System.out.println("< registerHierarchy "+type.getName());
		// TODO inners of those supertypes/interfaces?
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
			System.out.println("Adding all the classes for this key: " + k);
			if (!k.equals(EnableAutoconfigurationKey) && !k.equals(PropertySourceLoaderKey)) {
				if (Type.shouldBeProcessed(k,ts)) {
					for (String v : p.getProperty(k).split(",")) {
						registerTypeReferencedBySpringFactoriesKey(v);
					}
				} else {
					System.out.println("Skipping processing spring.factories key "+k+" due to missing types");
				}
			}
		}

		// Handle PropertySourceLoader
		String propertySourceLoaderValues = (String) p.get(PropertySourceLoaderKey);
		if (propertySourceLoaderValues != null) {
			List<String> propertySourceLoaders = new ArrayList<>();
			for (String s : propertySourceLoaderValues.split(",")) {
				if(!s.equals("org.springframework.boot.env.YamlPropertySourceLoader") || !ConfigOptions.shouldRemoveYamlSupport()) {
					registerTypeReferencedBySpringFactoriesKey(s);
					propertySourceLoaders.add(s);
				}
				else {
					forRemoval.add(s);
				}
			}
			System.out.println("Processing spring.factories - PropertySourceLoader lists #" + propertySourceLoaders.size()
					+ " property source loaders");
			SpringFeature.log("These property source loaders are remaining in the PropertySourceLoader key value:");
			for (int c = 0; c < propertySourceLoaders.size(); c++) {
				SpringFeature.log((c + 1) + ") " + propertySourceLoaders.get(c));
			}
			p.put(PropertySourceLoaderKey, String.join(",", propertySourceLoaders));

		}

		// Handle EnableAutoConfiguration
		String enableAutoConfigurationValues = (String) p.get(EnableAutoconfigurationKey);
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
				p.put(EnableAutoconfigurationKey, String.join(",", configurations));
				SpringFeature.log("These configurations are remaining in the EnableAutoConfiguration key value:");
				for (int c = 0; c < configurations.size(); c++) {
					SpringFeature.log((c + 1) + ") " + configurations.get(c));
				}
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
				System.out.println("The new spring.factories is: vvvvvvvvv");
				System.out.println(new String(bs));
				System.out.println("^^^^^^^^");
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
		/*
		if (config.equals("org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration")
				|| config.equals("org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration") 
				) {
			System.out.println("MUSTFIX - SKIPPING "+config);
			return false;
		}
		*/
		boolean b = processType(ts.resolveDotted(config), visited, 0);
		SpringFeature.log("Configuration type " + config + " has " + (b ? "passed" : "failed") + " validation");
		return b;
	}

	/**
	 * Used when registering types not easily identifiable from the bytecode that we
	 * are simply capturing as specific references in the Compilation Hints defined
	 * in the feature for now (see Type.<clinit>). Used for import selectors, import
	 * registrars, configuration. For @Configuration types here, need only bean
	 * method access (not all methods), for other types (import selectors/etc) may
	 * not need any method reflective access at all (and no resource access in that
	 * case).
	 * 
	 * @param tar
	 */
	private boolean registerSpecific(String typename, AccessRequired accessRequired, TypeAccessRequestor tar) {
		Type t = ts.resolveDotted(typename, true);
		if (t != null) {
			// System.out.println("> registerSpecific for "+typename+" ar="+accessRequired);
			boolean importRegistrarOrSelector = false;
			try {
				importRegistrarOrSelector = t.isImportRegistrar() || t.isImportSelector();
			} catch (MissingTypeException mte) {
				// something is missing, reflective access not going to work here!
				return false;
			}
			if (importRegistrarOrSelector) {
				// reflectionHandler.addAccess(typename,Flag.allDeclaredConstructors);
				tar.request(typename, AccessRequired.REGISTRAR);
			} else {
				if (accessRequired.isResourceAccess()) {
					tar.request(typename, AccessRequired.request(true, accessRequired.getFlags()));
				} else {
					// TODO worth limiting it solely to @Bean methods? Need to check how many
					// configuration classes typically have methods that are not @Bean
					tar.request(typename, AccessRequired.request(false, accessRequired.getFlags()));
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
		} else {
			return false;
		}
	}

	private boolean processType(Type type, Set<String> visited, int depth) {
		SpringFeature.log(spaces(depth) + "> processType: " + type.getName());

		// 1. Check the hierarchy of the type, if bits are missing resolution of this
		// type at runtime will not work - that suggests that in this particular
		// run the types are not on the classpath and so this type isn't being used.
		Set<String> missingTypes = ts.findMissingTypesInHierarchyOfThisType(type);
		if (!missingTypes.isEmpty()) {
			SpringFeature.log(spaces(depth) + "for " + type.getName() + " missing types are " + missingTypes);
			return false;
		}

		Set<String> missingAnnotationTypes = ts.resolveCompleteFindMissingAnnotationTypes(type);
		if (!missingAnnotationTypes.isEmpty()) {
			// If only the annotations are missing, it is ok to reflect on the existence of
			// the type, it is just not safe to reflect on the annotations on that type.
			SpringFeature.log(spaces(depth) + "for " + type.getName() + " missing annotation types are "
					+ missingAnnotationTypes);
		}

		boolean passesTests = true;
		Map<String,String> conditionalOnPropertyValues = type.getAnnotationValuesInHierarchy("Lorg/springframework/boot/autoconfigure/condition/ConditionalOnProperty;");
		// depth==0 means we won't skip something inside something else. If we did that and the outer succeeded it will crash when digging
		// through the inner stuff. For example:
		// static class PooledDataSourceCondition extends AnyNestedCondition {
		// ...
		//   @ConditionalOnProperty(prefix = "spring.datasource", name = "type")
		//	 static class ExplicitType { 
		if (depth==0 && ConfigOptions.shouldRemoveUnusedAutoconfig() && conditionalOnPropertyValues.size()!=0 &&
			(conditionalOnPropertyValues.get("matchIfMissing")==null || conditionalOnPropertyValues.get("matchIfMissing").equals("false"))) {
			SpringFeature.log(spaces(depth) + "skipping "+type.getName()+" due to ConditionalOnPropertyCheck");
			passesTests = false;
		}
		TypeAccessRequestor tar = new TypeAccessRequestor();
		List<Hint> hints = passesTests?type.getHints():Collections.emptyList();
		if (hints.size() != 0) {
			SpringFeature.log(spaces(depth) + "#" + hints.size() + " hints on " + type.getName() + " are: ");
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
				// directly encoded. For example when a CompilationHint on an ImportSelector encodes
				// the types that might be returned from it.
				Map<String, AccessRequired> specificNames = hint.getSpecificTypes();
				SpringFeature.log(spaces(depth) + "attempting registration of " + specificNames.size() + " specific types");
				for (Map.Entry<String, AccessRequired> specificNameEntry : specificNames.entrySet()) {
					String specificTypeName = specificNameEntry.getKey();
					if (!registerSpecific(specificTypeName, specificNameEntry.getValue(), tar)) {
						if (hint.isSkipIfTypesMissing()) {
							passesTests = false;
						}
					} else {
						if (hint.isFollow()) {
							// TODO I suspect only certain things need following, specific types lists could specify that in a suffix (like access required)
							SpringFeature.log(spaces(depth) + "will follow specific type reference " + specificTypeName);
							toFollow.add(ts.resolveDotted(specificTypeName));
						}
					}
				}

				Map<String, AccessRequired> inferredTypes = hint.getInferredTypes();
				SpringFeature.log(spaces(depth) + "attempting registration of " + inferredTypes.size() + " inferred types");
				for (Map.Entry<String, AccessRequired> inferredType : inferredTypes.entrySet()) {
					String s = inferredType.getKey();
					Type t = ts.resolveDotted(s, true);
					boolean exists = (t != null);
					if (!exists) {
						SpringFeature.log(spaces(depth) + "inferred type " + s + " not found");
					} else {
						SpringFeature.log(spaces(depth) + "inferred type " + s + " found, will get accessibility "
								+ inferredType.getValue());
					}
					if (exists) {
						// TODO if already there, should we merge access required values?
						tar.request(s, inferredType.getValue());
						if (hint.isFollow()) {
							SpringFeature.log(spaces(depth) + "will follow " + t);
							toFollow.add(t);
						}
					} else if (hint.isSkipIfTypesMissing() && depth==0) {
						// TODO If processing secondary type (depth>0) we can't skip things as we don't know if the 
						// top level type that refers to us is going to fail or not.  Ideally we should pass in the 
						// tar and accumulate types in secondary type processing and leave it to the outermost
						// processing to decide if they need registration. 
						passesTests = false;
						// Once failing, no need to process other hints
						break hints;
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
						SpringFeature.log(spaces(depth) + "IMPORTANT2: whilst processing type " + type.getName()
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
				// because this configuration
				// needs to refer to them.
				List<Type> boaTypes = type.getAutoConfigureBeforeOrAfter();
				if (boaTypes.size() != 0) {
					SpringFeature.log(spaces(depth) + "registering " + boaTypes.size()
							+ " @AutoConfigureBefore/After references");
				}
				for (Type t : boaTypes) {
					tar.request(t.getDottedName(), AccessRequired.EXISTENCE_CHECK);
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
						// I believe null means that type is not on the classpath so skip further analysis
						continue;
					} else {
						tar.request(returnType.getDottedName(),  AccessRequired.EXISTENCE_MC);
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
					Set<Type> signatureTypes = atBeanMethod.getSignatureTypes();
					for (Type signatureType: signatureTypes) {
						System.out.println("Flibble: "+signatureType.getDottedName());
						tar.request(signatureType.getDottedName(), AccessRequired.EXISTENCE_MC);
					}
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
						Map<String, AccessRequired> specificNames = hint.getSpecificTypes();
						SpringFeature.log(spaces(depth) + "attempting registration of " + specificNames.size()
								+ " specific types");
						for (Map.Entry<String, AccessRequired> specificNameEntry : specificNames.entrySet()) {
							registerSpecific(specificNameEntry.getKey(), specificNameEntry.getValue(), tar);
						}

						Map<String, AccessRequired> inferredTypes = hint.getInferredTypes();
						SpringFeature.log(spaces(depth) + "attempting registration of " + inferredTypes.size()
								+ " inferred types");
						for (Map.Entry<String, AccessRequired> inferredType : inferredTypes.entrySet()) {
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
					
						
					// Register other runtime visible annotations from the @Bean method. For example this ensures @Role is visible on:
					// @Bean
					// @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
					// @ConditionalOnMissingBean(Validator.class)
					// public static LocalValidatorFactoryBean defaultValidator() {
					List<Type> annotationsOnMethod = atBeanMethod.getAnnotationTypes();
					for (Type annotationOnMethod: annotationsOnMethod) {
						tar.request(annotationOnMethod.getDottedName(),AccessRequired.ANNOTATION);
					}
				}
			}
			// Follow transitively included inferred types only if necessary:
			for (Type t : toFollow) {
				boolean b = processType(t, visited, depth + 1);
				if (!b) {
					SpringFeature.log(spaces(depth) + "followed " + t.getName() + " and it failed validation");
				}
			}
			for (Map.Entry<String, AccessRequired> t : tar.entrySet()) {
				String dname = t.getKey();
				
				// Let's produce a message if this computed value is also in reflect.json
				// This is a sign we can probably remove that entry from reflect.json (maybe depend if inferred access required matches declared)
				if (reflectionHandler.getConstantData().hasClassDescriptor(dname)) {
					System.out.println("This is in the constant data, does it need to stay in there? "+dname+"  (dynamically requested access is "+t.getValue()+")");
				}
				
				SpringFeature.log(spaces(depth) + "making this accessible: " + dname + "   " + t.getValue());
				Flag[] flags = t.getValue().getFlags();
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
				if (t.getValue().isResourceAccess()) {
					resourcesRegistry.addResources(dname.replace(".", "/").replace("$", ".") + ".class");
				}
			}
		}


		// If the outer type is failing a test, we don't need to go into nested types...
		if (passesTests || !ConfigOptions.shouldRemoveUnusedAutoconfig()) {
			// if (type.isAtConfiguration() || type.isAbstractNestedCondition()) {
			List<Type> nestedTypes = type.getNestedTypes();
			for (Type t : nestedTypes) {
				if (visited.add(t.getName())) {
					boolean b = processType(t, visited, depth + 1);
					if (!b) {
						SpringFeature.log(spaces(depth) + "verification of nested type " + t.getName() + " failed");
					}
				}
			}
			// }
		}
		return passesTests;
	}

	private void registerAnnotationChain(int depth, TypeAccessRequestor tar, List<Type> annotationChain) {
		SpringFeature.log(spaces(depth) + "attempting registration of " + annotationChain.size()
				+ " elements of annotation chain");
		for (int i = 0; i < annotationChain.size(); i++) {
			// i=0 is the annotated type, i>0 are all annotation types
			Type t = annotationChain.get(i);
			tar.request(t.getDottedName(), t.isAnnotation() ? AccessRequired.ANNOTATION : AccessRequired.ALL);
			/*
			 * if (i==0) { tar.request(t.getDottedName(), AccessRequired.ALL); } else {
			 * tar.request(t.getDottedName(), AccessRequired.ANNOTATION); }
			 */
		}
	}

	String fromLtoDotted(String lDescriptor) {
		return lDescriptor.substring(1, lDescriptor.length() - 1).replace("/", ".");
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

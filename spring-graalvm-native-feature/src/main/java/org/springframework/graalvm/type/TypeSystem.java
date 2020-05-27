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
package org.springframework.graalvm.type;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.springframework.graalvm.domain.resources.ResourcesDescriptor;
import org.springframework.graalvm.domain.resources.ResourcesJsonMarshaller;
import org.springframework.graalvm.extension.ComponentProcessor;

/**
 * Simple type system with some rudimentary caching.
 */
public class TypeSystem {

	public static String SPRING_AT_CONFIGURATION = "Lorg/springframework/context/annotation/Configuration;";

	// Map of all types on the classpath that have some kind of annotations on them
	Map<String, AnnotationInfo> annotatedTypes;

	private SpringConfiguration hintLocator = null;

	// Classpath from which this type system will resolve types
	private List<String> classpath;

	// Cache of resolved types TODO time out entries?
	private Map<String, Type> typeCache = new HashMap<>();

	// Map of which zip files contain which packages TODO split package support
	private Map<String, File> packageCache = new HashMap<>();

	// Map of which application files contain particular packages
	private Map<String, List<File>> appPackages = new HashMap<>();

	private Map<String, ResourcesDescriptor> resourceConfigurations;

	public static TypeSystem get(List<String> classpath) {
		return new TypeSystem(classpath);
	}

	public TypeSystem(List<String> classpath) {
		this.classpath = classpath;
		index();
	}

	public List<String> getClasspath() {
		return classpath;
	}

	public Type resolveName(String dottedTypeName) {
		return resolveDotted(dottedTypeName);
	}

	public Type resolveDotted(String dottedTypeName) {
		String slashedTypeName = toSlashedName(dottedTypeName);
		return resolveSlashed(slashedTypeName);
	}

	public Type resolveName(String desc, boolean silent) {
		return resolveDotted(desc, silent);
	}

	public Type resolveDotted(String desc, boolean silent) {
		try {
			return resolveDotted(desc);
		} catch (MissingTypeException mte) {
			if (silent)
				return null;
			else
				throw mte;
		}
	}

	public boolean canResolveSlashed(String slashedTypeName) {
		try {
			return resolveSlashed(slashedTypeName) != null;
		} catch (RuntimeException re) {
			if (re.getMessage().startsWith("Unable to find class file for")) {
				return false;
			}
			throw re;
		}
	}

	public Type resolveSlashed(String slashedTypeName) {
		return resolveSlashed(slashedTypeName, false);
	}

	public Type resolveSlashed(String slashedTypeName, boolean allowNotFound) {
		Type type = typeCache.get(slashedTypeName);
		if (type == Type.MISSING) {
			if (allowNotFound) {
				return null;
			} else {
				throw new MissingTypeException(slashedTypeName);
			}
		}
		if (type != null) {
			return type;
		}
		int dimensions = 0;
		String typeToLocate = slashedTypeName;
		if (slashedTypeName.endsWith("[]")) {
			String n = slashedTypeName;
			while (n.endsWith("[]")) {
				dimensions++;
				n = n.substring(0,n.length()-2);
			}
			typeToLocate = n;
		}
		byte[] bytes = find(typeToLocate);
		if (bytes == null) {
			// System class?
			InputStream resourceAsStream = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(typeToLocate + ".class");
			if (resourceAsStream == null) {
				// cache a missingtype so we don't go looking again!
				typeCache.put(slashedTypeName, Type.MISSING);
				if (allowNotFound) {
					return null;
				} else {
					throw new MissingTypeException(slashedTypeName);
				}
			}
			try {
				bytes = loadFromStream(resourceAsStream);
			} catch (RuntimeException e) {
				throw new RuntimeException("Problems loading class from resource stream: " + slashedTypeName, e);
			}
		}
		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, ClassReader.SKIP_DEBUG);
		type = Type.forClassNode(this, node,dimensions);
		typeCache.put(slashedTypeName, type);
		return type;
	}

	private String toSlashedName(String dottedTypeName) {
		return dottedTypeName.replace(".", "/");
	}

	public boolean canResolve(String classname) {
		if (classname.contains(".")) {
			throw new RuntimeException("Dont pass dotted names to resolve() :" + classname);
		}
		return canResolveSlashed(classname);
	}

	public Type resolve(String classname, boolean allowNotFound) {
		if (classname.contains(".")) {
			throw new RuntimeException("Dont pass dotted names to resolve() :" + classname);
		}
		return resolveSlashed(classname, allowNotFound);
	}

	public Type resolve(String classname) {
		return resolve(classname, false);
	}

	public Type Lresolve(String desc) {
		return resolve(desc.substring(1, desc.length() - 1));
	}

	public Type resolve(org.objectweb.asm.Type type, boolean silent) {
		try {
			String desc = type.getDescriptor();
			return resolve(desc.substring(1, desc.length() - 1));
		} catch (MissingTypeException mte) {
			if (silent)
				return null;
			else
				throw mte;
		}
	}

	public Type Lresolve(String desc, boolean silent) {
		try {
			return resolve(desc.substring(1, desc.length() - 1));
		} catch (MissingTypeException mte) {
			if (silent)
				return null;
			else
				throw mte;
		}
	}

	public Set<String> findMissingTypesInHierarchyOfThisType(Type type) {
		return resolveComplete(type.getDescriptor());
	}

	public Set<String> resolveCompleteFindMissingAnnotationTypes(Type type) {
		Set<String> missingAnnotationTypes = new LinkedHashSet<>();
		type.collectMissingAnnotationTypesHelper(missingAnnotationTypes, new HashSet<>());
		return missingAnnotationTypes;
	}

	/**
	 * Verifies the type plus all its super types, interfaces and any type
	 * references in generic specifications exist.
	 * 
	 * @return List of missing types, empty if all good!
	 */
	public Set<String> resolveComplete(String desc) {
		Set<String> missingTypes = new LinkedHashSet<>();
		resolveComplete(desc.substring(1, desc.length() - 1), missingTypes, new HashSet<>());
		return missingTypes;
	}

	private void resolveComplete(String slashedDescriptor, Set<String> missingTypes, Set<String> visited) {
		if (visited.add(slashedDescriptor)) {
			Type baseType = resolve(slashedDescriptor, true);
			if (baseType == null) {
				missingTypes.add(slashedDescriptor);
			} else {
				// Check generics
				List<String> typesInSignature = baseType.getTypesInSignature();
//				for (String t: typesInSignature) {
//					System.out.println("Found this "+t+" in signature of "+baseType.getName());
//				}
				String superclassString = baseType.getSuperclassString();
				if (superclassString != null) {
					resolveComplete(superclassString, missingTypes, visited);
				}
				List<String> interfaces = baseType.getInterfacesStrings();
				if (interfaces != null) {
					for (String interfce : interfaces) {
						resolveComplete(interfce, missingTypes, visited);
					}
				}
			}
		}
	}

	public void index() {
		for (String s : classpath) {
			File f = new File(s);
			if (f.isDirectory()) {
				indexDir(f);
			} else {
				indexJar(f);
			}
		}
	}

	public void indexDir(File dir) {
		Path root = Paths.get(dir.toURI());
		try {
			Files.walk(root).filter(f -> f.toString().endsWith(".class")).map(f -> {
				String name = f.toString().substring(root.toString().length() + 1);
				int lastSlash = name.lastIndexOf("/");
				if (lastSlash != -1 && name.endsWith(".class")) {
					return name.substring(0, lastSlash);
				}
				return null;
			}).forEach(n -> {
				if (n != null) {
					List<File> dirs = appPackages.get(n);
					if (dirs == null) {
						dirs = new ArrayList<>();
						appPackages.put(n, dirs);
					}
					dirs.add(dir);
				}
			});
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to walk " + dir, ioe);
		}
	}

	public void indexJar(File jar) {
		// Walk the jar, index entries and cache package > this jar
		try {
			try (ZipFile zf = new ZipFile(jar)) {
				Enumeration<? extends ZipEntry> entries = zf.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					String name = entry.getName();
					if (name.endsWith(".class")) {
						int lastSlash = name.lastIndexOf("/");
						if (lastSlash != -1 && name.endsWith(".class")) {
							packageCache.put(name.substring(0, lastSlash), jar);
						}
					}
				}
			}
		} catch (FileNotFoundException | NoSuchFileException fileIsntThere) {
			System.err.println("WARNING: Unable to find jar '" + jar + "' whilst scanning filesystem");
		} catch (IOException ioe) {
			throw new RuntimeException("Problem during scan of " + jar, ioe);
		}
	}

	public byte[] find(String slashedTypeName) {
		String search = slashedTypeName + ".class";
		try {
			int index = slashedTypeName.lastIndexOf("/");
			String packageName = index == -1 ? "" : slashedTypeName.substring(0, index);

			if (appPackages.containsKey(packageName)) {
				List<File> list = appPackages.get(packageName);
				for (File f : list) {
					File toTry = new File(f, search);
					if (toTry.exists()) {
						return loadFromStream(new FileInputStream(toTry));
					}
				}
			} else {
				File jarfile = packageCache.get(packageName);
				if (jarfile != null) {
					try (ZipFile zf = new ZipFile(jarfile)) {
						Enumeration<? extends ZipEntry> entries = zf.entries();
						while (entries.hasMoreElements()) {
							ZipEntry entry = entries.nextElement();
							String name = entry.getName();
							if (name.equals(search)) {
								return loadFromStream(zf.getInputStream(entry));
							}
						}
					}
				}
			}

			return null;
		} catch (IOException ioe) {
			throw new RuntimeException("Problem finding " + slashedTypeName, ioe);
		}
	}

	public static byte[] loadFromStream(InputStream stream) {
		try {
			BufferedInputStream bis = new BufferedInputStream(stream);
			int size = 2048;
			byte[] theData = new byte[size];
			int dataReadSoFar = 0;
			byte[] buffer = new byte[size / 2];
			int read = 0;
			while ((read = bis.read(buffer)) != -1) {
				if ((read + dataReadSoFar) > theData.length) {
					// need to make more room
					byte[] newTheData = new byte[theData.length * 2];
					// System.out.println("doubled to " + newTheData.length);
					System.arraycopy(theData, 0, newTheData, 0, dataReadSoFar);
					theData = newTheData;
				}
				System.arraycopy(buffer, 0, theData, dataReadSoFar, read);
				dataReadSoFar += read;
			}
			bis.close();
			// Resize to actual data read
			byte[] returnData = new byte[dataReadSoFar];
			System.arraycopy(theData, 0, returnData, 0, dataReadSoFar);
			return returnData;
		} catch (IOException e) {
			throw new RuntimeException("Unexpectedly unable to load bytedata from input stream", e);
		} finally {
			try {
				stream.close();
			} catch (IOException ioe) {
			}
		}
	}

	public String toString() {
		return "TypeSystem for cp(" + classpath + ")  jarPackages=#" + packageCache.size() + " appPackages="
				+ appPackages;
	}

	public void scan() {
		// Scan the classpath for things of interest, do this only once!
		for (String classpathEntry : classpath) {
			File f = new File(classpathEntry);
			if (f.isDirectory()) {
				scanFiles(f, f);
			} else {
				scanArchive(f);
			}
		}
	}

	private void scanArchive(File f) {
		try (ZipFile zf = new ZipFile(f)) {
			Enumeration<? extends ZipEntry> entries = zf.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.getName().endsWith(".class")) {
					ClassReader reader = new ClassReader(zf.getInputStream(entry));
					ClassNode node = new ClassNode();
					reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
					AnnotationInfo ai = new AnnotationInfo(this, node);
					if (ai.hasData()) {
						System.out.println("From " + entry.toString() + " got " + ai.toAnnotationString());
						annotatedTypes.put(node.name, ai);
					}
				}
				// TODO resources?
			}
		} catch (IOException ioe) {
			throw new IllegalStateException(ioe);
		}
	}

	private void scanFiles(File file, File base) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				scanFiles(f, base);
			}
		} else if (file.getName().endsWith(".class")) {
			try {
				byte[] bytes = Files.readAllBytes(Paths.get(file.toURI()));
				ClassReader reader = new ClassReader(bytes);
				ClassNode node = new ClassNode();
				reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
				AnnotationInfo ai = new AnnotationInfo(this, node);
				if (ai.hasData()) {
					System.out.println("From " + file.getName() + " got " + ai.toAnnotationString());
					annotatedTypes.put(node.name, ai);
				}
			} catch (IOException ioe) {
				throw new IllegalStateException(ioe);
			}
		}
//		else // resource?
	}

	public static class AnnotationInfo {

		private String name;
		private TypeSystem typeSystem;
		private List<AnnotationNode> annotations;

		// if this is the annotationinfo for an annotation, this will cache meta
		// annotations
		private List<AnnotationNode> metaAnnotationsList = null;

		// need file?

		public AnnotationInfo(TypeSystem typeSystem, ClassNode node) {
			this.typeSystem = typeSystem;
			this.name = node.name;
			annotations = node.visibleAnnotations;
		}

		public boolean hasData() {
			return annotations != null && annotations.size() != 0;
		}

		public String toAnnotationString() {
			StringBuilder sb = new StringBuilder();
			if (annotations != null) {
				for (AnnotationNode an : annotations) {
					sb.append(an.desc);
					sb.append("(");
					List<Object> values = an.values;
					if (values != null) {
						for (int j = 0; j < values.size(); j += 2) {
							sb.append(values.get(j));
							sb.append("=");
							sb.append(values.get(j + 1));
						}
					}
					sb.append(")");
				}
			}
			return sb.toString();
		}

		public boolean hasDescriptor(String annotationDescriptor) {
			for (AnnotationNode an : annotations) {
				if (an.desc.equals(annotationDescriptor)) {
					return true;
				}
			}
			return false;
		}

		// TODO filter out java/lang/annotation annotations? Surely we don't need all of
		// them

		List<AnnotationNode> getMetaAnnotations() {
			if (metaAnnotationsList == null) {
				metaAnnotationsList = new ArrayList<>();
				collectMetaAnnotations();
				if (metaAnnotationsList.size() == 0) {
					metaAnnotationsList = Collections.emptyList();
				}
			}
			return metaAnnotationsList;
		}

		public boolean hasDescriptorMeta(String annotationDescriptor) {
			for (AnnotationNode an : annotations) {
				if (an.desc.equals(annotationDescriptor)) {
					return true;
				}
			}
			for (AnnotationNode an : getMetaAnnotations()) {
				if (an.desc.equals(annotationDescriptor)) {
					return true;
				}
			}
			return false;
		}

		private void collectMetaAnnotations() {
			for (AnnotationNode an : annotations) {
				// Go through our annotations and grab their meta annotations
				AnnotationInfo ai = typeSystem.annotatedTypes.get(an.desc.substring(1, an.desc.length() - 1));
				if (ai != null && ai.hasData()) {
					metaAnnotationsList.addAll(ai.getAnnotations());
					metaAnnotationsList.addAll(ai.getMetaAnnotations());
					if (name.endsWith("DemoApplication")) {
						System.out.println("111");
						for (AnnotationNode ann : metaAnnotationsList) {
							System.out.println(ann.desc);
						}
						System.out.println("222");
					}
				}
			}
		}

		private Collection<? extends AnnotationNode> getAnnotations() {
			return annotations;
		}
	}

	private void ensureScanned() {
		if (annotatedTypes == null) {
			annotatedTypes = new HashMap<>();
			long t = System.currentTimeMillis();
			scan();
			System.out.println("SBG: scan time: " + (System.currentTimeMillis() - t) + "ms");
		}
	}

	public List<String> findTypesAnnotated(String annotationDescriptor, boolean metaAnnotated) {
		ensureScanned();
		if (metaAnnotated) {
			return annotatedTypes.values().stream().filter(ai -> ai.hasDescriptorMeta(annotationDescriptor))
					.map(ai -> ai.name).collect(Collectors.toList());
		} else {
			return annotatedTypes.values().stream().filter(ai -> ai.hasDescriptor(annotationDescriptor))
					.map(ai -> ai.name).collect(Collectors.toList());
		}
	}

	public List<String> findTypesAnnotationAtConfiguration(boolean metaAnnotated) {
		return findTypesAnnotated(SPRING_AT_CONFIGURATION, metaAnnotated);
	}

	public List<CompilationHint> findHints(String typename) {
		if (typename.contains("/")) {
			if (typename.endsWith(";")) {
				typename= typename.substring(1,typename.length()-1).replace("/", ".");
			} else {
				typename= typename.replace("/", ".");
			}
		}
		if (hintLocator == null) {
			hintLocator = new SpringConfiguration(this);
		}
		// The result should include hints directly on the type as well
		// as discovered hints from separate configuration
		List<CompilationHint> results = new ArrayList<>();
		results.addAll(hintLocator.findProposedHints(typename));
		List<CompilationHint> declaredHints = resolveName(typename).getCompilationHints();
		results.addAll(declaredHints);
		return results;
	}
	
	static class Tuple<K,V> {
		private final K key;
		private final V value;
		Tuple(K key,V value) {
			this.key = key;
			this.value = value;
		}
		public K getKey() {
			return key;
		}
		public V getValue() {
			return value;
		}
		public String toString() { return key+": hasData?"+(value!=null); }
	}

	/**
	 * Retrieve the map from files (possibly inside jar files) to {@link ResourcesDescriptor} objects parsed
	 * from the contents of those files. This method is looking for files that start <tt>META-INF/native-image</tt>
	 * and end with <tt>resource-config.json</tt>. If the file is a path into a jar it has the form
	 * <tt>path/to/foo.jar!path/to/file</tt>.
	 * 
	 * @return map from files to @link {@link ResourcesDescriptor}
	 */
	public Map<String, ResourcesDescriptor> getResourceConfigurationsOnClasspath() {
		if (this.resourceConfigurations == null) {
			Map<String,ResourcesDescriptor> configs = new HashMap<>();
			for (String s: classpath) {
				File f = new File(s);
				if (f.isDirectory()) {
					searchDir(f, filepath -> { 
						return filepath.contains("META-INF/native-image") && filepath.endsWith("resource-config.json");
					}, 
					ResourcesJsonMarshaller::read,
					configs);
				} else if (f.isFile() && f.toString().endsWith(".jar")) {
					searchJar(f, filepath -> { 
						return filepath.contains("META-INF/native-image") && filepath.endsWith("resource-config.json");
					}, 
					ResourcesJsonMarshaller::read,
					configs);
				}
			}
			if (configs.isEmpty()) {
				this.resourceConfigurations = Collections.emptyMap();
			} else {
				this.resourceConfigurations = configs;
			}
		}
		return this.resourceConfigurations;
	}
	

	/**
	 * Recursively search a specified directory. Any files that match the specified predicate will have
	 * their contents converted by the supplied function and the resultant information stored in the collector map.
	 * 
	 * @param <T> The type of object produced by the converter function
	 * @param dir the directory to recursively search
	 * @param matchPredicate the predicate against which to match file paths
	 * @param converter the converter that processes file contents to produce something of type T
	 * @param collector the place to store mappings from matched file paths to T objects
	 */
	private <T> void searchDir(File dir, Predicate<String> matchPredicate, Function<InputStream, T> converter,
			Map<String, T> collector) {
		Path root = Paths.get(dir.toURI());
		try {
			List<Tuple<String, T>> found =
					Files.walk(root).filter(p -> matchPredicate.test(p.toAbsolutePath().toString())).map(p -> {
				try {
					T t = converter.apply(Files.newInputStream(p));
					return new Tuple<>(p.toString(), t);
				} catch (Exception e) {
					System.err.println("Unexpected problem reading " + p + ": " + e.getMessage());
					return new Tuple<>(p.toString(), (T)null);
				}
			}).collect(Collectors.toList());
			for (Tuple<String,T> t: found) {
				collector.put(t.getKey(), t.getValue());
			}
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to walk " + dir, ioe);
		}
	}

	/**
	 * Scan the entries in a specified jar file. Any entry paths that match the specified predicate will have
	 * their contents converted by the supplied function and the resultant information stored in the collector map.
	 * Jar file paths are of the form <tt>foo/bar/boo.jar!path/index/jar.txt</tt>.
	 * 
	 * @param <T> The type of object produced by the converter function
	 * @param jar the jar to scan
	 * @param matchPredicate the predicate against which to match file paths
	 * @param converter the converter that processes file contents to produce something of type T
	 * @param collector the place to store mappings from matched jar file paths to T objects
	 */
	private <T> void searchJar(File jar, Predicate<String> matchPredicate, Function<InputStream, T> converter, Map<String, T> collector) {
		try {
			try (ZipFile zf = new ZipFile(jar)) {
				Enumeration<? extends ZipEntry> entries = zf.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					String name = entry.getName();
					if (matchPredicate.test(name)) {
						collector.put(jar.toURI().getPath().toString()+"!"+name, converter.apply(zf.getInputStream(entry)));
					}
				}
			}
		} catch (FileNotFoundException fnfe) {
			System.err.println("WARNING: Unable to find jar '" + jar + "' whilst scanning filesystem");
		} catch (IOException ioe) {
			throw new RuntimeException("Problem during scan of " + jar, ioe);
		}
	}

	/**
	 * Discover if there is any <tt>resource-config.json</tt> on the classpath for this type system that
	 * contains an entry that would include <tt>META-INF/spring.factories</tt>.
	 * 
	 * @return the file path to the <tt>resource-config.json</tt> containing <tt>META-INF/spring.factories</tt> or null if there is none
	 */
	public String findAnyResourceConfigIncludingSpringFactoriesPattern() {
		String existingConfigThatIncludesSpringFactories = null; 
		Map<String,ResourcesDescriptor> resourceConfigurations = getResourceConfigurationsOnClasspath();
		outer: for (Map.Entry<String,ResourcesDescriptor> resourceConfiguration: resourceConfigurations.entrySet()) {
			List<String> patterns = resourceConfiguration.getValue().getPatterns();
			for (String pattern: patterns) {
				String slash = File.separator;
				// Catches it raw or escaped (as the agent would do) - will not currently catch funky wildcarded variants
				if (pattern.equals("META-INF"+slash+"spring.factories") || pattern.equals("\\QMETA-INF"+slash+"spring.factories\\E")) {
					existingConfigThatIncludesSpringFactories = resourceConfiguration.getKey();
					break outer;
				}
			}
		}
		return existingConfigThatIncludesSpringFactories;
	}

	/**
	 * Check if there are guard type(s) for this key from a spring.factory file.  The key (and associated entries it points at)
	 * should only be processed if one of the guard types exists. If none of them exist, this spring.factories entry is irrelevant.
	 * @param key entry key from a spring.factories file
	 * @return true if the key and associated value should be processed
	 */
	public boolean shouldBeProcessed(String key) {
		String[] guardTypes = SpringConfiguration.findProposedFactoryGuards(key);
		if (guardTypes == null) {
			return true;
		} else {
			for (String guardType : guardTypes) {
				Type resolvedType = resolveDotted(guardType, true);
				if (resolvedType != null) {
					return true;
				}
			}
			return false;
		}
	}

	public List<ComponentProcessor> getComponentProcessors() {
		return SpringConfiguration.getComponentProcessors();
	}

}
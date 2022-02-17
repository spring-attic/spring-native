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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.nativex.AotOptions;
import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.domain.reflect.MethodDescriptor;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;
import org.springframework.nativex.domain.serialization.SerializationDescriptor;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;

/**
 * Centralized collector of all computed configuration, can be used to produce JSON files.
 * 
 * @author Andy Clement
 * @author Ariel Carrera
 */
public class ConfigurationCollector {

	private final AotOptions aotOptions;
	
	private static Log logger = LogFactory.getLog(ConfigurationCollector.class);

	private ReflectionDescriptor reflectionDescriptor = new ReflectionDescriptor();

	private ResourcesDescriptor resourcesDescriptor = new ResourcesDescriptor();

	private ProxiesDescriptor proxiesDescriptor = new ProxiesDescriptor();

	private Set<AotProxyDescriptor> classProxyDescriptors = new HashSet<>();

	private InitializationDescriptor initializationDescriptor = new InitializationDescriptor();
	
	private SerializationDescriptor serializationDescriptor = new SerializationDescriptor();
	
	private ReflectionDescriptor jniReflectionDescriptor = new ReflectionDescriptor();
	
	private Set<String> options = new HashSet<>();

	private Map<String,byte[]> newResourceFiles = new HashMap<>();
	
	private TypeSystem ts;

	public ConfigurationCollector(AotOptions aotOptions) {
		this.aotOptions = aotOptions;
	}

	public ProxiesDescriptor getProxyDescriptors() {
		return proxiesDescriptor;
	}

	public Set<AotProxyDescriptor> getClassProxyDescriptors() {
		return classProxyDescriptors;
	}

	public ReflectionDescriptor getReflectionDescriptor() {
		String exclusions = System.getProperty("spring.native.reflection-exclusions");
		if (exclusions != null) {
			List<Pattern> patterns = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(exclusions,",");
			while (st.hasMoreElements()) {
				String nextPattern = st.nextToken();
				patterns.add(Pattern.compile(nextPattern));
			}
			ReflectionDescriptor newRD = new ReflectionDescriptor();
			List<ClassDescriptor> classDescriptors = reflectionDescriptor.getClassDescriptors();
			for (ClassDescriptor cd: classDescriptors) {
				boolean exclude = false;
				for (Pattern p: patterns) {
					if (p.matcher(cd.getName()).matches()) {
						exclude = true;
						System.out.println("ReflectionExclusion: excluding "+cd.getName());
						break;
					}
				}
				if (!exclude) {
					newRD.add(cd);
				}
			}
			return newRD;
		}
		return reflectionDescriptor;
	}

	public ResourcesDescriptor getResourcesDescriptors() {
		return resourcesDescriptor;
	}
	
	public SerializationDescriptor getSerializationDescriptor() {
		return serializationDescriptor;
	}
	
	public ReflectionDescriptor getJNIReflectionDescriptor() {
		return jniReflectionDescriptor;
	}
	
	public byte[] getResources(String name) {
		return newResourceFiles.get(name);
	}

	public InitializationDescriptor getInitializationDescriptor() {
		return initializationDescriptor;
	}
	
	public void setTypeSystem(TypeSystem ts) {
		this.ts = ts;
	}

	private boolean checkTypes(Collection<String> types, Predicate<Type> test) {
		for (String className : types) {
			Type clazz = ts.resolveDotted(className, true);
			if (!test.test(clazz)) {
				return false;
			}
		}
		return true;
	}

	public boolean addProxy(Collection<String> interfaceNames, boolean verify) {
		if (verify) {
			if (!checkTypes(interfaceNames, t -> t!=null && t.isInterface())) {
				return false;
			}
		}
		proxiesDescriptor.add(JdkProxyDescriptor.of(interfaceNames));
		return true;
	}

	public boolean addClassProxy(AotProxyDescriptor cpd, boolean verify) {
		if (verify) {
			if (ts.resolveName(cpd.getTargetClassType(), true)==null) {
				return false;
			}
			if (!checkTypes(cpd.getInterfaceTypes(), t -> t!=null && t.isInterface())) {
				return false;
			}
		}
		classProxyDescriptors.add(cpd);
		return true;
	}

	public static String[] subarray(String[] array) {
		if (array.length == 1) {
			return null;
		} else {
			return Arrays.copyOfRange(array, 1, array.length);
		}
	}
	
	private void writeNativeImageProperties(File file) throws IOException {
		String content = getNativeImagePropertiesContent();
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(content.getBytes());
		}
	}
	
	public String getNativeImagePropertiesContent() {
		StringBuilder s = new StringBuilder();
		// From the existing native-image.properties
		s.append("Args = --allow-incomplete-classpath --report-unsupported-elements-at-runtime --no-fallback --install-exit-handlers");
		if (!initializationDescriptor.getBuildtimeClasses().isEmpty() || 
			!initializationDescriptor.getBuildtimePackages().isEmpty()) {
			s.append(" \\\n");
			s.append("--initialize-at-build-time=");
			int i = 0;
			for (String name: initializationDescriptor.getBuildtimeClasses()) {
				if (i>0) {
					s.append(",");
				}
				s.append(name);
				i++;
			}
			for (String name: initializationDescriptor.getBuildtimePackages()) {
				if (i>0) {
					s.append(",");
				}
				s.append(name);
				i++;
			}
		}
		if (!initializationDescriptor.getRuntimeClasses().isEmpty() || 
			!initializationDescriptor.getRuntimePackages().isEmpty()) {
			s.append(" \\\n");
			s.append("--initialize-at-run-time=");
			int i = 0;
			for (String name: initializationDescriptor.getRuntimeClasses()) {
				if (i>0) {
					s.append(",");
				}
				s.append(name);
				i++;
			}
			for (String name: initializationDescriptor.getRuntimePackages()) {
				if (i>0) {
					s.append(",");
				}
				s.append(name);
				i++;
			}
		}
		for (String option : options) {
			s.append(" \\\n");
			s.append(option);
		}
		s.append("\n");
		return s.toString();
	}

	public void addResourcesDescriptor(ResourcesDescriptor resourcesDescriptor) {
		this.resourcesDescriptor.merge(resourcesDescriptor);
	}

	public ReflectionDescriptor addReflectionDescriptor(ReflectionDescriptor reflectionDescriptor) {
		return addReflectionDescriptor(reflectionDescriptor, true);
	}

	public ReflectionDescriptor addReflectionDescriptor(ReflectionDescriptor reflectionDescriptor, boolean verify) {
		ReflectionDescriptor filteredReflectionDescriptor = verify?filterVerified(reflectionDescriptor):reflectionDescriptor;
		this.reflectionDescriptor.merge(filteredReflectionDescriptor);
		return filteredReflectionDescriptor;
	}
	
	public boolean addSerializationType(String className, boolean verify) {
		if (verify) {
			Type clazz = ts.resolveDotted(className, true);
			if (clazz == null) {
				return false;
			}
		}
		serializationDescriptor.add(className);
		return true;
	}
	
	private boolean areMembersSpecified(ClassDescriptor cd) {
		List<MethodDescriptor> methods = cd.getMethods();
		if (methods != null && !methods.isEmpty()) {
			return true;
		}
		List<FieldDescriptor> fields = cd.getFields();
		if (fields != null && !fields.isEmpty()) {
			return true;
		}
		if (cd.getAccess()!=null && (cd.getAccess().contains(TypeAccess.DECLARED_CONSTRUCTORS) || cd.getAccess().contains(TypeAccess.PUBLIC_CONSTRUCTORS))) {
			return true;
		}
		return false;
	}

	private ReflectionDescriptor filterVerified(ReflectionDescriptor reflectionDescriptor2) {
		boolean anyFailed = false;
		List<ClassDescriptor> verified = new ArrayList<>();
		List<ClassDescriptor> classDescriptors = reflectionDescriptor2.getClassDescriptors();
		for (ClassDescriptor classDescriptor: classDescriptors) {
			boolean verifyType = verifyType(classDescriptor);
			if (!verifyType) {
				if (aotOptions.isDebugVerify()) {
					logger.debug("FAILED: filtering out "+classDescriptor.getName());
				}
				// Give up now
				anyFailed=true;
				continue;
			}
			if (areMembersSpecified(classDescriptor)) {
				if (!verifyMembers(classDescriptor)) {
					logger.debug("Stripped down to a base class descriptor for "+classDescriptor.getName());
					Set<TypeAccess> access = classDescriptor.getAccess();
					classDescriptor = ClassDescriptor.of(classDescriptor.getName());
					// TODO should set some flags here?
					anyFailed=true;
				}
			}
			verified.add(classDescriptor);
		}
		return anyFailed?new ReflectionDescriptor(verified):reflectionDescriptor2;
	}

	private boolean verifyType(ClassDescriptor classDescriptor) {
		Type t = ts.resolveDotted(classDescriptor.getName(),true);
		if (t == null) {
			logger.debug("Failed verification check: this type was requested to be added to configuration but is not resolvable: "+classDescriptor.getName()+" it will be skipped");
			return false;
		} else {
			return t.verifyType(aotOptions.isDebugVerify());
		}
	}

	private boolean verifyMembers(ClassDescriptor classDescriptor) {
		Type t = ts.resolveDotted(classDescriptor.getName(),true);
		if (t == null) {
			logger.debug("Failed verification check: this type was requested to be added to configuration but is not resolvable "+classDescriptor.getName()+" it will be skipped");
			return false;
		} else {
			return t.verifyMembers(aotOptions.isDebugVerify());
		}
	}

	public void addInitializationDescriptor(InitializationDescriptor initializationDescriptor) {
		this.initializationDescriptor.merge(initializationDescriptor);
	}

	public void addJNIClassDescriptor(ClassDescriptor classDescriptor) {
		if (!verifyType(classDescriptor)) {
			return;
		}
		if (areMembersSpecified(classDescriptor)) {
			if (!verifyMembers(classDescriptor)) {
				Set<TypeAccess> access = classDescriptor.getAccess();
				classDescriptor = ClassDescriptor.of(classDescriptor.getName());
				// TODO should set some access here? e.g	classDescriptor.setAccess(access);
			}
		}
		jniReflectionDescriptor.merge(classDescriptor);
	}
	
	public void addClassDescriptor(ClassDescriptor classDescriptor) {
		if (!verifyType(classDescriptor)) {
			return;
		}
		if (areMembersSpecified(classDescriptor)) {
			if (!verifyMembers(classDescriptor)) {
				Set<TypeAccess> access = classDescriptor.getAccess();
				classDescriptor = ClassDescriptor.of(classDescriptor.getName());
				// TODO should set some flags here? e.g	classDescriptor.setAccess(access);
			}
		}
		reflectionDescriptor.merge(classDescriptor);
	}

	public void registerResource(String resourceName, byte[] bytes) {
		resourcesDescriptor.add(resourceName);
		newResourceFiles.put(resourceName, bytes);
	}
	
	private boolean verifyBundle(String pattern) {
		Collection<String> bundles = ts.getBundles(pattern);
		if (bundles.size()==0) {
			logger.debug("Failed verification check: Invalid attempt to add bundle to configuration, no bundles found for this pattern: "+pattern);
		}
		return bundles.size()!=0;
	}
	
	public void addResource(String pattern, boolean isBundle) {
		if (isBundle) {
			if (verifyBundle(pattern)) {
				resourcesDescriptor.addBundle(pattern);
			}
		} else {
			resourcesDescriptor.add(pattern);
		}
	}

	public void initializeClassesAtBuildTime(String... typenames) {
		for (String typename: typenames) {
			initializationDescriptor.addBuildtimeClass(typename);
		}
	}

	public void initializeClassesAtRunTime(String... typeNames) {
		for (String typeName : typeNames) {
			initializationDescriptor.addRuntimeClass(typeName);
		}
	}

	public void initializePackagesAtBuildTime(String... packageNames) {
		for (String packageName : packageNames) {
			initializationDescriptor.addBuildtimePackage(packageName);
		}
	}

	public void initializePackagesAtRunTime(String... packageNames) {
		for (String packagename : packageNames) {
			initializationDescriptor.addRuntimePackage(packagename);
		}
	}

	public InputStream getNativeImagePropertiesInputStream() {
		return new ByteArrayInputStream(getNativeImagePropertiesContent().getBytes());
	}

	public void addOption(String option) {
		options.add(option);
	}

	public ClassDescriptor getClassDescriptorFor(String typename) {
		return reflectionDescriptor.getClassDescriptor(typename);
	}

	public ClassDescriptor getJNIClassDescriptorFor(String typename) {
		return jniReflectionDescriptor.getClassDescriptor(typename);
	}

}

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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.domain.proxies.ProxyDescriptor;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;

/**
 * Centralized collector of all computed configuration, can be used to produce JSON files, can optionally forward that info to GraalVM.
 * 
 * @author Andy Clement
 */
public class ConfigurationCollector {
	
	private GraalVMConnector graalVMConnector;
	
	private TypeSystem ts;
	
	private List<ReflectionDescriptor> reflectionDescriptors;

	private List<ResourcesDescriptor> resourcesDescriptors;

	private ProxiesDescriptor proxiesDescriptor = new ProxiesDescriptor();
	
	public ProxiesDescriptor getProxyDescriptors() {
		return proxiesDescriptor;
	}
	
	public List<ReflectionDescriptor> getReflectionDescriptors() {
		return reflectionDescriptors;
	}
	
	public List<ResourcesDescriptor> getResourcesDescriptors() {
		return resourcesDescriptors;
	}
	
	public void setGraalConnector(GraalVMConnector graalConnector) {
		this.graalVMConnector = graalConnector;
	}
	
	public void setTypeSystem(TypeSystem ts) {
		this.ts = ts;
	}

	private boolean checkTypes(List<String> types, Predicate<Type> test) {
		for (int i = 0; i < types.size(); i++) {
			String className = types.get(i);
			Type clazz = ts.resolveDotted(className, true);
			if (!test.test(clazz)) {
				return false;
			}
		}
		return true;
	}

	public boolean addProxy(List<String> interfaceNames, boolean verify) {
		if (verify) {
			if (!checkTypes(interfaceNames, t -> t!=null && t.isInterface())) {
				return false;
			}
		}
		if (graalVMConnector!=null) {
			graalVMConnector.addProxy(interfaceNames);
		}
		proxiesDescriptor.add(ProxyDescriptor.of(interfaceNames));
		return true;
	}
	

//	public void includeInDump(String typename, String[][] methodsAndConstructors, Flag[] flags) {
//		if (!ConfigOptions.shouldDumpConfig()) {
//			return;
//		}
//		ClassDescriptor currentCD = null;
//		for (ClassDescriptor cd: activeClassDescriptors) {
//			if (cd.getName().equals(typename)) {
//				currentCD = cd;
//				break;
//			}
//		}
//		if (currentCD == null) {
//			currentCD  = ClassDescriptor.of(typename);
//			activeClassDescriptors.add(currentCD);
//		}
//		// Update flags...
//		for (Flag f : flags) {
//			currentCD.setFlag(f);
//		}
//		if (methodsAndConstructors != null) {
//			for (String[] mc: methodsAndConstructors) {
//				MethodDescriptor md = MethodDescriptor.of(mc[0], subarray(mc));
//				if (!currentCD.contains(md)) {
//					currentCD.addMethodDescriptor(md);	
//				}
//			}
//		}
//	}
	
	public static String[] subarray(String[] array) {
		if (array.length == 1) {
			return null;
		} else {
			return Arrays.copyOfRange(array, 1, array.length);
		}
	}
		
		
	public void dump() {
		if (!ConfigOptions.shouldDumpConfig()) {
			return;
		}
		File folder = new File(ConfigOptions.getDumpConfigLocation());
		if (!folder.exists()) {
			folder.mkdirs();
		}
		if (!folder.exists()) {
			throw new RuntimeException("Unable to work with dump directory location: "+folder);
		}
//		activeClassDescriptors.sort((c1,c2) -> c1.getName().compareTo(c2.getName()));
//		ReflectionDescriptor rd = new ReflectionDescriptor();
//		for (ClassDescriptor cd: activeClassDescriptors) {
//			rd.add(cd);
//		}
//		try (FileOutputStream fos = new FileOutputStream(new File(ConfigOptions.getDumpConfigLocation()))) {
//			JsonMarshaller.write(rd,fos);
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
	}

//	DuringSetupAccessImpl access = (DuringSetupAccessImpl) a;
//	RuntimeReflectionSupport rrs = ImageSingletons.lookup(RuntimeReflectionSupport.class);
//	cl = access.getImageClassLoader();
//	ts = TypeSystem.get(cl.getClasspath());
//	rra = new ReflectionRegistryAdapter(rrs, cl);

	public void addReflectionDescriptor(ReflectionDescriptor reflectionDescriptor) {
		if (graalVMConnector != null) {
			graalVMConnector.addReflectionDescriptor(reflectionDescriptor);
		}
	}

	public void addClassDescriptor(ClassDescriptor classDescriptor) {
		// add it to existing refl desc stuff...
		if (graalVMConnector != null) {
			graalVMConnector.addClassDescriptor(classDescriptor);
		}
	}
		
}

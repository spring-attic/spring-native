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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptorJsonMarshaller;
import org.springframework.nativex.domain.proxies.ProxyDescriptor;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.JsonMarshaller;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;
import org.springframework.nativex.domain.resources.ResourcesJsonMarshaller;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;

/**
 * Centralized collector of all computed configuration, can be used to produce JSON files, can optionally forward that info to GraalVM.
 * 
 * @author Andy Clement
 */
public class ConfigurationCollector {

	private TypeSystem ts;

	private ReflectionDescriptor reflectionDescriptor = new ReflectionDescriptor();

	private ResourcesDescriptor resourcesDescriptor = new ResourcesDescriptor();

	private ProxiesDescriptor proxiesDescriptor = new ProxiesDescriptor();

	private GraalVMConnector graalVMConnector;

	public ProxiesDescriptor getProxyDescriptors() {
		return proxiesDescriptor;
	}
	
	public ReflectionDescriptor getReflectionDescriptors() {
		return reflectionDescriptor;
	}
	
	public ResourcesDescriptor getResourcesDescriptors() {
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
		SpringFeature.log("Writing out configuration to directory "+folder);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		if (!folder.exists()) {
			throw new RuntimeException("Unable to work with dump directory location: "+folder);
		}
		try {
			try (FileOutputStream fos = new FileOutputStream(new File(folder,"reflect-config.json"))) {
				JsonMarshaller.write(reflectionDescriptor,fos);
			}
			try (FileOutputStream fos = new FileOutputStream(new File(folder,"resource-config.json"))) {
				ResourcesJsonMarshaller.write(resourcesDescriptors,fos);
			}
			try (FileOutputStream fos = new FileOutputStream(new File(folder,"proxy-config.json"))) {
				ProxiesDescriptorJsonMarshaller.write(proxiesDescriptor,fos);
			}
		} catch (IOException ioe) {
			throw new RuntimeException("Problem writing out configuration",ioe);
		}
	}
	
	public void addResourcesDescriptor(ResourcesDescriptor resourcesDescriptor) {
		this.resourcesDescriptor.merge(resourcesDescriptor);
		if (graalVMConnector != null) {
			graalVMConnector.addResourcesDescriptor(resourcesDescriptor);
		}
	}

	public void addReflectionDescriptor(ReflectionDescriptor reflectionDescriptor) {
		this.reflectionDescriptor.merge(reflectionDescriptor);
		if (graalVMConnector != null) {
			graalVMConnector.addReflectionDescriptor(reflectionDescriptor);
		}
	}

	public void addClassDescriptor(ClassDescriptor classDescriptor) {
		reflectionDescriptor.merge(classDescriptor);
		// add it to existing refl desc stuff...
		if (graalVMConnector != null) {
			graalVMConnector.addClassDescriptor(classDescriptor);
		}
	}

}

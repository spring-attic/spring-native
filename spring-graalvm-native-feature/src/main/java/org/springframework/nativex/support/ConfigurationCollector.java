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

import java.util.List;
import java.util.function.Predicate;

import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.domain.proxies.ProxyDescriptor;
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

}

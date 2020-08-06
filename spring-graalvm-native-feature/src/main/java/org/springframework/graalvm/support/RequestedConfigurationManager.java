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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.graalvm.domain.init.InitializationDescriptor;
import org.springframework.graalvm.type.ProxyDescriptor;
import org.springframework.graalvm.type.ResourcesDescriptor;

/**
 * Collects up potential configuration that should be passed to native-image. By collecting it rather than
 * immediately processing it (i.e. passing it to native-image), the system can decide to discard it all
 * if it encounters a late reason why it shouldn't be requested (e.g. a ConditionalOnClass check failing).
 * 
 * @author Andy Clement
 */
public class RequestedConfigurationManager {

	private Map<String, Integer> requestedTypeAccesses = new HashMap<>();
	
	private List<ProxyDescriptor> requestedProxies = new ArrayList<>();
	
	private List<ResourcesDescriptor> requestedResources = new ArrayList<>();

	private List<InitializationDescriptor> requestedInitializations = new ArrayList<>();
	
	public void requestTypeAccess(String type, Integer accessRequired) {
		if (type.indexOf("/")!=-1) {
			throw new IllegalStateException("Only pass dotted names to request(), name was: "+type);
		}
		Integer existsAlready = requestedTypeAccesses.get(type);
		if (existsAlready != null) {
			requestedTypeAccesses.put(type, existsAlready | accessRequired);//existsAlready.with(accessRequired));
		} else {
			requestedTypeAccesses.put(type, accessRequired);
		}
	}
	
	public void requestProxyDescriptors(List<ProxyDescriptor> proxyDescriptors) {
		requestedProxies.addAll(proxyDescriptors);
	}
	
	public void requestResourcesDescriptors(List<ResourcesDescriptor> resourcesDescriptors) {
		requestedResources.addAll(resourcesDescriptors);
	}

	public void requestInitializationDescriptors(List<InitializationDescriptor> initializationDescriptors) {
		requestedInitializations.addAll(initializationDescriptors);
	}
	
	public Set<Entry<String,Integer>> getRequestedTypeAccesses() {
		return requestedTypeAccesses.entrySet();
	}

	public List<ProxyDescriptor> getRequestedProxies() {
		return requestedProxies;
	}
	
	public List<ResourcesDescriptor> getRequestedResources() {
		return requestedResources;
	}

	public List<InitializationDescriptor> getRequestedInitializations() {
		return requestedInitializations;
	}

}
	

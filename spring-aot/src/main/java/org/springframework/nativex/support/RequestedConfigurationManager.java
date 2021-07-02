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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.MethodDescriptor;
import org.springframework.nativex.type.ResourcesDescriptor;

/**
 * Collects up potential configuration that should be passed to native-image. By collecting it rather than
 * immediately processing it (i.e. passing it to native-image), the system can decide to discard it all
 * if it encounters a late reason why it shouldn't be requested (e.g. a ConditionalOnClass check failing).
 * 
 * @author Andy Clement
 */
public class RequestedConfigurationManager {

	private Map<String, Integer> requestedTypeAccesses = new HashMap<>();
	private Map<String, List<MethodDescriptor>> requestedMethodAccesses = new HashMap<>();
	private Map<String, List<FieldDescriptor>> requestedFieldAccesses = new HashMap<>();
	
	private List<JdkProxyDescriptor> requestedProxies = new ArrayList<>();
	
	private List<ResourcesDescriptor> requestedResources = new ArrayList<>();

	private List<InitializationDescriptor> requestedInitializations = new ArrayList<>();

	private Set<String> requestedOptions = new HashSet<>();
	
	private Set<String> requestedSerializableTypes = new HashSet<>();
	
	private Map<String, AccessDescriptor> requestedJNITypes = new HashMap<>();
	
	public void requestTypeAccess(String type, Integer accessRequired) {
		requestTypeAccess(type, accessRequired, null, null);
	}
	
	public void requestTypeAccess(String type, Integer accessRequired, List<MethodDescriptor> mds, List<FieldDescriptor> fds) {
		if (type.indexOf("/")!=-1) {
			throw new IllegalStateException("Only pass dotted names to request(), name was: "+type);
		}
		Integer existsAlready = requestedTypeAccesses.get(type);
		if (existsAlready != null) {
			requestedTypeAccesses.put(type, existsAlready | accessRequired);
		} else {
			requestedTypeAccesses.put(type, accessRequired);
		}
		if (mds != null && mds.size()>0) {
			List<MethodDescriptor> existingMds = requestedMethodAccesses.get(type);
			if (existingMds == null) {
				requestedMethodAccesses.put(type, new ArrayList<>(mds));
			} else {
				existingMds.addAll(mds);
			}
		}
		if (fds != null && fds.size()>0) {
			List<FieldDescriptor> existingFds = requestedFieldAccesses.get(type);
			if (existingFds == null) {
				requestedFieldAccesses.put(type, new ArrayList<>(fds));
			} else {
				existingFds.addAll(fds);
			}
		}
	}
	
	public Integer getTypeAccessRequestedFor(String type) {
		return requestedTypeAccesses.get(type);
	}

	public List<MethodDescriptor> getMethodAccessRequestedFor(String type) {
		return requestedMethodAccesses.get(type);
	}

	public List<FieldDescriptor> getFieldAccessRequestedFor(String type) {
		return requestedFieldAccesses.get(type);
	}

	public void reduceTypeAccess(String type, int newAccess) {
		if (type.indexOf("/")!=-1) {
			throw new IllegalStateException("Only pass dotted names to request(), name was: "+type);
		}
		Integer existsAlready = requestedTypeAccesses.get(type);
		if (existsAlready==null) {
			throw new IllegalStateException("Cannot reduce access for "+type+" - it hasn't been previously registered");
		}
		requestedTypeAccesses.put(type, newAccess);
	}
	
	public void requestProxyDescriptors(List<JdkProxyDescriptor> proxyDescriptors) {
		for (JdkProxyDescriptor pd: proxyDescriptors) {
			requestedProxies.add(pd);
		}
	}
	
	public void requestResourcesDescriptors(List<ResourcesDescriptor> resourcesDescriptors) {
		for (ResourcesDescriptor rd: resourcesDescriptors) {
			requestedResources.add(rd);
		}
	}

	public void requestInitializationDescriptors(List<InitializationDescriptor> initializationDescriptors) {
		for (InitializationDescriptor id: initializationDescriptors) {
			requestedInitializations.add(id);
		}
	}
	
	public void requestOptions(Set<String> options) {
		requestedOptions.addAll(options);
	}

	public void requestInitializationDescriptors(InitializationDescriptor initializationDescriptor) {
		requestedInitializations.add(initializationDescriptor);
	}
	
	public Set<Entry<String,Integer>> getRequestedTypeAccesses() {
		return requestedTypeAccesses.entrySet();
	}

	public List<JdkProxyDescriptor> getRequestedProxies() {
		return requestedProxies;
	}
	
	public List<ResourcesDescriptor> getRequestedResources() {
		return requestedResources;
	}

	public List<InitializationDescriptor> getRequestedInitializations() {
		return requestedInitializations;
	}
	
	public Set<String> getRequestedOptions() {
		return requestedOptions;
	}
	
	public Map<String, AccessDescriptor> getRequestedJNITypes() {
		return requestedJNITypes;
	}
	
	public Set<String> getRequestedSerializableTypes() {
		return requestedSerializableTypes;
	}

	public void mergeIn(RequestedConfigurationManager incomingRCM) {
		for (Entry<String, Integer> entry : incomingRCM.getRequestedTypeAccesses()) {
			String typename = entry.getKey();
			requestTypeAccess(typename, entry.getValue(), incomingRCM.getMethodAccessRequestedFor(typename), incomingRCM.getFieldAccessRequestedFor(typename));
		}
		requestInitializationDescriptors(incomingRCM.getRequestedInitializations());
		requestProxyDescriptors(incomingRCM.getRequestedProxies());
		requestResourcesDescriptors(incomingRCM.getRequestedResources());
		requestOptions(incomingRCM.getRequestedOptions());
		requestSerializationTypes(incomingRCM.getSerializationTypes());
		requestJniTypes(incomingRCM.getJNITypes());
	}

	public void addMethodDescriptors(String type, String[][] methods) {
		requestedMethodAccesses.put(type, MethodDescriptor.of(methods));
	}

	public void removeTypeAccess(String typename) {
		requestedTypeAccesses.remove(typename);
		requestedMethodAccesses.remove(typename);
		requestedFieldAccesses.remove(typename);
	}

	public void requestSerializationTypes(Set<String> serializationTypes) {
		requestedSerializableTypes.addAll(serializationTypes);
	}

	public void requestJniTypes(Map<String, AccessDescriptor> jniTypes) {
		requestedJNITypes.putAll(jniTypes);
	}

	private Map<String, AccessDescriptor> getJNITypes() {
		return requestedJNITypes;
	}

	private Set<String> getSerializationTypes() {
		return requestedSerializableTypes;
	}

}
	

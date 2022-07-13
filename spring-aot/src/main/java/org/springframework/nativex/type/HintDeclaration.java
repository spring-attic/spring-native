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

package org.springframework.nativex.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;

/**
 * 
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
public class HintDeclaration {

	private String triggerTypename;

	private Set<String> options = new LinkedHashSet<>();

	private Map<String, AccessDescriptor> specificTypes = new LinkedHashMap<>();

	private Map<String, AccessDescriptor> jniTypes = new LinkedHashMap<>();

	private Set<String> serializationTypes = new HashSet<>();

	private Set<String> serializationLambdaCapturingTypes = new HashSet<>();
	
	private List<JdkProxyDescriptor> proxyDescriptor = new ArrayList<>();

	private List<ResourcesDescriptor> resourceDescriptors = new ArrayList<>();

	private List<InitializationDescriptor> initializationDescriptors = new ArrayList<>();

	public boolean follow = false;

	public boolean skipIfTypesMissing = false;
	
	public List<String> extractAttributeNames;
	
	public HintDeclaration() {
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("HintDeclaration");
		if (triggerTypename != null) {
			sb.append(" for ").append(triggerTypename);
		}
		if (options != null && !options.isEmpty()) {
			sb.append(":options=");
			sb.append(options);
		}
		sb.append(":");
		sb.append(specificTypes);
		if (resourceDescriptors!=null && !resourceDescriptors.isEmpty()) {
			sb.append(":resDes=").append(resourceDescriptors);
		}
		if (initializationDescriptors!=null && !initializationDescriptors.isEmpty()) {
			sb.append(":initDes=").append(initializationDescriptors);
		}
		if (proxyDescriptor!=null && !proxyDescriptor.isEmpty()) {
			sb.append(":proxyDes=").append(proxyDescriptor);
		}
		if (jniTypes!=null && !jniTypes.isEmpty()) {
			sb.append(":jniTypes=").append(jniTypes);
		}
		return sb.toString();
	}

	public void setTriggerTypename(String triggerTypename) {
		this.triggerTypename = triggerTypename;
	}

	public String getTriggerTypename() {
		return triggerTypename;
	}

	public void addOption(String option) {
		options.add(option);
	}
	
	public Map<String, AccessDescriptor> getDependantTypes() {
		return specificTypes;
	}
	
	public Map<String, AccessDescriptor> getJNITypes() {
		return jniTypes;
	}

	public void addSerializationType(String className) {
		serializationTypes.add(className);
	}

	public void addSerializationLambdaCapturingType(String className) {
		serializationLambdaCapturingTypes.add(className);
	}
	
	public Set<String> getSerializationTypes() {
		return serializationTypes;
	}

	public Set<String> getSerializationLambdaCapturingTypes() {
		return serializationLambdaCapturingTypes;
	}

	public void addDependantType(String className, AccessDescriptor accessDescriptor) {
		specificTypes.put(className, accessDescriptor);
	}
	
	public void addJniType(String className, AccessDescriptor accessDescriptor) {
		jniTypes.put(className, accessDescriptor);	
	}

	public void setAbortIfTypesMissing(Boolean b) {
		skipIfTypesMissing = b;
	}

	public boolean isAbortIfTypesMissing() {
		return skipIfTypesMissing;
	}

	public void setFollow(Boolean b) {
		follow = b;
	}

	public void addProxyDescriptor(JdkProxyDescriptor pd) {
		proxyDescriptor.add(pd);
	}
	
	public List<JdkProxyDescriptor> getProxyDescriptors() {
		return proxyDescriptor;
	}
	
	public void addResourcesDescriptor(ResourcesDescriptor rd) {
		resourceDescriptors.add(rd);
	}

	public void addInitializationDescriptor(InitializationDescriptor id) {
		initializationDescriptors.add(id);
	}
	
	public List<ResourcesDescriptor> getResourcesDescriptors() {
		return resourceDescriptors;
	}

	public List<InitializationDescriptor> getInitializationDescriptors() {
		return initializationDescriptors;
	}

	public void setAttributesToExtract(List<String> extractAttributeNames) {
		this.extractAttributeNames = extractAttributeNames;
	}
	
	public List<String> getExtractAttributeNames() {
		return extractAttributeNames;
	}

	public Set<String> getOptions() {
		return options;
	}
}
/*
 * Copyright 2021 the original author or authors.
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

package org.springframework.aop.framework;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 * @author Andy Clement
 * @author Ariel Carrera
 */
public class BuildTimeProxyDescriptor {

	private final String targetClassName;
	
	private final SortedSet<String> interfaceNames;

	private final int proxyFeatures; // ProxyBits
	
	public BuildTimeProxyDescriptor(String targetClassName, Collection<String> interfaceNames, int proxyFeatures) {
		this.targetClassName = targetClassName;
		this.interfaceNames = new TreeSet<String>(interfaceNames);
		this.proxyFeatures = proxyFeatures;
	}

	public boolean isFeatureSet(int bit) {
		return (proxyFeatures&bit)!=0;
	}

	public SortedSet<String> getInterfaceTypes() {
		return interfaceNames;
	}

	public String getTargetClassType() {
		return targetClassName;
	}

}

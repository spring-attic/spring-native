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

import java.util.List;

/**
 * 
 * @author Andy Clement
 */
public class BuildTimeProxyDescriptor {

	private final String targetClassName;
	
	private final List<String> interfaceNames;

	private final int proxyFeatures; // ProxyBits
	
	public BuildTimeProxyDescriptor(String targetClassName, List<String> interfaceNames, int proxyFeatures) {
		this.targetClassName = targetClassName;
		this.interfaceNames = interfaceNames;
		this.proxyFeatures = proxyFeatures;
	}

	public boolean isFeatureSet(int bit) {
		return (proxyFeatures&bit)!=0;
	}

	public List<String> getInterfaceTypes() {
		return interfaceNames;
	}

	public String getTargetClassType() {
		return targetClassName;
	}

}

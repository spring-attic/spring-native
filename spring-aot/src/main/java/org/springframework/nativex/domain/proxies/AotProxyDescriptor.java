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

package org.springframework.nativex.domain.proxies;

import java.util.Arrays;
import java.util.List;

import org.springframework.aop.framework.BuildTimeProxyDescriptor;
import org.springframework.nativex.hint.ProxyBits;

/**
 * 
 * @author Andy Clement
 */
public class AotProxyDescriptor extends JdkProxyDescriptor {
	
	private final String targetClassName;

	private final int proxyFeatures;
	
	public AotProxyDescriptor(String targetClassName, List<String> interfaceNames, int proxyFeatures) {
		super(interfaceNames);
		this.targetClassName = targetClassName;
		this.proxyFeatures = proxyFeatures;
	}
	
	public String getTargetClassType() {
		return targetClassName;
	}

	public List<String> getInterfaceTypes() {
		return types;
	}

	public List<String> getTypes() {
		throw new IllegalStateException();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ClassProxyDescriptor(class=").append(targetClassName)
			.append(",interfaces=").append(Arrays.asList(getInterfaceTypes()))
			.append(",proxyFeatures=").append(ProxyBits.toString(proxyFeatures)).append(")");
		return sb.toString();
	}
	
	public int getProxyFeatures() {
		return proxyFeatures;
	}
	
	public boolean isClassProxy() {
		return true;
	}

	public int hashCode() {
		int hc = targetClassName.hashCode();
		if (this.types!=null) {
			for (String type: types) {
				hc = hc + type.hashCode() * 37;
			}
		}
		hc = hc + proxyFeatures * 37;
		return hc;
	}
	
	public boolean equals(Object other) {
		if (other instanceof AotProxyDescriptor) {
			AotProxyDescriptor that = (AotProxyDescriptor)other;
			if (!that.getTargetClassType().equals(getTargetClassType())) {
				return false;
			}
			if (that.getProxyFeatures()!=getProxyFeatures()) {
				return false;
			}
			return getInterfaceTypes().equals(that.getInterfaceTypes());
		}
		return false;
	}

	public boolean isFeatureSet(int bit) {
		return (proxyFeatures&bit)!=0;
	}

	public BuildTimeProxyDescriptor asCPDescriptor() {
		return new BuildTimeProxyDescriptor(targetClassName, types, proxyFeatures);
	}
}

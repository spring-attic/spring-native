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

import java.util.ArrayList;
import java.util.List;

import org.springframework.aop.SpringProxy;
import org.springframework.core.DecoratingProxy;
import org.springframework.nativex.hint.ProxyBits;

/**
 * This configuration is built based <b>only</b> on the state in an AdvisedSupport class that
 * will effect the shape of the generated code for a proxy.
 * 
 * @author Andy Clement
 */
public class ProxyConfiguration {

	private String targetClass;

	private List<String> interfaces;

	private boolean exposeProxy;

	private boolean isStatic;

	private boolean isFrozen;

	private boolean isOpaque;

	public ProxyConfiguration(String targetClass, List<String> interfaces, boolean exposeProxy, boolean isStatic, boolean isFrozen, boolean isOpaque) {
		this.targetClass = targetClass;
		this.interfaces = new ArrayList<>();
		if (interfaces != null) {
			for (String intf: interfaces) {
				if (!intf.equals(_AdvisedSupportAware.class.getName())) {
					this.interfaces.add(intf);
				}
			}
		}
		this.exposeProxy = exposeProxy;
		this.isStatic = isStatic;
		this.isFrozen = isFrozen;
		this.isOpaque = isOpaque;
	}
	
	public static ProxyConfiguration get(BuildTimeProxyDescriptor pd, ClassLoader classLoader) {
		List<String> interfaceTypes = new ArrayList<>();
		if (pd.getInterfaceTypes()!=null) {
			for (String interfaceTypename: pd.getInterfaceTypes()) {
				interfaceTypes.add(interfaceTypename);
			}
		}
		completeProxiedInterfaces(pd.getTargetClassType(), pd.isFeatureSet(ProxyBits.IS_OPAQUE), false, classLoader, interfaceTypes);
		return new ProxyConfiguration(
				pd.getTargetClassType(),
				interfaceTypes,
				pd.isFeatureSet(ProxyBits.EXPOSE_PROXY),
				pd.isFeatureSet(ProxyBits.IS_STATIC),
				pd.isFeatureSet(ProxyBits.IS_FROZEN),
				pd.isFeatureSet(ProxyBits.IS_OPAQUE));
	}

	public static ProxyConfiguration get(AdvisedSupport advised, ClassLoader classLoader) {
		Class<?>[] proxiedInterfaces = advised.getProxiedInterfaces();
		List<String> interfaces = new ArrayList<>();
		if (proxiedInterfaces != null) {
			for (Class<?> proxiedInterface : proxiedInterfaces) {
				interfaces.add(proxiedInterface.getName());
			}
		}
		completeProxiedInterfaces(advised, false, classLoader, interfaces);
		return new ProxyConfiguration(advised.getTargetClass().getName(), interfaces, advised.isExposeProxy(), advised.getTargetSource().isStatic(), advised.isFrozen(), advised.isOpaque());
	}

	static void completeProxiedInterfaces(AdvisedSupport advised, boolean decoratingProxy, ClassLoader classLoader, List<String> specifiedInterfaces) {
		completeProxiedInterfaces(advised.getTargetClass().getName(), advised.isOpaque(), decoratingProxy, classLoader, specifiedInterfaces);
	}
		
	static void completeProxiedInterfaces(String targetClassName, boolean isOpaque, boolean decoratingProxy, ClassLoader classLoader, List<String> specifiedInterfaces) {
		boolean addSpringProxy = !specifiedInterfaces.contains(SpringProxy.class.getName());
		boolean addAdvised = !isOpaque && !specifiedInterfaces.contains(Advised.class.getName());
		boolean addDecoratingProxy = (decoratingProxy && !specifiedInterfaces.contains(DecoratingProxy.class.getName()));
		if (addSpringProxy) {
			specifiedInterfaces.add(SpringProxy.class.getName());
		}
		if (addAdvised) {
			specifiedInterfaces.add(Advised.class.getName());
		}
		if (addDecoratingProxy) {
			specifiedInterfaces.add(DecoratingProxy.class.getName());
		}
	}

	public void setTargetClass(String targetClass) {
		this.targetClass = targetClass;
	}

	public String getTargetClass() {
		return this.targetClass;
	}

	public List<String> getProxiedInterfaces() {
		return interfaces;
	}

	public boolean isInterfaceProxied(String intface) {
		return interfaces.contains(intface);
	}

	public boolean isExposeProxy() {
		return exposeProxy;
	}

	public boolean isFrozen() {
		return isFrozen;
	}

	public boolean isStatic() {
		return isStatic;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		ProxyConfiguration that = (ProxyConfiguration) object;
		if (this.exposeProxy != that.exposeProxy) {
			return false;
		}
		if (this.isStatic != that.isStatic) {
			return false;
		}
		if (this.isFrozen != that.isFrozen) {
			return false;
		}
		if (!this.targetClass.equals(that.targetClass)) {
			return false;
		}
		if (!this.interfaces.equals(that.interfaces)) {
			return false;
		}
		return true;
	}


	public void setInterfaces(String interfaceName) {
		this.interfaces = new ArrayList<>();
		this.interfaces.add(interfaceName);
	}

	public void setInterfaces(List<String> interfaceNames) {
		this.interfaces = interfaceNames;
	}

	public boolean includesInterface(String interfaceName) {
		return interfaces.contains(interfaceName);
	}

	public void addInterface(String name) {
		this.interfaces.add(name);
	}

	public boolean isOpaque() {
		return this.isOpaque;
	}

	@Override
	public int hashCode() {
		int result = this.targetClass.hashCode();
		if (!interfaces.isEmpty()) {
			for (String intface: interfaces) {
				result = 37 * result + intface.hashCode();
			}
		}
		result = 37 * result + (this.exposeProxy ? 1 : 0);
		result = 37 * result + (this.isStatic ? 1 : 0);
		result = 37 * result + (this.isFrozen ? 1 : 0);
		result = 37 * result + (this.isOpaque ? 1 : 0);
		return result;
	}

	/**
	 * Return a reliable computed name for the proxy class, based on the type that is
	 * being extended, with a Spring infix, with a suffix hash code computed based on the
	 * state within this ProxyConfiguration (implemented interfaces and boolean flags).
	 * The double '$$' is deliberate and ensures these proxies pass the 'isClassProxy()' test
	 * in Spring because the check is already looking for this pattern in the CGLIB proxies.
	 *
	 * @return a reliable computed name based on this ProxyConfiguration
	 */
	public String getProxyClassName() {
		StringBuilder name = new StringBuilder();
		name.append(targetClass);
		name.append("$$SpringProxy$");
		name.append(Integer.toHexString(hashCode()));
		return name.toString();
	}

	public String asHint() {
		StringBuilder s = new StringBuilder();
		s.append("@AotProxyHint(");
		boolean commaNeeded = false;
		if (targetClass != null) {
			// Visibility check?
			s.append("targetClass=").append(targetClass).append(".class");
			commaNeeded=true;
		}
		List<String> filteredInterfaces = new ArrayList<>();
		for (String intface: interfaces) {
			if (!intface.equals(Advised.class.getName()) && 
				!intface.equals(SpringProxy.class.getName())) {
				filteredInterfaces.add(intface);
			}
		}
		if (!filteredInterfaces.isEmpty()) {
			if (commaNeeded) {
				s.append(", ");
			}
			s.append("interfaces={");
			for (int i=0;i<filteredInterfaces.size();i++) {
				if (i>0) {
					s.append(", ");
				}
				s.append(filteredInterfaces.get(i)).append(".class");
			}
			s.append("}");
			commaNeeded=true;
		}
		if (exposeProxy || isStatic || isFrozen || isOpaque) {
			if (commaNeeded) {
				s.append(", ");
			}
			s.append("proxyFeatures = ");
			boolean orNeeded = false;
			if (isStatic) {
				if (orNeeded) {
					s.append("|");
				}
				s.append("ProxyBits.IS_STATIC");
				orNeeded=true;
			}
			if (isOpaque) {
				if (orNeeded) {
					s.append("|");
				}
				s.append("ProxyBits.IS_OPAQUE");
				orNeeded=true;
			}
			if (isFrozen) {
				if (orNeeded) {
					s.append("|");
				}
				s.append("ProxyBits.IS_FROZEN");
				orNeeded=true;
			}
			if (exposeProxy) {
				if (orNeeded) {
					s.append("|");
				}
				s.append("ProxyBits.EXPOSE_PROXY");
				orNeeded=true;
			}
			commaNeeded=true;
		}
		s.append(")");
		return s.toString();
	}
}

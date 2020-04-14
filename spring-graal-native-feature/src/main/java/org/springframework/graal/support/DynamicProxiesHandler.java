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
package org.springframework.graal.support;

import java.io.InputStream;
import java.util.List;

import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature.DuringSetupAccess;
import org.springframework.graal.domain.proxies.ProxiesDescriptor;
import org.springframework.graal.domain.proxies.ProxiesDescriptorJsonMarshaller;
import org.springframework.graal.domain.proxies.ProxyDescriptor;

import com.oracle.svm.core.jdk.proxy.DynamicProxyRegistry;
import com.oracle.svm.hosted.FeatureImpl.DuringSetupAccessImpl;
import com.oracle.svm.hosted.ImageClassLoader;

public class DynamicProxiesHandler {
	
	private ImageClassLoader imageClassLoader;

	public ProxiesDescriptor compute() {
		try {
			InputStream s = this.getClass().getResourceAsStream("/proxies.json");
			ProxiesDescriptor pd = ProxiesDescriptorJsonMarshaller.read(s);
			return pd;
		} catch (Exception e) {
			return null;
		}
	}
	
	public boolean addProxy(List<String> interfaceNames) {
		boolean isOK = true;
		Class<?>[] interfaces = new Class<?>[interfaceNames.size()];
		for (int i = 0; i < interfaceNames.size(); i++) {
			String className = interfaceNames.get(i);
			Class<?> clazz = imageClassLoader.findClassByName(className, false);
			if (clazz == null) {
				isOK=false;
				break;
			}
			if (!clazz.isInterface()) {
				throw new RuntimeException("The class \"" + className + "\" is not an interface.");
			}
			interfaces[i] = clazz;
		}
		if (isOK) {
			addProxy(interfaces);
			return true;
		} else {
			return false;
		}
	}
	
	public void addProxy(Class<?>[] interfaces) {
		DynamicProxyRegistry dynamicProxySupport = ImageSingletons.lookup(DynamicProxyRegistry.class);
		dynamicProxySupport.addProxyClass(interfaces);
	}

	public void register(DuringSetupAccess a) {
		ProxiesDescriptor pd = compute();
		System.out.println("Attempting proxy registration of #"+pd.getProxyDescriptors().size()+" proxies");
		int skippedProxiesCount = 0;
		DuringSetupAccessImpl access = (DuringSetupAccessImpl) a;
		imageClassLoader = access.getImageClassLoader();
		// Should have been registered by DynamicProxyFeature already
		DynamicProxyRegistry dynamicProxySupport = ImageSingletons.lookup(DynamicProxyRegistry.class);
//      DynamicProxySupport dynamicProxySupport = new DynamicProxySupport(imageClassLoader.getClassLoader());
//      ImageSingletons.add(DynamicProxyRegistry.class, dynamicProxySupport);
		for (ProxyDescriptor proxyDescriptor : pd.getProxyDescriptors()) {
			List<String> interfaceNames = proxyDescriptor.getInterfaces();
			boolean isOK = true;
			Class<?>[] interfaces = new Class<?>[interfaceNames.size()];
			for (int i = 0; i < interfaceNames.size(); i++) {
				String className = interfaceNames.get(i);
				Class<?> clazz = imageClassLoader.findClassByName(className, false);
				if (clazz == null) {
					SpringFeature.log("Skipping proxy registration for "+interfaceNames+" because of missing type: "+className);
					skippedProxiesCount++;
					isOK=false;
					break;
				}
				if (!clazz.isInterface()) {
					throw new RuntimeException("The class \"" + className + "\" is not an interface.");
				}
				interfaces[i] = clazz;
			}
			if (isOK) {
				/* The interfaces array can be empty. The java.lang.reflect.Proxy API allows it. */
				dynamicProxySupport.addProxyClass(interfaces);
			}
		}
		if (skippedProxiesCount != 0) {
			System.out.println("Skipped registration of #"+skippedProxiesCount+" proxies - relevant types not on classpath");
		}
	}
}

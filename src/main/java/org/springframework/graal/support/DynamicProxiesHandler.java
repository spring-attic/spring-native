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
import java.util.function.Consumer;

import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature.DuringSetupAccess;
import org.springframework.graal.domain.proxies.ProxiesDescriptor;
import org.springframework.graal.domain.proxies.ProxiesDescriptorJsonMarshaller;

import com.oracle.svm.core.jdk.proxy.DynamicProxyRegistry;
import com.oracle.svm.hosted.ImageClassLoader;
import com.oracle.svm.hosted.FeatureImpl.DuringSetupAccessImpl;

public class DynamicProxiesHandler {

	public ProxiesDescriptor compute() {
		try {
			InputStream s = this.getClass().getResourceAsStream("/proxies.json");
			ProxiesDescriptor pd = ProxiesDescriptorJsonMarshaller.read(s);
			return pd;
		} catch (Exception e) {
			return null;
		}
	}

	public void register(DuringSetupAccess a) {
    	ProxiesDescriptor pd = compute();
    	System.out.println("SBG: Proxy registration: #"+pd.getProxyDescriptors().size()+" proxies");
    	DuringSetupAccessImpl access = (DuringSetupAccessImpl) a;
    	ImageClassLoader imageClassLoader = access.getImageClassLoader();
    	// Should have been registered by DynamicProxyFeature already
    	DynamicProxyRegistry dynamicProxySupport = ImageSingletons.lookup(DynamicProxyRegistry.class);
//      DynamicProxySupport dynamicProxySupport = new DynamicProxySupport(imageClassLoader.getClassLoader());
//      ImageSingletons.add(DynamicProxyRegistry.class, dynamicProxySupport);
    	Consumer<List<String>> proxyRegisteringConsumer = interfaceNames -> {
    		System.out.println("- "+interfaceNames);
    		boolean isOK= true;
            Class<?>[] interfaces = new Class<?>[interfaceNames.size()];
            for (int i = 0; i < interfaceNames.size(); i++) {
                String className = interfaceNames.get(i);
                Class<?> clazz = imageClassLoader.findClassByName(className, false);
                if (clazz == null) {
                	System.out.println("Skipping dynamic proxy registration due to missing type: "+className);
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
    	};
    	pd.consume(proxyRegisteringConsumer);
	}
}

package org.springframework.nativex.support;

import java.util.List;

import org.graalvm.nativeimage.ImageSingletons;

import com.oracle.svm.core.jdk.proxy.DynamicProxyRegistry;
import com.oracle.svm.hosted.ImageClassLoader;

/**
 * Acts as a bridge from the ConfigurationCollector to the Graal build system. Enables the general processing not to be aware
 * of the destination for configuration (pure json from the collector, or runtime API configuration if running as a feature).
 * 
 * @author Andy Clement
 */
public class GraalVMConnector {

	private ImageClassLoader imageClassLoader;

	public GraalVMConnector(ImageClassLoader imageClassLoader) {
		this.imageClassLoader = imageClassLoader;
	}

	public void addProxy(List<String> interfaceNames) {
		Class<?>[] interfaces = new Class<?>[interfaceNames.size()];
		for (int i = 0; i < interfaceNames.size(); i++) {
			String className = interfaceNames.get(i);
			Class<?> clazz = imageClassLoader.findClassByName(className, false);
			if (clazz == null) {
				return;
			}
			if (!clazz.isInterface()) {
				return;
			}
			interfaces[i] = clazz;
		}
		addProxy(interfaces);
	}
	
	public void addProxy(Class<?>[] interfaces) {
		DynamicProxyRegistry dynamicProxySupport = ImageSingletons.lookup(DynamicProxyRegistry.class);
		dynamicProxySupport.addProxyClass(interfaces);
	}


}

/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.nativex.buildtools.nativex;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.nativex.buildtools.BootstrapContributor;
import org.springframework.nativex.buildtools.BuildContext;
import org.springframework.nativex.support.ConfigOptions;
import org.springframework.nativex.support.ConfigurationCollector;
import org.springframework.nativex.support.Mode;
import org.springframework.nativex.support.SpringAnalyzer;
import org.springframework.nativex.type.TypeSystem;

/**
 * Contributes the configuration files for native image construction. This includes the reflection,
 * resource and proxy configuration in addition to the native-image.properties file that includes
 * special args and initialization configuration.
 * 
 * @author Andy Clement
 */
public class ConfigurationContributor implements BootstrapContributor {

	private final static boolean active = System.getProperty("spring.nativex.collector.active","false").equalsIgnoreCase("true");

	Path META_INF_NATIVE_IMAGE = Paths.get("META-INF", "native-image");

	@Override
	public void contribute(BuildContext context) {
		System.out.println("active says "+active);
		if (!active) {
			return;
		}
		ConfigOptions.setMode(Mode.REFLECTION);
		ConfigOptions.setShouldRemoveUnusedAutoconfig(false);
		System.out.println("Configuration contributor is running!");
		TypeSystem typeSystem = context.getTypeSystem();
		SpringAnalyzer springAnalyzer = new SpringAnalyzer(typeSystem);
		springAnalyzer.analyze();
		ConfigurationCollector configurationCollector = springAnalyzer.getConfigurationCollector();
		// TODO maybe should use groupid/artifactid in this path
		configurationCollector.dump(new File("target/classes/META-INF/native-image"));
//		ReflectionDescriptor reflectionDescriptor = configurationCollector.getReflectionDescriptor();
//		ResourcesDescriptor resourcesDescriptor = configurationCollector.getResourcesDescriptors();
//		ProxiesDescriptor proxiesDescriptor = configurationCollector.getProxyDescriptors();
//		context.addResources(
//			ResourceFiles.fromInputStream(META_INF_NATIVE_IMAGE, "reflect-config.json", reflectionDescriptor.getInputStream()),
//			ResourceFiles.fromInputStream(META_INF_NATIVE_IMAGE, "resource-config.json", resourcesDescriptor.getInputStream()),
//			ResourceFiles.fromInputStream(META_INF_NATIVE_IMAGE, "proxy-config.json", proxiesDescriptor.getInputStream()),
//			ResourceFiles.fromInputStream(META_INF_NATIVE_IMAGE, "native-image.properties", configurationCollector.getNativeImagePropertiesInputStream()));
	}

}

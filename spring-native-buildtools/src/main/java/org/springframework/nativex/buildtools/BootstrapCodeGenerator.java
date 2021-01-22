/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.nativex.buildtools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptorJsonMarshaller;
import org.springframework.nativex.domain.reflect.JsonMarshaller;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;
import org.springframework.nativex.domain.resources.ResourcesJsonMarshaller;

/**
 * Generate code for bootstrapping Spring applications in a GraalVM native environment.
 * <p>For that, we are looking up {@link BootstrapContributor}, registered as Spring factories
 * and let them contribute to the bootstrap code.
 *
 * @author Brian Clozel
 */
public class BootstrapCodeGenerator {

	private static Log logger = LogFactory.getLog(BootstrapCodeGenerator.class);

	/**
	 * Generate bootstrap code for the application.
	 *
	 * @param path the root path generated files should be written to
	 * @param classpath the "compile+runtime" classpath of the application
	 * @param resourceFolders paths to folders containing project main resources
	 */
	public void generate(Path path, List<String> classpath, Set<Path> resourceFolders) throws IOException {
		logger.debug("Starting code generation with classpath: " + classpath);
		DefaultBuildContext buildContext = new DefaultBuildContext(classpath);
		ServiceLoader<BootstrapContributor> contributors = ServiceLoader.load(BootstrapContributor.class);
		for (BootstrapContributor contributor : contributors) {
			logger.debug("Executing Contributor: " + contributor.getClass().getName());
			contributor.contribute(buildContext);
		}
		
		if (!resourceFolders.isEmpty()) {
			System.out.println("Processing resource folders: " + resourceFolders);
			for (Path resourceFolder : resourceFolders) {
				int resourceFolderLen = resourceFolder.toString().length() + 1;
				if (Files.exists(resourceFolder)) {
					Files.walk(resourceFolder).filter(p -> !p.toFile().isDirectory()).forEach(p -> {
						String resourcePattern = p.toString().substring(resourceFolderLen);
						if (!resourcePattern.startsWith("META-INF/native-image")) {
							System.out.println("Resource pattern: " + resourcePattern);
							// TODO recognize resource bundles?
							// TODO escape the patterns (add leading trailing Q and E sequences...)
							buildContext.describeResources(crd -> crd.add(resourcePattern));
						}
					});
				}
			}
		}
		
		logger.debug("Writing generated sources to: " + path);
		for (SourceFile sourceFile : buildContext.getSourceFiles()) {
			sourceFile.writeTo(path);
		}
		for (ResourceFile resourceFile : buildContext.getResourceFiles()) {
			resourceFile.writeTo(path);
		}

		Path graalVMConfigPath = path.resolve(ResourceFile.NATIVE_CONFIG_PATH);
		Files.createDirectories(graalVMConfigPath);
		// reflect-config.json
		ReflectionDescriptor reflectionDescriptor = buildContext.getReflectionDescriptor();
		if (!reflectionDescriptor.isEmpty()) {
			Path reflectConfigPath = graalVMConfigPath.resolve(Paths.get("reflect-config.json"));
			JsonMarshaller.write(reflectionDescriptor, Files.newOutputStream(reflectConfigPath));
		}
		// proxy-config.json
		ProxiesDescriptor proxiesDescriptor = buildContext.getProxiesDescriptor();
		if (!proxiesDescriptor.isEmpty()) {
			Path proxiesConfigPath = graalVMConfigPath.resolve(Paths.get("proxy-config.json"));
			ProxiesDescriptorJsonMarshaller proxiesMarshaller = new ProxiesDescriptorJsonMarshaller();
			proxiesMarshaller.write(proxiesDescriptor, Files.newOutputStream(proxiesConfigPath));
		}
		// resource-config.json
		ResourcesDescriptor resourcesDescriptor = buildContext.getResourcesDescriptor();
		if (!resourcesDescriptor.isEmpty()) {
			Path resourceConfigPath = graalVMConfigPath.resolve(Paths.get("resource-config.json"));
			ResourcesJsonMarshaller resourcesMarshaller = new ResourcesJsonMarshaller();
			resourcesMarshaller.write(resourcesDescriptor, Files.newOutputStream(resourceConfigPath));
		}
	}

}

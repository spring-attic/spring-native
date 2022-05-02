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

package org.springframework.aot.nativex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.BuildTimeProxyDescriptor;
import org.springframework.aop.framework.ProxyConfiguration;
import org.springframework.aop.framework.ProxyGenerator;
import org.springframework.aot.build.BootstrapContributor;
import org.springframework.aot.build.context.BuildContext;
import org.springframework.aot.build.context.ResourceFile;
import org.springframework.boot.loader.tools.MainClassFinder;
import org.springframework.nativex.AotOptions;
import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.support.ConfigurationCollector;
import org.springframework.nativex.support.SpringAnalyzer;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.util.ClassUtils;

/**
 * Contributes the configuration files for native image construction. This includes the reflection,
 * resource and proxy configuration in addition to the native-image.properties file that includes
 * special args and initialization configuration.
 *
 * @author Andy Clement
 */
public class ConfigurationContributor implements BootstrapContributor {

	private static final Log logger = LogFactory.getLog(ConfigurationContributor.class);

	@Override
	public void contribute(BuildContext context, AotOptions aotOptions) {
		TypeSystem typeSystem = new TypeSystem(context.getClasspath(), context.getMainClass());
		typeSystem.setAotOptions(aotOptions);
		SpringAnalyzer springAnalyzer = new SpringAnalyzer(typeSystem, aotOptions);
		springAnalyzer.analyze();
		ConfigurationCollector configurationCollector = springAnalyzer.getConfigurationCollector();
		processBuildTimeClassProxyRequests(context, configurationCollector);
		context.describeReflection(reflect -> reflect.merge(configurationCollector.getReflectionDescriptor()));
		context.describeResources(resources -> resources.merge(configurationCollector.getResourcesDescriptors()));
		context.describeProxies(proxies -> proxies.merge(configurationCollector.getProxyDescriptors()));
		context.describeSerialization(serial -> serial.merge(configurationCollector.getSerializationDescriptor()));
		context.describeJNIReflection(jniReflect -> jniReflect.merge(configurationCollector.getJNIReflectionDescriptor()));
		context.describeInitialization(init -> init.merge(configurationCollector.getInitializationDescriptor()));
		context.getOptions().forEach(configurationCollector::addOption);
		String mainClass = getMainClass(context);
		if (mainClass != null) {
			configurationCollector.addOption("-H:Class=" + mainClass);
		}
		byte[] springComponentsFileContents = configurationCollector.getResources("META-INF/spring.components");
		if (springComponentsFileContents != null) {
			logger.debug("Storing synthesized META-INF/spring.components");
			context.addResources((resourcesPath) -> {
				Path metaInfFolder = resourcesPath.resolve(Paths.get("META-INF"));
				Files.createDirectories(metaInfFolder);
				try (FileOutputStream fos = new FileOutputStream(resourcesPath.resolve(ResourceFile.SPRING_COMPONENTS_PATH).toFile())) {
					fos.write(springComponentsFileContents);
				}
			});
		}
		// Create native-image.properties
		context.addResources(new ResourceFile() {
			@Override
			public void writeTo(Path rootPath) throws IOException {
				Path nativeConfigFolder = rootPath.resolve(ResourceFile.NATIVE_CONFIG_PATH);
				Files.createDirectories(nativeConfigFolder);
				Path nativeImagePropertiesFile = nativeConfigFolder.resolve("native-image.properties");
				try (FileOutputStream fos = new FileOutputStream(nativeImagePropertiesFile.toFile())) {
					fos.write(configurationCollector.getNativeImagePropertiesContent().getBytes());
				}
			}
		});
	}

	private String getMainClass(BuildContext context) {
		if (context.getMainClass() != null) {
			return context.getMainClass();
		}
		for (String path : context.getClasspath()) {
			String mainClass = null;
			try {
				File file = new File(path);
				// For now only search in directories, could be extended to JARs using the JarFile parameter variant
				if (file.isDirectory()) {
					mainClass = MainClassFinder.findSingleMainClass(file);
				}
			}
			catch (Exception e) {
				logger.error(e);
			}
			if (mainClass != null) {
				logger.debug("ManifestContributor found Spring Boot main class: " + mainClass);
				return mainClass;
			}
		}
		logger.debug("Unable to find main class");
		return null;
	}


	/**
	 * If the configuration has requested any class proxies to be constructed at build time, create them and register
	 * reflective access to their necessary parts.
	 */
	private void processBuildTimeClassProxyRequests(BuildContext context, ConfigurationCollector configurationCollector) {
		List<String> classProxyNames = generateBuildTimeClassProxies(configurationCollector, context);
		ReflectionDescriptor reflectionDescriptor = new ReflectionDescriptor();
		if (!classProxyNames.isEmpty()) {
			for (String classProxyName : classProxyNames) {
				ClassDescriptor classDescriptor = ClassDescriptor.of(classProxyName);
				classDescriptor.setAccess(TypeAccess.DECLARED_CONSTRUCTORS);
				// TODO [build time proxies] not all proxy variants will need method access - depends on what
				// it is for - perhaps surface access bit configuration in the classproxyhint to allow flexibility?
				classDescriptor.setAccess(TypeAccess.DECLARED_METHODS);
				reflectionDescriptor.add(classDescriptor);
			}
			configurationCollector.addReflectionDescriptor(reflectionDescriptor, false);
		}
	}

	public List<String> generateBuildTimeClassProxies(ConfigurationCollector configurationCollector, BuildContext context) {
		List<String> classProxyNames = new ArrayList<>();
		Set<AotProxyDescriptor> classProxyDescriptors = configurationCollector.getClassProxyDescriptors();
		for (JdkProxyDescriptor proxyDescriptor : context.getProxiesDescriptor().getProxyDescriptors()) {
			if (proxyDescriptor.isClassProxy()) {
				classProxyDescriptors.add((AotProxyDescriptor) proxyDescriptor);
			}
		}
		for (AotProxyDescriptor classProxyDescriptor : classProxyDescriptors) {
			if (!ClassUtils.isPresent(classProxyDescriptor.getTargetClassType(), context.getClassLoader())) {
				logger.debug("Cannot reach class proxy target type of: " + classProxyDescriptor);
				continue;
			}
			try {
				Class<?> proxySuperClass = ClassUtils.forName(classProxyDescriptor.getTargetClassType(), context.getClassLoader());
				proxySuperClass.getDeclaredMethods();
			} catch (NoClassDefFoundError | ClassNotFoundException ex) {
				logger.debug("Unable to proxy methods on class [" + classProxyDescriptor + "] due to missing type: " + ex.getMessage());
				continue;
			}
			classProxyNames.add(generateBuildTimeClassProxy(classProxyDescriptor, context));
		}
		return classProxyNames;
	}

	@SuppressWarnings("deprecation")
	private String generateBuildTimeClassProxy(AotProxyDescriptor cpd, BuildContext context) {
		BuildTimeProxyDescriptor c = cpd.asCPDescriptor();
		ProxyConfiguration proxyConfiguration = ProxyConfiguration.get(c, null);
		List<String> classpath = context.getClasspath();
		URL[] urls = new URL[classpath.size()];
		for (int i = 0; i < classpath.size(); i++) {
			try {
				urls[i] = new File(classpath.get(i)).toURI().toURL();
			}
			catch (MalformedURLException e) {
				throw new IllegalStateException(e);
			}
		}
		// TODO [build time proxies] is this parent OK?
		URLClassLoader ucl = new URLClassLoader(urls, ConfigurationContributor.class.getClassLoader());
		logger.debug("Creating build time class proxy for class " + c.getTargetClassType());

		Unloaded<?> unloadedProxy = ProxyGenerator.getProxyBytes(c, ucl);

		Path primaryProxyFilepath = Paths.get(proxyConfiguration.getProxyClassName().replace(".", "/") + ".class");
		context.addResources((resourcesPath) -> {
			Path primaryProxyRelativeFolder = primaryProxyFilepath.getParent();
			Path primaryProxyFilename = primaryProxyFilepath.getFileName();
			Path primaryProxyFolder = resourcesPath.resolve(primaryProxyRelativeFolder);
			Files.createDirectories(primaryProxyFolder);
			Path primaryProxyAbsolutePath = primaryProxyFolder.resolve(primaryProxyFilename);
			logger.debug("Writing out build time class proxy as resource for type " + primaryProxyFilepath);
			try (FileOutputStream fos = new FileOutputStream(primaryProxyAbsolutePath.toFile())) {
				fos.write(unloadedProxy.getBytes());
			}
		});
		Map<TypeDescription, byte[]> auxiliaryTypes = unloadedProxy.getAuxiliaryTypes();
		if (auxiliaryTypes != null) {
			for (Map.Entry<TypeDescription, byte[]> auxiliaryType : auxiliaryTypes.entrySet()) {
				context.addResources(new ProxyAuxResourceFile(auxiliaryType.getKey(), auxiliaryType.getValue()));
			}
		}
		return proxyConfiguration.getProxyClassName();
	}

	/**
	 * Represents one of the auxiliary classes generated to support each method
	 * invocation that needs delegation in a proxy class.
	 */
	static class ProxyAuxResourceFile implements ResourceFile {

		private Path auxProxyFilepath;

		private byte[] data;

		ProxyAuxResourceFile(TypeDescription td, byte[] data) {
			this.data = data;
			auxProxyFilepath = Paths.get(td.getName().replace(".", "/") + ".class");
		}

		@Override
		public void writeTo(Path resourcesPath) throws IOException {
			Path auxProxyRelativeFolder = auxProxyFilepath.getParent();
			Path auxProxyFilename = auxProxyFilepath.getFileName();
			Path auxProxyFolder = resourcesPath.resolve(auxProxyRelativeFolder);
			Files.createDirectories(auxProxyFolder);
			Path auxProxyAbsolutePath = auxProxyFolder.resolve(auxProxyFilename);
			logger.debug("Writing out build time class proxy support class as resource for type " + auxProxyFilepath);
			try (FileOutputStream fos = new FileOutputStream(auxProxyAbsolutePath.toFile())) {
				fos.write(data);
			}
		}
	}

}

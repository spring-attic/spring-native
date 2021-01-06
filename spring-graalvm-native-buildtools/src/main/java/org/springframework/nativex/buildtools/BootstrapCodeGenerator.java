package org.springframework.nativex.buildtools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ServiceLoader;

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
	 */
	public void generate(Path path, List<String> classpath) throws IOException {
		logger.debug("Starting code generation with classpath: " + classpath);
		DefaultBuildContext buildContext = new DefaultBuildContext(classpath);
		ServiceLoader<BootstrapContributor> contributors = ServiceLoader.load(BootstrapContributor.class);
		for (BootstrapContributor contributor : contributors) {
			logger.debug("Executing Contributor: " + contributor.getClass().getName());
			contributor.contribute(buildContext);
		}
		logger.debug("Writing generated sources to: " + path);
		for (SourceFile sourceFile : buildContext.getSourceFiles()) {
			sourceFile.writeTo(path);
		}
		for (ResourceFile resourceFile : buildContext.getResourceFiles()) {
			resourceFile.writeTo(path);
		}
		Path resourcesPath = path.resolve(ResourceFile.MAIN_RESOURCES_PATH);
		Files.createDirectories(resourcesPath);
		
		// reflect-config.json
		ReflectionDescriptor reflectionDescriptor = buildContext.getReflectionDescriptor();
		if (!reflectionDescriptor.isEmpty()) {
			Path reflectConfigPath = resourcesPath.resolve(Paths.get("reflect-config.json"));
			JsonMarshaller.write(reflectionDescriptor, Files.newOutputStream(reflectConfigPath));
		}
		// proxy-config.json
		ProxiesDescriptor proxiesDescriptor = buildContext.getProxiesDescriptor();
		if (!proxiesDescriptor.isEmpty()) {
			Path proxiesConfigPath = resourcesPath.resolve(Paths.get("proxy-config.json"));
			ProxiesDescriptorJsonMarshaller proxiesMarshaller = new ProxiesDescriptorJsonMarshaller();
			proxiesMarshaller.write(proxiesDescriptor, Files.newOutputStream(proxiesConfigPath));
		}
		// resource-config.json
		ResourcesDescriptor resourcesDescriptor = buildContext.getResourcesDescriptor();
		if (!resourcesDescriptor.isEmpty()) {
			Path resourceConfigPath = resourcesPath.resolve(Paths.get("resource-config.json"));
			ResourcesJsonMarshaller resourcesMarshaller = new ResourcesJsonMarshaller();
			resourcesMarshaller.write(resourcesDescriptor, Files.newOutputStream(resourceConfigPath));
		}
	}

}

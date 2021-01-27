package org.springframework.nativex.buildtools.factories;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.buildtools.BootstrapContributor;
import org.springframework.nativex.buildtools.BuildContext;
import org.springframework.nativex.buildtools.CodeGenerationException;
import org.springframework.nativex.buildtools.SourceFiles;
import org.springframework.util.StringUtils;

/**
 * Contribute source code for registering {@code spring.factories} at build time.
 * Currently this is supported by a substitution on {@link SpringFactoriesLoader}.
 *
 * @author Brian Clozel
 */
public class SpringFactoriesContributor implements BootstrapContributor {

	private static final Log logger = LogFactory.getLog(SpringFactoriesContributor.class);

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 1;
	}

	@Override
	public void contribute(BuildContext context) {
		try {
			List<SpringFactory> springFactories = loadSpringFactories(context.getTypeSystem());
			FactoriesCodeContributors contributors = new FactoriesCodeContributors();
			CodeGenerator codeGenerator = contributors.createCodeGenerator(springFactories, context);

			context.addSourceFiles(SourceFiles.fromJavaFile(codeGenerator.generateStaticSpringFactories()));
			codeGenerator.generateStaticFactoryClasses().forEach(javaFile -> {
				context.addSourceFiles(SourceFiles.fromJavaFile(javaFile));
			});
			ClassPathResource factoriesLoader = new ClassPathResource("SpringFactoriesLoader.java", getClass());
			context.addSourceFiles(SourceFiles.fromStaticFile("org.springframework.core.io.support",
					"SpringFactoriesLoader", factoriesLoader.getInputStream()));
		}
		catch (Exception exc) {
			throw new CodeGenerationException("Could not generate spring.factories source code", exc);
		}
	}

	List<SpringFactory> loadSpringFactories(TypeSystem typeSystem) throws IOException {
		List<SpringFactory> factories = new ArrayList<>();
		Enumeration<URL> factoriesLocations = typeSystem.getResourceLoader()
				.getClassLoader().getResources(SpringFactoriesLoader.FACTORIES_RESOURCE_LOCATION);
		while (factoriesLocations.hasMoreElements()) {
			URL url = factoriesLocations.nextElement();
			UrlResource resource = new UrlResource(url);
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);
			for (Map.Entry<?, ?> entry : properties.entrySet()) {
				String factoryTypeName = ((String) entry.getKey()).trim();
				logger.debug("Loading factory Type:" + factoryTypeName);
				String[] factoryNames = StringUtils.commaDelimitedListToStringArray((String) entry.getValue());

				// TODO: sorting required here
				// AnnotationAwareOrderComparator.sort();

				for (String factoryName : factoryNames) {
					logger.debug("Loading factory Impl:" + factoryName);
					SpringFactory springFactory = SpringFactory.resolve(factoryTypeName, factoryName, typeSystem);
					if (springFactory != null) {
						factories.add(springFactory);
					}
					else {
						logger.debug("Could not load factory: " + factoryTypeName + " " + factoryName);
					}
				}
			}
		}
		return factories;
	}
}

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

package org.springframework.aot.factories;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aot.build.BootstrapContributor;
import org.springframework.aot.build.context.BuildContext;
import org.springframework.aot.build.CodeGenerationException;
import org.springframework.aot.build.context.SourceFiles;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.nativex.AotOptions;
import org.springframework.util.StringUtils;

/**
 * Contribute source code for registering {@code spring.factories} at build time.
 * Currently this is supported by a substitution on {@link SpringFactoriesLoader}.
 *
 * @author Brian Clozel
 * @author Sebastien Deleuze
 */
public class SpringFactoriesContributor implements BootstrapContributor {

	private static final Log logger = LogFactory.getLog(SpringFactoriesContributor.class);
	
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 1;
	}

	@Override
	public void contribute(BuildContext context, AotOptions aotOptions) {
		try {
			Set<SpringFactory> springFactories = loadSpringFactories(context);
			FactoriesCodeContributors contributors = new FactoriesCodeContributors(aotOptions);
			CodeGenerator codeGenerator = contributors.createCodeGenerator(springFactories, context, aotOptions);

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

	Set<SpringFactory> loadSpringFactories(BuildContext buildContext) throws IOException {
		Set<SpringFactory> factories = new LinkedHashSet<>();
		Enumeration<URL> factoriesLocations = buildContext.getClassLoader()
				.getResources(SpringFactoriesLoader.FACTORIES_RESOURCE_LOCATION);
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
					SpringFactory springFactory = SpringFactory.resolve(factoryTypeName, factoryName, buildContext.getClassLoader());
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


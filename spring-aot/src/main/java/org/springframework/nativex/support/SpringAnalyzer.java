/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.nativex.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.nativex.type.TypeSystem;

/**
 * Drive analysis of the Spring application to compute the configuration for the native-image build.
 * 
 * @author Andy Clement
 */
public class SpringAnalyzer {
	
	private static Log logger = LogFactory.getLog(SpringAnalyzer.class);	

	private final TypeSystem typeSystem;

	private ConfigurationCollector collector;

	private ReflectionHandler reflectionHandler;

	private DynamicProxiesHandler dynamicProxiesHandler;

	private ResourcesHandler resourcesHandler;

	private InitializationHandler initializationHandler;

	public SpringAnalyzer(TypeSystem typeSystem) {
		this.typeSystem = typeSystem;
	}

	public void analyze() {
		logger.debug("Spring analysis running");
		collector = new ConfigurationCollector();

		reflectionHandler = new ReflectionHandler(collector);
		dynamicProxiesHandler = new DynamicProxiesHandler(collector);
		initializationHandler = new InitializationHandler(collector);
		resourcesHandler = new ResourcesHandler(collector, reflectionHandler, dynamicProxiesHandler, initializationHandler);

		collector.setTypeSystem(typeSystem);
		// This cannot be done via other means because those other means attempt resolution to see if it is a valid name.
		// Whereas it may not be compiled yet
		collector.initializeAtBuildTime("org.springframework.aot.StaticSpringFactories");
		dynamicProxiesHandler.setTypeSystem(typeSystem);
		reflectionHandler.setTypeSystem(typeSystem);
		resourcesHandler.setTypeSystem(typeSystem);
		initializationHandler.setTypeSystem(typeSystem);

		ConfigOptions.ensureModeInitialized(typeSystem);
//		if (ConfigOptions.isAnnotationMode() || ConfigOptions.isAgentMode()) {
		reflectionHandler.register();
//		}
		resourcesHandler.register();
	}

	public ConfigurationCollector getConfigurationCollector() {
		return this.collector;
	}

}

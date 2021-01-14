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

import org.graalvm.nativeimage.hosted.Feature;
import org.springframework.nativex.type.TypeSystem;

/**
 * Drive analysis of the Spring application to compute the configuration for the native-image build.
 * This analyzer drives the same stages of the analysis that SpringFeature does, except this uses no
 * GraalVM dependencies.
 * 
 * @author Andy Clement
 */
public class SpringAnalyzer {

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
		System.out.println("Spring analysis running");
		collector = new ConfigurationCollector();

		reflectionHandler = new ReflectionHandler(collector);
		dynamicProxiesHandler = new DynamicProxiesHandler(collector);
		initializationHandler = new InitializationHandler(collector);
		resourcesHandler = new ResourcesHandler(collector, reflectionHandler, dynamicProxiesHandler, initializationHandler);

		collector.setTypeSystem(typeSystem);
		dynamicProxiesHandler.setTypeSystem(typeSystem);
		reflectionHandler.setTypeSystem(typeSystem);
		resourcesHandler.setTypeSystem(typeSystem);
		initializationHandler.setTypeSystem(typeSystem);

		ConfigOptions.ensureModeInitialized(typeSystem);
//		if (ConfigOptions.isAnnotationMode() || ConfigOptions.isAgentMode()) {
			reflectionHandler.register();
			dynamicProxiesHandler.register();
//		}


		initializationHandler.register();
		resourcesHandler.register();
	}

	public ConfigurationCollector getConfigurationCollector() {
		return this.collector;
	}

}

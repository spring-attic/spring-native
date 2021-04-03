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

package org.springframework.nativex.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.nativex.AotOptions;
import org.springframework.nativex.type.TypeSystem;

/**
 * Drive analysis of the Spring application to compute the configuration for the native-image build.
 * 
 * @author Andy Clement
 */
public class SpringAnalyzer {
	
	private static Log logger = LogFactory.getLog(SpringAnalyzer.class);	

	private final TypeSystem typeSystem;

	private final AotOptions aotOptions;

	private ConfigurationCollector collector;

	private ReflectionHandler reflectionHandler;

	private DynamicProxiesHandler dynamicProxiesHandler;

	private ResourcesHandler resourcesHandler;

	private InitializationHandler initializationHandler;

	private JNIReflectionHandler jniReflectionHandler;

	private SerializationHandler serializationHandler;

	private OptionHandler optionHandler;

	public SpringAnalyzer(TypeSystem typeSystem, AotOptions aotOptions) {
		this.typeSystem = typeSystem;
		this.aotOptions = aotOptions;
	}

	public void analyze() {
		logger.debug("Spring analysis running");
		collector = new ConfigurationCollector(aotOptions);

		reflectionHandler = new ReflectionHandler(collector, aotOptions);
		dynamicProxiesHandler = new DynamicProxiesHandler(collector);
		initializationHandler = new InitializationHandler(collector);
		optionHandler = new OptionHandler(collector);
		jniReflectionHandler = new JNIReflectionHandler(collector);
		serializationHandler = new SerializationHandler(collector);
		resourcesHandler = new ResourcesHandler(collector, reflectionHandler, 
				dynamicProxiesHandler, initializationHandler, serializationHandler, 
				jniReflectionHandler, optionHandler, aotOptions);

		collector.setTypeSystem(typeSystem);
		// This cannot be done via other means because those other means attempt resolution to see if it is a valid name.
		// Whereas it may not be compiled yet
		collector.initializeClassesAtBuildTime("org.springframework.aot.StaticSpringFactories");
		dynamicProxiesHandler.setTypeSystem(typeSystem);
		reflectionHandler.setTypeSystem(typeSystem);
		jniReflectionHandler.setTypeSystem(typeSystem);
		resourcesHandler.setTypeSystem(typeSystem);
		serializationHandler.setTypeSystem(typeSystem);
		initializationHandler.setTypeSystem(typeSystem);

		logger.info("Spring Native operating mode: " + aotOptions.toMode().toString());

		reflectionHandler.register();
		resourcesHandler.register();
	}

	public ConfigurationCollector getConfigurationCollector() {
		return this.collector;
	}

}

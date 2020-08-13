/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.graalvm.support;

import java.util.ArrayList;
import java.util.List;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.hosted.ResourcesFeature;
import com.oracle.svm.reflect.hosted.ReflectionFeature;
import com.oracle.svm.reflect.proxy.hosted.DynamicProxyFeature;
import org.graalvm.nativeimage.hosted.Feature;

@AutomaticFeature
public class SpringFeature implements Feature {

	private ReflectionHandler reflectionHandler;

	private DynamicProxiesHandler dynamicProxiesHandler;

	private ResourcesHandler resourcesHandler;

	private InitializationHandler initializationHandler;
	
	private final static String banner = 
		"\n" +
		".d88888b                    oo                       .88888.                             dP dP     dP 8888ba.88ba  \n" +
		"88.    \"'                                           d8'   `88                            88 88     88 88  `8b  `8b \n" + 
		"`Y88888b. 88d888b. 88d888b. dP 88d888b. .d8888b.    88        88d888b. .d8888b. .d8888b. 88 88    .8P 88   88   88 \n" + 
		"      `8b 88'  `88 88'  `88 88 88'  `88 88'  `88    88   YP88 88'  `88 88'  `88 88'  `88 88 88    d8' 88   88   88 \n" + 
		"d8'   .8P 88.  .88 88       88 88    88 88.  .88    Y8.   .88 88       88.  .88 88.  .88 88 88  .d8P  88   88   88 \n" + 
		" Y88888P  88Y888P' dP       dP dP    dP `8888P88     `88888'  dP       `88888P8 `88888P8 dP 888888'   dP   dP   dP \n" + 
		"          88                                 .88                                                                   \n" + 
		"          dP                             d8888P                                                                    \n" + 
		"                                 888888ba             dP   oo                                                      \n" + 
		"                                 88    `8b            88                                                           \n" + 
		"                                 88     88 .d8888b. d8888P dP dP   .dP .d8888b.                                    \n" + 
		"                                 88     88 88'  `88   88   88 88   d8' 88ooood8                                    \n" + 
		"                                 88     88 88.  .88   88   88 88 .88'  88.  ...                                    \n" + 
		"                                 dP     dP `88888P8   dP   dP 8888P'   `88888P'                                    \n" + 
		"                                                                                                                   \n" + 
		"                                                                                                                   ";

	public SpringFeature() {
		System.out.println(banner);
				                                                                                                                   
		if (!ConfigOptions.isVerbose()) {
			System.out.println("Use -Dspring.native.verbose=true on native-image call to see more detailed information from the feature");
		}
		reflectionHandler = new ReflectionHandler();
		dynamicProxiesHandler = new DynamicProxiesHandler();
		initializationHandler = new InitializationHandler();
		resourcesHandler = new ResourcesHandler(reflectionHandler, dynamicProxiesHandler,initializationHandler);
	}

	public boolean isInConfiguration(IsInConfigurationAccess access) {
		return true;
	}

	public List<Class<? extends Feature>> getRequiredFeatures() {
		List<Class<? extends Feature>> fs = new ArrayList<>();
		fs.add(DynamicProxyFeature.class); // Ensures DynamicProxyRegistry available
		fs.add(ResourcesFeature.class); // Ensures ResourcesRegistry available
		fs.add(ReflectionFeature.class); // Ensures RuntimeReflectionSupport available
		return fs;
	}

	public void duringSetup(DuringSetupAccess access) {
		if (ConfigOptions.isDefaultMode() || ConfigOptions.isHybridMode()) {
			reflectionHandler.register(access);
			dynamicProxiesHandler.register(access);
		}
		if (ConfigOptions.isFunctionalMode()) {
			reflectionHandler.registerFunctional(access);
		}
		if (ConfigOptions.isHybridMode()) {
			reflectionHandler.registerHybrid(access);
			dynamicProxiesHandler.registerHybrid(access);
		}
		if (ConfigOptions.isAgentMode()) {
			reflectionHandler.registerAgent(access);
		}
	}

	public void beforeAnalysis(BeforeAnalysisAccess access) {
		initializationHandler.register(access);
		resourcesHandler.register(access);
		if (ConfigOptions.isDefaultMode()) {
			System.out.println("Number of types dynamically registered for reflective access: #"+reflectionHandler.getTypesRegisteredForReflectiveAccessCount());
			reflectionHandler.dump();
		}
	}

	public static void log(String msg) {
		if (ConfigOptions.isVerbose()) {
			System.out.println(msg);
		}
	}
}

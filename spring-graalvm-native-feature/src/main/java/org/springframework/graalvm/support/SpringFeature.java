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
	"  ___          _              ___               ___   ____  __   _  _      _   _         \n"+
	" / __|_ __ _ _(_)_ _  __ _   / __|_ _ __ _ __ _| \\ \\ / /  \\/  | | \\| |__ _| |_(_)_ _____ \n"+
	" \\__ \\ '_ \\ '_| | ' \\/ _` | | (_ | '_/ _` / _` | |\\ V /| |\\/| | | .` / _` |  _| \\ V / -_)\n"+
	" |___/ .__/_| |_|_||_\\__, |  \\___|_| \\__,_\\__,_|_| \\_/ |_|  |_| |_|\\_\\__,_|\\__|_|\\_/\\___|\n"+
	"     |_|             |___/                                                          ";

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

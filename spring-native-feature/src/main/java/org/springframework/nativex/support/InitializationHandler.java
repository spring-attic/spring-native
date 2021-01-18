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
package org.springframework.nativex.support;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.init.InitializationJsonMarshaller;
import org.springframework.nativex.type.Type;

/**
 * 
 * @author Andy Clement
 */
public class InitializationHandler extends Handler {
	

	InitializationHandler(ConfigurationCollector collector) {
		super(collector);
	}

	public InitializationDescriptor compute() {
		try {
			InputStream s = this.getClass().getResourceAsStream("/initialization.json");
			return InitializationJsonMarshaller.read(s);
		} catch (Exception e) {
			return null;
		}
	}

	public void register() {
		InitializationDescriptor id = compute();
		System.out.println("Configuring initialization time for specific types and packages:");
		if (id == null) {
			throw new IllegalStateException("Unable to load initialization descriptor");
		}
		List<Type> typenames = id.getBuildtimeClasses().stream()
				.map(t -> ts.resolveDotted(t,true)/*access::findClassByName*/).filter(Objects::nonNull).collect(Collectors.toList());
		collector.initializeAtBuildTime(typenames);

		List<Type> collect = id.getRuntimeClasses().stream()
				.map(t->ts.resolveDotted(t,true)).filter(Objects::nonNull)
				.collect(Collectors.toList());
		collector.initializeAtRunTime(collect);
		
		// This cannot be done via other means because those other means attempt resolution to see if it is a valid name.
		if (ConfigOptions.isBuildTimeTransformation()) {
			collector.initializeAtBuildTime("org.springframework.nativex.buildtools.StaticSpringFactories");
		}

		SpringFeature.log("Registering these packages for buildtime initialization: \n"+id.getBuildtimePackages());
		collector.initializeAtBuildTimePackages(id.getBuildtimePackages().toArray(new String[] {}));
		SpringFeature.log("Registering these packages for runtime initialization: \n"+id.getRuntimePackages());
		collector.initializeAtRunTimePackages(id.getRuntimePackages().toArray(new String[] {}));

		if (ConfigOptions.isVerifierOn()) {
			for (Map.Entry<String, List<String>> e : ts.getSpringClassesMakingIsPresentChecks().entrySet()) {
				String k = e.getKey();
				if (!id.getBuildtimeClasses().contains(k)) {
					System.out.println("[verification] The type " + k
							+ " is making isPresent() calls in the static initializer, could be worth specifying build-time-initialization. "
							+ "It appears to be making isPresent() checks on " + e.getValue());
				}
			}
		}
	}

	public void registerInitializationDescriptor(InitializationDescriptor initializationDescriptor) {
		List<String> buildtimeClasses = initializationDescriptor.getBuildtimeClasses();
		if (buildtimeClasses.size()!=0) {
			buildtimeClasses.stream()
			.map(c -> ts.resolveDotted(c, true)/*cl.findClassByName(c, false)*/).filter(Objects::nonNull)
			.forEach(collector::initializeAtBuildTime);
		}
		List<String> runtimeClasses = initializationDescriptor.getRuntimeClasses();
		if (runtimeClasses.size()!=0) {
			runtimeClasses.stream()
			.map(c -> ts.resolveDotted(c,true)/*cl.findClassByName(c, false)*/).filter(Objects::nonNull)
			.forEach(collector::initializeAtRunTime);
		}
		List<String> buildtimePackages = initializationDescriptor.getBuildtimePackages();
		if (buildtimePackages.size()!=0) {
			collector.initializeAtBuildTimePackages(buildtimePackages.toArray(new String[0]));
		}
		List<String> runtimePackages = initializationDescriptor.getRuntimePackages();
		if (runtimePackages.size()!=0) {
			collector.initializeAtRunTimePackages(runtimePackages.toArray(new String[0]));
		}
	}
	
}

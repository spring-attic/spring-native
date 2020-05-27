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
package org.springframework.graalvm.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.springframework.graalvm.extension.ComponentProcessor;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.support.SpringFeature;

/**
 * @author Andy Clement
 */
public class SpringConfiguration {

	private TypeSystem typeSystem;

	private final static Map<String, List<CompilationHint>> proposedHints = new HashMap<>();
	
	private final static Map<String, String[]> proposedFactoryGuards = new HashMap<>();
	
	private final static List<ComponentProcessor> processors = new ArrayList<>();

	public SpringConfiguration(TypeSystem typeSystem) {
		this.typeSystem = typeSystem;
		SpringFeature.log("SpringConfiguration: Discovering hints");
		ServiceLoader<NativeImageConfiguration> hintProviders = ServiceLoader.load(NativeImageConfiguration.class);
		for (NativeImageConfiguration hintProvider: hintProviders) {
			SpringFeature.log("SpringConfiguration: processing provider: "+hintProvider.getClass().getName());
			Type t = typeSystem.resolveName(hintProvider.getClass().getName());
			if (t != null) {
				List<CompilationHint> hints = t.getCompilationHints();
				SpringFeature.log("Found "+hints.size()+" hints: "+hints);
				for (CompilationHint hint: hints) {
				  List<CompilationHint> existingHints = proposedHints.get(hint.getTargetType());
				  if (existingHints == null) {
					  existingHints = new ArrayList<>();
					  proposedHints.put(hint.getTargetType(), existingHints);
				  }
				  existingHints.add(hint);
				}
			}
		}
		SpringFeature.log("Discovering component processors...");
		ServiceLoader<ComponentProcessor> componentProcessors = ServiceLoader.load(ComponentProcessor.class);
		for (ComponentProcessor componentProcessor: componentProcessors) {
			SpringFeature.log("SpringConfiguration: processing component processor: "+componentProcessor.getClass().getName());
			processors.add(componentProcessor);
		}
	}

	static {
		// This specifies that the TemplateAvailabilityProvider key will only be processed if one of the
		// specified types is around. This ensures we don't provide reflective access to the value of this
		// key in apps that don't use mvc or webflux
		proposedFactoryGuards.put(
			"org.springframework.boot.autoconfigure.template.TemplateAvailabilityProvider",new String[] {
				"org.springframework.web.reactive.config.WebFluxConfigurer",
				"org.springframework.web.servlet.config.annotation.WebMvcConfigurer"
			});
	}
	
	// TODO sort out callers so they use a proper dotted name
	public List<CompilationHint> findProposedHints(String typename) {
		List<CompilationHint> results = proposedHints.get(typename);
		return (results==null?Collections.emptyList():results);
	}
	
	public static List<ComponentProcessor> getComponentProcessors() {
		return processors;
	}

	public static String[] findProposedFactoryGuards(String key) {
		return proposedFactoryGuards.get(key);
	}

}
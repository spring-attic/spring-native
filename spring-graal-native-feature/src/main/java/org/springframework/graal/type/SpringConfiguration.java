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
package org.springframework.graal.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.springframework.graal.extension.NativeImageConfiguration;

/**
 * @author Andy Clement
 */
public class SpringConfiguration {
    	
	private TypeSystem typeSystem;

	private final static Map<String, List<CompilationHint>> proposedHints = new HashMap<>();
	
	private final static Map<String, String[]> proposedFactoryGuards = new HashMap<>();

	public SpringConfiguration(TypeSystem typeSystem) {
		this.typeSystem = typeSystem;
		System.out.println("SpringConfiguration: Discovering hints");
		ServiceLoader<NativeImageConfiguration> hintProviders = ServiceLoader.load(NativeImageConfiguration.class);
		for (NativeImageConfiguration hintProvider: hintProviders) {
			System.out.println("SpringConfiguration: processing provider: "+hintProvider.getClass().getName());
			Type t = typeSystem.resolveName(hintProvider.getClass().getName());
			if (t != null) {
				List<CompilationHint> hints = t.getCompilationHints();
				System.out.println("Found "+hints.size()+" hints: "+hints);
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
		System.out.println("SpringConfiguration: Done");
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
	
	public List<CompilationHint> findProposedHints(String typename) {
		// TODO sort out callers so they use a proper dotted name
		if (typename.contains("/")) {
			if (typename.endsWith(";")) {
				typename= typename.substring(1,typename.length()-1).replace("/", ".");
			} else {
				typename= typename.replace("/", ".");
			}
		}
        List<CompilationHint> results = proposedHints.get(typename);     
//        System.out.println("Found these for "+typename+" #"+(results==null?0:results.size()));
        return (results==null?Collections.emptyList():results);
	}

	public static String[] findProposedFactoryGuards(String key) {
        return proposedFactoryGuards.get(key);
	}

}
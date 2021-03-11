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

package org.springframework.nativex.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Andy Clement
 */
public class SpringConfiguration {

	private static Log logger = LogFactory.getLog(SpringConfiguration.class);

	private final static Map<String, List<HintDeclaration>> proposedHints = new HashMap<>();
	
	private final static Map<String, String[]> proposedFactoryGuards = new HashMap<>();
	
	private final static List<AccessChecker> accessVerifiers = new ArrayList<>();
	
	private final static List<ComponentProcessor> processors = new ArrayList<>();

	private final static List<SpringFactoriesProcessor> springFactoriesProcessors = new ArrayList<>();
	
	public SpringConfiguration(TypeSystem typeSystem) {
		logger.debug("SpringConfiguration: Discovering hints");
		ServiceLoader<NativeConfiguration> hintProviders = ServiceLoader.load(NativeConfiguration.class);
		for (NativeConfiguration hintProvider: hintProviders) {
			logger.debug("SpringConfiguration: processing provider: "+hintProvider.getClass().getName());
			Type t = typeSystem.resolveName(hintProvider.getClass().getName());
			if (t != null) {
				boolean valid = hintProvider.isValid(typeSystem);
				if (!valid) {
					continue;
				}
				List<HintDeclaration> hints = new ArrayList<>();
				hints.addAll(t.getCompilationHints());
				try {
					hints.addAll(hintProvider.computeHints(typeSystem));
				} catch (NoClassDefFoundError ncdfe) {
					System.out.println("WARNING: Hint provider computeHints() method in "+
						hintProvider.getClass().getName()+" threw a NoClassDefFoundError for "+ncdfe.getMessage()+
						": it is better if they handle that internally in case they are computing a variety of hints");
				}
				logger.debug("Found "+hints.size()+" hints from provider "+hintProvider.getClass().getName());
				for (HintDeclaration hint: hints) {
					if (hint.getTriggerTypename() == null) {
						// Default to Object which means this hint always applies
						hint.setTriggerTypename("java.lang.Object");
					}
					List<HintDeclaration> existingHints = proposedHints.get(hint.getTriggerTypename());
					if (existingHints == null) {
						existingHints = new ArrayList<>();
						proposedHints.put(hint.getTriggerTypename(), existingHints);
					}
					existingHints.add(hint);
				}
			}
		}
		logger.debug("Discovering component processors...");
		ServiceLoader<ComponentProcessor> componentProcessors = ServiceLoader.load(ComponentProcessor.class);
		for (ComponentProcessor componentProcessor: componentProcessors) {
			logger.debug("SpringConfiguration: found component processor: "+componentProcessor.getClass().getName());
			processors.add(componentProcessor);
		}
		logger.debug("Discovering spring.factories processors...");
		ServiceLoader<SpringFactoriesProcessor> sfps = ServiceLoader.load(SpringFactoriesProcessor.class);
		for (SpringFactoriesProcessor springFactoryProcessor: sfps) {
			logger.debug("SpringConfiguration: found spring.factories processor: "+springFactoryProcessor.getClass().getName());
			springFactoriesProcessors.add(springFactoryProcessor);
		}
		logger.debug("Discovering access verifiers...");
		ServiceLoader<AccessChecker> avs = ServiceLoader.load(AccessChecker.class);
		for (AccessChecker av: avs) {
			logger.debug("SpringConfiguration: found access verifier: "+av.getClass().getName());
			accessVerifiers.add(av);
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
	public List<HintDeclaration> findProposedHints(String typename) {
		List<HintDeclaration> results = proposedHints.get(typename);
		return (results==null?Collections.emptyList():results);
	}
	
	public static Map<String, List<HintDeclaration>> getProposedhints() {
		return proposedHints;
	}
	
	public List<ComponentProcessor> getComponentProcessors() {
		return processors;
	}
	
	public List<AccessChecker> getAccessVerifiers() {
		return accessVerifiers;
	}
	
	public List<SpringFactoriesProcessor> getSpringFactoriesProcessors() {
		return springFactoriesProcessors;
	}

	public static String[] findProposedFactoryGuards(String key) {
		return proposedFactoryGuards.get(key);
	}

}
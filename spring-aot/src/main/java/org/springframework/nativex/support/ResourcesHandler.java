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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.nativex.AotOptions;
import org.springframework.nativex.Mode;
import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.domain.reflect.MethodDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.Type;

public class ResourcesHandler extends Handler {

	private static Log logger = LogFactory.getLog(ResourcesHandler.class);	

	private final ReflectionHandler reflectionHandler;

	private final DynamicProxiesHandler dynamicProxiesHandler;

	private final InitializationHandler initializationHandler;

	private SerializationHandler serializationHandler;

	private JNIReflectionHandler jniReflectionHandler;

	private final OptionHandler optionHandler;

	private final AotOptions aotOptions;

	public ResourcesHandler(ConfigurationCollector collector, ReflectionHandler reflectionHandler, 
			DynamicProxiesHandler dynamicProxiesHandler, InitializationHandler initializationHandler,
			SerializationHandler serializationHandler, JNIReflectionHandler jniReflectionHandler,
			OptionHandler optionHandler, AotOptions aotOptions) {

		super(collector);
		this.reflectionHandler = reflectionHandler;
		this.dynamicProxiesHandler = dynamicProxiesHandler;
		this.initializationHandler = initializationHandler;
		this.serializationHandler = serializationHandler;
		this.jniReflectionHandler = jniReflectionHandler;
		this.optionHandler = optionHandler;
		this.aotOptions = aotOptions;
	}

	/**
	 * Callback from native-image. Determine resources related to Spring applications that need to be added to the image.
	 */
	public void register() {
		handleConstantHints(aotOptions.toMode() == Mode.NATIVE_AGENT);
		if (aotOptions.toMode() == Mode.NATIVE) {
			handleSpringComponents();
		}
	}

	/**
	 * Some types need reflective access in every Spring Boot application. When hints are scanned
	 * these 'constants' are registered against the java.lang.Object key. Because they won't have been 
	 * registered in regular analysis, here we explicitly register those. Initialization hints are handled
	 * separately.
	 */
	private void handleConstantHints(boolean isAgentMode) {
		List<HintDeclaration> constantHints = ts.findActiveDefaultHints();
		logger.debug("> Registering fixed hints: " + constantHints);
		for (HintDeclaration ch : constantHints) {
			if (!isAgentMode) {
				Map<String, AccessDescriptor> dependantTypes = ch.getDependantTypes();
				for (Map.Entry<String, AccessDescriptor> dependantType : dependantTypes.entrySet()) {
					String typename = dependantType.getKey();
					AccessDescriptor ad = dependantType.getValue();
					logger.debug("  fixed type registered " + typename + " with " + ad);
					if (AccessBits.isResourceAccessRequired(ad.getAccessBits()) && !typename.contains("[]")) {
						org.springframework.nativex.type.ResourcesDescriptor resourcesDescriptor = org.springframework.nativex.type.ResourcesDescriptor.ofType(typename);
						registerResourcesDescriptor(resourcesDescriptor);
					}
					TypeAccess[] access = AccessBits.getAccess(ad.getAccessBits());
					List<org.springframework.nativex.type.MethodDescriptor> mds = ad.getMethodDescriptors();
					if (mds != null && mds.size() != 0 && AccessBits.isSet(ad.getAccessBits(),
							AccessBits.DECLARED_METHODS | AccessBits.PUBLIC_METHODS)) {
						logger.debug("  type has #" + mds.size()
								+ " members specified, removing typewide method access");
						access = filterAccess(access, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS);
					}
					List<org.springframework.nativex.type.MethodDescriptor> qmds = ad.getQueriedMethodDescriptors();
					if (qmds != null && qmds.size() != 0 && AccessBits.isSet(ad.getAccessBits(),
							AccessBits.QUERY_DECLARED_METHODS | AccessBits.QUERY_PUBLIC_METHODS)) {
						logger.debug("  type has #" + qmds.size()
								+ " queried members specified, removing typewide method access");
						access = filterAccess(access, TypeAccess.QUERY_DECLARED_METHODS, TypeAccess.QUERY_PUBLIC_METHODS);
					}
					List<FieldDescriptor> fds = ad.getFieldDescriptors();
					reflectionHandler.addAccess(typename, ch.getTriggerTypename(), MethodDescriptor.toStringArray(mds),
							MethodDescriptor.toStringArray(qmds), FieldDescriptor.toStringArray(fds), true, access);
				}
				for (Map.Entry<String, AccessDescriptor> dependantType : ch.getJNITypes().entrySet()) {
					String typename = dependantType.getKey();
					AccessDescriptor ad = dependantType.getValue();
					logger.debug("  fixed JNI access type registered " + typename + " with " + ad);
					List<org.springframework.nativex.type.MethodDescriptor> mds = ad.getMethodDescriptors();
					TypeAccess[] access = AccessBits.getAccess(ad.getAccessBits());
					if (mds != null && mds.size() != 0 && AccessBits.isSet(ad.getAccessBits(),
							AccessBits.DECLARED_METHODS | AccessBits.PUBLIC_METHODS)) {
						logger.debug("  type has #" + mds.size()
								+ " members specified, removing typewide method access");
						access = filterAccess(access, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS);
					}
					List<FieldDescriptor> fds = ad.getFieldDescriptors();
					jniReflectionHandler.addAccess(typename, MethodDescriptor.toStringArray(mds),
							FieldDescriptor.toStringArray(fds), true, access);
				}
				List<JdkProxyDescriptor> proxyDescriptors = ch.getProxyDescriptors();
				for (JdkProxyDescriptor pd : proxyDescriptors) {
					logger.debug("Registering proxy descriptor: " + pd);
					dynamicProxiesHandler.addProxy(pd);
				}
				Set<String> serializationTypes = ch.getSerializationTypes();
				if (!serializationTypes.isEmpty()) {
					logger.debug("Registering types as serializable: "+serializationTypes);
					for (String st: serializationTypes) {
						serializationHandler.addType(st);
					}
				}

				Set<String> serializationLambdaCapturingTypes = ch.getSerializationLambdaCapturingTypes();
				if (!serializationLambdaCapturingTypes.isEmpty()) {
					logger.debug("Registering lambda capturing types as serializable: "+serializationLambdaCapturingTypes);
					for (String st: serializationLambdaCapturingTypes) {
						serializationHandler.addLambdaCapturingType(st);
					}
				}
				List<org.springframework.nativex.type.ResourcesDescriptor> resourcesDescriptors = ch
						.getResourcesDescriptors();
				for (org.springframework.nativex.type.ResourcesDescriptor rd : resourcesDescriptors) {
					logger.debug("Registering resource descriptor: " + rd);
					registerResourcesDescriptor(rd);
				}
			}
			for (InitializationDescriptor initializationDescriptor : ch.getInitializationDescriptors()) {
				logger.debug("Registering initialization descriptor: " + initializationDescriptor);
				initializationHandler.registerInitializationDescriptor(initializationDescriptor);
			}
			if (!ch.getOptions().isEmpty()) {
				logger.debug("Registering options: "+ch.getOptions());
				optionHandler.addOptions(ch.getOptions());
			}
		}
		logger.debug("< Registering fixed hints");
	}
	
	public void registerResourcesDescriptor(org.springframework.nativex.type.ResourcesDescriptor rd) {
		String[] patterns = rd.getPatterns();
		for (String pattern: patterns) {
			collector.addResource(pattern,rd.isBundle());
			
		}	
	}

	/**
	 * Discover existing spring.components or synthesize one if none are found. If not running
	 * in hybrid mode then process the spring.components entries.
	 */
	public void handleSpringComponents() {
		Collection<byte[]> springComponents = ts.getResources("META-INF/spring.components");
		if (springComponents.size()==0) {
			logger.debug("Found no META-INF/spring.components -> synthesizing one...");
			synthesizeSpringComponents();
		}
	}

	private Properties synthesizeSpringComponents() {
		Properties p = new Properties();
		List<Entry<Type, List<Type>>> components = ts.scanForSpringComponents();
		List<Entry<Type, List<Type>>> filteredComponents = filterOutNestedConfigurationTypes(components);
		for (Entry<Type, List<Type>> filteredComponent : filteredComponents) {
			String k = filteredComponent.getKey().getDottedName();
			p.put(k, filteredComponent.getValue().stream().map(t -> t.getDottedName())
					.collect(Collectors.joining(",")));
		}
		logger.debug("Computed spring.components is ");
		logger.debug("vvv");
		for (Object k : p.keySet()) {
			logger.debug(k + "=" + p.getProperty((String) k));
		}
		logger.debug("^^^");
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			p.store(baos, "");
			baos.close();
			byte[] bs = baos.toByteArray();
			collector.registerResource("META-INF/spring.components", bs);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return p;
	}

	private List<Entry<Type, List<Type>>> filterOutNestedConfigurationTypes(List<Entry<Type, List<Type>>> indexedComponents) {
		List<Entry<Type, List<Type>>> filtered = new ArrayList<>();
		List<Entry<Type, List<Type>>> subtypesToRemove = new ArrayList<>();
		for (Entry<Type, List<Type>> indexedComponent : indexedComponents) {
			Type componentKey = indexedComponent.getKey();
			String type = componentKey.getDottedName();
			if (componentKey.isAtConfiguration()) {
				subtypesToRemove.addAll(indexedComponents.stream()
						.filter(e -> e.getKey().getDottedName().startsWith(type + "$")).collect(Collectors.toList()));
			}
		}
		filtered.addAll(indexedComponents);
		filtered.removeAll(subtypesToRemove);
		return filtered;
	}
	
	private TypeAccess[] filterAccess(TypeAccess[] accesses, TypeAccess... toFilter) {
		List<TypeAccess> ok = new ArrayList<>();
		for (TypeAccess access : accesses) {
			boolean skip  =false;
			for (TypeAccess f: toFilter) {
				if (f== access) {
					skip = true;
				}
			}
			if (!skip) {
				ok.add(access);
			}
		}
		return ok.toArray(new TypeAccess[0]);
	}

}

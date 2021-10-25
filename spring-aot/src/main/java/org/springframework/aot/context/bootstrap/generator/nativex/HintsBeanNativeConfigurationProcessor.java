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

package org.springframework.aot.context.bootstrap.generator.nativex;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ReflectionConfiguration;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeInitializationEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeResourcesEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeSerializationEntry;
import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.MethodDescriptor;
import org.springframework.nativex.type.ResourcesDescriptor;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.util.ClassUtils;

import static org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.InitializationConfiguration;
import static org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ProxyConfiguration;
import static org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ResourcesConfiguration;
import static org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.SerializationConfiguration;

/**
 * Add bean level hints to the generated native configuration.
 *
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
class HintsBeanNativeConfigurationProcessor implements BeanNativeConfigurationProcessor {

	private static final Log logger = LogFactory.getLog(HintsBeanNativeConfigurationProcessor.class);

	@Override
	public void process(BeanInstanceDescriptor descriptor, NativeConfigurationRegistry registry) {
		findAndRegisterRelevantNativeHints(descriptor.getUserBeanClass(), registry);
	}

	/**
	 * Lookup any native hints that have been declared with the declaring class of the
	 * instance creator as a trigger, then register information from those hints with the
	 * registry.
	 * @param beanType the bean type
	 * @param registry the registry into which hint info should be added
	 */
	private void findAndRegisterRelevantNativeHints(Class<?> beanType, NativeConfigurationRegistry registry) {
		ReflectionConfiguration reflectionConfiguration = registry.reflection();
		ResourcesConfiguration resourcesConfiguration = registry.resources();
		ProxyConfiguration proxyConfiguration = registry.proxy();
		InitializationConfiguration initializationConfiguration = registry.initialization();
		SerializationConfiguration serializationConfiguration = registry.serialization();
		ReflectionConfiguration jniConfiguration = registry.jni();

		List<HintDeclaration> hints = TypeSystem.getClassLoaderBasedTypeSystem().findHints(beanType.getName());
		if (hints != null) {
			for (HintDeclaration hint : hints) {
				try {
					// Types
					Map<String, AccessDescriptor> dependantTypes = hint.getDependantTypes();
					for (Map.Entry<String, AccessDescriptor> entry : dependantTypes.entrySet()) {
						if(!ClassUtils.isPresent(entry.getKey(), null)) {
							continue;
						}
						Class<?> keyClass = ClassUtils.forName(entry.getKey(), null);
						AccessDescriptor value = entry.getValue();
						Integer accessBits = value.getAccessBits();
						if (accessBits != 0) {
							reflectionConfiguration.forType(keyClass).withFlags(AccessBits.getFlags(accessBits));
						}
						if ((accessBits & AccessBits.RESOURCE) != 0) {
							if (keyClass.isArray()) {
								// This could be more strongly enforced if perfect hints are desired
								logger.debug("Skipping resource access specified in hint for array class "+keyClass);
							} else {
								registry.resources().add(NativeResourcesEntry.ofClass(keyClass));
							}
						}
						for (MethodDescriptor methodDescriptor : value.getMethodDescriptors()) {
							Executable method = methodDescriptor.findOnClass(keyClass);
							reflectionConfiguration.forType(keyClass).withExecutables(method);
						}
						for (MethodDescriptor methodDescriptor : value.getQueriedMethodDescriptors()) {
							Executable method = methodDescriptor.findOnClass(keyClass);
							reflectionConfiguration.forType(keyClass).withQueriedExecutables(method);
						}
						for (FieldDescriptor fieldDescriptor : value.getFieldDescriptors()) {
							Field field = keyClass.getDeclaredField(fieldDescriptor.getName());
							reflectionConfiguration.forType(keyClass).withFields(field);
						}
					}

					// Resources
					for (ResourcesDescriptor resourcesDescriptor : hint.getResourcesDescriptors()) {
						if (resourcesDescriptor.isBundle()) {
							for (String pattern : resourcesDescriptor.getPatterns()) {
								resourcesConfiguration.add(NativeResourcesEntry.ofBundle(pattern));
							}
						}
						else {
							for (String pattern : resourcesDescriptor.getPatterns()) {
								resourcesConfiguration.add(NativeResourcesEntry.of(pattern));
							}
						}
					}

					// Proxies
					for (JdkProxyDescriptor proxyDescriptor : hint.getProxyDescriptors()) {
						if (proxyDescriptor.isClassProxy()) {
							AotProxyDescriptor aotProxyDescriptor = (AotProxyDescriptor) proxyDescriptor;
							proxyConfiguration.add(NativeProxyEntry.ofClassName(
									aotProxyDescriptor.getTargetClassType(),
									aotProxyDescriptor.getProxyFeatures(),
									aotProxyDescriptor.getInterfaceTypes().toArray(String[]::new)));
						}
						else {
							proxyConfiguration.add(NativeProxyEntry.ofInterfaceNames(proxyDescriptor.getTypes().toArray(String[]::new)));
						}
					}

					// Initialization
					for (InitializationDescriptor initializationDescriptor : hint.getInitializationDescriptors()) {
						initializationDescriptor.getBuildtimeClasses().forEach(buildTimeClass -> {
							try {
								initializationConfiguration.add(NativeInitializationEntry.ofBuildTimeTypeName(buildTimeClass));
							} catch (IllegalArgumentException ex) {
								logger.debug("Type " + buildTimeClass + " not found in the classpath while processing build-time initialization hint hint with trigger" + beanType.getName());
							}
						});
						initializationDescriptor.getRuntimeClasses().forEach(runtimeClass -> {
							try {
								initializationConfiguration.add(NativeInitializationEntry.ofRuntimeTypeName(runtimeClass));
							} catch (IllegalArgumentException ex) {
								logger.debug("Type " + runtimeClass + " not found in the classpath while processing runtime initialization hint with trigger" + beanType.getName());
							}
						});
						initializationDescriptor.getBuildtimePackages().forEach(buildTimePackage ->
								initializationConfiguration.add(NativeInitializationEntry.ofBuildTimePackage(buildTimePackage)));
						initializationDescriptor.getRuntimePackages().forEach(runtimePackage ->
								initializationConfiguration.add(NativeInitializationEntry.ofRuntimePackage(runtimePackage)));
					}

					// native-image Options
					registry.options().addAll(hint.getOptions());

					// Serialization
					hint.getSerializationTypes().forEach(typeName -> serializationConfiguration.add(NativeSerializationEntry.ofTypeName(typeName)));

					// JNI
					Map<String, AccessDescriptor> jniTypes = hint.getJNITypes();
					for (Map.Entry<String, AccessDescriptor> entry : jniTypes.entrySet()) {
						Class<?> keyClass = ClassUtils.forName(entry.getKey(), null);
						AccessDescriptor value = entry.getValue();
						Integer accessBits = value.getAccessBits();
						if (accessBits != 0) {
							jniConfiguration.forType(keyClass).withFlags(AccessBits.getFlags(accessBits));
						}
						for (MethodDescriptor methodDescriptor : value.getMethodDescriptors()) {
							Executable method = methodDescriptor.findOnClass(keyClass);
							jniConfiguration.forType(keyClass).withExecutables(method);
						}
						for (FieldDescriptor fieldDescriptor : value.getFieldDescriptors()) {
							Field field = keyClass.getDeclaredField(fieldDescriptor.getName());
							jniConfiguration.forType(keyClass).withFields(field);
						}
					}
				}
				catch (Throwable t) {
					logger.error("Error while processing hint with trigger " + beanType.getName() + " : " +  t.getMessage());
				}
			}
		}
		if (beanType.getSuperclass() != null && beanType.getSuperclass() != Object.class) {
			findAndRegisterRelevantNativeHints(beanType.getSuperclass(), registry);
		}
	}
}

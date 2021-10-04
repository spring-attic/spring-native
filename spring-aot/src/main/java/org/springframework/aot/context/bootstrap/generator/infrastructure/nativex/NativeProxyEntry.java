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
package org.springframework.aot.context.bootstrap.generator.infrastructure.nativex;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Describe the need for proxy configuration.
 *
 * @author Sebastien Deleuze
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/DynamicProxy/">GraalVM native image dynamic proxy documentation</a>
 */
public class NativeProxyEntry {

	private final List<Class<?>> interfaceTypes;

	private final Class<?> targetClass;

	private final int proxyFeatures;

	private NativeProxyEntry(List<Class<?>> interfaceTypes) {
		this.interfaceTypes = interfaceTypes;
		this.targetClass = null;
		this.proxyFeatures = 0;
	}

	private NativeProxyEntry(Class<?> targetClass, List<Class<?>> interfaceTypes, int proxyFeatures) {
		this.targetClass = targetClass;
		this.interfaceTypes = interfaceTypes;
		this.proxyFeatures = proxyFeatures;
	}

	/**
	 * Create a new {@link NativeProxyEntry} for the specified interface types.
	 * @param types the ordered list of interface types defining the proxy
	 * @return a proxy entry
	 */
	public static NativeProxyEntry ofInterfaces(Class<?>... types) {
		Assert.notNull(types, "types must not be null");
		return new NativeProxyEntry(Arrays.asList(types));
	}

	/**
	 * Create a new {@link NativeProxyEntry} for the specified class type.
	 * @param classType the type of the class
	 * @param proxyFeatures the proxy features as defined in {@link ProxyBits}
	 * @param interfaceTypes the ordered list of interface types
	 * @return a proxy entry
	 */
	public static NativeProxyEntry ofClass(Class<?> classType, int proxyFeatures, Class<?>... interfaceTypes) {
		Assert.notNull(classType, "classType must not be null");
		Assert.notNull(interfaceTypes, "interfaceTypes must not be null");
		return new NativeProxyEntry(classType, Arrays.asList(interfaceTypes), proxyFeatures);
	}

	/**
	 * Create a new {@link NativeProxyEntry} for the specified interface type names.
	 * @param typeNames the ordered list of type names defining the proxy
	 * @return a proxy entry
	 */
	public static NativeProxyEntry ofInterfaceNames(String... typeNames) {
		Assert.notNull(typeNames, "typeNames must not be null");
		List<Class<?>> types = Arrays.asList(typeNames).stream()
				.map(typeName -> ClassUtils.resolveClassName(typeName, null))
				.collect(Collectors.toList());
		return new NativeProxyEntry(types);
	}

	/**
	 * Create a new {@link NativeProxyEntry} for the specified class name.
	 * @param className the type name of the class
	 * @param proxyFeatures the proxy features as defined in {@link ProxyBits}
	 * @param interfaceTypeNames the ordered list of interface type names
	 * @return a proxy entry
	 */
	public static NativeProxyEntry ofClassName(String className, int proxyFeatures, String... interfaceTypeNames) {
		Assert.notNull(className, "className must not be null");
		Assert.notNull(interfaceTypeNames, "interfaceTypeNames must not be null");
		List<Class<?>> interfaceTypes = Arrays.asList(interfaceTypeNames).stream()
				.map(typeName -> ClassUtils.resolveClassName(typeName, null))
				.collect(Collectors.toList());
		return new NativeProxyEntry(ClassUtils.resolveClassName(className, null), interfaceTypes, proxyFeatures);
	}

	public void contribute(ProxiesDescriptor descriptor) {
		List<String> interfaceTypesAsString = this.interfaceTypes.stream().map(Class::getName).collect(Collectors.toList());
		if (this.targetClass == null) {
			descriptor.add(new JdkProxyDescriptor(interfaceTypesAsString));
		}
		else
			descriptor.add(new AotProxyDescriptor(this.targetClass.getName(), interfaceTypesAsString, this.proxyFeatures));
	}

}

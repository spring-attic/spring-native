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

package org.springframework.context.bootstrap.generator.infrastructure.nativex;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;

/**
 * Collect the required native configuration, such as the need for
 * {@link NativeConfigurationRegistry#reflection() runtime reflection} or
 * {@link NativeConfigurationRegistry#resources() resources}.
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 */
public class NativeConfigurationRegistry {

	private final ReflectionConfiguration reflection = new ReflectionConfiguration();

	private final ResourcesConfiguration resources = new ResourcesConfiguration();

	private final ProxyConfiguration proxy = new ProxyConfiguration();

	private final InitializationConfiguration initialization = new InitializationConfiguration();

	private final Set<String> options = new LinkedHashSet<>();

	/**
	 * Access the reflection configuration of this registry.
	 * @return the reflection configuration
	 */
	public ReflectionConfiguration reflection() {
		return this.reflection;
	}

	/**
	 * Access the resources configuration of this registry.
	 * @return the resources configuration
	 */
	public ResourcesConfiguration resources() {
		return this.resources;
	}

	/**
	 * Access the proxy configuration of this registry.
	 * @return the proxy configuration
	 */
	public ProxyConfiguration proxy() {
		return this.proxy;
	}

	/**
	 * Access the initialization configuration of this registry.
	 * @return the initialization configuration
	 */
	public InitializationConfiguration initialization() {
		return this.initialization;
	}

	/**
	 * Access the native-image options of this registry.
	 * @return the proxy configuration
	 */
	public Set<String> options() {
		return this.options;
	}

	/**
	 * Configure the need for runtime reflection metadata on classes, methods, and fields.
	 */
	public static final class ReflectionConfiguration {

		private final Map<Class<?>, NativeReflectionEntry.Builder> reflection;

		private ReflectionConfiguration() {
			this.reflection = new LinkedHashMap<>();
		}

		/**
		 * Register the specified {@link Executable method or constructor}.
		 * @param executable the executable to register
		 * @return this for method chaining
		 */
		public ReflectionConfiguration addExecutable(Executable executable) {
			forType(executable.getDeclaringClass()).withMethods(executable);
			return this;
		}

		/**
		 * Register the specified {@link Field field}.
		 * @param field the field to register
		 * @return this for method chaining
		 */
		public ReflectionConfiguration addField(Field field) {
			forType(field.getDeclaringClass()).withFields(field);
			return this;
		}

		/**
		 * Return the {@link NativeReflectionEntry.Builder} to further describe the specified
		 * type.
		 * @param type a type to provide runtime reflection for
		 * @return a builder to further describe the need for runtime reflection
		 */
		public NativeReflectionEntry.Builder forType(Class<?> type) {
			return this.reflection.computeIfAbsent(type, NativeReflectionEntry.Builder::new);
		}

		/**
		 * Return the {@link NativeReflectionEntry entries} of this registry.
		 * @return the entries in the registry
		 */
		public List<NativeReflectionEntry> getEntries() {
			return this.reflection.values().stream().map(NativeReflectionEntry.Builder::build)
					.collect(Collectors.toList());
		}

		/**
		 * Return the {@link ClassDescriptor entries} of this registry.
		 * @return the classes entries in the registry, as {@link ClassDescriptor} instances
		 */
		public List<ClassDescriptor> toClassDescriptors() {
			return this.reflection.values().stream().map((builder) -> builder.build()
					.toClassDescriptor()).collect(Collectors.toList());
		}

	}

	/**
	 * Configure the needs for runtime resources.
	 */
	public static final class ResourcesConfiguration {

		private final Set<NativeResourcesEntry> resources;

		private ResourcesConfiguration() {
			this.resources = new LinkedHashSet<>();
		}

		/**
		 * Register the specified {@link NativeResourcesEntry resource}.
		 * @param resource the resource that should be available
		 * @return this for method chaining
		 */
		public ResourcesConfiguration add(NativeResourcesEntry resource) {
			this.resources.add(resource);
			return this;
		}

		/**
		 * Return the {@link ResourcesDescriptor} of this registry.
		 * @return the resources entries in the registry, as a {@link ResourcesDescriptor} instance
		 */
		public ResourcesDescriptor toResourcesDescriptor() {
			ResourcesDescriptor resourcesDescriptor = new ResourcesDescriptor();
			this.resources.forEach((resource) -> resource.contribute(resourcesDescriptor));
			return resourcesDescriptor;
		}

	}

	/**
	 * Configure the needs for runtime proxies.
	 */
	public static final class ProxyConfiguration {

		private final Set<NativeProxyEntry> proxies;

		private ProxyConfiguration() {
			this.proxies = new LinkedHashSet<>();
		}

		/**
		 * Register the specified {@link NativeProxyEntry proxy}.
		 * @param proxy the proxy that should be available
		 * @return this for method chaining
		 */
		public ProxyConfiguration add(NativeProxyEntry proxy) {
			this.proxies.add(proxy);
			return this;
		}

		/**
		 * Return the {@link ProxiesDescriptor} of this registry.
		 * @return the proxy entries in the registry, as a {@link ProxiesDescriptor} instance
		 */
		public ProxiesDescriptor toProxiesDescriptor() {
			ProxiesDescriptor proxiesDescriptor = new ProxiesDescriptor();
			this.proxies.forEach((proxy) -> proxy.contribute(proxiesDescriptor));
			return proxiesDescriptor;
		}

	}

	/**
	 * Configure the needs for class initialization.
	 */
	public static final class InitializationConfiguration {

		private final Set<NativeInitializationEntry> initialization;

		private InitializationConfiguration() {
			this.initialization = new LinkedHashSet<>();
		}

		/**
		 * Register the specified {@link InitializationConfiguration initialization}.
		 * @param initialization the initialization that should be configured
		 * @return this for method chaining
		 */
		public InitializationConfiguration add(NativeInitializationEntry initialization) {
			this.initialization.add(initialization);
			return this;
		}

		/**
		 * Return the {@link InitializationDescriptor} of this registry.
		 * @return the initialization entries in the registry, as a {@link InitializationDescriptor} instance
		 */
		public InitializationDescriptor toInitializationDescriptor() {
			InitializationDescriptor initializationDescriptor = new InitializationDescriptor();
			this.initialization.forEach((entry) -> entry.contribute(initializationDescriptor));
			return initializationDescriptor;
		}

	}

}

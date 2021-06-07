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

import java.util.List;
import java.util.Set;

import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.Flag;

/**
 * Allows plugins to the native image build process to participate in programmatic analysis of the application
 * and call back into the system to register proxies/reflective-access/etc.
 *
 * @author Andy Clement
 * @author Christoph Strobl
 */
public interface NativeContext {

	boolean addProxy(List<String> interfaces);

	boolean addProxy(String... interfaces);

	/**
	 * Add a {@link org.springframework.nativex.domain.proxies.AotProxyDescriptor} for the given type (dotted type name).
	 *
	 * @param dottedClassName the dotted class name (eg. com.example.ToBeProxied).
	 * @param interfaces must not be {@literal null}.
	 * @param proxyFeatures the feature {@link org.springframework.nativex.hint.ProxyBits bits}.
	 * @since 0.10
	 */
	default void addAotProxy(String dottedClassName, List<String> interfaces, int proxyFeatures) {
		addAotProxy(new AotProxyDescriptor(dottedClassName, interfaces, proxyFeatures));
	}

	/**
	 * Add the given {@link AotProxyDescriptor}.
	 *
	 * @param proxyDescriptor must not be {@literal null}.
	 * @since 0.10
	 */
	void addAotProxy(AotProxyDescriptor proxyDescriptor);

	TypeSystem getTypeSystem();

	default void addReflectiveAccess(Type type, Flag... flags) {
		addReflectiveAccess(type.getDottedName(), flags);
	}

	void addReflectiveAccess(String key, Flag... flags);

	/**
	 * Add full {@link AccessDescriptor} for the given type (dotted type name).
	 * This allows to specify {@link org.springframework.nativex.domain.reflect.MethodDescriptor method} and
	 * {@link org.springframework.nativex.domain.reflect.FieldDescriptor field} access configuration.
	 *
	 * @param typeName the dotted type name (eg. java.lang.String). Must not be {@literal null}.
	 * @param descriptor must not be {@literal null}.
	 */
	void addReflectiveAccess(String typeName, AccessDescriptor descriptor);

	/**
	 * Add full {@link AccessDescriptor} for the given {@link Type}.
	 * This allows to specify {@link org.springframework.nativex.domain.reflect.MethodDescriptor method} and
	 * {@link org.springframework.nativex.domain.reflect.FieldDescriptor field} access configuration.
	 *
	 * @param type must not be {@literal null}.
	 * @param descriptor must not be {@literal null}.
	 */
	default void addReflectiveAccess(Type type, AccessDescriptor descriptor) {
		addReflectiveAccess(type.getDottedName(), descriptor);
	}

	default Set<String> addReflectiveAccessHierarchy(Type type, int accessBits) {
		return addReflectiveAccessHierarchy(type.getDottedName(), accessBits);
	}

	Set<String> addReflectiveAccessHierarchy(String type, int accessBits);

	boolean hasReflectionConfigFor(String typename);

	void initializeAtBuildTime(Type type);

	default boolean hasReflectionConfigFor(Type type) {
		return hasReflectionConfigFor(type.getDottedName());
	}

	void log(String string);

	default void addReflectiveAccess(String parameterTypename, int accessBits) {
		addReflectiveAccess(parameterTypename, AccessBits.getFlags(accessBits));
	}

	// TODO Should probably be named addResource and provide a isBundle parameter
	void addResourceBundle(String string);
}

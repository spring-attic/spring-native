/*
 * Copyright 2002-2021 the original author or authors.
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Describe the need for proxy configuration.
 * TODO support AOT proxies
 *
 * @author Sebastien Deleuze
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/DynamicProxy/">GraalVM native image dynamic proxy documentation</a>
 */
public class NativeProxyEntry {

	private final List<Class<?>> types;

	private NativeProxyEntry(List<Class<?>> types) {
		this.types = types;
	}

	/**
	 * Create a new {@link NativeProxyEntry} for the specified types.
	 * @param types The ordered list of types defining the proxy
	 * @return a proxy entry
	 */
	public static NativeProxyEntry ofTypes(Class<?>... types) {
		Assert.notNull(types, "types must not be null");
		return new NativeProxyEntry(Arrays.asList(types));
	}

	/**
	 * Create a new {@link NativeProxyEntry} for the specified types.
	 * @param typeNames The ordered list of type names defining the proxy
	 * @return a proxy entry
	 */
	public static NativeProxyEntry ofTypeNames(String... typeNames) {
		Assert.notNull(typeNames, "typeNames must not be null");
		List<Class<?>> types = Arrays.asList(typeNames).stream()
				.map(typeName -> ClassUtils.resolveClassName(typeName, null))
				.collect(Collectors.toList());
		return new NativeProxyEntry(types);
	}

	public void contribute(ProxiesDescriptor descriptor) {
		List<String> typesAsString = this.types.stream().map(Class::getName).collect(Collectors.toList());
		descriptor.add(new JdkProxyDescriptor(typesAsString));
	}

}

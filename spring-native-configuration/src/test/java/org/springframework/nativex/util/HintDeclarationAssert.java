/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.nativex.util;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintDeclaration;

public class HintDeclarationAssert extends AbstractAssert<HintDeclarationAssert, HintDeclaration> {

	public HintDeclarationAssert(HintDeclaration hintDeclaration) {
		super(hintDeclaration, HintDeclarationAssert.class);
	}

	protected HintDeclarationAssert(HintDeclaration actual, Class selfType) {
		super(actual, selfType);
	}

	public HintDeclarationAssert containsResources(String... resourcePatterns) {

		isNotNull();
		Assertions.assertThat(actual.getResourcesDescriptors()).isNotEmpty();

		Assertions.assertThat(actual.getResourcesDescriptors().stream().flatMap(descriptor -> {
			return Arrays.stream(descriptor.getPatterns());
		})).contains(resourcePatterns);

		return this;
	}

	public HintDeclarationAssert doesNotContainResources() {

		Assertions.assertThat(actual.getResourcesDescriptors()).isNullOrEmpty();
		return this;
	}

	public HintDeclarationAssert containsProxyFor(Class<?> type) {

		if (!actual.getProxyDescriptors().stream().map(JdkProxyDescriptor::getTypes)
				.anyMatch(types -> type.getName().equals(types.get(0)))) {

			failWithMessage("Expected HintDeclaration to contain proxy config for %s but was not in %s",
					type.getName(), actual.getProxyDescriptors());
		}
		return this;
	}

	public HintDeclarationAssert containsProxy(Class<?>... interfaces) {
		return containsProxy(JdkProxyDescriptor.of(Arrays.stream(interfaces).map(Class::getName).collect(Collectors.toList())));
	}

	public HintDeclarationAssert containsProxy(JdkProxyDescriptor proxyDescriptor) {

		Assertions.assertThat(actual.getProxyDescriptors()).contains(proxyDescriptor);
		return this;
	}

	public HintDeclarationAssert doesNotContainsProxies() {

		Assertions.assertThat(actual.getProxyDescriptors()).isNullOrEmpty();
		return this;
	}

	public HintDeclarationAssert containsDependantType(Class<?> type, AccessBits accessBits) {
		return containsDependantType(type.getName(), new AccessDescriptor(accessBits.getValue()));
	}

	public HintDeclarationAssert containsDependantType(Class<?> type, AccessDescriptor accessDescriptor) {
		return containsDependantType(type.getName(), accessDescriptor);
	}

	public HintDeclarationAssert containsDependantType(String typeName, AccessDescriptor accessDescriptor) {

		isNotNull();
		Assertions.assertThat(actual.getDependantTypes()).containsEntry(typeName, accessDescriptor);
		return this;
	}
}

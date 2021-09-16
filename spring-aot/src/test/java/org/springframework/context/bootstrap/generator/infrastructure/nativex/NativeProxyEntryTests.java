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

import org.junit.jupiter.api.Test;

import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link NativeProxyEntry}.
 *
 * @author Sebastien Deleuze
 */
public class NativeProxyEntryTests {

	@Test
	void ofTypesWithNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> NativeProxyEntry.ofTypes(null));
	}

	@Test
	void contributeTypes() {
		ProxiesDescriptor proxiesDescriptor = new ProxiesDescriptor();
		NativeProxyEntry.ofTypes(SpringProxy.class, Advised.class).contribute(proxiesDescriptor);
		assertThat(proxiesDescriptor.getProxyDescriptors()).singleElement()
				.satisfies((proxyDescriptor) -> {
					assertThat(proxyDescriptor.getTypes()).containsExactly(SpringProxy.class.getName(), Advised.class.getName());
					assertThat(proxyDescriptor.isClassProxy()).isFalse();
				});
	}

	@Test
	void contributeTypeNames() {
		ProxiesDescriptor proxiesDescriptor = new ProxiesDescriptor();
		NativeProxyEntry.ofTypeNames(SpringProxy.class.getName(), Advised.class.getName()).contribute(proxiesDescriptor);
		assertThat(proxiesDescriptor.getProxyDescriptors()).singleElement()
				.satisfies((proxyDescriptor) -> {
					assertThat(proxyDescriptor.getTypes()).containsExactly(SpringProxy.class.getName(), Advised.class.getName());
					assertThat(proxyDescriptor.isClassProxy()).isFalse();
				});
	}
}

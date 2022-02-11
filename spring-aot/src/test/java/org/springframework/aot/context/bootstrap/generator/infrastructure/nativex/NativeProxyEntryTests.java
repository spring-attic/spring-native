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

import org.junit.jupiter.api.Test;

import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.hint.ProxyBits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link NativeProxyEntry}.
 *
 * @author Sebastien Deleuze
 */
public class NativeProxyEntryTests {

	@Test
	void ofInterfacesWithNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> NativeProxyEntry.ofInterfaces(null));
	}

	@Test
	void contributeInterfaces() {
		ProxiesDescriptor proxiesDescriptor = new ProxiesDescriptor();
		NativeProxyEntry.ofInterfaces(SpringProxy.class, Advised.class).contribute(proxiesDescriptor);
		assertThat(proxiesDescriptor.getProxyDescriptors()).singleElement()
				.satisfies((proxyDescriptor) -> {
					assertThat(proxyDescriptor.getTypes()).containsExactly(SpringProxy.class.getName(), Advised.class.getName());
					assertThat(proxyDescriptor.isClassProxy()).isFalse();
				});
	}

	@Test
	void contributeInterfaceNames() {
		ProxiesDescriptor proxiesDescriptor = new ProxiesDescriptor();
		NativeProxyEntry.ofInterfaceNames(SpringProxy.class.getName(), Advised.class.getName()).contribute(proxiesDescriptor);
		assertThat(proxiesDescriptor.getProxyDescriptors()).singleElement()
				.satisfies((proxyDescriptor) -> {
					assertThat(proxyDescriptor.getTypes()).containsExactly(SpringProxy.class.getName(), Advised.class.getName());
					assertThat(proxyDescriptor.isClassProxy()).isFalse();
				});
	}

	@Test
	void ofClassWithNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> NativeProxyEntry.ofClass(null, ProxyBits.NONE, null));
		assertThatIllegalArgumentException().isThrownBy(() -> NativeProxyEntry.ofClass(null, ProxyBits.NONE, Comparable.class));
		assertThatIllegalArgumentException().isThrownBy(() -> NativeProxyEntry.ofClass(String.class, ProxyBits.NONE, null));
	}

	@Test
	void contributeClass() {
		ProxiesDescriptor proxiesDescriptor = new ProxiesDescriptor();
		NativeProxyEntry.ofClass(NativeProxyEntryTests.class, ProxyBits.IS_STATIC, SpringProxy.class, Advised.class).contribute(proxiesDescriptor);
		assertThat(proxiesDescriptor.getProxyDescriptors()).singleElement()
				.satisfies((proxyDescriptor) -> {
					assertThat(proxyDescriptor.isClassProxy()).isTrue();
					assertThat(proxyDescriptor).isInstanceOf(AotProxyDescriptor.class);
					assertThat(((AotProxyDescriptor) proxyDescriptor).getInterfaceTypes()).containsExactly(SpringProxy.class.getName(), Advised.class.getName());
					assertThat(((AotProxyDescriptor) proxyDescriptor).getTargetClassType()).isEqualTo(NativeProxyEntryTests.class.getName());
					assertThat(((AotProxyDescriptor) proxyDescriptor).getProxyFeatures()).isEqualTo(ProxyBits.IS_STATIC);
				});
	}

	@Test
	void contributeClassNames() {
		ProxiesDescriptor proxiesDescriptor = new ProxiesDescriptor();
		NativeProxyEntry.ofClassName(NativeProxyEntryTests.class.getName(), ProxyBits.IS_STATIC, SpringProxy.class.getName(), Advised.class.getName()).contribute(proxiesDescriptor);
		assertThat(proxiesDescriptor.getProxyDescriptors()).singleElement()
				.satisfies((proxyDescriptor) -> {
					assertThat(proxyDescriptor.isClassProxy()).isTrue();
					assertThat(proxyDescriptor).isInstanceOf(AotProxyDescriptor.class);
					assertThat(((AotProxyDescriptor) proxyDescriptor).getInterfaceTypes()).containsExactly(SpringProxy.class.getName(), Advised.class.getName());
					assertThat(((AotProxyDescriptor) proxyDescriptor).getTargetClassType()).isEqualTo(NativeProxyEntryTests.class.getName());
					assertThat(((AotProxyDescriptor) proxyDescriptor).getProxyFeatures()).isEqualTo(ProxyBits.IS_STATIC);
				});
	}
}

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

package org.springframework.nativex.domain.proxies;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md
 * 
 * @author Andy Clement
 */
public class ProxiesDescriptor {

	private final List<JdkProxyDescriptor> proxyDescriptors;

	public ProxiesDescriptor() {
		this.proxyDescriptors = new ArrayList<>();
	}

	public ProxiesDescriptor(ProxiesDescriptor metadata) {
		this.proxyDescriptors = new ArrayList<>(metadata.proxyDescriptors);
	}

	public List<JdkProxyDescriptor> getProxyDescriptors() {
		return this.proxyDescriptors;
	}

	public void add(JdkProxyDescriptor proxyDescriptor) {
		boolean alreadyExists = false;
		for (JdkProxyDescriptor pd: proxyDescriptors) {
			if (pd.equals(proxyDescriptor)) {
				alreadyExists = true;
			}
		}
		if (!alreadyExists) {
			this.proxyDescriptors.add(proxyDescriptor);
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(String.format("ProxyDescriptors #%s\n",proxyDescriptors.size()));
		this.proxyDescriptors.forEach(cd -> {
			result.append(String.format("%s: \n",cd));
		});
		return result.toString();
	}

	public boolean isEmpty() {
		return proxyDescriptors.isEmpty();
	}

	public static ProxiesDescriptor of(String jsonString) {
		try {
			return ProxiesDescriptorJsonMarshaller.read(jsonString);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to read json:\n"+jsonString, e);
		}
	}

	public void consume(Consumer<List<String>> consumer) {
		proxyDescriptors.stream().forEach(pd -> consumer.accept(pd.getTypes()));
	}

	public void merge(ProxiesDescriptor otherProxyDescriptor) {
		proxyDescriptors.addAll(otherProxyDescriptor.getProxyDescriptors());
	}

	public static ProxiesDescriptor fromJSON(String jsonString) {
		return ProxiesDescriptorJsonMarshaller.read(jsonString);
	}

	public String toJSON() {
		return ProxiesDescriptorJsonMarshaller.write(this);
	}

}

/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.graalvm.domain.proxies;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md
 * 
 * @author Andy Clement
 */
public class ProxiesDescriptor {

	private final List<ProxyDescriptor> proxyDescriptors;

	public ProxiesDescriptor() {
		this.proxyDescriptors = new ArrayList<>();
	}

	public ProxiesDescriptor(ProxiesDescriptor metadata) {
		this.proxyDescriptors = new ArrayList<>(metadata.proxyDescriptors);
	}

	public List<ProxyDescriptor> getProxyDescriptors() {
		return this.proxyDescriptors;
	}

	public void add(ProxyDescriptor proxyDescriptor) {
		this.proxyDescriptors.add(proxyDescriptor);
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
		proxyDescriptors.stream().forEach(pd -> consumer.accept(pd.getInterfaces()));
	}

//	public boolean hasClassDescriptor(String string) {
//		for (ProxyDescriptor cd: classDescriptors) {
//			if (cd.getName().equals(string)) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	public ProxyDescriptor getClassDescriptor(String type) {
//		for (ProxyDescriptor cd: classDescriptors) {
//			if (cd.getName().equals(type)) {
//				return cd;
//			}
//		}
//		return null;
//	}

}

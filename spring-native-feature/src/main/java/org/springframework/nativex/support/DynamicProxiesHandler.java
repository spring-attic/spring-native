/*
 * Copyright 2019-2020 the original author or authors.
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
package org.springframework.nativex.support;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptorJsonMarshaller;
import org.springframework.nativex.domain.proxies.ProxyDescriptor;

/**
 * 
 * @author Andy Clement
 */
public class DynamicProxiesHandler extends Handler {

	public DynamicProxiesHandler(ConfigurationCollector collector) {
		super(collector);
	}

	public boolean addProxy(org.springframework.nativex.type.ProxyDescriptor pd) {
		return addProxy(Arrays.asList(pd.getTypes()));
	}

	public boolean addProxy(List<String> interfaceNames) {
		return collector.addProxy(interfaceNames, true);
	}
}

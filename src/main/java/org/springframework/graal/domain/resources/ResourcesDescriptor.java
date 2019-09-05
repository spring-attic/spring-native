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
package org.springframework.graal.domain.resources;

import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/oracle/graal/blob/master/substratevm/RESOURCES.md
 * 
 * @author Andy Clement
 */
public class ResourcesDescriptor {

	private final List<String> patterns;

	public ResourcesDescriptor() {
		this.patterns = new ArrayList<>();
	}

	public ResourcesDescriptor(ResourcesDescriptor metadata) {
		this.patterns = new ArrayList<>(metadata.patterns);
	}

	public List<String> getPatterns() {
		return this.patterns;
	}

	public void add(String pattern) {
		this.patterns.add(pattern);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(String.format("ResourcesDescriptors #%s\n",patterns.size()));
		this.patterns.forEach(cd -> {
			result.append(String.format("%s: \n",cd));
		});
		return result.toString();
	}

	public boolean isEmpty() {
		return patterns.isEmpty();
	}

	public static ResourcesDescriptor of(String jsonString) {
		try {
			return ResourcesJsonMarshaller.read(jsonString);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to read json:\n"+jsonString, e);
		}
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

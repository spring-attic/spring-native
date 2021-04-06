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

package org.springframework.nativex.domain.resources;

import java.util.Set;
import java.util.TreeSet;

/**
 * https://github.com/oracle/graal/blob/master/substratevm/RESOURCES.md
 *
 * @author Andy Clement
 */
public class ResourcesDescriptor {

	private final Set<String> patterns;
	private final Set<String> bundles;

	public ResourcesDescriptor() {
		this.patterns = new TreeSet<>();
		this.bundles = new TreeSet<>();
	}

	public ResourcesDescriptor(ResourcesDescriptor metadata) {
		this.patterns = new TreeSet<>(metadata.patterns);
		this.bundles = new TreeSet<>(metadata.bundles);
	}

	public Set<String> getPatterns() {
		return this.patterns;
	}

	public void add(String pattern) {
		this.patterns.add(pattern);
	}

	public Set<String> getBundles() {
		return this.bundles;
	}

	public void addBundle(String name) {
		this.bundles.add(name);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(String.format("ResourcesDescriptors Resources: #%s\n",patterns.size()));
		this.patterns.forEach(cd -> result.append(String.format("%s: \n",cd)));
		result.append(String.format("ResourcesDescriptors Bundles: #%s\n",bundles.size()));
		this.bundles.forEach(cd -> result.append(String.format("%s: \n",cd)));
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

	public static ResourcesDescriptor ofBundle(String bundleName) {
		ResourcesDescriptor rd = new ResourcesDescriptor();
		rd.addBundle(bundleName);
		return rd;
	}

	public void merge(ResourcesDescriptor resourcesDescriptor) {
		Set<String> patterns = resourcesDescriptor.getPatterns();
		Set<String> bundles = resourcesDescriptor.getBundles();
		this.patterns.addAll(patterns);
		this.bundles.addAll(bundles);
	}

	public static ResourcesDescriptor fromJSON(String jsonString) {
		return ResourcesJsonMarshaller.read(jsonString);
	}

	public String toJSON() {
		return ResourcesJsonMarshaller.write(this);
	}
}

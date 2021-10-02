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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Describes a proxy via a set of types it should implement.
 *
 * @author Andy Clement
 * @author Ariel Carrera
 */
public class JdkProxyDescriptor {

	protected Set<String> types; // e.g. java.io.Serializable

	JdkProxyDescriptor() {
	}

	public JdkProxyDescriptor(Collection<String> types) {
		this.types = new LinkedHashSet<>(types);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		JdkProxyDescriptor o = (JdkProxyDescriptor) other;
		Set<String> l = this.types;
		Set<String> r = o.types;
		if (l.size() != r.size()) {
			return false;
		}
		if (l.isEmpty()) {
			return true;
		}
		Iterator<String> iterator1 = l.iterator();
		Iterator<String> iterator2 = r.iterator();
		while (iterator1.hasNext() && iterator2.hasNext()) {
			boolean matchingInterfaces = iterator1.next().equals(iterator2.next());
			if (!matchingInterfaces) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = 17;
		Iterator<String> iterator = types.iterator();
		while (iterator.hasNext()) {
			result = result+iterator.next().hashCode()*37;
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		buildToStringProperty(string, "interfaces", this.types);
		return string.toString();
	}

	protected void buildToStringProperty(StringBuilder string, String property, Object value) {
		if (value != null) {
			string.append(" ").append(property).append(":").append(value);
		}
	}

	public static JdkProxyDescriptor of(Collection<String> interfaces) {
		JdkProxyDescriptor pd = new JdkProxyDescriptor();
		pd.setInterfaces(interfaces);
		return pd;
	}

	public void setInterfaces(Collection<String> interfaces) {
		this.types = new LinkedHashSet<>(interfaces);
	}

	public boolean containsInterface(String intface) {
		return types.contains(intface);
	}

	public Set<String> getTypes() {
		return types;
	}

	public boolean isClassProxy() {
		return false;
	}

}

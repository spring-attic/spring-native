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
public class JdkProxyDescriptor implements Comparable<JdkProxyDescriptor> {

	protected Set<String> types; // e.g. java.io.Serializable

	JdkProxyDescriptor() {
	}

	public JdkProxyDescriptor(Collection<String> types) {
		this.types = new LinkedHashSet<>(types);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JdkProxyDescriptor other = (JdkProxyDescriptor) o;
		boolean result = true;
		result = result && nullSafeEquals(this.types, other.types);
		return result;
	}

	@Override
	public int hashCode() {
		int result = nullSafeHashCode(this.types);
		return result;
	}

	private boolean nullSafeEquals(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		return o1.equals(o2);
	}

	private int nullSafeHashCode(Object o) {
		return (o != null) ? o.hashCode() : 0;
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

	@Override
	public int compareTo(JdkProxyDescriptor o) {
		Set<String> l = this.types;
		Set<String> r = o.types;
		if (l.size() != r.size()) {
			return l.size() - r.size();
		}
		if (l.isEmpty()) return 0;
		
		Iterator<String> iterator1 = l.iterator();
		Iterator<String> iterator2 = r.iterator();
		while (iterator1.hasNext() && iterator2.hasNext()) {
			int value = iterator1.next().compareTo(iterator2.next());
			if (value != 0)
				return value;
		}
		return 0; // equal!
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

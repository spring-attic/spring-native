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

/**
 * Reflection information about a single class.
 *
 * @author Andy Clement
 * @see ReflectionDescriptor
 */
public final class ProxyDescriptor implements Comparable<ProxyDescriptor> {

	private List<String> interfaces; // e.g. java.io.Serializable

	ProxyDescriptor() {
	}

	ProxyDescriptor(List<String> interfaces) {
		this.interfaces = interfaces;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProxyDescriptor other = (ProxyDescriptor) o;
		boolean result = true;
		result = result && nullSafeEquals(this.interfaces, other.interfaces);
		return result;
	}

	@Override
	public int hashCode() {
		int result = nullSafeHashCode(this.interfaces);
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
		buildToStringProperty(string, "interfaces", this.interfaces);
		return string.toString();
	}

	protected void buildToStringProperty(StringBuilder string, String property, Object value) {
		if (value != null) {
			string.append(" ").append(property).append(":").append(value);
		}
	}

	@Override
	public int compareTo(ProxyDescriptor o) {
		List<String> l = this.interfaces;
		List<String> r = o.interfaces;
		if (l.size() != r.size()) {
			return l.size() - r.size();
		}
		for (int i = 0; i < l.size(); i++) {
			int cmpTo = l.get(i).compareTo(r.get(i));
			if (cmpTo != 0) {
				return cmpTo;
			}
		}
		return 0; // equal!
	}

	public static ProxyDescriptor of(List<String> interfaces) {
		ProxyDescriptor pd = new ProxyDescriptor();
		pd.setInterfaces(interfaces);
		return pd;
	}

	public List<String> getInterfaces() {
		return this.interfaces;
	}

	public void setInterfaces(List<String> interfaces) {
		this.interfaces = new ArrayList<>();
		this.interfaces.addAll(interfaces);
	}

	/**
	 * Used when new data is to be added to an already existing class descriptor
	 * (additional members, flag settings).
	 * 
	 * @param cd the ClassDescriptor to merge into this one
	 */
//	public void merge(ProxyDescriptor cd) {
//		for (Flag flag : cd.getFlags()) {
//			this.setFlag(flag);
//		}
//		for (FieldDescriptor fd : cd.getFields()) {
//			FieldDescriptor existingSimilarOne = null;
//			for (FieldDescriptor existingFd: getFields()) {
//				if (existingFd.getName().equals(fd.getName())) {
//					existingSimilarOne = existingFd;
//					break;
//				}
//			}
//			if (existingSimilarOne != null) {
//				if (fd.isAllowWrite()) {
//					existingSimilarOne.setAllowWrite(true);
//				}
//			} else {
//				addFieldDescriptor(fd);
//			}
//		}
//		for (MethodDescriptor methodDescriptor : cd.getMethods()) {
//			if (!containsMethodDescriptor(methodDescriptor)) {
//				addMethodDescriptor(methodDescriptor);
//			}
//		}
//	}

	public boolean containsInterface(String intface) {
		return interfaces.contains(intface);
	}

}

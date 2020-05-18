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

package org.springframework.graalvm.domain.reflect;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Andy Clement
 * @see ReflectionDescriptor
 */
public final class MethodDescriptor extends MemberDescriptor implements Comparable<MethodDescriptor> {

	public final static String CONSTRUCTOR_NAME = "<init>";
	
	public final static List<String> NO_PARAMS = Collections.emptyList();
	
	private List<String> parameterTypes; // e.g. char[], java.lang.String, java.lang.Object[]

	MethodDescriptor() {
	}
	
	MethodDescriptor(String name, List<String> parameterTypes) {
		super(name);
		this.parameterTypes = parameterTypes;
	}

	public List<String> getParameterTypes() {
		return this.parameterTypes;
	}
	
	public void setParameterTypes(List<String> parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MethodDescriptor other = (MethodDescriptor) o;
		boolean result = true;
		result = result && nullSafeEquals(this.name, other.name);
		result = result && nullSafeEquals(this.parameterTypes, other.parameterTypes);
		return result;
	}

	@Override
	public int hashCode() {
		int result = nullSafeHashCode(this.name);
		result = 31 * result + nullSafeHashCode(this.parameterTypes);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder string = new StringBuilder(this.name);
		buildToStringProperty(string, "name", this.name);
		buildToStringProperty(string, "parameterTypes", this.parameterTypes);
		return string.toString();
	}

	@Override
	public int compareTo(MethodDescriptor o) {
		return getName().compareTo(o.getName());
	}
	
	public static MethodDescriptor of(String name, String... parameterTypes) {
		MethodDescriptor md = new MethodDescriptor();
		md.setName(name);
		if (parameterTypes != null) {
			md.setParameterTypes(Arrays.asList(parameterTypes));
		} else {
			md.setParameterTypes(NO_PARAMS);
		}
		return md;
	}
	
}

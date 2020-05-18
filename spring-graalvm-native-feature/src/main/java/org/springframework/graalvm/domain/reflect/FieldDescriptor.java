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

/**
 *
 * @author Andy Clement
 * @see ReflectionDescriptor
 */
public final class FieldDescriptor extends MemberDescriptor implements Comparable<FieldDescriptor> {

	private boolean allowWrite = false;
	
	private boolean allowUnsafeAccess = false;

	FieldDescriptor(String name, boolean allowWrite, boolean allowUnsafeAccess) {
		super(name);
		this.allowWrite = allowWrite;
		this.allowUnsafeAccess = allowUnsafeAccess;
	}

	public boolean isAllowWrite() {
		return this.allowWrite;
	}
	
	public boolean isAllowUnsafeAccess() {
		return this.allowUnsafeAccess;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FieldDescriptor other = (FieldDescriptor) o;
		boolean result = true;
		result = result && nullSafeEquals(this.name, other.name);
		result = result && nullSafeEquals(this.allowWrite, other.allowWrite);
		result = result && nullSafeEquals(this.allowUnsafeAccess, other.allowUnsafeAccess);
		return result;
	}

	@Override
	public int hashCode() {
		int result = nullSafeHashCode(this.name);
		result = 31 * result + nullSafeHashCode(this.allowWrite);
		result = 31 * result + nullSafeHashCode(this.allowUnsafeAccess);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder string = new StringBuilder(this.name);
		buildToStringProperty(string, "name", this.name);
		buildToStringProperty(string, "allowWrite", this.allowWrite);
		buildToStringProperty(string, "allowUnsafeAccess", this.allowUnsafeAccess);
		return string.toString();
	}

	@Override
	public int compareTo(FieldDescriptor o) {
		return getName().compareTo(o.getName());
	}

	public void setAllowWrite(boolean b) {
		this.allowWrite = b;
	}
	
	public void setAllowUnsafeAccess(boolean b) {
		this.allowUnsafeAccess = b;
	}
	
}

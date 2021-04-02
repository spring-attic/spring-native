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

package org.springframework.nativex.domain.reflect;

import java.util.List;

/**
 *
 * @author Andy Clement
 */
public final class FieldDescriptor extends MemberDescriptor {

	private boolean allowWrite = false;

	private boolean allowUnsafeAccess = false;

	public FieldDescriptor(String name, boolean allowWrite, boolean allowUnsafeAccess) {
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

	public void setAllowWrite(boolean b) {
		this.allowWrite = b;
	}
	
	public void setAllowUnsafeAccess(boolean b) {
		this.allowUnsafeAccess = b;
	}

	public static FieldDescriptor of(String name, boolean allowWrite, boolean allowUnsafeAccess) {
		return new FieldDescriptor(name, allowWrite, allowUnsafeAccess);
	}

	public FieldDescriptor copy() {
		return new FieldDescriptor(name, allowWrite, allowUnsafeAccess);
	}

	public void merge(FieldDescriptor fd) {
		if (!this.getName().equals(fd.getName())) {
			throw new IllegalStateException("Attempt to merge a field descriptor for a field named "+this.getName()+" with a field descriptor for a field name "+fd.getName());
		}
		if (fd.allowUnsafeAccess) {
			setAllowUnsafeAccess(true);
		}
		if (fd.allowWrite) {
			setAllowWrite(true);
		}
	}

	public static String[][] toStringArray(List<FieldDescriptor> fds) {
		if (fds == null) {
			return null;
		}
		String[][] array = new String[fds.size()][];
		for (int m = 0; m < fds.size(); m++) {
			FieldDescriptor fd = fds.get(m);
			array[m] = toStringArray(fd.getName(), fd.allowUnsafeAccess, fd.allowWrite);
		}
		return array;
	}

	public static String[] toStringArray(String fieldname, boolean allowUnsafeAccess, boolean allowWrite) {
		String[] array = new String[3];
		array[0] = fieldname;
		array[1] = Boolean.toString(allowUnsafeAccess);
		array[2] = Boolean.toString(allowWrite);
		return array;
	}

	public static FieldDescriptor of(String[] array) {
		boolean allowUnsafeAccess = Boolean.valueOf(array[1]);
		boolean allowWrite = Boolean.valueOf(array[2]);
		return FieldDescriptor.of(array[0], allowWrite, allowUnsafeAccess);
	}
	
}

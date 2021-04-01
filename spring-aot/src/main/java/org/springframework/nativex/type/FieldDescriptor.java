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

package org.springframework.nativex.type;

import java.util.List;

public class FieldDescriptor {

	private final String name;
	private final boolean allowUnsafeAccess;
	private final boolean allowWrite;

	public FieldDescriptor(String name, boolean allowUnsafeAccess, boolean allowWrite) {
		this.name = name;
		this.allowUnsafeAccess = allowUnsafeAccess;
		this.allowWrite = allowWrite;
	}

	public String getName() {
		return name;
	}

	public boolean isAllowUnsafeAccess() {
		return allowUnsafeAccess;
	}

	public boolean isAllowWrite() {
		return allowWrite;
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

}

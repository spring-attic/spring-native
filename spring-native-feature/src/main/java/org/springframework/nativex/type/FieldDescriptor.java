/*
 * Copyright 2020 Contributors
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
	
	public FieldDescriptor(String name, boolean allowUnsafeAccess) {
		this.name = name;
		this.allowUnsafeAccess = allowUnsafeAccess;
	}

	public String getName() {
		return name;
	}

	public boolean isAllowUnsafeAccess() {
		return allowUnsafeAccess;
	}

	public static String[][] toStringArray(List<FieldDescriptor> fds) {
		if (fds == null) {
			return null;
		}
		String[][] array = new String[fds.size()][];
		for (int m=0;m<fds.size();m++) {
			org.springframework.nativex.type.FieldDescriptor fd = fds.get(m);
			boolean aua = fd.allowUnsafeAccess;
			String name = fd.getName();
			if (aua) {
				array[m] = new String[2];
				array[m][0] = name;
				array[m][1] = "true";
			} else {
				array[m] = new String[1];
				array[m][0] = name;
			}
		}
		return array;
	}

}

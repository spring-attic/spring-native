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

import java.util.Collections;
import java.util.List;

import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.hint.AccessBits;

public class AccessDescriptor {

	private Integer accessBits;
	private List<MethodDescriptor> methodDescriptors;
	private List<FieldDescriptor> fieldDescriptors;
	
	public AccessDescriptor(Integer accessBits) {
		this(accessBits, Collections.emptyList(), Collections.emptyList());
	}

	public AccessDescriptor(Integer accessBits, List<MethodDescriptor> mds, List<FieldDescriptor> fds) {
		this.accessBits = accessBits;
		this.methodDescriptors = mds;
		this.fieldDescriptors = fds;
	}

	public Integer getAccessBits() {
		return accessBits;
	}

	public List<MethodDescriptor> getMethodDescriptors() {
		return methodDescriptors;
	}

	public List<FieldDescriptor> getFieldDescriptors() {
		return fieldDescriptors;
	}

	public boolean noMembersSpecified() {
		return methodDescriptors.isEmpty() && fieldDescriptors.isEmpty();
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("AccessDesc:"+ AccessBits.toString(accessBits));
		if (methodDescriptors!=null && !methodDescriptors.isEmpty()) {
			s.append(",md="+methodDescriptors);
		}
		if (fieldDescriptors!=null && !fieldDescriptors.isEmpty()) {
			s.append(",fd="+fieldDescriptors);
		}
		return s.toString();
	}
}

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
package org.springframework.graalvm.type;

import java.util.List;

public class MethodDescriptor {
	
	private final String name;
	private final List<String> parameterTypes;
	
	public MethodDescriptor(String name, List<String> parameterTypes) {
		this.name = name;
		this.parameterTypes = parameterTypes;
	}

	public String getName() {
		return name;
	}

	public List<String> getParameterTypes() {
		return parameterTypes;
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(name);
		s.append("(");
		for (int p=0;p<parameterTypes.size();p++) {
			if (p>0) { s.append(","); }
			s.append(parameterTypes.get(p));
		}
		s.append(")");
		return s.toString();
	}

}

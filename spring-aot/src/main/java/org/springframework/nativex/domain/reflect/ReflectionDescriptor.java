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

import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md
 * 
 * @author Andy Clement
 */
public class ReflectionDescriptor {

	private final List<ClassDescriptor> classDescriptors;

	public ReflectionDescriptor() {
		this.classDescriptors = new ArrayList<>();
	}

	public ReflectionDescriptor(ReflectionDescriptor reflectionDescriptor) {
		this.classDescriptors = new ArrayList<>(reflectionDescriptor.classDescriptors);
	}

	public ReflectionDescriptor(List<ClassDescriptor> classDescriptors) {
		this.classDescriptors = new ArrayList<>(classDescriptors);
	}
	
	public void sort() {
		classDescriptors.sort((a,b) -> a.getName().compareTo(b.getName()));
	}

	public List<ClassDescriptor> getClassDescriptors() {
		return this.classDescriptors;
	}

	public void add(ClassDescriptor classDescriptor) {
		this.classDescriptors.add(classDescriptor);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(String.format("ClassDescriptors #%s\n",classDescriptors.size()));
		this.classDescriptors.forEach(cd -> {
			result.append(String.format("%s: \n",cd));
		});
		return result.toString();
	}

	public boolean isEmpty() {
		return classDescriptors.isEmpty();
	}

	public static ReflectionDescriptor of(String jsonString) {
		try {
			return JsonMarshaller.read(jsonString);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to read json:\n"+jsonString, e);
		}
	}

	public boolean hasClassDescriptor(String string) {
		for (ClassDescriptor cd: classDescriptors) {
			if (cd.getName().equals(string)) {
				return true;
			}
		}
		return false;
	}

	public ClassDescriptor getClassDescriptor(String type) {
		for (ClassDescriptor cd: classDescriptors) {
			if (cd.getName().equals(type)) {
				return cd;
			}
		}
		return null;
	}

	public void merge(ReflectionDescriptor rd) {
		List<ClassDescriptor> otherClassDescriptors = rd.getClassDescriptors();
		if (otherClassDescriptors != null) {
			List<ClassDescriptor> toAdd = new ArrayList<>();
			for (ClassDescriptor otherClassDescriptor: otherClassDescriptors) {
				String typename = otherClassDescriptor.getName();
				ClassDescriptor existingCD = getClassDescriptor(typename);
				if (existingCD != null) {
					existingCD.merge(otherClassDescriptor);
				} else {
					toAdd.add(otherClassDescriptor.copy());
				}
			}
			classDescriptors.addAll(toAdd);
		}
	}

	public void merge(ClassDescriptor classDescriptor) {
		ClassDescriptor existingCD = getClassDescriptor(classDescriptor.getName());
		if (existingCD != null) {
			existingCD.merge(classDescriptor);
		} else {
			classDescriptors.add(classDescriptor.copy());
		}
	}

}

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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class MethodDescriptor {
	
	private final String name;
	private final List<String> parameterTypes;
	
	public MethodDescriptor(String name, List<String> parameterTypes) {
		this.name = name;
		this.parameterTypes = parameterTypes;
	}
	
	public static List<MethodDescriptor> of(String[][] methodDescriptors) {
		if (methodDescriptors == null) {
			return null;
		}
		List<MethodDescriptor> mds = new ArrayList<>();
		for (int i=0;i<methodDescriptors.length;i++) {
			String[] methodDescriptor = methodDescriptors[i];
			mds.add(of(methodDescriptor));
		}
		return mds;
	}
	
	public static MethodDescriptor of(String[] methodDescriptor) {
		List<String> params = new ArrayList<>();
		for (int p=1;p<methodDescriptor.length;p++) {
			params.add(methodDescriptor[p]);
		}
		return new MethodDescriptor(methodDescriptor[0], params);
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

	public static boolean includesConstructors(List<MethodDescriptor> mds) {
		for (MethodDescriptor md: mds) {
			if (md.getName().equals("<init>")) {
				return true;
			}
		}
		return false;
	}

	public static boolean includesMethods(List<MethodDescriptor> mds) {
		int methodCount = 0;
		for (MethodDescriptor md: mds) {
			if (!md.getName().equals("<init>") && !md.getName().equals("<clinit>")) {
				methodCount++;
			}
		}
		return methodCount != 0;
	}

	public static boolean includesStaticInitializers(List<MethodDescriptor> mds) {
		for (MethodDescriptor md: mds) {
			if (md.getName().equals("<clinit>")) {
				return true;
			}
		}
		return false;
	}
	 
	public Executable findOnClass(Class<?> clazz) {
		if (name.equals("<init>")) {
			for (Constructor<?> constructor: clazz.getConstructors()) {
				if (parametersMatch(constructor)) {
					return constructor;
				}
			}
		} else {
			for (java.lang.reflect.Method method : clazz.getMethods()) {
				if (method.getName().equals(name) && parametersMatch(method)) {
					return method;
				}
			}
		}
		return null;
	}
	
	private boolean parametersMatch(Executable executable) {
		if (this.parameterTypes.size()==executable.getParameterCount()) {
			Parameter[] parameters = executable.getParameters();
			for (int i=0;i<parameterTypes.size();i++) {
				String expected = parameterTypes.get(i);
				String actual = parameters[i].getType().getName();
				actual = TypeSystem.decodeName(actual);
				if (!expected.equals(actual)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
		

}

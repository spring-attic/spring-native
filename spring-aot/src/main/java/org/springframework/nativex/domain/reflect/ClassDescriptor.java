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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.nativex.hint.TypeAccess;

/**
 * Reflection information about a single class.
 *
 * @author Andy Clement
 * @author Sebastien Deleuze
 * @see ReflectionDescriptor
 */
public final class ClassDescriptor {

	private String name; // e.g. java.lang.Class

	private ConditionDescriptor condition;

	private List<FieldDescriptor> fields;

	private List<MethodDescriptor> methods; // includes constructors "<init>"

	private List<MethodDescriptor> queriedMethods; // includes constructors "<init>"

	private Set<TypeAccess> access; // Inclusion in list indicates they are set

	ClassDescriptor() {
	}

	ClassDescriptor(String name, ConditionDescriptor condition, List<FieldDescriptor> fields, List<MethodDescriptor> methods, List<MethodDescriptor> queriedMethods, Set<TypeAccess> access) {
		this.name = name;
		this.condition = condition;
		this.fields = fields;
		this.methods = methods;
		this.queriedMethods = queriedMethods;
		this.access = access;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ClassDescriptor other = (ClassDescriptor) o;
		boolean result = true;
		result = result && nullSafeEquals(this.name, other.name);
		result = result && nullSafeEquals(this.condition, other.condition);
		result = result && nullSafeEquals(this.access, other.access);
		result = result && nullSafeEquals(this.fields, other.fields);
		result = result && nullSafeEquals(this.methods, other.methods);
		result = result && nullSafeEquals(this.queriedMethods, other.queriedMethods);
		return result;
	}

	@Override
	public int hashCode() {
		int result = nullSafeHashCode(this.name);
		result = 31 * result + nullSafeHashCode(this.condition);
		result = 31 * result + nullSafeHashCode(this.access);
		result = 31 * result + nullSafeHashCode(this.fields);
		result = 31 * result + nullSafeHashCode(this.methods);
		result = 31 * result + nullSafeHashCode(this.queriedMethods);
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
		StringBuilder string = new StringBuilder(this.name);
		buildToStringProperty(string, "condition", this.condition);
		buildToStringProperty(string, "access", this.access);
		buildToStringProperty(string, "fields", this.fields);
		buildToStringProperty(string, "methods", this.methods);
		buildToStringProperty(string, "queriedMethods", this.queriedMethods);
		return string.toString();
	}

	protected void buildToStringProperty(StringBuilder string, String property, Object value) {
		if (value != null) {
			string.append(" ").append(property).append(":").append(value);
		}
	}

	public ConditionDescriptor getCondition() {
		return condition;
	}

	public Set<TypeAccess> getAccess() {
		return this.access;
	}

	public List<FieldDescriptor> getFields() {
		return this.fields;
	}

	public List<MethodDescriptor> getMethods() {
		return this.methods;
	}

	public List<MethodDescriptor> getQueriedMethods() {
		return this.queriedMethods;
	}

	public static ClassDescriptor of(String name) {
		ClassDescriptor cd = new ClassDescriptor();
		cd.setName(name);
		return cd;
	}

	public void setCondition(ConditionDescriptor condition) {
		this.condition = condition;
	}

	public void setAccess(TypeAccess f) {
		if (access == null) {
			access = new TreeSet<>();
		}
		access.add(f);
	}
	
	public void setAccess(Set<TypeAccess> accesses) {
		for (TypeAccess access : accesses) {
			this.setAccess(access);
		}
	}
	
	public void unsetAccess(TypeAccess f) {
		if (access == null) {
			return;
		}
		access.remove(f);
	}

	public void addMethodDescriptor(MethodDescriptor methodDescriptor) {
		if (methods == null) {
			methods = new ArrayList<>();
		}
		methods.add(methodDescriptor);
	}

	private void addMethodDescriptors(List<MethodDescriptor> methodDescriptors) {
		for (MethodDescriptor md: methodDescriptors) {
			addMethodDescriptor(md);
		}
	}

	public void addQueriedMethodDescriptor(MethodDescriptor methodDescriptor) {
		if (queriedMethods == null) {
			queriedMethods = new ArrayList<>();
		}
		queriedMethods.add(methodDescriptor);
	}

	private void addQueriedMethodDescriptors(List<MethodDescriptor> methodDescriptors) {
		for (MethodDescriptor md: methodDescriptors) {
			addQueriedMethodDescriptor(md);
		}
	}

	public void addFieldDescriptor(FieldDescriptor fieldDescriptor) {
		if (fields == null) {
			fields = new ArrayList<>();
		}
		fields.add(fieldDescriptor);
	}
	
	private void addFieldDescriptors(List<FieldDescriptor> fieldDescriptors) {
		for (FieldDescriptor fieldDescriptor: fieldDescriptors) {
			addFieldDescriptor(fieldDescriptor);
		}
	}

	/**
	 * Used when new data is to be added to an already existing class descriptor
	 * (additional members, flag settings).
	 * 
	 * @param cd the ClassDescriptor to merge into this one
	 */
	public void merge(ClassDescriptor cd) {
		if (cd.getCondition() != null && condition == null) {
			condition = cd.getCondition();
		}
		if (cd.getAccess()!= null) {
			for (TypeAccess access : cd.getAccess()) {
				this.setAccess(access);
			}
		}
		if (cd.getFields() != null) {
			if (fields == null) {
				addFieldDescriptors(cd.getFields());
			} else {
				for (FieldDescriptor fd : cd.getFields()) {
					FieldDescriptor existingFieldDescriptor = null;
					for (FieldDescriptor fieldDescriptor : getFields()) {
						if (fieldDescriptor.getName().equals(fd.getName())) {
							existingFieldDescriptor = fieldDescriptor;
							break;
						}
					}
					if (existingFieldDescriptor != null) {
						existingFieldDescriptor.merge(fd);
					} else {
						addFieldDescriptor(fd);
					}
				}
			}
		}
		if (cd.getMethods() != null) {
			for (MethodDescriptor methodDescriptor : cd.getMethods()) {
				if (!containsMethodDescriptor(methodDescriptor)) {
					addMethodDescriptor(methodDescriptor);
				}
			}
		}
		if (cd.getQueriedMethods() != null) {
			for (MethodDescriptor methodDescriptor : cd.getQueriedMethods()) {
				if (!containsQueriedMethodDescriptor(methodDescriptor)) {
					addQueriedMethodDescriptor(methodDescriptor);
				}
			}
		}
	}

	private boolean containsMethodDescriptor(MethodDescriptor methodDescriptor) {
		return methods == null?false:methods.contains(methodDescriptor);
	}

	private boolean containsQueriedMethodDescriptor(MethodDescriptor methodDescriptor) {
		return queriedMethods == null?false:queriedMethods.contains(methodDescriptor);
	}

	public MethodDescriptor getMethodDescriptor(String name, String... parameterTypes) {
		if (methods != null) {
			MethodDescriptor toFind = MethodDescriptor.of(name, parameterTypes);
			for (MethodDescriptor md : methods) {
				if (md.equals(toFind)) {
					return md;
				}
			}
		}
		return null;
	}

	public MethodDescriptor getQueriedMethodDescriptor(String name, String... parameterTypes) {
		if (queriedMethods != null) {
			MethodDescriptor toFind = MethodDescriptor.of(name, parameterTypes);
			for (MethodDescriptor md : queriedMethods) {
				if (md.equals(toFind)) {
					return md;
				}
			}
		}
		return null;
	}

	public boolean containsMethod(MethodDescriptor toFind) {
		if (methods != null) {
			for (MethodDescriptor md : methods) {
				if (md.equals(toFind)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsQueriedMethod(MethodDescriptor toFind) {
		if (queriedMethods != null) {
			for (MethodDescriptor md : queriedMethods) {
				if (md.equals(toFind)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsField(FieldDescriptor toFind) {
		if (fields != null) {
			for (FieldDescriptor fd : fields) {
				if (fd.equals(toFind)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean hasConstructors() {
		if (methods != null) {
			for (MethodDescriptor md : methods) {
				if (md.getName().equals("<init>")) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasQueriedConstructors() {
		if (queriedMethods != null) {
			for (MethodDescriptor md : queriedMethods) {
				if (md.getName().equals("<init>")) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasMethods() {
		if (methods != null) {
			for (MethodDescriptor md : methods) {
				if (!md.getName().equals("<init>")) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasQueriedMethods() {
		if (queriedMethods != null) {
			for (MethodDescriptor md : queriedMethods) {
				if (!md.getName().equals("<init>")) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasCondition() {
		return condition != null && condition.getTypeReachable() != null;
	}

	public String toJsonString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"name\":").append("\""+name+"\"");
		if (hasCondition()) {
			sb.append(",\"condition\":").append("\""+condition.toJsonString()+"\"");
		}
		if (hasConstructors()) {
			sb.append(",\"allDeclaredConstructors\":true");
		}
		if (hasQueriedConstructors()) {
			sb.append(",\"queryAllDeclaredConstructors\":true");
		}
		if (hasMethods()) {
			sb.append(",\"allDeclaredMethods\":true");
		}
		if (hasQueriedMethods()) {
			sb.append(",\"queryAllDeclaredMethods\":true");
		}
		sb.append("}");
		return sb.toString();
	}

	public ClassDescriptor subtract(ClassDescriptor toSubtract) {
		Set<TypeAccess> access = new HashSet<>();
		if (this.getAccess()!=null) {
			access.addAll(this.getAccess());
		}
		if (toSubtract.getAccess()!=null) {
			access.removeAll(toSubtract.getAccess());
		}

		List<MethodDescriptor> resultMethods = new ArrayList<>();
		if (this.getMethods()!=null) {
			resultMethods.addAll(this.getMethods());
		}
		if (toSubtract.getMethods()!=null) {
			resultMethods.removeAll(toSubtract.getMethods());
		}

		List<MethodDescriptor> resultQueriedMethods = new ArrayList<>();
		if (this.getQueriedMethods()!=null) {
			resultQueriedMethods.addAll(this.getQueriedMethods());
		}
		if (toSubtract.getQueriedMethods()!=null) {
			resultQueriedMethods.removeAll(toSubtract.getQueriedMethods());
		}
		
		List<FieldDescriptor> resultFields = new ArrayList<>();
		if (this.getFields()!=null) {
			resultFields.addAll(this.getFields());
		}
		if (toSubtract.getFields()!=null) {
			resultFields.removeAll(toSubtract.getFields());
		}
		ClassDescriptor result = ClassDescriptor.of(this.getName());
		if (!access.isEmpty()) {
			result.setAccess(access);
		}
		if (!resultMethods.isEmpty()) {
			result.addMethodDescriptors(resultMethods);
		}
		if (!resultQueriedMethods.isEmpty()) {
			result.addQueriedMethodDescriptors(resultQueriedMethods);
		}
		if (!resultFields.isEmpty()) {
			result.addFieldDescriptors(resultFields);
		}
		return result;
	}

	public ClassDescriptor copy() {
		List<FieldDescriptor> fieldsCopy = null;
		if (fields != null) {
			fieldsCopy = new ArrayList<>();
			for (FieldDescriptor fd: fields) {
				fieldsCopy.add(fd.copy());
			}
		}
		List<MethodDescriptor> methodsCopy = null;
		if (methods != null) {
			methodsCopy = new ArrayList<>();
			for (MethodDescriptor md: methods) {
				methodsCopy.add(md.copy());
			}
		}
		List<MethodDescriptor> queriedMethodsCopy = null;
		if (queriedMethods != null) {
			queriedMethodsCopy = new ArrayList<>();
			for (MethodDescriptor md: queriedMethods) {
				queriedMethodsCopy.add(md.copy());
			}
		}
		Set<TypeAccess> accessCopy = null;
		if (access != null) {
			accessCopy = new HashSet<>();
			accessCopy.addAll(access);
		}
		return new ClassDescriptor(name, condition, fieldsCopy, methodsCopy, queriedMethodsCopy, accessCopy);
	}

	public FieldDescriptor getFieldDescriptorNamed(String name) {
		if (fields != null) {
			for (FieldDescriptor fd: fields) {
				if (fd.getName().equals(name)) {
					return fd;
				}
			}
		}
		return null;
	}

	public static ClassDescriptor of(Class<?> type) {
		return of(computeClassName(type));
	}
	
	// TODO Move to TypeSystem, maybe TypeName? (Although TypeName is not currently used for anything primitive).
	//      This code overlaps with code in TypeName.fromTypeSignature() but that code isn't for primitive arrays
	/**
	 * Convert from a class to a String signature. This copes with the special handling for arrays (and
	 * primitive arrays)
	 * @param type the type for which to create a class name
	 * @return the class name
	 */
	private static String computeClassName(Class<?> type) {
		StringBuilder result = new StringBuilder();
		String typename = type.getName();
		int dims = 0;
		while (typename.charAt(dims)=='[') {
			dims++;
		}
		if (dims > 0) {
			if (typename.endsWith(";")) {
				result.append(typename.substring(dims+1, typename.length()-1).replace("/", "."));
			} else {
				result.append(primitiveToName(typename.substring(dims)));
			}
		} else {
			result.append(typename);
		}
		while (dims > 0) {
			result.append("[]");
			dims--;
		}
		return result.toString();
	}

	private static String primitiveToName(String primitiveDescriptor) {
		switch (primitiveDescriptor) {
		case "I": return "int";
		case "Z": return "boolean";
		case "J": return "long";
		case "B": return "byte";
		case "C": return "char";
		case "S": return "short";
		case "F": return "float";
		case "D": return "double";
		default: throw new IllegalStateException(primitiveDescriptor);
		}
	}

}

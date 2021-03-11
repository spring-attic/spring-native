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

package org.springframework.core.type.classreading;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Name of a Java type in its pure Java format {@code io.spring.sample.Book[]}
 * or by its Type signature {@code [Lio/spring/sample/Book;}.
 * This implementation does not provide Generics information.
 * @author Brian Clozel
 */
class TypeName {

	private final TypeCode typeCode;

	private final String name;

	private final int arrayDimensions;

	private TypeName(String name, TypeCode typeCode, int arrayDimensions) {
		this.name = name;
		this.typeCode = typeCode;
		this.arrayDimensions = arrayDimensions;
	}

	/**
	 * Provide the array dimensions of the current type.
	 * {@code io.spring.sample.Book -> 0}
	 * {@code io.spring.sample.Book[] -> 1}
	 * {@code io.spring.sample.Book[][] -> 2}
	 */
	public int getArrayDimensions() {
		return this.arrayDimensions;
	}

	/**
	 * {@code io.spring.sample.Book[]} -> {@code io.spring.sample.Book[]}
	 * {@code java.util.List<io.spring.sample.Book>} -> {@code java.util.List}
	 */
	public String getClassName() {
		return appendDimensions(this.name);
	}

	/**
	 * {@code io.spring.sample.Book[]} -> {@code Book}
	 */
	@Nullable
	public String getConstructorName() {
		return this.name.substring(this.name.lastIndexOf('.') + 1);
	}

	/**
	 * {@code io.spring.sample.Book[]} -> {@code i.s.s.Book[]}
	 */
	public String toShortName() {
		String dname = getClassName();
		StringBuilder s = new StringBuilder();
		boolean hasDot = dname.contains(".");
		while (dname.contains(".")) {
			s.append(dname.charAt(0));
			dname = dname.substring(dname.indexOf(".") + 1);
		}
		if (hasDot) {
			s.append(".");
		}
		s.append(dname);
		return s.toString();
	}

	private String appendDimensions(String name) {
		if (this.arrayDimensions == 0) {
			return name;
		}
		else {
			StringBuilder builder = new StringBuilder(name);
			for (int i = 0; i < this.arrayDimensions; i++) {
				builder.append("[]");
			}
			return builder.toString();
		}
	}

	/**
	 * {@code io.spring.sample.Book[]} -> {@code [Lio/spring/sample/Book;}
	 * {@code int[]} -> {@code [I;}
	 */
	public String toTypeSignature() {
		StringBuilder typeSignature = new StringBuilder();
		for (int i = 0; i < this.arrayDimensions; i++) {
			typeSignature.append('[');
		}
		typeSignature.append(this.typeCode.getSignature());
		if (!this.typeCode.isPrimitive()) {
			typeSignature
					.append(this.name.replace(".", "/"))
					.append(';');
		}
		return typeSignature.toString();
	}

	/**
	 * {@code io.spring.sample.Book[]} -> {@code io.spring.sample}
	 */
	public String getPackageName() {
		if (this.typeCode.isPrimitive()) {
			return "";
		}
		return this.name.substring(0, this.name.lastIndexOf('.'));
	}

	public boolean isPrimitive() {
		return this.typeCode.isPrimitive();
	}

	public static TypeName from(String typeName) {
		int dimensions = 0;
		TypeCode typeCode = TypeCode.forType(typeName);
		while (typeName.endsWith("[]")) {
			dimensions++;
			typeName = typeName.substring(0, typeName.length() - 2);
		}
		if (typeName.endsWith(">")) {
			typeName = typeName.substring(0, typeName.indexOf('<'));
		}
		return new TypeName(typeName, typeCode, dimensions);
	}

	public static TypeName fromTypeSignature(String typeSignature) {
		int dimensions = 0;
		List<TypeName> genericTypes = Collections.emptyList();
		int pos = 0;
		TypeCode typeCode = TypeCode.forTypeSignature(typeSignature);
		while (typeSignature.charAt(pos) == '[') {
			pos++;
			dimensions++;
		}
		if (typeSignature.endsWith(">")) {
			typeSignature = typeSignature.substring(0, typeSignature.indexOf('<'));
		}
		String className = typeSignature.substring(pos + 1, typeSignature.length() - 1).replace('/', '.');
		return new TypeName(className, typeCode, dimensions);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TypeName typeName = (TypeName) o;
		return arrayDimensions == typeName.arrayDimensions && name.equals(typeName.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, arrayDimensions);
	}

	enum TypeCode {
		INT('I', int.class.getName()), DOUBLE('D', double.class.getName()),
		LONG('J', long.class.getName()), SHORT('S', short.class.getName()),
		BYTE('B', byte.class.getName()), CHAR('C', char.class.getName()),
		FLOAT('F', float.class.getName()), BOOLEAN('Z', boolean.class.getName()),
		REFERENCE('L', null);

		final char signature;

		@Nullable final String keyword;

		TypeCode(char signature, @Nullable String keyword) {
			this.signature = signature;
			this.keyword = keyword;
		}

		public char getSignature() {
			return this.signature;
		}

		@Nullable
		public String getKeyword() {
			return this.keyword;
		}

		public boolean isPrimitive() {
			return !TypeCode.REFERENCE.equals(this);
		}

		static TypeCode forType(String typeName) {
			Assert.notNull(typeName, "type name should not be null");
			while (typeName.endsWith("[]")) {
				typeName = typeName.substring(0, typeName.length() - 2);
			}
			for (TypeCode typeCode : TypeCode.values()) {
				if (typeName.equals(typeCode.keyword)) {
					return typeCode;
				}
			}
			return TypeCode.REFERENCE;
		}

		static TypeCode forTypeSignature(String typeSignature) {
			Assert.notNull(typeSignature, "type signature should not be null");
			while (typeSignature.charAt(0) == '[') {
				typeSignature = typeSignature.substring(1);
			}
			char firstChar = typeSignature.charAt(0);
			for (TypeCode code : TypeCode.values()) {
				if (code.signature == firstChar) {
					return code;
				}
			}
			for (TypeCode code : TypeCode.values()) {
				if (typeSignature.equals(code.keyword)) {
					return code;
				}
			}
			throw new IllegalArgumentException("Illegal type signature: " + typeSignature);
		}
	}
}
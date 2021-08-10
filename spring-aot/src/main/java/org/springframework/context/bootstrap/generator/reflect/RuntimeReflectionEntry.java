/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.context.bootstrap.generator.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.domain.reflect.MethodDescriptor;

/**
 * Describe the need for reflection for a particular {@link Class type}.
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 */
public class RuntimeReflectionEntry {

	private static final String CONSTRUCTOR_NAME = "<init>";

	private final Class<?> type;

	private final Set<Executable> methods;

	private final Set<Field> fields;

	private RuntimeReflectionEntry(Builder builder) {
		this.type = builder.type;
		this.methods = Collections.unmodifiableSet(builder.methods);
		this.fields = Collections.unmodifiableSet(builder.fields);
	}

	/**
	 * Create a new {@link Builder} for the specified type.
	 * @param type the type to consider
	 * @return a new builder
	 */
	public static Builder of(Class<?> type) {
		return new Builder(type);
	}

	/**
	 * Return the type to consider.
	 * @return the type to consider
	 */
	public Class<?> getType() {
		return this.type;
	}

	/**
	 * Return the {@link Executable methods and constructors}.
	 * @return the methods and constructors
	 */
	public Set<Executable> getMethods() {
		return this.methods;
	}

	/**
	 * Return the {@link Field fields}.
	 * @return the fields
	 */
	public Set<Field> getFields() {
		return this.fields;
	}

	/**
	 * Create a {@link ClassDescriptor} from this entry
	 * @return a class descriptor describing this entry
	 */
	public ClassDescriptor toClassDescriptor() {
		ClassDescriptor descriptor = ClassDescriptor.of(this.type.getCanonicalName());
		for (Executable method : methods) {
			descriptor.addMethodDescriptor(toMethodDescriptor(method));
		}
		for (Field field : fields) {
			descriptor.addFieldDescriptor(toFieldDescriptor(field));
		}
		return descriptor;
	}

	private MethodDescriptor toMethodDescriptor(Executable method) {
		String name = (method instanceof Constructor) ? CONSTRUCTOR_NAME : method.getName();
		return MethodDescriptor.of(name, Arrays.stream(method.getParameterTypes())
				.map(Class::getCanonicalName).toArray(String[]::new));
	}

	private FieldDescriptor toFieldDescriptor(Field field) {
		return FieldDescriptor.of(field.getName(), true, false);
	}

	public static class Builder {

		private final Class<?> type;

		private final Set<Executable> methods = new HashSet<>();

		private final Set<Field> fields = new HashSet<>();

		Builder(Class<?> type) {
			this.type = type;
		}

		/**
		 * Add the specified {@link Executable methods or constructors}.
		 * @param methods the methods to add
		 * @return this for method chaning
		 */
		public Builder withMethods(Executable... methods) {
			this.methods.addAll(Arrays.asList(methods));
			return this;
		}

		/**
		 * Add the specified {@link Field fields}.
		 * @param fields the fields to add
		 * @return this for method chaning
		 */
		public Builder withFields(Field... fields) {
			this.fields.addAll(Arrays.asList(fields));
			return this;
		}

		/**
		 * Create a {@link RuntimeReflectionEntry} from the state of this builder
		 * @return a new entry
		 */
		public RuntimeReflectionEntry build() {
			return new RuntimeReflectionEntry(this);
		}

	}

}

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

package org.springframework.context.bootstrap.generator.infrastructure.nativex;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;

import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.domain.reflect.MethodDescriptor;
import org.springframework.nativex.hint.Flag;

/**
 * Describe the need for reflection for a particular {@link Class type}.
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/Reflection/">GraalVM native image reflection documentation</a>
 */
public class NativeReflectionEntry {

	private static final String CONSTRUCTOR_NAME = "<init>";

	private final Class<?> type;

	private final Set<Constructor<?>> constructors;

	private final Set<Method> methods;

	private final Set<Field> fields;

	private final Set<Flag> flags;

	private NativeReflectionEntry(Builder builder) {
		this.type = builder.type;
		this.constructors = Collections.unmodifiableSet(builder.constructors);
		this.methods = Collections.unmodifiableSet(builder.methods);
		this.fields = Collections.unmodifiableSet(builder.fields);
		this.flags = Collections.unmodifiableSet(builder.flags);
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
	 * Return the {@link Constructor constructor}.
	 * @return the constructors
	 */
	public Set<Constructor<?>> getConstructors() {
		return this.constructors;
	}

	/**
	 * Return the {@link Method methods}.
	 * @return the methods
	 */
	public Set<Method> getMethods() {
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
	 * Return the {@link Flag flags} to set.
	 * @return the flags to set
	 */
	public Set<Flag> getFlags() {
		return this.flags;
	}

	/**
	 * Create a {@link ClassDescriptor} from this entry
	 * @return a class descriptor describing this entry
	 */
	public ClassDescriptor toClassDescriptor() {
		ClassDescriptor descriptor = ClassDescriptor.of(this.type.getName());
		registerIfNecessary(this.constructors, Flag.allDeclaredConstructors, Flag.allPublicConstructors,
				(constructor) -> descriptor.addMethodDescriptor(toMethodDescriptor(constructor)));
		registerIfNecessary(this.methods, Flag.allDeclaredMethods, Flag.allPublicMethods,
				(method) -> descriptor.addMethodDescriptor(toMethodDescriptor(method)));
		registerIfNecessary(this.fields, Flag.allDeclaredFields, Flag.allPublicFields,
				(field) -> descriptor.addFieldDescriptor(toFieldDescriptor(field)));
		descriptor.setFlags(this.flags);
		return descriptor;
	}

	private <T extends Member> void registerIfNecessary(Iterable<T> members, Flag allFlag,
			Flag publicFlag, Consumer<T> memberConsumer) {
		if (!this.flags.contains(allFlag)) {
			boolean checkVisibility = this.flags.contains(publicFlag);
			for (T member : members) {
				if (!checkVisibility || !Modifier.isPublic(member.getModifiers())) {
					memberConsumer.accept(member);
				}
			}
		}
	}

	private MethodDescriptor toMethodDescriptor(Executable method) {
		String name = (method instanceof Constructor) ? CONSTRUCTOR_NAME : method.getName();
		return MethodDescriptor.of(name, Arrays.stream(method.getParameterTypes())
				.map(Class::getName).toArray(String[]::new));
	}

	private FieldDescriptor toFieldDescriptor(Field field) {
		return FieldDescriptor.of(field.getName(), true, false);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", NativeReflectionEntry.class.getSimpleName() + "[", "]")
				.add("type=" + this.type).add("constructors=" + this.constructors)
				.add("methods=" + this.methods).add("fields=" + this.fields)
				.add("flags=" + this.flags).toString();
	}

	public static class Builder {

		private final Class<?> type;

		private final Set<Constructor<?>> constructors = new LinkedHashSet<>();

		private final Set<Method> methods = new LinkedHashSet<>();

		private final Set<Field> fields = new LinkedHashSet<>();

		private final Set<Flag> flags = new LinkedHashSet<>();

		Builder(Class<?> type) {
			this.type = type;
		}

		/**
		 * Add the specified {@link Executable methods or constructors}.
		 * @param methods the methods to add
		 * @return this for method chaining
		 */
		public Builder withMethods(Executable... methods) {
			Arrays.stream(methods).forEach((method) -> {
				if (method instanceof Method) {
					this.methods.add((Method) method);
				}
				else {
					this.constructors.add((Constructor<?>) method);
				}
			});
			return this;
		}

		/**
		 * Add the specified {@link Field fields}.
		 * @param fields the fields to add
		 * @return this for method chaining
		 */
		public Builder withFields(Field... fields) {
			this.fields.addAll(Arrays.asList(fields));
			return this;
		}

		/**
		 * Set the specified {@link Flag flags}.
		 * @param flags the flags to set
		 * @return this for method chaining
		 */
		public Builder withFlags(Flag... flags) {
			this.flags.addAll(Arrays.asList(flags));
			return this;
		}

		/**
		 * Create a {@link NativeReflectionEntry} from the state of this builder
		 * @return a new entry
		 */
		public NativeReflectionEntry build() {
			return new NativeReflectionEntry(this);
		}

	}

}

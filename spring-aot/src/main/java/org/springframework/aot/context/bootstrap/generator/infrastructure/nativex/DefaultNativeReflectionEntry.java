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

package org.springframework.aot.context.bootstrap.generator.infrastructure.nativex;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;

import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.domain.reflect.MethodDescriptor;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * A {@link NativeReflectionEntry} that uses standard reflection.
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 */
public class DefaultNativeReflectionEntry extends NativeReflectionEntry {

	private static final String CONSTRUCTOR_NAME = "<init>";

	private final Class<?> type;

	private final Set<Constructor<?>> constructors;

	private final Set<Constructor<?>> queriedConstructors;

	private final Set<Method> methods;

	private final Set<Method> queriedMethods;

	private final MultiValueMap<Field,FieldAccess> fields;


	private DefaultNativeReflectionEntry(Builder builder) {
		super(builder);
		this.type = builder.type;
		this.constructors = Collections.unmodifiableSet(builder.constructors);
		this.queriedConstructors = Collections.unmodifiableSet(builder.queriedConstructors);
		this.methods = Collections.unmodifiableSet(builder.methods);
		this.queriedMethods = Collections.unmodifiableSet(builder.queriedMethods);
		this.fields = new LinkedMultiValueMap(builder.fields);

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
		return this.fields.keySet();
	}

	@Override
	protected ClassDescriptor initializerClassDescriptor() {
		ClassDescriptor descriptor = ClassDescriptor.of(this.type);
		registerIfNecessary(this.constructors, TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_CONSTRUCTORS,
				(constructor) -> descriptor.addMethodDescriptor(toMethodDescriptor(constructor)));
		registerIfNecessary(this.queriedConstructors, TypeAccess.QUERY_DECLARED_CONSTRUCTORS, TypeAccess.QUERY_PUBLIC_CONSTRUCTORS,
				(constructor) -> descriptor.addQueriedMethodDescriptor(toMethodDescriptor(constructor)));
		registerIfNecessary(this.methods, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS,
				(method) -> descriptor.addMethodDescriptor(toMethodDescriptor(method)));
		registerIfNecessary(this.queriedMethods, TypeAccess.QUERY_DECLARED_METHODS, TypeAccess.QUERY_PUBLIC_METHODS,
				(method) -> descriptor.addQueriedMethodDescriptor(toMethodDescriptor(method)));
		registerFieldIfNecessary(this.fields, TypeAccess.DECLARED_FIELDS, TypeAccess.PUBLIC_FIELDS,
				(field) -> descriptor.addFieldDescriptor(toFieldDescriptor(field.getKey(), field.getValue())));
		return descriptor;
	}

	private void registerFieldIfNecessary(MultiValueMap<Field, FieldAccess> members, TypeAccess allAccess,
			TypeAccess publicAccess, Consumer<Entry<Field, List<FieldAccess>>> memberConsumer) {
		if (!getAccess().contains(allAccess)) {
			boolean checkVisibility = getAccess().contains(publicAccess);
			for (Entry<Field, List<FieldAccess>> member : members.entrySet()) {
				if (!checkVisibility || !Modifier.isPublic(member.getKey().getModifiers())) {
					memberConsumer.accept(member);
				}
			}
		} else {
			for (Entry<Field, List<FieldAccess>> member : members.entrySet()) {
				if(member.getValue().contains(FieldAccess.UNSAFE)) {
					memberConsumer.accept(member);
				}
			}
		}
	}

	private <T extends Member> void registerIfNecessary(Iterable<T> members, TypeAccess allAccess,
			TypeAccess publicAccess, Consumer<T> memberConsumer) {
		registerIfNecessary(members, allAccess, publicAccess,
				(member) -> Modifier.isPublic(member.getModifiers()), memberConsumer);
	}

	private MethodDescriptor toMethodDescriptor(Executable method) {
		String name = (method instanceof Constructor) ? CONSTRUCTOR_NAME : method.getName();
		return MethodDescriptor.of(name, Arrays.stream(method.getParameterTypes())
				.map(Class::getName).toArray(String[]::new));
	}

	private FieldDescriptor toFieldDescriptor(Field field, Collection<FieldAccess> access) {
		return FieldDescriptor.of(field.getName(), access.contains(FieldAccess.ALLOW_WRITE), access.contains(FieldAccess.UNSAFE));
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", DefaultNativeReflectionEntry.class.getSimpleName() + "[", "]")
				.add("type=" + this.type).add("constructors=" + this.constructors)
				.add("methods=" + this.methods).add("fields=" + this.fields)
				.add("queriedConstructors=" + this.queriedConstructors).add("queriedMethods=" + this.queriedMethods)
				.add("access=" + getAccess()).toString();
	}

	public enum FieldAccess {
		ALLOW_WRITE, UNSAFE
	}

	public static class Builder extends NativeReflectionEntry.Builder<Builder, DefaultNativeReflectionEntry> {

		private final Class<?> type;

		private final Set<Constructor<?>> constructors = new LinkedHashSet<>();

		private final Set<Constructor<?>> queriedConstructors = new LinkedHashSet<>();

		private final Set<Method> methods = new LinkedHashSet<>();

		private final Set<Method> queriedMethods = new LinkedHashSet<>();

		private final MultiValueMap<Field,FieldAccess> fields = new LinkedMultiValueMap<>();

		Builder(Class<?> type) {
			this.type = type;
		}

		/**
		 * Makes this entry conditional to provided type.
		 * @param conditionalOnTypeReachable The type which should make this entry taken in account.
		 * @return this for method chaining
		 */
		public Builder conditionalOnTypeReachable(Class<?> conditionalOnTypeReachable) {
			this.conditionalOnTypeReachable = (conditionalOnTypeReachable != null) ? conditionalOnTypeReachable.getName() : null;
			return this;
		}


		/**
		 * Add the specified {@link Executable methods or constructors}.
		 * @param executables the executables to add
		 * @return this for method chaining
		 */
		public Builder withExecutables(Executable... executables) {
			Arrays.stream(executables).forEach((executable) -> {
				if (executable instanceof Method) {
					this.methods.add((Method) executable);
				}
				else if (executable instanceof Constructor) {
					this.constructors.add((Constructor<?>) executable);
				}
			});
			return this;
		}

		/**
		 * Add the specified {@link Executable methods or constructors} for metadata query access only.
		 * @param executables the executables to add
		 * @return this for method chaining
		 */
		public Builder withQueriedExecutables(Executable... executables) {
			Arrays.stream(executables).forEach((executable) -> {
				if (executable instanceof Method) {
					this.queriedMethods.add((Method) executable);
				}
				else if (executable instanceof Constructor) {
					this.queriedConstructors.add((Constructor<?>) executable);
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
			for(Field field: fields) {
				withField(field, FieldAccess.ALLOW_WRITE);
			}
			return this;
		}

		public Builder withField(Field field, FieldAccess... access) {
			if(!fields.containsKey(field)) {
				this.fields.addAll(field, Arrays.asList(access));
			}

			ArrayList<FieldAccess> tmp = new ArrayList<>(Arrays.asList(access));
			tmp.removeAll(fields.get(field));
			if(!tmp.isEmpty()) {
				this.fields.addAll(field, tmp);
			}

			return this;
		}

		/**
		 * Create a {@link DefaultNativeReflectionEntry} from the state of this builder
		 * @return a new entry
		 */
		@Override
		public DefaultNativeReflectionEntry build() {
			return new DefaultNativeReflectionEntry(this);
		}

	}

}

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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.domain.reflect.MethodDescriptor;
import org.springframework.nativex.hint.TypeAccess;

/**
 * A {@link NativeReflectionEntry} for generated code.
 *
 * @author Stephane Nicoll
 */
public class GeneratedCodeNativeReflectionEntry extends NativeReflectionEntry {

	private final ClassName type;

	private final Set<MethodSpec> methods;

	private final Set<MethodSpec> queriedMethods;

	private final Set<FieldSpec> fields;

	public GeneratedCodeNativeReflectionEntry(Builder builder) {
		super(builder);
		this.type = builder.type;
		this.methods = builder.methods;
		this.queriedMethods = builder.queriedMethods;
		this.fields = builder.fields;
	}

	/**
	 * Create a new {@link GeneratedCodeNativeReflectionEntry.Builder} for the specified
	 * class name
	 * @param className the class name to consider
	 * @return a new builder
	 */
	public static Builder of(ClassName className) {
		return new Builder(className);
	}

	@Override
	protected ClassDescriptor initializerClassDescriptor() {
		ClassDescriptor descriptor = ClassDescriptor.of(toFullyQualifiedClassName(this.type));
		registerIfNecessary(this.methods.stream().filter(MethodSpec::isConstructor),
				TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_CONSTRUCTORS,
				(constructor) -> descriptor.addMethodDescriptor(toMethodDescriptor(constructor)));
		registerIfNecessary(this.queriedMethods.stream().filter(MethodSpec::isConstructor),
				TypeAccess.QUERY_DECLARED_CONSTRUCTORS, TypeAccess.QUERY_PUBLIC_CONSTRUCTORS,
				(constructor) -> descriptor.addQueriedMethodDescriptor(toMethodDescriptor(constructor)));
		registerIfNecessary(this.methods.stream().filter(Predicate.not(MethodSpec::isConstructor)),
				TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS,
				(method) -> descriptor.addMethodDescriptor(toMethodDescriptor(method)));
		registerIfNecessary(this.queriedMethods.stream().filter(Predicate.not(MethodSpec::isConstructor)),
				TypeAccess.QUERY_DECLARED_METHODS, TypeAccess.QUERY_PUBLIC_METHODS,
				(method) -> descriptor.addQueriedMethodDescriptor(toMethodDescriptor(method)));
		registerFieldsIfNecessary(this.fields, (field) -> descriptor.addFieldDescriptor(toFieldDescriptor(field)));
		return descriptor;
	}

	private MethodDescriptor toMethodDescriptor(MethodSpec method) {
		return MethodDescriptor.of(method.name, method.parameters.stream()
				.map((parameterSpec) -> toFullyQualifiedClassName(parameterSpec.type)).toArray(String[]::new));
	}

	private FieldDescriptor toFieldDescriptor(FieldSpec field) {
		return FieldDescriptor.of(field.name, true, false);
	}

	private static String toFullyQualifiedClassName(TypeName typeName) {
		if (typeName instanceof ClassName) {
			return toFullyQualifiedClassName((ClassName) typeName);
		}
		return typeName.toString();
	}

	private static String toFullyQualifiedClassName(ClassName className) {
		String candidate = className.canonicalName();
		if (className.enclosingClassName() == null) {
			return candidate;
		}
		StringBuilder sb = new StringBuilder(candidate);
		sb.setCharAt(candidate.lastIndexOf("."), '$');
		return sb.toString();
	}

	private void registerIfNecessary(Stream<MethodSpec> methods, TypeAccess allAccess,
			TypeAccess publicAccess, Consumer<MethodSpec> memberConsumer) {
		registerIfNecessary(methods.collect(Collectors.toList()), allAccess, publicAccess,
				(method) -> method.hasModifier(Modifier.PUBLIC), memberConsumer);
	}

	private void registerFieldsIfNecessary(Iterable<FieldSpec> fields, Consumer<FieldSpec> memberConsumer) {
		registerIfNecessary(fields, TypeAccess.DECLARED_FIELDS, TypeAccess.PUBLIC_FIELDS,
				(field) -> field.hasModifier(Modifier.PUBLIC), memberConsumer);
	}

	public static class Builder extends NativeReflectionEntry.Builder<GeneratedCodeNativeReflectionEntry.Builder, GeneratedCodeNativeReflectionEntry> {

		private final ClassName type;

		private final Set<MethodSpec> methods = new LinkedHashSet<>();

		private final Set<MethodSpec> queriedMethods = new LinkedHashSet<>();

		private final Set<FieldSpec> fields = new LinkedHashSet<>();

		public Builder(ClassName type) {
			this.type = type;
		}

		/**
		 * Makes this entry conditional to the provided type.
		 * @param conditionalOnTypeReachable the type which should make this entry taken into account.
		 * @return this for method chaining
		 */
		public Builder conditionalOnTypeReachable(ClassName conditionalOnTypeReachable) {
			this.conditionalOnTypeReachable = toFullyQualifiedClassName(conditionalOnTypeReachable);
			return this;
		}

		/**
		 * Add the specified {@link MethodSpec methods or constructors}.
		 * @param methods the method specs to add
		 * @return this for method chaining
		 */
		public Builder withMethods(MethodSpec... methods) {
			this.methods.addAll(Arrays.asList(methods));
			return this;
		}


		/**
		 * Add the specified {@link MethodSpec methods or constructors}.
		 * @param methods the method specs to add
		 * @return this for method chaining
		 */
		public Builder withQueriedMethods(MethodSpec... methods) {
			this.queriedMethods.addAll(Arrays.asList(methods));
			return this;
		}

		/**
		 * Add the specified {@link FieldSpec fields}.
		 * @param fields the field specs to add
		 * @return this for method chaining
		 */
		public Builder withFields(FieldSpec... fields) {
			this.fields.addAll(Arrays.asList(fields));
			return this;
		}

		@Override
		GeneratedCodeNativeReflectionEntry build() {
			return new GeneratedCodeNativeReflectionEntry(this);
		}
	}
}

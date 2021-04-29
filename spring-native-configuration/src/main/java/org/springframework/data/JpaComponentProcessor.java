/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.Field;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeProcessor;

/**
 * @author Christoph Strobl
 */
public class JpaComponentProcessor implements ComponentProcessor {

	private final TypeProcessor typeProcessor = new TypeProcessor(
			(type, context) -> {

				if (type.isPartOfDomain("sun.") || type.isPartOfDomain("jdk.")) {
					return false;
				}
				return true;
			},
			this::registerTypeInConfiguration,
			this::registerAnnotationInConfiguration
	).named("JpaComponentProcessor");

	private JpaImplementation jpaImplementation;

	public JpaComponentProcessor() {
		this.jpaImplementation = new HibernateJpaImplementation(); // TODO: others?
	}

	@Override
	public boolean handle(NativeContext imageContext, String componentType, List<String> classifiers) {

		if (classifiers.contains("javax.persistence.Entity")) {
			return true;
		}

		Type type = imageContext.getTypeSystem().resolveName(componentType);

		return type.getAnnotations() //
				.stream() //
				.anyMatch(it -> it.getDottedName().contains("javax.persistence"));
	}

	@Override
	public void process(NativeContext imageContext, String componentType, List<String> classifiers) {

		Type domainType = imageContext.getTypeSystem().resolveName(componentType);
		typeProcessor.use(imageContext).toProcessType(domainType);
	}

	private void registerAnnotationInConfiguration(Type annotation, NativeContext context) {

		if (!jpaImplementation.recognizesType(annotation)) {
			return;
		}

		context.log(String.format("JpaComponentProcessor: adding reflection configuration AccessBits.ANNOTATION for annotation %s.", annotation.getDottedName()));
		context.addReflectiveAccess(annotation.getDottedName(), AccessBits.ANNOTATION);
	}

	private void registerTypeInConfiguration(Type type, NativeContext context) {

		AccessDescriptor accessDescriptor = new AccessDescriptor(AccessBits.FULL_REFLECTION, Collections.emptyList(), fieldDescriptorsForType(type, context));

		context.log(String.format("JpaComponentProcessor: adding reflection configuration type %s - %s", type.getDottedName(), accessDescriptor));

		context.addReflectiveAccess(type.getDottedName(), accessDescriptor);

		if (jpaImplementation.requiresSpecialTreatment(type)) {
			jpaImplementation.process(type, context);
		}
	}

	private List<FieldDescriptor> fieldDescriptorsForType(Type type, NativeContext context) {

		if (type.isPartOfDomain("java.")) { // other well known domains ?

			context.log(String.format("JpaComponentProcessor: skipping field inspection for type %s.", type.getDottedName()));
			return Collections.emptyList();
		}

		return type.getFields()
				.stream()
				.filter(Field::isFinal)
				.map(field -> {

					context.log(String.format("JpaComponentProcessor: detected final field %s for type %s. Setting allowWrite=true.", field.getName(), type.getDottedName()));
					return new FieldDescriptor(field.getName(), true, true);
				})
				.collect(Collectors.toList());
	}

	interface JpaImplementation {

		boolean isAvailable(NativeContext context);

		boolean requiresSpecialTreatment(Type type);

		void process(Type type, NativeContext context);

		default boolean recognizesType(Type type) {
			return type.isPartOfDomain(getNamespace()) || type.isPartOfDomain("javax.persistence");
		}

		String getNamespace();
	}

	static class HibernateJpaImplementation implements JpaImplementation {

		public static final String ENUM_TYPE = "org.hibernate.type.EnumType";

		@Override
		public boolean isAvailable(NativeContext context) {
			return context.getTypeSystem().canResolve("org.hibernate.jpa.HibernateEntityManager");
		}

		@Override
		public boolean requiresSpecialTreatment(Type type) {
			return type.isEnum();
		}

		@Override
		public void process(Type type, NativeContext context) {

			if (type.isEnum() && !context.hasReflectionConfigFor(ENUM_TYPE)) {

				context.log(String.format("JpaComponentProcessor: detected enum usage in entity. Adding load/construct for %s.", type, ENUM_TYPE));
				context.addReflectiveAccess(ENUM_TYPE, new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
			}
		}

		@Override
		public String getNamespace() {
			return "org.hibernate.";
		}
	}
}

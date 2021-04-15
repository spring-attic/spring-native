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

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.data.jpa.entities.EntityWithAnnotations;
import org.springframework.data.jpa.entities.Order;
import org.springframework.data.jpa.entities.WithFinalField;
import org.springframework.data.jpa.entities.WithTypesInMethods;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.util.NativeTestContext;
import org.springframework.nativex.util.TestTypeSystem;

/**
 * @author Christoph Strobl
 */
public class TypeProcessorTests {

	static TestTypeSystem typeSystem;
	private NativeTestContext nativeContext;
	private TypeProcessor processor;

	private List<String> capturedTypes;
	private List<String> capturedAnnotations;

	@Before
	public void setUp() {

		typeSystem = new TestTypeSystem();
		nativeContext = new NativeTestContext(typeSystem);

		capturedTypes = new ArrayList<>();
		capturedAnnotations = new ArrayList<>();

		processor = new TypeProcessor(
				(type, context) -> true, // do not filter out types
				(type, context) -> {

					// types we discovered
					context.addReflectiveAccess(type, new AccessDescriptor(AccessBits.FULL_REFLECTION));
					capturedTypes.add(type.getDottedName());
				},
				(type, context) -> {

					// annotations we discovered
					context.addReflectiveAccess(type, new AccessDescriptor(AccessBits.ANNOTATION));
					capturedAnnotations.add(type.getDottedName());
				}
		);
	}

	@Test
	public void detectsUsedTypesAndAnnotations() {

		processor.process(typeSystem.resolve(Order.class), nativeContext);

		assertThat(capturedTypes).containsExactlyInAnyOrder("org.springframework.data.jpa.entities.Order", "java.lang.Long", "java.lang.String", "java.util.List", "org.springframework.data.jpa.entities.LineItem", "java.util.Date");
		assertThat(capturedAnnotations).contains("javax.persistence.Entity", "javax.persistence.ManyToOne", "javax.persistence.OneToMany", "javax.persistence.GeneratedValue", "org.springframework.data.jpa.entities.SomeAnnotation", "javax.annotation.Nullable", "org.springframework.beans.factory.annotation.Value");
	}

	@Test
	public void ignoresAnnotationsNotReachable() {

		typeSystem.excludePackages("javax.annotation"); //  @Nullable should not be there

		processor.process(typeSystem.resolve(Order.class), nativeContext);

		assertThat(capturedTypes).containsExactlyInAnyOrder("org.springframework.data.jpa.entities.Order", "java.lang.Long", "java.lang.String", "java.util.List", "org.springframework.data.jpa.entities.LineItem", "java.util.Date");
		assertThat(capturedAnnotations).contains("org.springframework.data.jpa.entities.SomeAnnotation").doesNotContain("javax.annotation.Nullable");
	}

	@Test
	public void includesFieldsBasedOnFilter() {

		processor.includeFieldsMatching(field -> field.isFinal())
				.process(typeSystem.resolve(WithFinalField.class), nativeContext);

		assertThat(capturedTypes).containsExactlyInAnyOrder("org.springframework.data.jpa.entities.WithFinalField", "java.lang.String").doesNotContain("java.lang.Long");
	}

	@Test
	public void includesAnnotationsBasedOnFilter() {

		processor.includeAnnotationsMatching(annotation -> annotation.isPartOfDomain("javax."))
				.process(typeSystem.resolve(EntityWithAnnotations.class), nativeContext);

		assertThat(capturedAnnotations)
				.containsExactlyInAnyOrder("javax.persistence.Entity", "javax.persistence.GeneratedValue")
				.doesNotContain("org.springframework.data.jpa.entities.SomeAnnotation");
	}

	@Test
	public void excludesAnnotationsBasedOnFilter() {

		processor.skipAnnotationsMatching(annotation -> annotation.isPartOfDomain("javax."))
				.process(typeSystem.resolve(EntityWithAnnotations.class), nativeContext);

		assertThat(capturedAnnotations)
				.containsExactly("org.springframework.data.jpa.entities.SomeAnnotation")
				.doesNotContain("javax.persistence.Entity", "javax.persistence.GeneratedValue");
	}

	@Test
	public void excludesFieldsBasedOnFilter() {

		processor.skipFieldsMatching(field -> field.isFinal())
				.process(typeSystem.resolve(WithFinalField.class), nativeContext);

		assertThat(capturedTypes).containsExactlyInAnyOrder("org.springframework.data.jpa.entities.WithFinalField", "java.lang.Long").doesNotContain("java.lang.String");
	}

	@Test
	public void capturesMethodParameterTypes() {

		processor.process(typeSystem.resolve(WithTypesInMethods.class), nativeContext);

		assertThat(capturedTypes).containsExactlyInAnyOrder("org.springframework.data.jpa.entities.WithTypesInMethods", "java.util.Date", "java.util.List", "java.lang.Long", "java.lang.Integer", "java.lang.String");
	}

	@Test
	@Ignore("typeSystem.scan with predicate does not work")
	public void shortcutTypeSystemLookupFunction() {

		System.out.println(typeSystem.scan(type -> type.hasAnnotationInHierarchy("Ljavax/persistence/Entity;")));
		System.out.println(typeSystem.scan(type -> type.hasAnnotationInHierarchy("javax.persistence.Entity")));
		System.out.println(typeSystem.scan(type -> type.getName().contains("Entity")));
		System.out.println(typeSystem.scan(type -> true));

		System.out.println(processor.use(typeSystem).toProcessTypesMatching(type -> type.hasAnnotationInHierarchy("Ljavax/persistence/Entity;")));
	}
}

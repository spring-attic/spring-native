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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Collections;

import org.hibernate.type.EnumType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.jpa.entities.EntityWithEnum;
import org.springframework.data.jpa.entities.LineItem;
import org.springframework.data.jpa.entities.NotAnEntity;
import org.springframework.data.jpa.entities.Order;
import org.springframework.data.jpa.entities.SomeAnnotation;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.nativex.util.NativeTestContext;

public class JpaComponentProcessorTests {


	private NativeTestContext nativeContext = new NativeTestContext();
	private TypeSystem typeSystem = nativeContext.getTypeSystem();
	private JpaComponentProcessor processor;

	@Before
	public void setUp() {
		processor = new JpaComponentProcessor();
	}

	@Test
	public void shouldOnlyHandleJavaxPersistenceEntity() {

		// via detected classifier
		assertThat(processor.handle(nativeContext, typeSystem.resolve(NotAnEntity.class).getDottedName(), Collections.singletonList(typeSystem.resolve(Entity.class).getDottedName()))).isTrue();

		// @EntityOnType
		assertThat(processor.handle(nativeContext, typeSystem.resolve(Order.class).getDottedName(), Collections.emptyList())).isTrue();

		// not at all
		assertThat(processor.handle(nativeContext, typeSystem.resolve(NotAnEntity.class).getDottedName(), Collections.emptyList())).isFalse();
	}

	@Test
	public void shouldAddFullReflectionForEntityTypes() {

		process(Order.class);

		assertThat(nativeContext.getReflectionEntry(Order.class))
				.satisfies(config -> {
					assertThat(config.getAccessBits()).isEqualTo(AccessBits.FULL_REFLECTION);
				});
	}

	@Test
	public void shouldIncludeReachableTypes() {

		process(Order.class);

		assertThat(nativeContext.hasReflectionEntry(Order.class)).isTrue();
		assertThat(nativeContext.hasReflectionEntry(LineItem.class)).isTrue();
		assertThat(nativeContext.hasReflectionEntry(NotAnEntity.class)).isFalse();
	}

	@Test
	public void shouldRegisterReflectionAllowWriteForFinalFields/* looking at you hibernate */() {

		process(Order.class);

		assertThat(nativeContext.getReflectionEntry(Order.class).getFieldDescriptors())
				.hasSize(1)
				.satisfies(config -> {

					FieldDescriptor fieldDescriptor = config.iterator().next();
					assertThat(fieldDescriptor.getName()).isEqualTo("id");
					assertThat(fieldDescriptor.isAllowWrite()).isTrue();
				});
	}

	@Test
	public void shouldDoNothingFancyForNonFinalFields/* just leave them - full reflection we have anyways */() {

		process(Order.class);

		assertThat(nativeContext.getReflectionEntry(LineItem.class).getFieldDescriptors())
				.isEmpty();
	}

	@Test
	public void shouldAddReflectionForJavaxPersistenceAnnotations() {

		process(Order.class);

		assertThat(nativeContext.hasReflectionEntry(Entity.class)).isTrue();
		assertThat(nativeContext.getReflectionEntry(Entity.class).getAccessBits()).isEqualTo(AccessBits.ANNOTATION);

		assertThat(nativeContext.hasReflectionEntry(GeneratedValue.class)).isTrue();
		assertThat(nativeContext.getReflectionEntry(GeneratedValue.class).getAccessBits()).isEqualTo(AccessBits.ANNOTATION);

		assertThat(nativeContext.hasReflectionEntry(OneToMany.class)).isTrue();
		assertThat(nativeContext.getReflectionEntry(OneToMany.class).getAccessBits()).isEqualTo(AccessBits.ANNOTATION);

		assertThat(nativeContext.hasReflectionEntry(ManyToOne.class)).isTrue();
		assertThat(nativeContext.getReflectionEntry(ManyToOne.class).getAccessBits()).isEqualTo(AccessBits.ANNOTATION);
	}

	@Test
	public void shouldRegisterHibernateEnumTypeOnlyIfModelIsUsingEnums() {

		process(EntityWithEnum.class);

		assertThat(nativeContext.hasReflectionEntry(EnumType.class)).isTrue();
		assertThat(nativeContext.getReflectionEntry(EnumType.class).getAccessBits()).isEqualTo(AccessBits.LOAD_AND_CONSTRUCT);

		nativeContext.clear();

		process(Order.class);
		assertThat(nativeContext.hasReflectionEntry(EnumType.class)).isFalse();
	}

	@Test
	public void whatAboutOtherAnnotations/* like jackson */() {

		process(Order.class);

		assertThat(nativeContext.hasReflectionEntry(SomeAnnotation.class)).isFalse();
	}

	void process(Class<?> type) {
		processor.process(nativeContext, typeSystem.resolve(type).getDottedName(), Collections.emptyList());
	}
}

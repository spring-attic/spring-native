/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.sample.data.jpa.AuditingListener;
import org.springframework.sample.data.jpa.EntityWithListener;
import org.springframework.sample.data.jpa.LineItem;
import org.springframework.sample.data.jpa.NotAnEntity;
import org.springframework.sample.data.jpa.Order;
import org.springframework.sample.data.jpa.SomeAnnotation;

/**
 * Tests for {@link JpaConfigurationProcessor}.
 *
 * @author Christoph Strobl
 */
public class JpaConfigurationProcessorTests {

	@Test
	void shouldIncludeReachableTypes() {

		assertThat(process(Order.class)).satisfies(it -> {
			assertThat(it.hasReflectionEntry(Order.class)).isTrue();
			assertThat(it.hasReflectionEntry(LineItem.class)).isTrue();
			assertThat(it.hasReflectionEntry(NotAnEntity.class)).isFalse();
		});
	}

	@Test
	public void shouldRegisterReflectionAllowWriteForFinalFields/* looking at you hibernate */() {

		assertThat(process(Order.class)).satisfies(it -> {

			ClassDescriptor classDescriptor = it.getReflectionEntry(Order.class).toClassDescriptor();
			Optional<FieldDescriptor> id = classDescriptor.getFields().stream().filter(field -> {
				return field.getName().equals("id");
			}).findFirst();
			assertThat(id).isPresent().contains(new FieldDescriptor("id", true, true));
		});
	}

	@Test
	public void shouldNotDoFancyStuffForNonFinalFields/* looking at you hibernate */() {

		assertThat(process(Order.class)).satisfies(it -> {

			ClassDescriptor classDescriptor = it.getReflectionEntry(Order.class).toClassDescriptor();
			Optional<FieldDescriptor> name = classDescriptor.getFields().stream().filter(field -> {
				return field.getName().equals("name");
			}).findFirst();
			assertThat(name).isNotPresent();
		});
	}

	@Test
	public void shouldAddReflectionForJavaxPersistenceAnnotations() {

		assertThat(process(Order.class)).satisfies(it -> {

			assertThat(it.hasReflectionEntry(Entity.class)).isTrue();
			assertThat(it.hasReflectionEntry(GeneratedValue.class)).isTrue();
			assertThat(it.hasReflectionEntry(OneToMany.class)).isTrue();
			assertThat(it.hasReflectionEntry(ManyToOne.class)).isTrue();
		});
	}

	@Test
	public void whatAboutOtherAnnotations/* like jackson */() {

		assertThat(process(Order.class)).satisfies(it -> {
			assertThat(it.hasReflectionEntry(SomeAnnotation.class)).isFalse();
		});
	}

	@Test
	@Disabled("use a shadowing class loader maybe to test this?")
	public void shouldRegisterHibernateEnumTypeOnlyIfModelIsUsingEnums() {

//		assertThat(process(EntityWithEnum.class)).satisfies(it -> {
//			assertThat(it.hasReflectionEntry(org.hibernate.type.EnumType.class)).isTrue();
//		});
	}

	@Test
	public void shouldAddFullReflectionForEntityListener() {

		assertThat(process(EntityWithListener.class)).satisfies(it -> {
			assertThat(it.hasReflectionEntry(AuditingListener.class)).isTrue();
		});
	}


	NativeConfigRegistryHolder process(Class<?>... entities) {

		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new JpaConfigurationProcessor.JpaProcessor(this.getClass().getClassLoader()).process(new LinkedHashSet<>(Arrays.asList(entities)), registry);
		return new NativeConfigRegistryHolder(registry);
	}

	static class NativeConfigRegistryHolder {

		NativeConfigurationRegistry delegate;

		NativeConfigRegistryHolder(NativeConfigurationRegistry registry) {
			this.delegate = registry;
		}

		NativeReflectionEntry getReflectionEntry(Class<?> type) {
			return delegate.reflection().forType(type).build();
		}

		boolean hasReflectionEntry(Class<?> type) {
			return delegate.reflection().reflectionEntries().anyMatch(it -> it.getType().equals(type));
		}
	}
}

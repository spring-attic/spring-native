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
package org.springframework.data;

import java.lang.annotation.Documented;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.InitializationConfiguration;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ProxyConfiguration;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ReflectionConfiguration;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ResourcesConfiguration;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.SerializationConfiguration;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.BuildTimeBeanDefinitionsRegistrar;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.RepositoryDefinitionConfigurationProcessor.RepositoryConfiguration;
import org.springframework.data.RepositoryDefinitionConfigurationProcessor.RepositoryConfigurationContributor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.annotation.QueryAnnotation;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.domain.Page;
import org.springframework.data.geo.Point;
import org.springframework.data.repository.query.Param;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.sample.data.config.ConfigForTypeHavingDeclaredClass;
import org.springframework.sample.data.config.ConfigWithCustomImplementation;
import org.springframework.sample.data.config.ConfigWithCustomImplementation.CustomImplInterface;
import org.springframework.sample.data.config.ConfigWithCustomImplementation.RepositoryWithCustomImplementation;
import org.springframework.sample.data.config.ConfigWithCustomImplementation.RepositoryWithCustomImplementationImpl;
import org.springframework.sample.data.config.ConfigWithFragments;
import org.springframework.sample.data.config.ConfigWithFragments.CustomImplInterface1;
import org.springframework.sample.data.config.ConfigWithFragments.CustomImplInterface1Impl;
import org.springframework.sample.data.config.ConfigWithFragments.CustomImplInterface2;
import org.springframework.sample.data.config.ConfigWithFragments.CustomImplInterface2Impl;
import org.springframework.sample.data.config.ConfigWithFragments.RepositoryWithFragments;
import org.springframework.sample.data.config.ConfigWithQueryMethods;
import org.springframework.sample.data.config.ConfigWithQueryMethods.CustomerRepositoryWithQueryMethods;
import org.springframework.sample.data.config.ReactiveConfig;
import org.springframework.sample.data.config.ReactiveConfig.CustomerRepositoryReactive;
import org.springframework.sample.data.config.SimpleRepositoryConfig;
import org.springframework.sample.data.config.SimpleRepositoryConfig.CustomerRepository;
import org.springframework.sample.data.types.Address;
import org.springframework.sample.data.types.BaseEntity;
import org.springframework.sample.data.types.Customer;
import org.springframework.sample.data.types.DomainObjectWithSimpleTypesOnly;
import org.springframework.sample.data.types.LocationHolder;
import org.springframework.sample.data.types.ProjectionInterface;
import org.springframework.sample.data.types.WithDeclaredClass;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ObjectUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RepositoryDefinitionConfigurationProcessor}.
 *
 * @author Christoph Strobl
 */
public class RepositoryDefinitionConfigurationProcessorTests {

	@Test
	void computeReflectionForSimpleCrudRepository() {
		NativeConfigRegistryHolder registry = getNativeConfiguration(SimpleRepositoryConfig.class);
		assertThat(registry.reflection().reflectionEntries()).<Class>extracting(DefaultNativeReflectionEntry::getType)
				.contains(CustomerRepository.class) // Repository Interface
				.contains(BaseEntity.class, Customer.class, Address.class, LocationHolder.class) // User Domain Types
				.doesNotContain(Point.class) // Spring Data Domain Types
				.doesNotContain(Instant.class) // Types considered simple ones
				.contains(TypeAlias.class, Id.class, Persistent.class, Transient.class, PersistenceConstructor.class) // Spring Data Annotations
				.doesNotContain(Documented.class) // java.lang Annotations
				.doesNotContain(Component.class); // Spring Stereotype Annotations
	}

	@Test
	void computeReflectionForSimpleReactiveCrudRepository() {
		NativeConfigRegistryHolder registry = getNativeConfiguration(ReactiveConfig.class);
		assertThat(registry.reflection().reflectionEntries()).<Class>extracting(DefaultNativeReflectionEntry::getType)
				.contains(CustomerRepositoryReactive.class) // Repository Interface
				.contains(BaseEntity.class, Customer.class, Address.class, LocationHolder.class) // User Domain Types
				.doesNotContain(Point.class) // Spring Data Domain Types
				.doesNotContain(Instant.class) // Types considered simple ones
				.contains(TypeAlias.class, Id.class, Persistent.class, Transient.class, PersistenceConstructor.class) // Spring Data Annotations
				.doesNotContain(Documented.class) // java.lang Annotations
				.doesNotContain(Component.class); // Spring Stereotype Annotations
	}

	@Test
	void computeReflectionRepositoryQueryMethods() {
		NativeConfigRegistryHolder registry = getNativeConfiguration(ConfigWithQueryMethods.class);
		assertThat(registry.reflection().reflectionEntries()).<Class>extracting(DefaultNativeReflectionEntry::getType)
				.contains(CustomerRepositoryWithQueryMethods.class) // Repository Interface
				.contains(DomainObjectWithSimpleTypesOnly.class, ProjectionInterface.class) // User Domain Types
				.contains(Id.class, QueryAnnotation.class, Param.class) // Spring Data Annotations
				.doesNotContain(Documented.class) // java.lang Annotations
				.doesNotContain(Nullable.class) // javax. Annotation
				.doesNotContain(Page.class); // Spring Data Domain Type
	}

	@Test
	void computeReflectionForRepositoryWithCustomImplementation() {
		NativeConfigRegistryHolder registry = getNativeConfiguration(ConfigWithCustomImplementation.class);
		assertThat(registry.reflection().reflectionEntries()).<Class>extracting(DefaultNativeReflectionEntry::getType)
				.contains(RepositoryWithCustomImplementation.class) // Repository Interface
				.contains(CustomImplInterface.class) // Custom Implementation Interface
				.contains(RepositoryWithCustomImplementationImpl.class) // Custom Implementation target
				.contains(DomainObjectWithSimpleTypesOnly.class) // Domain Type Repository Interface
				.contains(BaseEntity.class, Customer.class, Address.class, LocationHolder.class) // Domain Types Custom Implementation
				.contains(TypeAlias.class, Id.class, Persistent.class, Transient.class, PersistenceConstructor.class) // Spring Data Annotations
				.doesNotContain(Documented.class) // java.lang Annotations
				.doesNotContain(Component.class); // Spring Stereotype Annotations
	}

	@Test
	void computeReflectionForRepositoryWithFragments() {
		NativeConfigRegistryHolder registry = getNativeConfiguration(ConfigWithFragments.class);
		assertThat(registry.reflection().reflectionEntries()).<Class>extracting(DefaultNativeReflectionEntry::getType)
				.contains(RepositoryWithFragments.class) // Repository Interface
				.contains(CustomImplInterface1.class) // Fragment 1 Interface
				.contains(CustomImplInterface2.class) // Fragment 2 Interface
				.contains(CustomImplInterface1Impl.class) // Fragment 1 Implementation target
				.contains(CustomImplInterface2Impl.class) // Fragment 1 Implementation target
				.contains(DomainObjectWithSimpleTypesOnly.class) // Domain Type Repository Interface
				.contains(BaseEntity.class, Customer.class, Address.class, LocationHolder.class) // Domain Types Custom Implementation
				.contains(TypeAlias.class, Id.class, Persistent.class, Transient.class, PersistenceConstructor.class, LastModifiedDate.class) // Spring Data Annotations
				.doesNotContain(Instant.class) // java.time
				.doesNotContain(Documented.class) // java.lang Annotations
				.doesNotContain(Component.class); // Spring Stereotype Annotations
	}


	@Test
	void onlyRegistersPreferredCtorIfPresent() {
		NativeConfigRegistryHolder registry = getNativeConfiguration(new RepositoryConfiguration(CustomerRepository.class));
		assertThat(registry.getReflectionEntry(Address.class))
				.satisfies(ctorWithArgs(String.class))
				.satisfies(doesNotContainsCtorFlag());
	}

	@Test
	@Disabled("if another component allows access to property descriptor methods, this will cause trouble")
	void onlyRegisterNonTransientFieldsButKeepTheAnnotation() {
		NativeConfigRegistryHolder registry = getNativeConfiguration(new RepositoryConfiguration(CustomerRepository.class));
		assertThat(registry.getReflectionEntry(Customer.class))
				.satisfies(onlyFields("id", "modifiedAt"));
		assertThat(registry.getProxyEntries(Transient.class)).hasSize(1);
	}

	@Test
	void writesFlagForDeclaredClassesIfPresent() {

		NativeConfigRegistryHolder registry = getNativeConfiguration(ConfigForTypeHavingDeclaredClass.class);
		assertThat(registry.getReflectionEntry(WithDeclaredClass.class))
				.satisfies(containsAccess(TypeAccess.DECLARED_CLASSES));
	}

	@Test
	void doesNotWriteFlagForDeclaredClassesIfNotPresent() {

		NativeConfigRegistryHolder registry = getNativeConfiguration(SimpleRepositoryConfig.class);
		assertThat(registry.getReflectionEntry(Customer.class))
				.satisfies(doesNotContainAccess(TypeAccess.DECLARED_CLASSES));
	}

	private NativeConfigRegistryHolder getNativeConfiguration(Class<?> configurationClass) {
		GenericApplicationContext context = new AnnotationConfigApplicationContext();
		context.registerBean(configurationClass);
		BuildTimeBeanDefinitionsRegistrar registrar = new BuildTimeBeanDefinitionsRegistrar();
		ConfigurableListableBeanFactory beanFactory = registrar.processBeanDefinitions(context);
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new RepositoryDefinitionConfigurationProcessor().process(beanFactory, registry);
		return new NativeConfigRegistryHolder(registry);
	}

	private NativeConfigRegistryHolder getNativeConfiguration(RepositoryConfiguration configuration) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new RepositoryConfigurationContributor(this.getClass().getClassLoader(), registry).writeConfiguration(configuration);
		return new NativeConfigRegistryHolder(registry);
	}

	static class NativeConfigRegistryHolder {

		NativeConfigurationRegistry delegate;

		NativeConfigRegistryHolder(NativeConfigurationRegistry registry) {
			this.delegate = registry;
		}

		DefaultNativeReflectionEntry getReflectionEntry(Class<?> type) {
			return delegate.reflection().forType(type).build();
		}

		public ReflectionConfiguration reflection() {
			return delegate.reflection();
		}

		public ReflectionConfiguration jni() {
			return delegate.jni();
		}

		public ResourcesConfiguration resources() {
			return delegate.resources();
		}

		public ProxyConfiguration proxy() {
			return delegate.proxy();
		}

		public InitializationConfiguration initialization() {
			return delegate.initialization();
		}

		public SerializationConfiguration serialization() {
			return delegate.serialization();
		}

		public Set<String> options() {
			return delegate.options();
		}

		@SuppressWarnings("unchecked")
		public List<NativeProxyEntry> getProxyEntries(Class<?> type) {
			return delegate.proxy().getEntries().stream()
					.filter(it -> {
						Object targetClass = ReflectionTestUtils.getField(it, "targetClass");
						if (ObjectUtils.nullSafeEquals(targetClass, it)) {
							return true;
						}
						List<Class<?>> interfaces = (List<Class<?>>) ReflectionTestUtils.getField(it, "interfaceTypes");
						if (ObjectUtils.isEmpty(interfaces)) {
							return false;
						}
						return ObjectUtils.nullSafeEquals(interfaces.iterator().next(), type);
					}).collect(Collectors.toList());
		}
	}

	private Consumer<DefaultNativeReflectionEntry> ctorWithArgs(Class<?>... args) {
		return (entry) -> {
			List<Constructor<?>> constructors = ReflectionUtils.findConstructors(entry.getType(),
					p -> ObjectUtils.nullSafeEquals(p.getParameterTypes(), args));
			assertThat(entry.getConstructors()).contains(constructors.iterator().next());
		};
	}

	private Consumer<DefaultNativeReflectionEntry.Builder> containsCtor(Constructor<?> constructor) {
		return (builder) -> {
			DefaultNativeReflectionEntry entry = builder.build();
			assertThat(entry.getConstructors()).contains(constructor);
		};
	}

	private Consumer<DefaultNativeReflectionEntry> doesNotContainsCtorFlag() {
		return (entry) -> assertThat(entry.getAccess()).doesNotContain(TypeAccess.DECLARED_CONSTRUCTORS);
	}

	private Consumer<DefaultNativeReflectionEntry> onlyFields(String... fields) {
		return (entry) -> assertThat(entry.getFields()).map(Field::getName).containsExactlyInAnyOrder(fields);
	}

	private Consumer<DefaultNativeReflectionEntry> containsAccess(TypeAccess... access) {
		return (entry) -> assertThat(entry.getAccess()).contains(access);
	}

	private Consumer<DefaultNativeReflectionEntry> doesNotContainAccess(TypeAccess... access) {
		return (entry) -> assertThat(entry.getAccess()).doesNotContain(access);
	}

	private Consumer<DefaultNativeReflectionEntry.Builder> allConstructors() {
		return (builder) -> {
			DefaultNativeReflectionEntry entry = builder.build();
			assertThat(entry.getAccess()).contains(TypeAccess.DECLARED_CONSTRUCTORS);
		};
	}

	private Consumer<DefaultNativeReflectionEntry> allDeclaredMethods(Class<?> type) {
		return (entry) -> {
			assertThat(entry.getType()).isEqualTo(type);
			assertThat(entry.getAccess()).containsOnly(TypeAccess.DECLARED_METHODS);
		};
	}

	private Consumer<DefaultNativeReflectionEntry> annotation(Class<?> type) {
		return (entry) -> {
			assertThat(entry.getType()).isEqualTo(type);
			assertThat(entry.getAccess()).containsOnly(TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS);
		};
	}

}

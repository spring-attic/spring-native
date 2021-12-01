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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.support.BeanFactoryProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.index.CandidateComponentsIndex;
import org.springframework.context.index.CandidateComponentsIndexLoader;
import org.springframework.core.annotation.AnnotationFilter;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Strobl
 */
public class JpaConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	private static Log logger = LogFactory.getLog(JpaConfigurationProcessor.class);

	private static final String JPA_ENTITY = "javax.persistence.Entity";
	private static final String JPA_PERSISTENCE_CONTEXT = "javax.persistence.PersistenceContext";
	private static final String JPA_ENTITY_LISTENERS = "javax.persistence.EntityListeners";

	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {

		if (ClassUtils.isPresent(JPA_ENTITY, beanFactory.getBeanClassLoader())) {
			logger.debug("JPA detected - processing types.");
			new JpaPersistenceContextProcessor().process(beanFactory, registry);
			new JpaEntityProcessor(beanFactory.getBeanClassLoader()).process(beanFactory, registry);
		}
	}

	/**
	 * Processor to inspect components for fields that require a {@literal javax.persistence.PersistenceContext}.
	 */
	static class JpaPersistenceContextProcessor {

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			new BeanFactoryProcessor(beanFactory).processBeans(
					(beanType) -> TypeUtils.hasAnnotatedField(beanType, JPA_PERSISTENCE_CONTEXT),
					(beanName, beanType) -> registry.reflection()
							.forType(beanType)
							.withFields(TypeUtils.getAnnotatedField(beanType, JPA_PERSISTENCE_CONTEXT).toArray(new Field[0])));
		}
	}

	/**
	 * Processor to inspect user domain types annotated with {@literal javax.persistence.Entity}.
	 */
	static class JpaEntityProcessor {

		private final AnnotationFilter annotationFilter;

		private final Class<? extends Annotation> entityAnnotation;
		private final Set<JpaImplementation> jpaImplementations;
		private final ClassLoader classLoader;

		public JpaEntityProcessor(ClassLoader classLoader) {

			this.classLoader = classLoader;
			entityAnnotation = loadIfPresent(JPA_ENTITY, classLoader);
			jpaImplementations = new LinkedHashSet<>(Arrays.asList(new HibernateJpaImplementation()))
					.stream()
					.filter(it -> it.isAvailable(classLoader))
					.collect(Collectors.toSet());

			HashSet<String> availableNamespaces = new HashSet<>();
			availableNamespaces.add("javax.persistence");
			jpaImplementations.forEach(it -> availableNamespaces.add(it.getNamespace()));
			annotationFilter = AnnotationFilter.packages(availableNamespaces.toArray(new String[0]));
		}

		/**
		 * Scan the path for JPA entities and process those.
		 * Tries to look up types within the {@literal spring.components} index first and will use types discovered there if present.
		 * Once no entities could be found in the index we'll try to find a component that defines an {@literal EntityScan} and read the
		 * {@literal basePackages} attribute to do some potentially slow class path scanning.
		 *
		 * @param registry must not be {@literal null}.
		 */
		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {

			Set<Class<?>> entities = readEntitiesFromIndex();

			if (!entities.isEmpty()) {
				process(entities, registry);
				return;
			}

			new BeanFactoryProcessor(beanFactory).processBeans(
					(beanType) -> MergedAnnotations.from(beanType).isPresent("org.springframework.boot.autoconfigure.domain.EntityScan"),
					(beanName, beanType) -> {

						MergedAnnotation<Annotation> entityScanAnnotation = MergedAnnotations.from(beanType).get("org.springframework.boot.autoconfigure.domain.EntityScan");
						String[] basePackages = entityScanAnnotation.getStringArray("basePackages");
						Set<Class<?>> resolvedTypes = new LinkedHashSet<>();

						if (ObjectUtils.isEmpty(basePackages)) {
							resolvedTypes.addAll(scanForEntities(beanType.getPackageName()));
						} else {
							for (String basePackage : basePackages) {
								resolvedTypes.addAll(scanForEntities(basePackage));
							}
						}
						process(resolvedTypes, registry);
					});
		}

		/**
		 * Process JPA to level entities.
		 *
		 * @param entities
		 * @param registry
		 */
		void process(Set<Class<?>> entities, NativeConfigurationRegistry registry) {
			TypeModelProcessor typeModelProcessor = new TypeModelProcessor();
			entities.forEach(type -> {

				/*
				 * If an EntityListener is defined we need to inspect the target and make sure
				 * reflection is configured so the methods can be invoked
				 */
				MergedAnnotation<Annotation> entityListener = MergedAnnotations.from(type).get(JPA_ENTITY_LISTENERS);
				if (entityListener.isPresent()) {
					Class<?>[] values = entityListener.getClassArray("value");
					for (Class<?> listener : values) {
						registry.reflection().forType(listener).withAccess(TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS);
					}
				}

				/*
				 * Retrieve all reachable types and register reflection for it.
				 * Final fields require special treatment having allowWrite set.
				 */
				typeModelProcessor.inspect(type).forEach(typeModel -> {

					DefaultNativeReflectionEntry.Builder builder = registry.reflection().forType(typeModel.getType());
					builder.withAccess(TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS, TypeAccess.DECLARED_CONSTRUCTORS);

					if(typeModel.hasDeclaredClasses()) {
						builder.withAccess(TypeAccess.DECLARED_CLASSES);
					}

					typeModel.doWithFields(field -> {
						if (Modifier.isFinal(field.getModifiers())) {
							builder.withField(field, DefaultNativeReflectionEntry.FieldAccess.ALLOW_WRITE, DefaultNativeReflectionEntry.FieldAccess.UNSAFE);
						}
					});

					typeModel.doWithAnnotatedElements(element -> {
						writeAnnotationConfigurationFor(element, registry);
					});

					jpaImplementations.forEach(it -> it.process(typeModel, classLoader, registry));
				});
			});
		}

		/**
		 * Write the required configuration for annotations that belong to the persistence namespace
		 *
		 * @param element
		 * @param registry
		 */
		private void writeAnnotationConfigurationFor(AnnotatedElement element, NativeConfigurationRegistry registry) {
			TypeUtils.resolveAnnotationsFor(element)
					.map(MergedAnnotation::getType)
					.filter(annotationFilter::matches)
					.forEach(annotation -> {
						registry.reflection().forType(annotation).withAccess(TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS);
					});
			if (element instanceof Constructor) {
				for (Parameter parameter : ((Constructor<?>) element).getParameters()) {
					writeAnnotationConfigurationFor(parameter, registry);
				}
			}
			if (element instanceof Method) {
				for (Parameter parameter : ((Method) element).getParameters()) {
					writeAnnotationConfigurationFor(parameter, registry);
				}
			}
		}

		/**
		 * Scan the {@literal spring.components} index for types annotated with {@link #JPA_ENTITY}
		 *
		 * @return the {@link Set} of top level entities.
		 */
		Set<Class<?>> readEntitiesFromIndex() {

			CandidateComponentsIndex index = CandidateComponentsIndexLoader.loadIndex(classLoader);
			if (index == null) {
				return Collections.emptySet();
			}
			Set<String> candidateTypes = index.getCandidateTypes("*", JPA_ENTITY);
			return candidateTypes.stream().map(it -> loadIfPresent(it, classLoader)).filter(it -> it != null).collect(Collectors.toSet());
		}

		/**
		 * Scan the classpath for types annotated with {@link #JPA_ENTITY}
		 *
		 * @param basePackage must not be null nor empty.
		 * @return the {@link Set} of top level entities.
		 */
		Set<Class<?>> scanForEntities(String basePackage) {

			if (entityAnnotation == null || !StringUtils.hasText(basePackage)) {
				return Collections.emptySet();
			}

			ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false, new StandardEnvironment());
			componentProvider.setResourceLoader(new DefaultResourceLoader(classLoader));
			componentProvider.addIncludeFilter(new AnnotationTypeFilter(entityAnnotation));

			Set<Class<?>> entities = new LinkedHashSet<>();
			for (BeanDefinition definition : componentProvider.findCandidateComponents(basePackage)) {

				Class<?> type = loadIfPresent(definition.getBeanClassName(), classLoader);
				if (type == null) {
					continue;
				}

				logger.debug("JPA entity" + type + " found on path.");
				entities.add(type);
			}
			return entities;
		}
	}

	private interface JpaImplementation {

		String getNamespace();

		boolean isAvailable(ClassLoader classLoader);

		void process(TypeModel type, ClassLoader classLoader, NativeConfigurationRegistry registry);
	}

	private static class HibernateJpaImplementation implements JpaImplementation {

		private Boolean present;

		@Override
		public String getNamespace() {
			return "org.hibernate";
		}

		@Override
		public boolean isAvailable(ClassLoader classLoader) {
			if (present == null) {
				present = ClassUtils.isPresent("org.hibernate.Hibernate", classLoader);
			}
			return present;
		}

		@Override
		public void process(TypeModel type, ClassLoader classLoader, NativeConfigurationRegistry registry) {
			if (!type.getType().isEnum()) {
				return;
			}
			Class<Object> objectClass = loadIfPresent("org.hibernate.type.EnumType", classLoader);
			if (objectClass != null) {
				registry.reflection().forType(objectClass).withAccess(TypeAccess.DECLARED_CONSTRUCTORS);
			}
		}
	}

	private static <T> Class<T> loadIfPresent(String name, ClassLoader classLoader) {

		try {
			return (Class<T>) ClassUtils.forName(name, classLoader);
		} catch (ClassNotFoundException e) {
			//
		}
		return null;
	}
}

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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.AotProxyNativeConfigurationProcessor.ComponentCallback;
import org.springframework.boot.context.AotProxyNativeConfigurationProcessor.ComponentFilter;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationFilter;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.nativex.hint.Flag;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

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
			new JpaEntityProcessor(beanFactory.getBeanClassLoader()).process(registry);
		}
	}

	/**
	 * Processor to inspect components for fields that require a {@literal javax.persistence.PersistenceContext}.
	 */
	static class JpaPersistenceContextProcessor {

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			doWithComponents(beanFactory,
					(beanName, beanType) -> {
						registry.reflection()
								.forType(beanType)
								.withFields(TypeUtils.getAnnotatedField(beanType, JPA_PERSISTENCE_CONTEXT).toArray(new Field[0]));
					},
					(beanName, beanType) -> {
						return TypeUtils.hasAnnotatedField(beanType, JPA_PERSISTENCE_CONTEXT);
					});
		}

		// TODO: copied from AotProxyNativeConfigurationProcessor so maybe find a better location for it in some utils?
		static void doWithComponents(ConfigurableListableBeanFactory beanFactory, ComponentCallback callback,
				ComponentFilter filter) {
			beanFactory.getBeanNamesIterator().forEachRemaining((beanName) -> {
				Class<?> beanType = beanFactory.getType(beanName);
				MergedAnnotation<Component> componentAnnotation = MergedAnnotations.from(beanType).get(Component.class);
				if (componentAnnotation.isPresent()) {
					if (filter == null || filter.test(beanName, beanType)) {
						callback.invoke(beanName, beanType);
					}
				}
			});
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
		 *
		 * @param registry must not be {@literal null}.
		 */
		void process(NativeConfigurationRegistry registry) {
			process(scanForEntities(), registry);
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
						registry.reflection().forType(listener).withFlags(Flag.allDeclaredConstructors, Flag.allPublicMethods);
					}
				}

				/*
				 * Retrieve all reachable types and register reflection for it.
				 * Final fields require special treatment having allowWrite set.
				 */
				typeModelProcessor.inspect(type).forEach(typeModel -> {

					DefaultNativeReflectionEntry.Builder builder = registry.reflection().forType(typeModel.getType());
					builder.withFlags(Flag.allDeclaredFields, Flag.allDeclaredMethods, Flag.allDeclaredConstructors);

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
						registry.reflection().forType(annotation).withFlags(Flag.allPublicConstructors, Flag.allPublicMethods);
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
		 * Scan the classpath for types annotated with {@link #JPA_MARKER}
		 *
		 * @return the {@link Set} of top level entities.
		 */
		Set<Class<?>> scanForEntities() {

			if (entityAnnotation == null) {
				return Collections.emptySet();
			}

			ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false, new StandardEnvironment());
			componentProvider.setResourceLoader(new DefaultResourceLoader(classLoader));
			componentProvider.addIncludeFilter(new AnnotationTypeFilter(entityAnnotation));

			Set<Class<?>> entities = new LinkedHashSet<>();
			for (BeanDefinition definition : componentProvider.findCandidateComponents("")) {

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
				registry.reflection().forType(objectClass).withFlags(Flag.allDeclaredConstructors);
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

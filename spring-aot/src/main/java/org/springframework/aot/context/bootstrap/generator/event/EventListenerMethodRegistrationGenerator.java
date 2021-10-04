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

package org.springframework.aot.context.bootstrap.generator.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.EventListenerFactory;
import org.springframework.context.event.EventListenerMetadata;
import org.springframework.context.event.EventListenerMethodProcessor;
import org.springframework.context.event.EventListenerRegistrar;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Detect beans with methods annotated with {@link EventListener} and write the code
 * necessary to register them as listeners. This essentially replaces what
 * {@link EventListenerMethodProcessor} does at runtime.
 *
 * @author Stephane Nicoll
 */
public class EventListenerMethodRegistrationGenerator {

	private static final String REGISTRAR_BEAN_NAME = "org.springframework.aot.EventListenerRegistrar";

	private static final Log logger = LogFactory.getLog(EventListenerMethodRegistrationGenerator.class);

	private final ConfigurableListableBeanFactory beanFactory;

	private final Map<String, EventListenerFactory> eventListenerFactories;

	public EventListenerMethodRegistrationGenerator(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		this.eventListenerFactories = lookupEventListenerFactories(beanFactory);
	}

	private static Map<String, EventListenerFactory> lookupEventListenerFactories(ConfigurableListableBeanFactory beanFactory) {
		Map<String, EventListenerFactory> candidates = beanFactory.getBeansOfType(EventListenerFactory.class, false, false);
		List<EventListenerFactory> factories = new ArrayList<>(candidates.values());
		AnnotationAwareOrderComparator.sort(factories);
		Map<String, EventListenerFactory> result = new LinkedHashMap<>();
		factories.forEach((factory) -> {
			String beanName = candidates.entrySet().stream().filter((entry) -> entry.getValue().equals(factory))
					.map(Entry::getKey).findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Unable to locate bean name for " + candidates));
			result.put(beanName, factory);
		});
		return result;
	}

	/**
	 * Generate the necessary {@code statements} to register event listeners annotated
	 * method in the context.
	 * @param context the writer context
	 * @param code the builder to use to add the registration statement(s)
	 */
	public void writeEventListenersRegistration(BootstrapWriterContext context, CodeBlock.Builder code) {
		List<EventListenerMetadataGenerator> eventGenerators = new ArrayList<>();
		for (String beanName : this.beanFactory.getBeanDefinitionNames()) {
			eventGenerators.addAll(process(beanName));
		}
		if (eventGenerators.isEmpty()) {
			return; // No listener detected
		}
		eventGenerators.forEach((eventGenerator) -> eventGenerator.registerReflectionMetadata(context.getNativeConfigurationRegistry()));
		code.add("context.registerBean($S, $T.class, () -> new $L(context, ", REGISTRAR_BEAN_NAME,
				EventListenerRegistrar.class, EventListenerRegistrar.class.getSimpleName());
		code.add(writeEventListenersMetadataRegistration(context, eventGenerators));
		code.addStatement("))");
	}

	private CodeBlock writeEventListenersMetadataRegistration(BootstrapWriterContext context,
			List<EventListenerMetadataGenerator> eventGenerators) {
		String targetPackageName = context.getPackageName();
		MultiValueMap<String, EventListenerMetadataGenerator> generatorsPerPackage =
				indexEventListeners(eventGenerators, targetPackageName);
		Builder registrations = CodeBlock.builder();
		boolean multipleMethods = generatorsPerPackage.size() > 1;
		if (multipleMethods) {
			registrations.add("$T.of(\n", List.class).indent().indent();
		}
		Iterator<Entry<String, List<EventListenerMetadataGenerator>>> it =
				generatorsPerPackage.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<EventListenerMetadataGenerator>> entry = it.next();
			MethodSpec method = createEventListenersMetadataMethod(entry.getValue());
			BootstrapClass bootstrapClass = context.getBootstrapClass(entry.getKey());
			bootstrapClass.addMethod(method);
			registrations.add("$T.$N()", bootstrapClass.getClassName(), method);
			if (it.hasNext()) {
				registrations.add(",\n");
			}
		}
		if (multipleMethods) {
			registrations.add("\n").unindent().unindent();
			registrations.add(").stream().flatMap($T::stream).collect($T.toList())",
					Collection.class, Collectors.class);
		}
		return registrations.build();
	}

	private MultiValueMap<String, EventListenerMetadataGenerator> indexEventListeners(List<EventListenerMetadataGenerator> eventGenerators,
			String targetPackageName) {
		MultiValueMap<String, EventListenerMetadataGenerator> generatorsPerPackage = new LinkedMultiValueMap<>();
		for (EventListenerMetadataGenerator eventGenerator : eventGenerators) {
			if (isAccessibleFrom(eventGenerator.getBeanType(), targetPackageName)) {
				generatorsPerPackage.add(targetPackageName, eventGenerator);
			}
			else {
				generatorsPerPackage.add(eventGenerator.getBeanType().getPackageName(), eventGenerator);
			}
		}
		return generatorsPerPackage;
	}

	private MethodSpec createEventListenersMetadataMethod(List<EventListenerMetadataGenerator> generators) {
		Builder code = CodeBlock.builder();
		code.add("return $T.of(", List.class);
		code.add("\n").indent();
		Iterator<EventListenerMetadataGenerator> it = generators.iterator();
		while (it.hasNext()) {
			it.next().writeEventListenerMetadata(code); ;
			if (it.hasNext()) {
				code.add(",\n");
			}
		}
		code.add("\n").unindent().addStatement(")");
		return MethodSpec.methodBuilder("getEventListenersMetadata")
				.returns(ParameterizedTypeName.get(List.class, EventListenerMetadata.class))
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC).addCode(code.build()).build();
	}

	public List<EventListenerMetadataGenerator> process(String beanName) {
		if (!ScopedProxyUtils.isScopedTarget(beanName)) {
			Class<?> type = null;
			try {
				type = AutoProxyUtils.determineTargetClass(this.beanFactory, beanName);
			}
			catch (Throwable ex) {
				// An unresolvable bean type, probably from a lazy bean - let's ignore it.
				if (logger.isDebugEnabled()) {
					logger.debug("Could not resolve target class for bean with name '" + beanName + "'", ex);
				}
			}
			if (type != null) {
				if (ScopedObject.class.isAssignableFrom(type)) {
					try {
						Class<?> targetClass = AutoProxyUtils.determineTargetClass(this.beanFactory,
								ScopedProxyUtils.getTargetBeanName(beanName));
						if (targetClass != null) {
							type = targetClass;
						}
					}
					catch (Throwable ex) {
						// An invalid scoped proxy arrangement - let's ignore it.
						if (logger.isDebugEnabled()) {
							logger.debug("Could not resolve target bean for scoped proxy '" + beanName + "'", ex);
						}
					}
				}
				try {
					return processBean(beanName, type);
				}
				catch (Throwable ex) {
					throw new BeanInitializationException(
							"Failed to process @EventListener " + "annotation on bean with name '" + beanName + "'",
							ex);
				}
			}
		}
		return Collections.emptyList();
	}

	private List<EventListenerMetadataGenerator> processBean(String beanName, Class<?> targetType) {
		List<EventListenerMetadataGenerator> result = new ArrayList<>();
		if (AnnotationUtils.isCandidateClass(targetType, EventListener.class)) {
			Map<Method, EventListener> annotatedMethods = null;
			try {
				annotatedMethods = MethodIntrospector.selectMethods(targetType,
						(MethodIntrospector.MetadataLookup<EventListener>) (method) -> AnnotatedElementUtils
								.findMergedAnnotation(method, EventListener.class));
			}
			catch (Throwable ex) {
				// An unresolvable type in a method signature, probably from a lazy bean -
				// let's ignore it.
				if (logger.isDebugEnabled()) {
					logger.debug("Could not resolve methods for bean with name '" + beanName + "'", ex);
				}
			}

			if (CollectionUtils.isEmpty(annotatedMethods)) {
				if (logger.isTraceEnabled()) {
					logger.trace("No @EventListener annotations found on bean class: " + targetType.getName());
				}
			}
			else {
				// Non-empty set of methods
				for (Method method : annotatedMethods.keySet()) {
					for (Entry<String, EventListenerFactory> entry : this.eventListenerFactories.entrySet()) {
						if (entry.getValue().supportsMethod(method)) {
							String factoryBeanName = (!entry.getKey()
									.equals(AnnotationConfigUtils.EVENT_LISTENER_FACTORY_BEAN_NAME)) ? entry.getKey()
									: null;
							result.add(new EventListenerMetadataGenerator(beanName, targetType, method,
									factoryBeanName));
							break;
						}
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug(annotatedMethods.size() + " @EventListener methods processed on bean '" + beanName
							+ "': " + annotatedMethods);
				}
			}
		}
		return result;
	}

	private static boolean isAccessibleFrom(Class<?> beanType, String packageName) {
		return isAccessible(packageName, beanType.getModifiers(),
				beanType.getPackageName());
	}

	private static boolean isAccessible(String packageName, int modifiers, String actualPackageName) {
		return java.lang.reflect.Modifier.isPublic(modifiers) || packageName.equals(actualPackageName);
	}

}

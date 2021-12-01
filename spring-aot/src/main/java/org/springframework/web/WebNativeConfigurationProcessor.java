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

package org.springframework.web;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationUtils;
import org.springframework.aot.support.BeanFactoryProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.util.ClassUtils;

/**
 * Basic native configuration processor that adds reflective access to types used in controller mappings if they are non
 * JDK or Spring Framework types.
 *
 * @author Andy Clement
 */
public class WebNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {
	
	private static Log logger = LogFactory.getLog(WebNativeConfigurationProcessor.class);

	protected final static String CONTROLLER_ANNOTATION_NAME = "org.springframework.stereotype.Controller";

	protected final static String MAPPING_ANNOTATION_NAME = "org.springframework.web.bind.annotation.Mapping";

	protected final static String MESSAGE_MAPPING_ANNOTATION_NAME = "org.springframework.messaging.handler.annotation.MessageMapping";

	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		if (ClassUtils.isPresent(CONTROLLER_ANNOTATION_NAME, beanFactory.getBeanClassLoader())) {
			new Processor().process(beanFactory, registry);
		}
	}

	/**
	 * Analyze a type and dig into it to add required access. This works but could probably be much smarter, so it is probably adding too much. The webmvc-kotlin will fail without this analysis.
	 */
	private static void recursivelyAnalyzeSignatureRelatedType(NativeConfigurationRegistry registry, Class<?> clazz, Set<String> added) {
		for (Field field: clazz.getDeclaredFields()) {
			Set<Class<?>> fieldSignatureTypes = NativeConfigurationUtils.collectTypesInSignature(field);
			for (Class<?> fieldSignatureType: fieldSignatureTypes) {
				String name = fieldSignatureType.getName();
				if (!ignore(name) && added.add(name)) {
					// TODO we are not included the hierarchy of the target here, is that OK because we are using allPublic* ?
					registry.reflection().forType(fieldSignatureType).withAccess(TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_FIELDS, TypeAccess.PUBLIC_FIELDS, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS);
					recursivelyAnalyzeSignatureRelatedType(registry, fieldSignatureType, added);
				}
			}
		}
	}

	private static boolean ignore(String name) {
		return (name.startsWith("java.") ||
				name.startsWith("org.springframework.ui.") ||
				name.startsWith("org.springframework.validation."));
	}

	private static class Processor {

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			final Set<String> added = new HashSet<>();
			new BeanFactoryProcessor(beanFactory).processBeans(this::isController,
				(beanName, controllerType) -> {
					logger.debug("reviewing mappings in controller "+controllerType);
					for (Method controllerMethod: controllerType.getDeclaredMethods()) {
						MergedAnnotations mas = MergedAnnotations.from(controllerMethod,SearchStrategy.TYPE_HIERARCHY);
						if (mas.isPresent(MAPPING_ANNOTATION_NAME) || mas.isPresent(MESSAGE_MAPPING_ANNOTATION_NAME)) {
							List<Class<?>> toProcess = new ArrayList<>();
							toProcess.addAll(NativeConfigurationUtils.collectTypesInSignature(controllerMethod));
							for (Class<?> clazz: toProcess) {
								String name = clazz.getName();
								if (name.startsWith("java.") ||
									name.startsWith("org.springframework.ui.") ||
									name.startsWith("org.springframework.validation.")) {
									continue;
								}
								if (added.add(name)) {
									registry.reflection().forType(clazz).withAccess(TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_FIELDS, TypeAccess.PUBLIC_FIELDS, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS);
									recursivelyAnalyzeSignatureRelatedType(registry, clazz, added);
									logger.debug("adding reflective access to "+added+" (whilst introspecting controller: "+controllerType.getName()+" mapping: "+controllerMethod.getName()+")");
								}
							}
						}
					}
				});
		}
		
		private boolean isController(Class<?> beanType) {
			MergedAnnotations mergedAnnotations = MergedAnnotations.from(beanType,SearchStrategy.TYPE_HIERARCHY);
			return mergedAnnotations.get(CONTROLLER_ANNOTATION_NAME).isPresent();
		}

	}

}

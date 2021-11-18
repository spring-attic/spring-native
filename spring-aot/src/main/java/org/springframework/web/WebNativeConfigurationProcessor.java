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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.AotProxyNativeConfigurationProcessor;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.nativex.hint.Flag;
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

	public static Set<Class<?>> collectTypesInSignature(Method controllerMethod) {
		Set<Class<?>> collector = new TreeSet<>((c1,c2) -> c1.getName().compareTo(c2.getName()));
		walk(controllerMethod.getGenericReturnType(), collector);
		for (Type parameterType: controllerMethod.getGenericParameterTypes()) {
			walk(parameterType, collector);
		}
		return collector;
	}

	// TODO does this handle all relevant cases?
	private static void walk(Type type, Set<Class<?>> collector) {
		if (type instanceof GenericArrayType) {
			GenericArrayType gaType = (GenericArrayType)type;
			walk(gaType.getGenericComponentType(), collector);
		} else if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType)type;
			walk(pType.getRawType(), collector);
			for (Type typeArg: pType.getActualTypeArguments()) {
				walk(typeArg, collector);
			}
		} else if (type instanceof TypeVariable) {
			
		} else if (type instanceof WildcardType) {
			
		} else if (type instanceof Class){
			Class<?> clazz = (Class<?>)type;
			if (!clazz.isPrimitive()) {
				collector.add((Class<?>)type);
			}
		}
	}

	private static class Processor {

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			final Set<String> added = new HashSet<>();
			AotProxyNativeConfigurationProcessor.doWithComponents(beanFactory,
				(beanName, controllerType) -> {
					logger.debug("reviewing mappings in controller "+controllerType);
					for (Method controllerMethod: controllerType.getMethods()) {
						MergedAnnotations mas = MergedAnnotations.from(controllerMethod,SearchStrategy.TYPE_HIERARCHY);
						if (mas.isPresent(MAPPING_ANNOTATION_NAME) || mas.isPresent(MESSAGE_MAPPING_ANNOTATION_NAME)) {
							List<Class<?>> toProcess = new ArrayList<>();
							toProcess.addAll(collectTypesInSignature(controllerMethod));
							for (Class<?> clazz: toProcess) {
								String name = clazz.getName();
								if (name.startsWith("java.") ||
									name.startsWith("org.springframework.ui.") ||
									name.startsWith("org.springframework.validation.")) {
									continue;
								}
								if (added.add(name)) {
									registry.reflection().forType(clazz).withFlags(Flag.allDeclaredConstructors, Flag.allPublicConstructors, Flag.allDeclaredFields, Flag.allPublicFields, Flag.allDeclaredMethods, Flag.allPublicMethods);
									// TODO this code was in the original component processor but I have not activated it here - not *sure* we still need it
//									Set<String> added = imageContext.addReflectiveAccessHierarchy(typename, AccessBits.FULL_REFLECTION);
//									analyze(imageContext, type, added);
									logger.debug("adding reflective access to "+added+" (whilst introspecting controller: "+controllerType.getName()+" mapping: "+controllerMethod.getName()+")");
								}
							}
						}
					}
				},
				(beanName, beanType) -> {
					logger.debug("Checking "+beanType);
		            MergedAnnotations mergedAnnotations = MergedAnnotations.from(beanType,SearchStrategy.TYPE_HIERARCHY);
		            return mergedAnnotations.get(CONTROLLER_ANNOTATION_NAME).isPresent();
				});
		}

	}

//	private void analyze(NativeContext imageContext, Type type, Set<String> added) {
//		List<Field> fields = type.getFields();
//		for (Field field: fields) {
//			Set<String> fieldTypenames = field.getTypesInSignature();
//			for (String fieldTypename: fieldTypenames) {
//				if (fieldTypename == null) {
//					continue;
//				}
//				String dottedFieldTypename = fieldTypename.replace("/", ".");
//				if (!ignore(dottedFieldTypename) && added.add(dottedFieldTypename)) {
//					added.addAll(imageContext.addReflectiveAccessHierarchy(dottedFieldTypename, AccessBits.FULL_REFLECTION));
//					// Recursive analysis - helps with something like a Vets type that includes a List<Vet>. Vet gets
//					// recognized too.
//					analyze(imageContext, imageContext.getTypeSystem().resolveDotted(dottedFieldTypename,true), added);
//				}
//			}
//		}
//	}
//
//	private boolean ignore(String name) {
//		return (name.startsWith("java.") ||
//				name.startsWith("org.springframework.ui.") ||
//				name.startsWith("org.springframework.validation."));
//	}
//}

}

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

package org.springframework.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
import org.springframework.aot.support.BeanFactoryProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AliasFor;

/**
 * Recognize components that rely on synthesized annotation proxies and register the required proxies.
 *
 * @author Andy Clement
 */
public class SynthesizedAnnotationNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {
	
	private static Log logger = LogFactory.getLog(SynthesizedAnnotationNativeConfigurationProcessor.class);

	private static final Predicate<Class<?>> IsSpringAnnotation =
			anno -> anno.getName().startsWith("org.springframework");

	// The @AliasFor'd attributes don't always seem to be accessed via a proxy so there is no
	// need to create a proxy. This is the list that don't seem to need a proxy (could be 
	// smarter - and indeed if others are not required due to only being used during Aot
	// processing, they will never need a proxy)
	private static final String[] DONT_NEED_PROXY = new String[] {
			"org.springframework.boot.autoconfigure.SpringBootApplication",
			"org.springframework.boot.SpringBootConfiguration",
			"org.springframework.context.annotation.Configuration",
			"org.springframework.context.annotation.ComponentScan",
			"org.springframework.web.bind.annotation.RestController",
			"org.springframework.stereotype.Controller",
			"org.springframework.web.bind.annotation.GetMapping"
	};

	protected final static String VALIDATED_CLASS_NAME = "org.springframework.validation.annotation.Validated";


	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		new Processor().process(beanFactory, registry);
	}

	private static class Processor {

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			new BeanFactoryProcessor(beanFactory).processBeans((beanType) -> true,
				(beanName, beanType) -> {
					final Set<Class<?>> collector = new HashSet<>();
					final Queue<Class<?>> toProcess = new LinkedList<>();
					toProcess.add(beanType);
					
					// TODO Needs to be smarter, won't catch recursive interfaces,
					while (!toProcess.isEmpty()) {
						Class<?> nextType = toProcess.poll();
						processType(nextType, collector);
						Arrays.stream(nextType.getInterfaces()).forEach(iface -> toProcess.add(iface));
						Class<?> superClass = nextType.getSuperclass();
						if (superClass != null) {
							toProcess.add(superClass);
						}
					}

					// From the candidate annotations, determine those that are truly the target
					// of aliases (either because @AliasFor'd from another annotation or using
					// internal @AliasFor references amongst its own attributes)
					Set<Class<?>> aliasForTargets = new HashSet<>();
					for (Class<?> annotationType: collector) {
						collectAnnotationsReferencedViaAliasFor(annotationType, aliasForTargets);
					}
			
					List<String> proxied = new ArrayList<>();
					for (Class<?> aliasForTarget: aliasForTargets) {
						if (!ignore(aliasForTarget)) {
							List<String> interfaces = new ArrayList<>();
							interfaces.add(aliasForTarget.getName());
							interfaces.add("org.springframework.core.annotation.SynthesizedAnnotation");
							registry.proxy().add(NativeProxyEntry.ofInterfaceNames(interfaces.toArray(new String[0])));
							proxied.add(aliasForTarget.getName());
						}
					}
					if (proxied.size()!=0) {
						logger.debug("from examining "+beanType.getName()+" registering "+proxied.size()+" types as synthesized proxies: "+proxied);
					}
				});
		}
	}

	public static void processType(Class<?> type, Set<Class<?>> collector) {
		for (Annotation anno: type.getAnnotations()) {
			if (IsSpringAnnotation.test(anno.annotationType())) {
				collector.add(anno.annotationType());
			}
		}

		// Example with annotations on the method and against parameters:
		// @GetMapping("/greeting")
		// public String greeting( @RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) {
		for (Method method:  type.getDeclaredMethods()) {
			for (Annotation a: method.getAnnotations()) {
				if (IsSpringAnnotation.test(a.annotationType())) {
					collector.add(a.annotationType());
				}
			}
			Annotation[][] allPannos = method.getParameterAnnotations();
			for (Annotation[] pannos: allPannos) {
				for (Annotation panno: pannos) {
					if (IsSpringAnnotation.test(panno.annotationType())) {
						collector.add(panno.annotationType());
					}
				}
			}
		}
		
		for (Field field: type.getDeclaredFields()) {
			for (Annotation a: field.getAnnotations()) {
				if (IsSpringAnnotation.test(a.annotationType())) {
					collector.add(a.annotationType());
				}
			}
		}
	}

	private static boolean ignore(Class<?> name) {
		String classname = name.getName();
		for (String entry: DONT_NEED_PROXY) {
			if (entry.equals(classname)) {
				return true;
			}
		}
		return false;
	}

	public static void collectAnnotationsReferencedViaAliasFor(Class<?> clazz, Set<Class<?>> collector) {
		// If this gets set the type is using @AliasFor amongst its own attributes, not
		// specifying one of them is an alias for an attribute in a meta annotation.
		boolean usesLocalAliases = false;
		logger.debug("analyzing "+clazz.getName());
		for (Method m : clazz.getMethods()) {
			Map.Entry<Class<?>, Boolean> aliasForInfo = getAliasForSummary(m);
			if (aliasForInfo != null) {
				Class<?> relatedMetaAnnotation = aliasForInfo.getKey();
				// Annotation is the default AliasFor target when none is actually specified (and so it is a local alias)
				if (relatedMetaAnnotation == Annotation.class) {
					usesLocalAliases |= aliasForInfo.getValue();
				} else {
					collector.add(relatedMetaAnnotation);
				}
			}
		}
		if (usesLocalAliases) {
			collector.add(clazz);
		}
		logger.debug("Collecting annotations referenced via @AliasFor from "+clazz.getName()+", they are "+collector);
	}

	/**
	 * Check for an @AliasFor on this method. If one exists check if it specifies a
	 * value for the 'value' or 'attribute' or 'annotation' fields. The result will be null
	 * if there is no @AliasFor otherwise it will be a pair containing the name of the type
	 * specified for 'annotation' (will be the default Annotation.class if not set) and
	 * and a boolean indicating if a name was specified for 'value' or 'attribute'.
	 * @return {@code null} or the related pair
	 */
	public static Map.Entry<Class<?>,Boolean> getAliasForSummary(Method method) {
		AliasFor aliasForAnnotation = method.getAnnotation(AliasFor.class);
		if (aliasForAnnotation != null) {
			boolean namedAttribute = false;
			String attribute = aliasForAnnotation.attribute();
			if (attribute != null && attribute.length()!=0) {
				namedAttribute = true;
			} else if (aliasForAnnotation.value() != null && aliasForAnnotation.value().length()!=0) {
				namedAttribute = true;
				attribute = aliasForAnnotation.value();
			}
			Class<? extends Annotation> targetAnnotationType = aliasForAnnotation.annotation();
			return Map.entry(targetAnnotationType, namedAttribute);
		}
		return null;
	}

}

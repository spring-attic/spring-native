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

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationUtils;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry.Builder;
import org.springframework.aot.support.BeanFactoryProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;
import org.springframework.validation.annotation.Validated;

/**
 * Register as much of the hierarchy of {@link Indexed} marked beans as is required.
 *
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
public class IndexedBeanHierarchyNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	private static Log logger = LogFactory.getLog(IndexedBeanHierarchyNativeConfigurationProcessor.class);

	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		new BeanFactoryProcessor(beanFactory).processBeansWithAnnotation(Indexed.class,
			(beanName, beanType) -> {
				MergedAnnotations mas = MergedAnnotations.from(beanType);
				if (mas.isPresent(ConfigurationProperties.class) && mas.isPresent(Validated.class)) {
					logger.debug("adding basic reflection configuration for @Validated @ConfigurationProperties bean "+beanType.getName());
					registry.reflection().forType(beanType).withAccess(TypeAccess.DECLARED_CONSTRUCTORS,TypeAccess.DECLARED_METHODS,TypeAccess.DECLARED_CLASSES,TypeAccess.DECLARED_FIELDS);
				} else if (mas.isPresent(Component.class) && !mas.isPresent(Configuration.class)) {
					// TODO there is no doubt further optimizations here and certain types of component will need subsets of this behaviour
					// (which could be pushed into the related NativeConfigurationProcessors - for example web components typically only
					//  need reflective access to the methods that host web related annotations in the hierarchy)
					walkTypeAndRegisterReflection(beanType, registry, new HashSet<>());
				}
			});
	}

	public void walkTypeAndRegisterReflection(Class<?> type, NativeConfigurationRegistry registry, Set<Class<?>> visited) {
		if (!visited.add(type)) {
			return;
		}
		Builder builder = registry.reflection().forType(type);
		if (!type.getPackageName().startsWith("java.")) {
			builder.withAccess(TypeAccess.DECLARED_METHODS);
		}
		Set<Class<?>> collector = new TreeSet<>((c1,c2) -> c1.getName().compareTo(c2.getName()));
		Type genericSuperclass = type.getGenericSuperclass();
		NativeConfigurationUtils.collectReferenceTypesUsed(genericSuperclass, collector);
		Type[] genericInterfaces = type.getGenericInterfaces();
		for (Type genericInterface: genericInterfaces) {
			NativeConfigurationUtils.collectReferenceTypesUsed(genericInterface, collector);
		}
		for (Class<?> sigtype: collector) {
			walkTypeAndRegisterReflection(sigtype, registry, visited);
		}
	}

}

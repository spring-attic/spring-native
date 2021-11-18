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

package org.springframework.repository.annotation;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationUtils;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationUtils.ComponentCallback;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
import org.springframework.aot.support.BeanFactoryProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.data.TypeModelProcessor;
import org.springframework.data.TypeUtils;
import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.stereotype.Repository;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Recognize {@link Repository} annotated beans and creates a proxy for them.
 *
 * @author Christoph Strobl
 */
public class RepositoryComponentConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	private static Log logger = LogFactory.getLog(RepositoryComponentConfigurationProcessor.class);

	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		new RepositoryComponentProcessor().process(beanFactory, registry);
		new RepositoryTypeProcessor().process(beanFactory, registry);
	}

	private static class RepositoryTypeProcessor {

		private TypeModelProcessor modelProcessor = new TypeModelProcessor()
				.filterTypes(it -> !it.isPrimitive() && !ClassUtils.isPrimitiveArray(it));

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {

			doWithRepositories(beanFactory,
					(beanName, beanType) -> {
						for (Class<?> type : TypeUtils.resolveTypesInSignature(beanType)) {
							registerDomainTypesResolvedFromPublicMethods(type, beanType, registry);
						}
					});
		}

		private void registerDomainTypesResolvedFromPublicMethods(Class<?> type, Class<?> beanType, NativeConfigurationRegistry registry) {

			ReflectionUtils.doWithMethods(type, method -> {

				Set<Class<?>> methodReturnTypes = TypeUtils.resolveTypesInSignature(ResolvableType.forMethodReturnType(method, type));
				for (Class<?> returnType : methodReturnTypes) {
					if (inSimilarPackage(returnType, beanType)) {
						processDomainType(returnType, beanType, registry);
					}
				}
			}, it -> Modifier.isPublic(it.getModifiers()));
		}

		private void processDomainType(Class<?> type, Class<?> beanType, NativeConfigurationRegistry registry) {

			modelProcessor.inspect(type).forEach(it -> {
				if (inSimilarPackage(it.getType(), beanType)) {

					DefaultNativeReflectionEntry.Builder builder = registry.reflection().forType(it.getType());
					builder.withFlags(Flag.allPublicMethods, Flag.allDeclaredConstructors);

					it.doWithFields(field -> {

						// we the fields to be visible to access annotations on them
						if (!ObjectUtils.isEmpty(field.getAnnotations())) {
							builder.withFields(field);
						}
					});
				}
			});
		}

		private boolean inSimilarPackage(Class<?> type, Class<?> repositoryInterface) {
			String repoPackage = repositoryInterface.getPackageName();
			String typePackage = type.getPackageName();
			if (repoPackage.startsWith(typePackage) || typePackage.startsWith(repoPackage)) {
				return true;
			}
			return false;
		}
	}

	private static class RepositoryComponentProcessor {

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			doWithRepositories(beanFactory,
					(beanName, beanType) -> {

						logger.debug("Creating AOTProxy for @Repository component: " + beanType.getName());
						registry.proxy().add(NativeProxyEntry.ofClass(beanType, ProxyBits.IS_STATIC));

						if (!ObjectUtils.isEmpty(beanType.getInterfaces())) {

							List<String> proxy = new ArrayList<>();
							proxy.addAll(Arrays.asList(beanType.getInterfaces()).stream().map(Class::getName).collect(Collectors.toList()));

							proxy.add("org.springframework.aop.SpringProxy");
							proxy.add("org.springframework.aop.framework.Advised");
							proxy.add("org.springframework.core.DecoratingProxy");
							registry.proxy().add(NativeProxyEntry.ofInterfaceNames(proxy.toArray(new String[0])));
						}
					});
		}
	}

	static void doWithRepositories(ConfigurableListableBeanFactory beanFactory, NativeConfigurationUtils.ComponentCallback callback) {
		new BeanFactoryProcessor(beanFactory).processBeansWithAnnotation(Repository.class, callback::invoke);
	}
}

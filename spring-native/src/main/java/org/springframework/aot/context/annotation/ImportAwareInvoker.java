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

package org.springframework.aot.context.annotation;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

/**
 * Utility class to invoke an {@link ImportAware} callback using a mapping computed
 * at build time.
 *
 * @author Stephane Nicoll
 */
public final class ImportAwareInvoker {

	public static final String BEAN_NAME = "org.springframework.aot.ImportAwareInvoker";

	private final MetadataReaderFactory metadataReaderFactory;

	private final Map<String, String> importsMapping;

	public ImportAwareInvoker(Map<String, String> importsMapping) {
		this.metadataReaderFactory = new CachingMetadataReaderFactory();
		this.importsMapping = importsMapping;
	}

	public static ImportAwareInvoker get(BeanFactory beanFactory) {
		return beanFactory.getBean(BEAN_NAME, ImportAwareInvoker.class);
	}

	public static void register(BeanDefinitionRegistry registry, Supplier<ImportAwareInvoker> instanceSupplier) {
		if (!registry.containsBeanDefinition(BEAN_NAME)) {
			registry.registerBeanDefinition(BEAN_NAME, BeanDefinitionBuilder
					.rootBeanDefinition(ImportAwareInvoker.class, instanceSupplier)
					.setRole(BeanDefinition.ROLE_INFRASTRUCTURE).getBeanDefinition());
		}
	}

	public void setAnnotationMetadata(ImportAware instance) {
		String importingClass = getImportingClassFor(instance);
		if (importingClass == null) {
			return; // import aware configuration class not imported
		}
		try {
			MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(importingClass);
			instance.setImportMetadata(metadataReader.getAnnotationMetadata());
		}
		catch (IOException ex) {
			throw new IllegalStateException(String.format("Failed to read metadata for '%s'", importingClass), ex);
		}
	}

	private String getImportingClassFor(ImportAware instance) {
		String target = ClassUtils.getUserClass(instance).getName();
		return importsMapping.get(target);
	}

}

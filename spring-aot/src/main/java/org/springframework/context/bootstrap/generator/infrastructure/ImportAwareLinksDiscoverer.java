package org.springframework.context.bootstrap.generator.infrastructure;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeResourcesEntry;
import org.springframework.context.origin.BeanDefinitionDescriptor;
import org.springframework.context.origin.BeanDefinitionDescriptor.Type;
import org.springframework.context.origin.BeanFactoryStructure;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

/**
 * Handle links for {@link ImportAware} callbacks.
 *
 * @author Stephane Nicoll
 */
class ImportAwareLinksDiscoverer {

	private final BeanFactoryStructure structure;

	private final ClassLoader classLoader;

	ImportAwareLinksDiscoverer(BeanFactoryStructure structure, ClassLoader classLoader) {
		this.structure = structure;
		this.classLoader = classLoader;
	}

	/**
	 * Identify the configuration classes that are {@link ImportAware} and return a
	 * mapping to their import class.
	 * @param nativeConfiguration the registry to use to declare the classes
	 * that should be loadable via ASM
	 * @return the mapping
	 */
	Map<String, Class<?>> buildImportAwareLinks(NativeConfigurationRegistry nativeConfiguration) {
		Map<String, Class<?>> result = new LinkedHashMap<>();
		this.structure.getDescriptors().forEach((beanName, descriptor) -> {
			if (isImportAwareCandidate(descriptor)) {
				Class<?> importingClass = getBestEffortImportingClass(descriptor);
				if (importingClass != null) {
					String beanClassName = descriptor.getBeanDefinition().getResolvableType().toClass().getName();
					result.put(beanClassName, importingClass);
					nativeConfiguration.resources().add(NativeResourcesEntry.ofClass(importingClass));
				}
			}
		});
		return result;
	}

	private boolean isImportAwareCandidate(BeanDefinitionDescriptor descriptor) {
		return descriptor.getType().equals(Type.CONFIGURATION) && descriptor.getOrigins() != null
				&& ImportAware.class.isAssignableFrom(descriptor.getBeanDefinition().getResolvableType().toClass());
	}

	private Class<?> getBestEffortImportingClass(BeanDefinitionDescriptor descriptor) {
		if (descriptor.getOrigins().isEmpty()) {
			return null;
		}
		LinkedList<String> tmp = new LinkedList<>(descriptor.getOrigins());
		BeanDefinitionDescriptor importingDescriptor = this.structure.getDescriptors().get(tmp.getLast());
		if (importingDescriptor != null) {
			AbstractBeanDefinition bd = (AbstractBeanDefinition) importingDescriptor.getBeanDefinition();
			if (!bd.getResolvableType().equals(ResolvableType.NONE)) {
				return ClassUtils.getUserClass(bd.getResolvableType().toClass());
			}
			if (bd.getBeanClassName() != null) {
				return loadBeanClassName(bd.getBeanClassName());
			}
		}
		return null;
	}

	private Class<?> loadBeanClassName(String className) {
		try {
			return ClassUtils.forName(className, this.classLoader);
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalStateException(
					"Bean definition refers to invalid class '" + className + "'", ex);
		}
	}

}

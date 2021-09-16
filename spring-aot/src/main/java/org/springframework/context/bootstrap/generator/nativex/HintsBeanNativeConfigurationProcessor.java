package org.springframework.context.bootstrap.generator.nativex;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.BeanNativeConfigurationProcessor;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ReflectionConfiguration;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeInitializationEntry;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeResourcesEntry;
import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.MethodDescriptor;
import org.springframework.nativex.type.ResourcesDescriptor;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.util.ClassUtils;

import static org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.*;

/**
 *
 * @author Andy Clement
 */
class HintsBeanNativeConfigurationProcessor implements BeanNativeConfigurationProcessor {

	@Override
	public void process(BeanInstanceDescriptor descriptor, NativeConfigurationRegistry registry) {
		findAndRegisterRelevantNativeHints(descriptor.getUserBeanClass(), registry);
	}

	// TODO is this code in the right place - if three beans come from three creators in the same declaring class, work will be duplicated (Should be same outcome, but wasteful)
	// TODO what happens if someone has a hint whose trigger is a configuration that has no beans in it, will that hint be missed? (because no creators are recorded for that configuration)

	/**
	 * Lookup any native hints that have been declared with the declaring class of the
	 * instance creator as a trigger, then register information from those hints with the
	 * registry.
	 * @param beanType the bean type
	 * @param registry the registry into which hint info should be added
	 */
	private void findAndRegisterRelevantNativeHints(Class<?> beanType, NativeConfigurationRegistry registry) {
		ReflectionConfiguration reflectionConfiguration = registry.reflection();
		ResourcesConfiguration resourcesConfiguration = registry.resources();
		ProxyConfiguration proxyConfiguration = registry.proxy();
		InitializationConfiguration initializationConfiguration = registry.initialization();

		try {
			List<HintDeclaration> hints = TypeSystem.getClassLoaderBasedTypeSystem().findHints(beanType.getName());
			if (hints != null) {
				for (HintDeclaration hint : hints) {
					// Types
					Map<String, AccessDescriptor> dependantTypes = hint.getDependantTypes();
					for (Map.Entry<String, AccessDescriptor> entry : dependantTypes.entrySet()) {
						Class<?> keyClass = ClassUtils.forName(entry.getKey(), null);
						AccessDescriptor value = entry.getValue();
						Integer accessBits = value.getAccessBits();
						if (accessBits != 0) {
							reflectionConfiguration.forType(keyClass).withFlags(AccessBits.getFlags(accessBits));
						}
						if ((accessBits & AccessBits.RESOURCE) != 0) {
							registry.resources().add(NativeResourcesEntry.ofClass(keyClass));
						}
						for (MethodDescriptor methodDescriptor : value.getMethodDescriptors()) {
							Executable method = methodDescriptor.findOnClass(keyClass);
							reflectionConfiguration.forType(keyClass).withMethods(method);
						}
						for (FieldDescriptor fieldDescriptor : value.getFieldDescriptors()) {
							Field field = keyClass.getDeclaredField(fieldDescriptor.getName());
							reflectionConfiguration.forType(keyClass).withFields(field);
						}
					}

					// Resources
					for (ResourcesDescriptor resourcesDescriptor : hint.getResourcesDescriptors()) {
						if (resourcesDescriptor.isBundle()) {
							for (String pattern : resourcesDescriptor.getPatterns()) {
								resourcesConfiguration.add(NativeResourcesEntry.ofBundle(pattern));
							}
						}
						else {
							for (String pattern : resourcesDescriptor.getPatterns()) {
								resourcesConfiguration.add(NativeResourcesEntry.of(pattern));
							}
						}
					}

					// JDK Proxies
					for (JdkProxyDescriptor proxyDescriptor : hint.getProxyDescriptors()) {
						proxyConfiguration.add(NativeProxyEntry.ofTypeNames(proxyDescriptor.getTypes().toArray(String[]::new)));
					}

					// Initialization
					for (InitializationDescriptor initializationDescriptor : hint.getInitializationDescriptors()) {
						initializationDescriptor.getBuildtimeClasses().forEach(buildTimeClass ->
								initializationConfiguration.add(NativeInitializationEntry.ofBuildTimeType(ClassUtils.resolveClassName(buildTimeClass, null))));
						initializationDescriptor.getRuntimeClasses().forEach(runtimeClass ->
								initializationConfiguration.add(NativeInitializationEntry.ofRuntimeType(ClassUtils.resolveClassName(runtimeClass, null))));
						initializationDescriptor.getBuildtimePackages().forEach(buildTimePackage ->
								initializationConfiguration.add(NativeInitializationEntry.ofBuildTimePackage(buildTimePackage)));
						initializationDescriptor.getRuntimePackages().forEach(runtimePackage ->
								initializationConfiguration.add(NativeInitializationEntry.ofRuntimePackage(runtimePackage)));
					}

					// native-image Options
					registry.options().addAll(hint.getOptions());

					// hint.getJNITypes();
					// hint.getSerializationTypes();
					// hint.getOptions();
				}
			}
			if (beanType.getSuperclass() != null) {
				findAndRegisterRelevantNativeHints(beanType.getSuperclass(), registry);
			}
		}
		catch (Throwable t) {
			// FIXME would like to log this...
		}
	}
}

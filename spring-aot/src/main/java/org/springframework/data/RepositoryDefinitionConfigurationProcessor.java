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
package org.springframework.data;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntry.Builder;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeInitializationEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.DecoratingProxy;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationFilter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.SynthesizedAnnotation;
import org.springframework.data.TypeUtils.TypeOps;
import org.springframework.data.annotation.QueryAnnotation;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.projection.TargetAware;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactoryInformation;
import org.springframework.data.repository.kotlin.CoroutineCrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.Nullable;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

/**
 * A {@link BeanFactoryNativeConfigurationProcessor} that register reflection access for
 * {@link Repository} interfaces, custom implementations, fragments and the involved domain types
 * reachable via the domain types exposed in type, field and method signatures.
 *
 * @author Christoph Strobl
 */
public class RepositoryDefinitionConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	private static final String REPO_MARKER = "org.springframework.data.repository.Repository";

	private static final Pattern REPOSITORY_METHOD_PATTERN = Pattern.compile("^(find|read|get|query|search|stream|count|exists|delete|remove).*");

	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		if (ClassUtils.isPresent(REPO_MARKER, beanFactory.getBeanClassLoader())) {
			new RepositoryDefinitionConfigurationProcessor.Processor().process(beanFactory, registry);
		}
	}

	/**
	 * The actual {@link BeanFactoryNativeConfigurationProcessor} contributing required configuration to the given {@link NativeConfigurationRegistry}
	 *
	 * @author Christoph Strobl
	 */
	static class Processor {

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			RepositoryConfigurationContributor configurationHandler = new RepositoryConfigurationContributor(beanFactory.getBeanClassLoader(), registry);
			collectRepositoryDefinitions(beanFactory).forEach(configurationHandler::writeConfiguration);
		}

		private Collection<RepositoryConfiguration> collectRepositoryDefinitions(ConfigurableListableBeanFactory beanFactory) {
			RepositoryConfigurationFactory configurationFactory = new RepositoryConfigurationFactory(beanFactory);
			List<RepositoryConfiguration> repositoryConfigurations = new ArrayList<>();
			for (String beanName : beanFactory.getBeanNamesForType(RepositoryFactoryInformation.class, false, false)) {
				repositoryConfigurations.add(configurationFactory.forBeanName(beanName));
			}
			return repositoryConfigurations;
		}
	}

	/**
	 * Factory to compute the {@link RepositoryConfiguration} for a given {@link BeanDefinition}, resolving
	 * domain types, fragments and custom implementations.
	 *
	 * @author Christoph Strobl
	 */
	static class RepositoryConfigurationFactory {

		private static final String FRAGMENTS_PROPERTY = "repositoryFragments";

		private static final String CUSTOM_IMPLEMENTATION_PROPERTY = "customImplementation";

		private final ConfigurableListableBeanFactory beanFactory;

		RepositoryConfigurationFactory(ConfigurableListableBeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		RepositoryConfiguration forBeanName(String beanName) {
			BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName.replace("&", ""));
			RepositoryConfiguration configuration = new RepositoryConfiguration(readRepositoryInterfaceFromBeanDefinition(beanName, beanDefinition));
			configuration.setBeanName(beanName);
			configuration.setCustomImplementation(readCustomImplementationFromBeanDefinition(beanDefinition));
			configuration.setFragments(readFragmentsFromBeanDefinition(beanDefinition));
			return configuration;
		}

		@SuppressWarnings("unchecked")
		private List<Class<?>> readFragmentsFromBeanDefinition(BeanDefinition beanDefinition) {
			if (!beanDefinition.getPropertyValues().contains(FRAGMENTS_PROPERTY)) {
				return Collections.emptyList();
			}
			List<Class<?>> detectedFragments = new ArrayList<>();
			PropertyValue repositoryFragments = beanDefinition.getPropertyValues().getPropertyValue(FRAGMENTS_PROPERTY);
			Object fragments = repositoryFragments.getValue();
			if (fragments instanceof RootBeanDefinition) {

				RootBeanDefinition fragmentsBeanDefinition = (RootBeanDefinition) fragments;
				ValueHolder argumentValue = fragmentsBeanDefinition.getConstructorArgumentValues().getArgumentValue(0, List.class);
				List<String> fragmentBeanNames = (List<String>) argumentValue.getValue();
				for (String beanName : fragmentBeanNames) {
					RootBeanDefinition bd = (RootBeanDefinition) beanFactory.getBeanDefinition(beanName);
					ValueHolder fragmentInterface = bd.getConstructorArgumentValues().getArgumentValue(0, String.class);
					try {
						detectedFragments.add(ClassUtils.forName(fragmentInterface.getValue().toString(), beanFactory.getBeanClassLoader()));
					}
					catch (ClassNotFoundException ex) {
						throw new CannotLoadBeanClassException(null, beanName, fragmentInterface.getValue().toString(), ex);
					}
					if (bd.getConstructorArgumentValues().hasIndexedArgumentValue(1)) { // fragment implementation
						ValueHolder fragmentImplementation = bd.getConstructorArgumentValues().getArgumentValue(1, BeanReference.class);
						if (fragmentImplementation.getValue() instanceof BeanReference) {
							detectedFragments.add(beanFactory.getType(((BeanReference) fragmentImplementation.getValue()).getBeanName(), false));
						}
					}
				}
			}
			return detectedFragments;
		}

		@Nullable
		private Class<?> readCustomImplementationFromBeanDefinition(BeanDefinition beanDefinition) {
			if (!beanDefinition.getPropertyValues().contains(CUSTOM_IMPLEMENTATION_PROPERTY)) {
				return null;
			}
			PropertyValue customImplementation = beanDefinition.getPropertyValues().getPropertyValue(CUSTOM_IMPLEMENTATION_PROPERTY);
			if (customImplementation.getValue() instanceof BeanReference) {
				return beanFactory.getType(((BeanReference) customImplementation.getValue()).getBeanName(), false);
			}
			throw new InvalidPropertyException(RepositoryFactoryBeanSupport.class, CUSTOM_IMPLEMENTATION_PROPERTY, "Not a BeanReference to custom repository implementation!");
		}

		private Class<?> readRepositoryInterfaceFromBeanDefinition(String beanName, BeanDefinition beanDefinition) {
			if (beanDefinition.getConstructorArgumentValues().getArgumentCount() != 1) {
				throw new BeanDefinitionValidationException("No repository interface defined on for " + beanDefinition);
			}
			ValueHolder repositoryInterfaceName = beanDefinition.getConstructorArgumentValues().getArgumentValue(0, Class.class);
			Object repositoryInterface = repositoryInterfaceName.getValue();
			if (repositoryInterface instanceof Class) {
				return (Class) repositoryInterface;
			}
			try {
				return ClassUtils.forName(repositoryInterface.toString(), beanFactory.getBeanClassLoader());
			}
			catch (ClassNotFoundException e) {
				throw new CannotLoadBeanClassException(null, beanName, repositoryInterface.toString(), e);
			}
		}
	}

	/**
	 * {@link RepositoryConfigurationContributor} computes {@link RepositoryConfiguration} and contributes data specific configuration
	 * to a {@link NativeConfigurationRegistry}.
	 *
	 * @author Christoph Strobl
	 */
	static class RepositoryConfigurationContributor {

		private static final TypeOps.PackageFilter JAVA_PACKAGE = TypeOps.PackageFilter.of("java");

		private static final TypeOps.PackageFilter SPRING_DATA_PACKAGE = TypeOps.PackageFilter.of("org.springframework.data");

		private static final TypeOps.PackageFilter SPRING_DATA_DOMAIN_TYPES_PACKAGES = TypeOps.PackageFilter.of(
				"org.springframework.data.domain", "org.springframework.data.geo");

		private static final AnnotationFilter ANNOTATION_FILTER = AnnotationFilter.packages("java.lang", "org.springframework.lang", "javax");

		private final ClassLoader classLoader;

		private final NativeConfigurationRegistry registry;

		private final TypeModelProcessor typeModelProcessor;

		private final Set<Class<?>> seen;

		RepositoryConfigurationContributor(ClassLoader classLoader, NativeConfigurationRegistry registry) {
			this.classLoader = classLoader;
			this.registry = registry;
			this.typeModelProcessor = new TypeModelProcessor();
			this.seen = new HashSet<>();
		}

		void writeConfiguration(RepositoryConfiguration configuration) {
			writeRepositoryInterfaceConfiguration(configuration);
			writeDomainTypeConfiguration(configuration.getDomainType().toClass());
			writeQueryMethodConfiguration(configuration);
			writeRepositoryFragments(configuration);
			writeCustomImplementation(configuration);
		}

		/**
		 * Write reflection and proxy config for the repository interface.
		 * @param configuration the repository configuration
		 */
		private void writeRepositoryInterfaceConfiguration(RepositoryConfiguration configuration) {
			// target access on methods
			registry.reflection().forType(configuration.getRepositoryInterface())
					.withAccess(TypeAccess.PUBLIC_METHODS);
			// proxy configuration
			registry.proxy().add(NativeProxyEntry.ofInterfaces(configuration.getRepositoryInterface(), SpringProxy.class, Advised.class, DecoratingProxy.class));
			// transactional proxy configuration
			// TODO: should we also ask the BeanFactory if a tx-manager is configured?
			if (ClassUtils.isPresent("org.springframework.transaction.interceptor.TransactionalProxy", classLoader)) {
				registry.proxy().add(NativeProxyEntry.ofInterfaceNames(configuration.getRepositoryInterface().getName(), Repository.class.getName(),
						"org.springframework.transaction.interceptor.TransactionalProxy", "org.springframework.aop.framework.Advised", DecoratingProxy.class.getName()));
				if (configuration.isAnnotationPresent(Component.class)) {
					registry.proxy().add(NativeProxyEntry.ofInterfaceNames(configuration.getRepositoryInterface().getName(), Repository.class.getName(),
							"org.springframework.transaction.interceptor.TransactionalProxy", "org.springframework.aop.framework.Advised",
							DecoratingProxy.class.getName(), Serializable.class.getName()));
				}
			}
			// reactive repo
			if (configuration.isReactiveRepository()) {
				// TODO: do we still need this?
				registry.initialization().add(NativeInitializationEntry.ofBuildTimeType(configuration.getRepositoryInterface()));
			}
			// Kotlin repo
			if (configuration.isKotlinRepository()) {
				registry.reflection().forType(Iterable.class).withAccess(TypeAccess.QUERY_PUBLIC_METHODS);
				safelyRegister("kotlinx.coroutines.flow.Flow", TypeAccess.QUERY_PUBLIC_METHODS);
				safelyRegister("kotlin.collections.Iterable", TypeAccess.QUERY_PUBLIC_METHODS);
				safelyRegister("kotlin.Unit", TypeAccess.QUERY_PUBLIC_METHODS);
				safelyRegister("kotlin.Long", TypeAccess.QUERY_PUBLIC_METHODS);
				safelyRegister("kotlin.Boolean", TypeAccess.QUERY_PUBLIC_METHODS);
			}
		}

		private void safelyRegister(String className, TypeAccess... access) {
			try {
				registry.reflection().forType(Class.forName(className)).withAccess(access);
			}
			catch (ClassNotFoundException ex) {
				// TODO: logging?
			}
		}

		/**
		 * Write reflection and proxy config for the given type.
		 * @param type the type to handle
		 */
		private void writeDomainTypeConfiguration(Class<?> type) {
			typeModelProcessor.inspect(type).forEach((domainType) -> {
				if (seen.contains(domainType.getType())) {
					return;
				}
				seen.add(domainType.getType());
				if (domainType.isPartOf(SPRING_DATA_DOMAIN_TYPES_PACKAGES)) {  // eg. Page, Slice
					return;
				}
				if (SimpleTypeHolder.DEFAULT.isSimpleType(domainType.getType())) { // eg. String, ...
					return;
				}
				Builder reflectBuilder = registry.reflection().forType(domainType.getType());
				if(domainType.hasDeclaredClasses()) {
						reflectBuilder.withAccess(TypeAccess.DECLARED_CLASSES);
				}
				if (domainType.hasMethods()) {
					reflectBuilder.withExecutables(domainType.getMethods().toArray(new Method[0]));
				}
				else {
					if (domainType.isPartOf("java")) {
						reflectBuilder.withAccess(TypeAccess.PUBLIC_METHODS);
					}
				}
				if (domainType.hasFields()) {
					reflectBuilder.withFields(domainType.getFields().toArray(new Field[0]));
				}
				if (domainType.hasPersistenceConstructor()) {
					reflectBuilder.withExecutables(domainType.getPersistenceConstructor());
				}
				else {
					reflectBuilder.withAccess(TypeAccess.DECLARED_CONSTRUCTORS);
				}
				domainType.doWithAnnotatedElements(this::writeAnnotationConfigurationFor);
			});
		}

		/**
		 * Write reflection config for repository interface query methods, involved domain
		 * types and annotations.
		 * @param configuration the repository configuration
		 */
		private void writeQueryMethodConfiguration(RepositoryConfiguration configuration) {
			writeRepositoryMethodConfiguration(configuration.getRepositoryInterface(), configuration.getDomainType().toClass(),
					(method) -> {
						if (REPOSITORY_METHOD_PATTERN.matcher(method.getName()).matches()) {
							return true;
						}
						return AnnotationUtils.findAnnotation(method, QueryAnnotation.class) != null;
					});
		}

		private void writeRepositoryMethodConfiguration(Class<?> typeToInspect, Class<?> repositoryDomainType, MethodFilter filter) {
			ReflectionUtils.doWithMethods(typeToInspect, method -> {
				Set<Class<?>> classes = TypeUtils.resolveTypesInSignature(ResolvableType.forMethodReturnType(method, typeToInspect));
				classes.stream().filter(it -> !SimpleTypeHolder.DEFAULT.isSimpleType(it)).forEach(it -> {
					if (it.equals(repositoryDomainType)) {
						return;
					}
					if (TypeUtils.type(it).isPartOf(SPRING_DATA_PACKAGE, JAVA_PACKAGE)) {
						return;
					}
					if (isProjectionInterface(repositoryDomainType, it)) {
						registry.proxy().add(NativeProxyEntry.ofInterfaces(it,
								TargetAware.class, SpringProxy.class, DecoratingProxy.class));
					}
					writeDomainTypeConfiguration(it);
				});
				writeAnnotationConfigurationFor(method);
			}, filter);
		}

		/**
		 * Write reflection config for repository fragments.
		 * @param configuration the repository configuration
		 */
		private void writeRepositoryFragments(RepositoryConfiguration configuration) {
			if (!configuration.hasFragments()) {
				return;
			}
			for (Class<?> fragment : configuration.getFragments()) {
				registry.reflection().forType(fragment).withAccess(TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS);
				writeRepositoryMethodConfiguration(fragment, configuration.getDomainType().toClass(),
						(method) -> Modifier.isPublic(method.getModifiers()));
			}
		}

		/**
		 * Write reflection config for custom implementations.
		 * @param configuration the repository configuration
		 */
		private void writeCustomImplementation(RepositoryConfiguration configuration) {
			if (!configuration.hasCustomImplementation()) {
				return;
			}
			Class<?> customImplementation = configuration.getCustomImplementation();
			registry.reflection().forType(customImplementation).withAccess(TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS);
			for (Class<?> repoInterface : configuration.getRepositoryInterface().getInterfaces()) {
				if (ClassUtils.isAssignable(repoInterface, customImplementation)) {
					registry.reflection().forType(repoInterface).withAccess(TypeAccess.PUBLIC_METHODS);
					break;
				}
			}
			writeRepositoryMethodConfiguration(customImplementation, configuration.getDomainType().toClass(),
					(method) -> Modifier.isPublic(method.getModifiers()));
		}

		private void writeAnnotationConfigurationFor(AnnotatedElement element) {
			TypeUtils.resolveAnnotationsFor(element, ANNOTATION_FILTER)
					.forEach(annotation -> {
						if (TypeUtils.type(annotation.getType()).isPartOf(SPRING_DATA_PACKAGE)
								|| annotation.getMetaTypes().stream().anyMatch(SPRING_DATA_PACKAGE::matches)) {
							registry.reflection().forType(annotation.getType()).withAccess(TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS);
							registry.proxy().add(NativeProxyEntry.ofInterfaces(annotation.getType(), SynthesizedAnnotation.class));
						}
					});
			if (element instanceof Constructor) {
				for (Parameter parameter : ((Constructor<?>) element).getParameters()) {
					writeAnnotationConfigurationFor(parameter);
				}
			}
			if (element instanceof Method) {
				for (Parameter parameter : ((Method) element).getParameters()) {
					writeAnnotationConfigurationFor(parameter);
				}
			}
		}

		private static boolean nonTransientField(Field field) {
			return AnnotationUtils.findAnnotation(field, Transient.class) == null;
		}
	}

	/**
	 * Resolved {@link RepositoryConfiguration} holding information about
	 * the repository interface, domain and id type, as well as fragments
	 * and custom implementations.
	 */
	static class RepositoryConfiguration {

		private String beanName;

		private final Class<?> repositoryInterface;

		private final ResolvableType repositoryType;

		private final ResolvableType domainType;

		private final ResolvableType idType;

		private final List<Class<?>> fragments = new ArrayList<>(0);

		private Class<?> customImplementation;

		RepositoryConfiguration(Class<?> repositoryInterface) {
			this.repositoryInterface = repositoryInterface;
			this.repositoryType = ResolvableType.forClass(Repository.class, repositoryInterface);
			ResolvableType[] args = repositoryType.getGenerics();
			this.domainType = args[0];
			this.idType = args[1];
		}

		void setFragments(Collection<Class<?>> fragments) {
			this.fragments.addAll(fragments);
		}

		void setCustomImplementation(Class<?> customImplementation) {
			this.customImplementation = customImplementation;
		}

		Class<?> getRepositoryInterface() {
			return repositoryInterface;
		}

		ResolvableType getDomainType() {
			return domainType;
		}

		boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
			return AnnotationUtils.findAnnotation(getRepositoryInterface(), annotation) != null;
		}

		boolean hasCustomImplementation() {
			return getCustomImplementation() != null;
		}

		Class<?> getCustomImplementation() {
			return customImplementation;
		}

		boolean isReactiveRepository() {
			return ClassUtils.isAssignable(ReactiveCrudRepository.class, repositoryInterface);
		}

		List<Class<?>> getFragments() {
			return fragments;
		}

		boolean hasFragments() {
			return !getFragments().isEmpty();
		}

		boolean isKotlinRepository() {
			return ClassUtils.isAssignable(CoroutineCrudRepository.class, repositoryInterface);
		}

		void setBeanName(String beanName) {
			this.beanName = beanName;
		}

		String getBeanName() {
			return beanName;
		}

		public ResolvableType getIdType() {
			return idType;
		}
	}

	private static boolean isProjectionInterface(Class<?> repositoryDomainType, Class<?> signatureType) {
		return signatureType.isInterface() && !signatureType.getPackageName().startsWith("java.")
				&& !signatureType.getPackageName().startsWith("org.springframework.data")
				&& !signatureType.isAssignableFrom(repositoryDomainType);
	}
}

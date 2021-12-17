package org.springframework.aot.beans.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

/**
 * Tests for {@link InjectedConstructionResolver}.
 *
 * @author Stephane Nicoll
 */
class InjectedConstructionResolverTests {

	@Test
	void resolveNoArgConstructor() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		InjectedElementAttributes attributes = createResolverForConstructor(
				InjectedConstructionResolverTests.class).resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
	}

	@ParameterizedTest
	@MethodSource("singleArgConstruction")
	void resolveSingleArgConstructor(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("one", "1");
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		assertThat((String) attributes.get(0)).isEqualTo("1");
	}

	@ParameterizedTest
	@MethodSource("singleArgConstruction")
	void resolveRequiredDependencyNotPresentThrowsUnsatisfiedDependencyException(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		assertThatThrownBy(() -> resolver.resolve(beanFactory))
				.isInstanceOfSatisfying(UnsatisfiedDependencyException.class, (ex) -> {
					assertThat(ex.getBeanName()).isEqualTo("test");
					assertThat(ex.getInjectionPoint()).isNotNull();
					assertThat(ex.getInjectionPoint().getMember()).isEqualTo(resolver.getExecutable());
				});
	}

	@ParameterizedTest
	@MethodSource("arrayOfBeansConstruction")
	void resolveArrayOfBeans(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("one", "1");
		beanFactory.registerSingleton("two", "2");
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		Object attribute = attributes.get(0);
		assertThat(Arrays.isArray(attribute)).isTrue();
		assertThat((Object[]) attribute).containsExactly("1", "2");
	}

	@ParameterizedTest
	@MethodSource("arrayOfBeansConstruction")
	void resolveRequiredArrayOfBeansInjectEmptyArray(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		Object attribute = attributes.get(0);
		assertThat(Arrays.isArray(attribute)).isTrue();
		assertThat((Object[]) attribute).isEmpty();

	}

	static Stream<Arguments> arrayOfBeansConstruction() {
		return Stream.of(Arguments.of(createResolverForConstructor(BeansCollectionConstructor.class, String[].class)),
				Arguments.of(createResolverForFactoryMethod(BeansCollectionFactory.class, "array", String[].class)));
	}

	@ParameterizedTest
	@MethodSource("listOfBeansConstruction")
	void resolveListOfBeans(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("one", "1");
		beanFactory.registerSingleton("two", "2");
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		Object attribute = attributes.get(0);
		assertThat(attribute).isInstanceOf(List.class).asList().containsExactly("1", "2");
	}

	@ParameterizedTest
	@MethodSource("listOfBeansConstruction")
	void resolveRequiredListOfBeansInjectEmptyList(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		Object attribute = attributes.get(0);
		assertThat(attribute).isInstanceOf(List.class);
		assertThat((List<?>) attribute).isEmpty();
	}

	static Stream<Arguments> listOfBeansConstruction() {
		return Stream.of(Arguments.of(createResolverForConstructor(BeansCollectionConstructor.class, List.class)),
				Arguments.of(createResolverForFactoryMethod(BeansCollectionFactory.class, "list", List.class)));
	}

	@ParameterizedTest
	@MethodSource("setOfBeansConstruction")
	void resolveSetOfBeans(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("one", "1");
		beanFactory.registerSingleton("two", "2");
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		Object attribute = attributes.get(0);
		assertThat(attribute).isInstanceOf(Set.class);
		assertThat((Set<String>) attribute).containsExactly("1", "2");
	}

	@ParameterizedTest
	@MethodSource("setOfBeansConstruction")
	void resolveRequiredSetOfBeansInjectEmptySet(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		Object attribute = attributes.get(0);
		assertThat(attribute).isInstanceOf(Set.class);
		assertThat((Set<?>) attribute).isEmpty();
	}

	static Stream<Arguments> setOfBeansConstruction() {
		return Stream.of(Arguments.of(createResolverForConstructor(BeansCollectionConstructor.class, Set.class)),
				Arguments.of(createResolverForFactoryMethod(BeansCollectionFactory.class, "set", Set.class)));
	}

	@ParameterizedTest
	@MethodSource("mapOfBeansConstruction")
	void resolveMapOfBeans(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("one", "1");
		beanFactory.registerSingleton("two", "2");
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		Object attribute = attributes.get(0);
		assertThat(attribute).isInstanceOf(Map.class);
		assertThat((Map<String, String>) attribute).containsExactly(entry("one", "1"), entry("two", "2"));
	}

	@ParameterizedTest
	@MethodSource("mapOfBeansConstruction")
	void resolveRequiredMapOfBeansInjectEmptySet(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		Object attribute = attributes.get(0);
		assertThat(attribute).isInstanceOf(Map.class);
		assertThat((Map<?, ?>) attribute).isEmpty();
	}

	static Stream<Arguments> mapOfBeansConstruction() {
		return Stream.of(Arguments.of(createResolverForConstructor(BeansCollectionConstructor.class, Map.class)),
				Arguments.of(createResolverForFactoryMethod(BeansCollectionFactory.class, "map", Map.class)));
	}

	@ParameterizedTest
	@MethodSource("multiArgsConstruction")
	void resolveMultiArgsConstructor(InjectedConstructionResolver resolver) {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		MockEnvironment environment = new MockEnvironment();
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerResolvableDependency(ResourceLoader.class, resourceLoader);
		beanFactory.registerSingleton("environment", environment);
		beanFactory.registerSingleton("one", "1");
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		assertThat((ResourceLoader) attributes.get(0)).isEqualTo(resourceLoader);
		assertThat((Environment) attributes.get(1)).isEqualTo(environment);
		ObjectProvider<String> provider = attributes.get(2);
		assertThat(provider.getIfAvailable()).isEqualTo("1");
	}

	@ParameterizedTest
	@MethodSource("mixedArgsConstruction")
	void resolveMixedArgsConstructorWithUserValue(InjectedConstructionResolver resolver) {
		ApplicationContext applicationContext = new GenericApplicationContext();
		MockEnvironment environment = new MockEnvironment();
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerResolvableDependency(ApplicationContext.class, applicationContext);
		beanFactory.registerSingleton("environment", environment);
		AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(MixedArgsConstructor.class)
				.setAutowireMode(RootBeanDefinition.AUTOWIRE_CONSTRUCTOR).getBeanDefinition();
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(1, "user-value");
		beanFactory.registerBeanDefinition("test", beanDefinition);
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		assertThat((ApplicationContext) attributes.get(0)).isEqualTo(applicationContext);
		assertThat((String) attributes.get(1)).isEqualTo("user-value");
		assertThat((Environment) attributes.get(2)).isEqualTo(environment);
	}

	@ParameterizedTest
	@MethodSource("mixedArgsConstruction")
	void resolveMixedArgsConstructorWithUserBeanReference(InjectedConstructionResolver resolver) {
		ApplicationContext applicationContext = new GenericApplicationContext();
		MockEnvironment environment = new MockEnvironment();
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerResolvableDependency(ApplicationContext.class, applicationContext);
		beanFactory.registerSingleton("environment", environment);
		beanFactory.registerSingleton("one", "1");
		beanFactory.registerSingleton("two", "2");
		AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(MixedArgsConstructor.class)
				.setAutowireMode(RootBeanDefinition.AUTOWIRE_CONSTRUCTOR).getBeanDefinition();
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(1, new RuntimeBeanReference("two"));
		beanFactory.registerBeanDefinition("test", beanDefinition);
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		assertThat((ApplicationContext) attributes.get(0)).isEqualTo(applicationContext);
		assertThat((String) attributes.get(1)).isEqualTo("2");
		assertThat((Environment) attributes.get(2)).isEqualTo(environment);
	}

	@Test
	void resolveUserValueWithTypeConversionRequired() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(CharDependency.class)
				.setAutowireMode(RootBeanDefinition.AUTOWIRE_CONSTRUCTOR).getBeanDefinition();
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, "\\");
		beanFactory.registerBeanDefinition("test", beanDefinition);
		InjectedElementAttributes attributes = createResolverForConstructor(CharDependency.class, char.class).resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		Object attribute = attributes.get(0);
		assertThat(attribute).isInstanceOf(Character.class);
		assertThat((Character) attribute).isEqualTo('\\');
	}

	@ParameterizedTest
	@MethodSource("singleArgConstruction")
	void resolveUserValueWithBeanReference(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("stringBean", "string");
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(SingleArgConstructor.class)
				.addConstructorArgReference("stringBean").getBeanDefinition());
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		Object attribute = attributes.get(0);
		assertThat(attribute).isEqualTo("string");
	}

	@ParameterizedTest
	@MethodSource("singleArgConstruction")
	void resolveUserValueWithBeanDefinition(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		AbstractBeanDefinition userValue = BeanDefinitionBuilder.rootBeanDefinition(String.class, () -> "string").getBeanDefinition();
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(SingleArgConstructor.class)
				.addConstructorArgValue(userValue).getBeanDefinition());
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		Object attribute = attributes.get(0);
		assertThat(attribute).isEqualTo("string");
	}

	@ParameterizedTest
	@MethodSource("singleArgConstruction")
	void resolveUserValueThatIsAlreadyResolved(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SingleArgConstructor.class).getBeanDefinition();
		ValueHolder valueHolder = new ValueHolder('a');
		valueHolder.setConvertedValue("this is an a");
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, valueHolder);
		beanFactory.registerBeanDefinition("test", beanDefinition);
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		Object attribute = attributes.get(0);
		assertThat(attribute).isEqualTo("this is an a");
	}

	@ParameterizedTest
	@MethodSource("qualifiedDependencyConstruction")
	void resolveQualifiedDependency(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
		beanFactory.registerSingleton("one", "1");
		beanFactory.registerSingleton("two", "2");
		InjectedElementAttributes attributes = resolver.resolve(beanFactory);
		assertThat(attributes.isResolved()).isTrue();
		assertThat((String) attributes.get(0)).isEqualTo("2");
	}

	@ParameterizedTest
	@MethodSource("singleArgConstruction")
	void createInvokeFactory(InjectedConstructionResolver resolver) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("one", "1");
		String instance = resolver.create(beanFactory, (attributes) -> attributes.get(0));
		assertThat(instance).isEqualTo("1");
	}

	private static InjectedConstructionResolver createResolverForConstructor(Class<?> beanType, Class<?>... parameterTypes) {
		try {
			Constructor<?> executable = beanType.getDeclaredConstructor(parameterTypes);
			return new InjectedConstructionResolver(executable, beanType, "test",
					InjectedConstructionResolverTests::safeGetBeanDefinition);
		}
		catch (NoSuchMethodException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static InjectedConstructionResolver createResolverForFactoryMethod(Class<?> targetType,
			String methodName, Class<?>... parameterTypes) {
		Method executable = ReflectionUtils.findMethod(targetType, methodName, parameterTypes);
		return new InjectedConstructionResolver(executable, targetType, "test",
				InjectedConstructionResolverTests::safeGetBeanDefinition);
	}

	private static BeanDefinition safeGetBeanDefinition(DefaultListableBeanFactory beanFactory) {
		try {
			return beanFactory.getBeanDefinition("test");
		}
		catch (NoSuchBeanDefinitionException ex) {
			return null;
		}
	}

	static Stream<Arguments> singleArgConstruction() {
		return Stream.of(Arguments.of(createResolverForConstructor(SingleArgConstructor.class, String.class)),
				Arguments.of(createResolverForFactoryMethod(SingleArgFactory.class, "single", String.class)));
	}

	@SuppressWarnings("unused")
	static class SingleArgConstructor {

		public SingleArgConstructor(String s) {
		}

	}

	@SuppressWarnings("unused")
	static class SingleArgFactory {

		String single(String s) {
			return s;
		}

	}

	@SuppressWarnings("unused")
	static class BeansCollectionConstructor {

		public BeansCollectionConstructor(String[] beans) {

		}

		public BeansCollectionConstructor(List<String> beans) {

		}

		public BeansCollectionConstructor(Set<String> beans) {

		}

		public BeansCollectionConstructor(Map<String, String> beans) {

		}

	}

	@SuppressWarnings("unused")
	static class BeansCollectionFactory {

		public String array(String[] beans) {
			return "test";
		}

		public String list(List<String> beans) {
			return "test";
		}

		public String set(Set<String> beans) {
			return "test";
		}

		public String map(Map<String, String> beans) {
			return "test";
		}

	}

	static Stream<Arguments> multiArgsConstruction() {
		return Stream.of(Arguments.of(createResolverForConstructor(MultiArgsConstructor.class, ResourceLoader.class, Environment.class, ObjectProvider.class)),
				Arguments.of(createResolverForFactoryMethod(MultiArgsFactory.class, "multiArgs", ResourceLoader.class, Environment.class, ObjectProvider.class)));
	}

	@SuppressWarnings("unused")
	static class MultiArgsConstructor {

		public MultiArgsConstructor(ResourceLoader resourceLoader, Environment environment, ObjectProvider<String> provider) {
		}
	}

	@SuppressWarnings("unused")
	static class MultiArgsFactory {

		String multiArgs(ResourceLoader resourceLoader, Environment environment, ObjectProvider<String> provider) {
			return "test";
		}
	}

	static Stream<Arguments> mixedArgsConstruction() {
		return Stream.of(Arguments.of(createResolverForConstructor(MixedArgsConstructor.class, ApplicationContext.class, String.class, Environment.class)),
				Arguments.of(createResolverForFactoryMethod(MixedArgsFactory.class, "mixedArgs", ApplicationContext.class, String.class, Environment.class)));
	}

	@SuppressWarnings("unused")
	static class MixedArgsConstructor {

		public MixedArgsConstructor(ApplicationContext context, String test, Environment environment) {

		}

	}

	@SuppressWarnings("unused")
	static class MixedArgsFactory {

		String mixedArgs(ApplicationContext context, String test, Environment environment) {
			return "test";
		}

	}

	static Stream<Arguments> qualifiedDependencyConstruction() {
		return Stream.of(Arguments.of(createResolverForConstructor(QualifiedDependency.class, String.class)),
				Arguments.of(createResolverForFactoryMethod(QualifiedDependencyFactory.class, "qualified", String.class)));
	}

	@SuppressWarnings("unused")
	static class QualifiedDependency {

		QualifiedDependency(@Qualifier("two") String s) {
		}

	}

	@SuppressWarnings("unused")
	static class QualifiedDependencyFactory {

		String qualified(@Qualifier("two") String s) {
			return s;
		}

	}

	@SuppressWarnings("unused")
	static class CharDependency {

		CharDependency(char escapeChar) {
		}

	}

}

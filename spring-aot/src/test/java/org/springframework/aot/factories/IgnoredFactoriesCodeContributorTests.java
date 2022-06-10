package org.springframework.aot.factories;

import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.aot.build.BootstrapContributor;
import org.springframework.aot.build.context.BuildContext;
import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriterSupplier;
import org.springframework.aot.context.origin.BeanDefinitionOriginAnalyzer;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationImportListener;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.BeanDefinitionPostProcessor;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.test.context.ContextCustomizerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for {@link IgnoredFactoriesCodeContributor}.
 *
 * @author Stephane Nicoll
 */
class IgnoredFactoriesCodeContributorTests {

	private final IgnoredFactoriesCodeContributor factoriesCodeContributor = new IgnoredFactoriesCodeContributor();

	@ParameterizedTest
	@MethodSource("springFactoriesToIgnore")
	void canContributeFactoryTypesToIgnore(SpringFactory springFactory) {
		assertThat(this.factoriesCodeContributor.canContribute(springFactory)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("springFactoriesNotHandled")
	void canContributeFactoryTypesToNotHandle(SpringFactory springFactory) {
		assertThat(this.factoriesCodeContributor.canContribute(springFactory)).isFalse();
	}

	@Test
	void contributeIsNoOp() {
		CodeGenerator codeGenerator = mock(CodeGenerator.class);
		BuildContext buildContext = mock(BuildContext.class);
		this.factoriesCodeContributor.contribute(createSpringFactory(BeanDefinitionPostProcessor.class, Object.class.getName()),
				codeGenerator, buildContext);
		verifyNoInteractions(codeGenerator, buildContext);
	}

	static Stream<Arguments> springFactoriesToIgnore() {
		return Stream.of(springFactory(AutoConfigurationImportListener.class),
				springFactory(AutoConfigurationImportFilter.class),
				springFactory(EnableAutoConfiguration.class),
				springFactory(BeanDefinitionPostProcessor.class),
				springFactory(BeanRegistrationWriterSupplier.class),
				springFactory(BeanDefinitionOriginAnalyzer.class),
				springFactory(NativeConfiguration.class),
				springFactory(BootstrapContributor.class),
				springFactory(ContextCustomizerFactory.class,
						"org.springframework.boot.test.autoconfigure.OverrideAutoConfigurationContextCustomizerFactory"),
				springFactory(ContextCustomizerFactory.class,
						"org.springframework.boot.test.autoconfigure.filter.TypeExcludeFiltersContextCustomizerFactory"),
				springFactory(ContextCustomizerFactory.class,
						"org.springframework.boot.test.context.ImportsContextCustomizerFactory"));
	}

	static Stream<Arguments> springFactoriesNotHandled() {
		return Stream.of(
				springFactory(ContextCustomizerFactory.class,
						"org.springframework.boot.test.web.client.TestRestTemplateContextCustomizerFactory"));
	}


	private static Arguments springFactory(Class<?> factoryType) {
		return springFactory(factoryType, "java.lang.Object");
	}

	private static Arguments springFactory(Class<?> factoryType, String factoryImplementation) {
		return Arguments.of(createSpringFactory(factoryType, factoryImplementation));
	}

	private static SpringFactory createSpringFactory(Class<?> factoryType, String factoryImplementation) {
		return SpringFactory.resolve(factoryType.getName(), factoryImplementation, IgnoredFactoriesCodeContributorTests.class.getClassLoader());
	}

}

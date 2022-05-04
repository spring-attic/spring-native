package org.springframework.aot.factories;

import org.junit.jupiter.api.Test;

import org.springframework.aot.factories.fixtures.TestFactory;
import org.springframework.boot.diagnostics.FailureAnalyzer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sebastien Deleuze
 */
public class FailureAnalyzersCodeContributorTests {

	private final FailureAnalyzersCodeContributor factoriesCodeContributor = new FailureAnalyzersCodeContributor();

	@Test
	void canContributeFactoryTypesToFailureAnalyzer() {
		assertThat(factoriesCodeContributor.canContribute(createSpringFactory(FailureAnalyzer.class, Object.class.getName()))).isTrue();
	}

	@Test
	void cantContributeFactoryTypes() {
		assertThat(factoriesCodeContributor.canContribute(createSpringFactory(TestFactory.class, Object.class.getName()))).isFalse();
	}

	private static SpringFactory createSpringFactory(Class<?> factoryType, String factoryImplementation) {
		return SpringFactory.resolve(factoryType.getName(), factoryImplementation, IgnoredFactoriesCodeContributorTests.class.getClassLoader());
	}
}

package org.springframework.boot.test.mock.mockito;

import java.util.Set;

import org.springframework.aot.context.bootstrap.generator.BeanDefinitionExcludeFilter;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * A {@link BeanDefinitionExcludeFilter} that removes unsupported test infrastructure like Mockito.
 *
 * @author Sebastien Deleuze
 */
class MockitoExcludeFilter implements BeanDefinitionExcludeFilter {

	private static final String MOCKITO_POST_PROCESSOR_BEAN_NAME = "org.springframework.boot.test.mock.mockito.MockitoPostProcessor";

	private static final String MOCKITO_SPY_POST_PROCESSOR_BEAN_NAME = MOCKITO_POST_PROCESSOR_BEAN_NAME + "$SpyPostProcessor";

	@Override
	public boolean isExcluded(String beanName, BeanDefinition beanDefinition) {
		boolean isExcluded = false;
		if (MOCKITO_POST_PROCESSOR_BEAN_NAME.equals(beanName)) {
			new MockitoBeanDefinitionChecker().check(beanDefinition);
			isExcluded = true;
		}
		else if (MOCKITO_SPY_POST_PROCESSOR_BEAN_NAME.equals(beanName)) {
			isExcluded = true;
		}
		return isExcluded;
	}

	static class MockitoBeanDefinitionChecker {

		private void check(BeanDefinition beanDefinition) {
			Set<Definition> definitions = (Set) beanDefinition.getConstructorArgumentValues().getArgumentValue(0, Set.class).getValue();
			for (Definition definition : definitions) {
				if (definition instanceof MockDefinition) {
					throw new IllegalStateException("@MockBean is not supported yet by Spring AOT and has been detected on type " + ((MockDefinition)definition).getTypeToMock());
				}
				if (definition instanceof SpyDefinition) {
					throw new IllegalStateException("@SpyBean is not supported yet by Spring AOT and has been detected on type " + ((SpyDefinition)definition).getTypeToSpy());
				}
			}
		}
	}
}

package org.springframework.aot.test.boot;

import org.springframework.aot.context.bootstrap.generator.BeanDefinitionExcludeFilter;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * A {@link BeanDefinitionExcludeFilter} that removes unsupported test infrastructure like Mockito.
 *
 * @author Stephane Nicoll
 */
class SpringBootTestInfrastructureExcludeFilter implements BeanDefinitionExcludeFilter {

	private final BeanDefinitionExcludeFilter filter = BeanDefinitionExcludeFilter.forBeanNames(
			"org.springframework.boot.test.mock.mockito.MockitoPostProcessor",
			"org.springframework.boot.test.mock.mockito.MockitoPostProcessor$SpyPostProcessor");

	@Override
	public boolean isExcluded(String beanName, BeanDefinition beanDefinition) {
		return this.filter.isExcluded(beanName, beanDefinition);
	}
}

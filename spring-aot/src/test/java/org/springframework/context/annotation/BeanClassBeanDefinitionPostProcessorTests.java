package org.springframework.context.annotation;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link BeanClassBeanDefinitionPostProcessor}.
 *
 * @author Stephane Nicoll
 */
class BeanClassBeanDefinitionPostProcessorTests {

	@Test
	void postProcessBeanDefinitionLoadBeanClass() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition("java.lang.String");
		assertThat(beanDefinition.hasBeanClass()).isFalse();
		postProcess(beanDefinition);
		assertThat(beanDefinition.hasBeanClass()).isTrue();
		assertThat(beanDefinition.getBeanClass()).isEqualTo(String.class);
	}

	@Test
	void postProcessBeanDefinitionWithBeanClassIsIgnored() {
		RootBeanDefinition beanDefinition = mock(RootBeanDefinition.class);
		given(beanDefinition.hasBeanClass()).willReturn(true);
		postProcess(beanDefinition);
		verify(beanDefinition).hasBeanClass();
		verifyNoMoreInteractions(beanDefinition);
	}

	@Test
	void postProcessBeanDefinitionIgnoreInvalidBeanClassName() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition("com.example.DoesNotExist");
		postProcess(beanDefinition);
		assertThat(beanDefinition.hasBeanClass()).isFalse();
	}

	private void postProcess(RootBeanDefinition beanDefinition) {
		BeanClassBeanDefinitionPostProcessor processor = new BeanClassBeanDefinitionPostProcessor();
		processor.setBeanFactory(new DefaultListableBeanFactory());
		processor.postProcessBeanDefinition("test", beanDefinition);
	}

}

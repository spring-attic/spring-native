package org.springframework.context.bootstrap.generator.bean;

import java.util.function.BiFunction;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link BeanRegistrationWriterOptions}.
 *
 * @author Stephane Nicoll
 */
class BeanRegistrationWriterOptionsTests {

	@Test
	void getWriterForWithoutFunctionReturnNull() {
		assertThat(BeanRegistrationWriterOptions.builder().build()
				.getWriterFor("test", new RootBeanDefinition())).isNull();
	}

	@Test
	@SuppressWarnings("unchecked")
	void getWriterForInvokeFunction() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		BeanRegistrationWriter writer = mock(BeanRegistrationWriter.class);
		BiFunction<String, BeanDefinition, BeanRegistrationWriter> writerFactory = mock(BiFunction.class);
		given(writerFactory.apply("test", beanDefinition)).willReturn(writer);
		BeanRegistrationWriterOptions options = BeanRegistrationWriterOptions.builder().withWriterFactory(writerFactory).build();
		assertThat(options.getWriterFor("test", beanDefinition)).isEqualTo(writer);
		verify(writerFactory).apply("test", beanDefinition);
	}

}

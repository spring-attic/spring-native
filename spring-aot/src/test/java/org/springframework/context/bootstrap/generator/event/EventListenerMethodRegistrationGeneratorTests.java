package org.springframework.context.bootstrap.generator.event;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.context.bootstrap.generator.sample.event.AnotherEventListener;
import org.springframework.context.bootstrap.generator.sample.event.SingleEventListener;
import org.springframework.context.bootstrap.generator.sample.event.SingleTransactionalEventListener;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.transaction.event.TransactionalEventListenerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EventListenerMethodRegistrationGenerator}.
 *
 * @author Stephane Nicoll
 */
class EventListenerMethodRegistrationGeneratorTests {

	@Test
	void writeEventListenersRegistrationWithNoEventListener() {
		DefaultListableBeanFactory beanFactory = prepareBeanFactory();
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(SimpleConfiguration.class)
				.getBeanDefinition());
		assertThat(generateCode(beanFactory)).isEmpty();
	}

	@Test
	void writeEventListenersRegistrationWithSingleEventListener() {
		DefaultListableBeanFactory beanFactory = prepareBeanFactory();
		beanFactory.registerBeanDefinition("single", BeanDefinitionBuilder.rootBeanDefinition(SingleEventListener.class)
				.getBeanDefinition());
		assertThat(generateCode(beanFactory)).lines().containsExactly("context.registerBean(\"org.springframework.aot.EventListenerRegistrar\", EventListenerRegistrar.class, () -> new EventListenerRegistrar(context,",
				"    EventListenerMetadata.forBean(\"single\", SingleEventListener.class).annotatedMethod(\"onStartup\", ApplicationStartedEvent.class))", ");");
	}

	@Test
	void writeEventListenersRegistrationWithEventListeners() {
		DefaultListableBeanFactory beanFactory = prepareBeanFactory();
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(SingleEventListener.class)
				.getBeanDefinition());
		beanFactory.registerBeanDefinition("another", BeanDefinitionBuilder.rootBeanDefinition(AnotherEventListener.class)
				.getBeanDefinition());
		assertThat(generateCode(beanFactory)).lines().containsExactly("context.registerBean(\"org.springframework.aot.EventListenerRegistrar\", EventListenerRegistrar.class, () -> new EventListenerRegistrar(context,",
				"    EventListenerMetadata.forBean(\"test\", SingleEventListener.class).annotatedMethod(\"onStartup\", ApplicationStartedEvent.class)),",
				"    EventListenerMetadata.forBean(\"another\", AnotherEventListener.class).annotatedMethod(\"onRefresh\"))", ");");
	}

	@Test
	void writeEventListenersRegistrationWithCustomEventListenerFactory() {
		DefaultListableBeanFactory beanFactory = prepareBeanFactory();
		beanFactory.registerBeanDefinition("internalTxEventListenerFactory",
				BeanDefinitionBuilder.rootBeanDefinition(TransactionalEventListenerFactory.class).setRole(BeanDefinition.ROLE_INFRASTRUCTURE).getBeanDefinition());
		beanFactory.registerBeanDefinition("simple", BeanDefinitionBuilder.rootBeanDefinition(SingleEventListener.class)
				.getBeanDefinition());
		beanFactory.registerBeanDefinition("transactional", BeanDefinitionBuilder.rootBeanDefinition(SingleTransactionalEventListener.class)
				.getBeanDefinition());
		assertThat(generateCode(beanFactory)).lines().containsExactly("context.registerBean(\"org.springframework.aot.EventListenerRegistrar\", EventListenerRegistrar.class, () -> new EventListenerRegistrar(context,",
				"    EventListenerMetadata.forBean(\"simple\", SingleEventListener.class).annotatedMethod(\"onStartup\", ApplicationStartedEvent.class)),",
				"    EventListenerMetadata.forBean(\"transactional\", SingleTransactionalEventListener.class).eventListenerFactoryBeanName(\"internalTxEventListenerFactory\").annotatedMethod(\"onEvent\", ApplicationEvent.class))",
				");");
	}


	private DefaultListableBeanFactory prepareBeanFactory() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		AnnotationConfigUtils.registerAnnotationConfigProcessors(beanFactory);
		return beanFactory;
	}

	private CodeSnippet generateCode(ConfigurableListableBeanFactory beanFactory) {
		EventListenerMethodRegistrationGenerator processor = new EventListenerMethodRegistrationGenerator(beanFactory);
		return CodeSnippet.of(processor::writeEventListenersRegistration);
	}

}

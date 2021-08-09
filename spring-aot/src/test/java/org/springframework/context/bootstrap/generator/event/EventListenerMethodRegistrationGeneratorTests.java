package org.springframework.context.bootstrap.generator.event;

import java.util.List;

import com.squareup.javapoet.CodeBlock;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.bootstrap.generator.BootstrapClass;
import org.springframework.context.bootstrap.generator.BootstrapWriterContext;
import org.springframework.context.bootstrap.generator.reflect.RuntimeReflectionEntry;
import org.springframework.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.context.bootstrap.generator.sample.event.AnotherEventListener;
import org.springframework.context.bootstrap.generator.sample.event.SingleEventListener;
import org.springframework.context.bootstrap.generator.sample.event.SingleTransactionalEventListener;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.util.ReflectionUtils;

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
				"    EventListenerMetadata.forBean(\"single\", SingleEventListener.class).annotatedMethod(\"onStartup\", ApplicationStartedEvent.class)", "));");
	}

	@Test
	void writeEventListenersRegistrationWithEventListeners() {
		DefaultListableBeanFactory beanFactory = prepareBeanFactory();
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(SingleEventListener.class)
				.getBeanDefinition());
		beanFactory.registerBeanDefinition("another", BeanDefinitionBuilder.rootBeanDefinition(AnotherEventListener.class)
				.getBeanDefinition());
		assertThat(generateCode(beanFactory)).lines().containsExactly("context.registerBean(\"org.springframework.aot.EventListenerRegistrar\", EventListenerRegistrar.class, () -> new EventListenerRegistrar(context,",
				"    EventListenerMetadata.forBean(\"test\", SingleEventListener.class).annotatedMethod(\"onStartup\", ApplicationStartedEvent.class),",
				"    EventListenerMetadata.forBean(\"another\", AnotherEventListener.class).annotatedMethod(\"onRefresh\")", "));");
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
				"    EventListenerMetadata.forBean(\"simple\", SingleEventListener.class).annotatedMethod(\"onStartup\", ApplicationStartedEvent.class),",
				"    EventListenerMetadata.forBean(\"transactional\", SingleTransactionalEventListener.class).eventListenerFactoryBeanName(\"internalTxEventListenerFactory\").annotatedMethod(\"onEvent\", ApplicationEvent.class)",
				"));");
	}

	@Test
	void writeEventListenersRegistrationRegisterReflectionMetadata() {
		DefaultListableBeanFactory beanFactory = prepareBeanFactory();
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(SingleEventListener.class)
				.getBeanDefinition());
		beanFactory.registerBeanDefinition("another", BeanDefinitionBuilder.rootBeanDefinition(AnotherEventListener.class)
				.getBeanDefinition());
		EventListenerMethodRegistrationGenerator processor = new EventListenerMethodRegistrationGenerator(beanFactory);
		BootstrapWriterContext context = new BootstrapWriterContext(new BootstrapClass("com.example", "Test"));
		processor.writeEventListenersRegistration(context, CodeBlock.builder());
		List<RuntimeReflectionEntry> entries = context.getRuntimeReflectionRegistry().getEntries();
		assertThat(entries).hasSize(2);
		assertThat(entries).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(SingleEventListener.class);
			assertThat(entry.getMethods()).containsOnly(
					ReflectionUtils.findMethod(SingleEventListener.class, "onStartup", ApplicationStartedEvent.class));
			assertThat(entry.getFields()).isEmpty();
		});
		assertThat(entries).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(AnotherEventListener.class);
			assertThat(entry.getMethods()).containsOnly(
					ReflectionUtils.findMethod(AnotherEventListener.class, "onRefresh"));
			assertThat(entry.getFields()).isEmpty();
		});
	}

	private DefaultListableBeanFactory prepareBeanFactory() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		AnnotationConfigUtils.registerAnnotationConfigProcessors(beanFactory);
		return beanFactory;
	}

	private CodeSnippet generateCode(ConfigurableListableBeanFactory beanFactory) {
		EventListenerMethodRegistrationGenerator processor = new EventListenerMethodRegistrationGenerator(beanFactory);
		BootstrapWriterContext context = new BootstrapWriterContext(new BootstrapClass("com.example", "Test"));
		return CodeSnippet.of((code) -> processor.writeEventListenersRegistration(context, code));
	}

}

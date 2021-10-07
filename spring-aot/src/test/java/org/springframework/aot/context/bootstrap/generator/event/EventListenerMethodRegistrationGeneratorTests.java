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

package org.springframework.aot.context.bootstrap.generator.event;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.function.BiConsumer;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry;
import org.springframework.aot.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.event.AnotherEventListener;
import org.springframework.aot.context.bootstrap.generator.sample.event.SingleEventListener;
import org.springframework.aot.context.bootstrap.generator.sample.event.SingleTransactionalEventListener;
import org.springframework.aot.context.bootstrap.generator.sample.scope.SimpleServiceImpl;
import org.springframework.aot.context.bootstrap.generator.sample.visibility.ProtectedEventListenerConfiguration;
import org.springframework.aot.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.aot.context.bootstrap.generator.test.TextAssert;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.BuildTimeBeanDefinitionsRegistrar;
import org.springframework.context.support.GenericApplicationContext;
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
		assertGeneratedCode(beanFactory, (code, writerContext) -> assertThat(code).isEmpty());
	}

	@Test
	void writeEventListenersRegistrationWithSingleEventListener() {
		DefaultListableBeanFactory beanFactory = prepareBeanFactory();
		beanFactory.registerBeanDefinition("single", BeanDefinitionBuilder.rootBeanDefinition(SingleEventListener.class)
				.getBeanDefinition());
		assertGeneratedCode(beanFactory, (code, writerContext) -> {
			assertThat(code).lines().contains("context.registerBean(\"org.springframework.aot.EventListenerRegistrar\", EventListenerRegistrar.class, "
					+ "() -> new EventListenerRegistrar(context, com.example.Test.getEventListenersMetadata()));");
			assertGeneratedCode(writerContext.getMainBootstrapClass()).removeIndent(1).lines().containsSequence(
					"public static List<EventListenerMetadata> getEventListenersMetadata() {",
					"  return List.of(",
					"    EventListenerMetadata.forBean(\"single\", SingleEventListener.class).annotatedMethod(\"onStartup\", ApplicationStartedEvent.class)",
					"  );",
					"}");
		});
	}

	@Test
	void writeEventListenersRegistrationWithEventListeners() {
		DefaultListableBeanFactory beanFactory = prepareBeanFactory();
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(SingleEventListener.class)
				.getBeanDefinition());
		beanFactory.registerBeanDefinition("another", BeanDefinitionBuilder.rootBeanDefinition(AnotherEventListener.class)
				.getBeanDefinition());
		assertGeneratedCode(beanFactory, (code, writerContext) -> assertGeneratedCode(writerContext.getMainBootstrapClass())
				.removeIndent(1).lines().containsSequence(
						"public static List<EventListenerMetadata> getEventListenersMetadata() {",
						"  return List.of(",
						"    EventListenerMetadata.forBean(\"test\", SingleEventListener.class).annotatedMethod(\"onStartup\", ApplicationStartedEvent.class),",
						"    EventListenerMetadata.forBean(\"another\", AnotherEventListener.class).annotatedMethod(\"onRefresh\")",
						"  );",
						"}"));
	}

	@Test
	void writeEventListenersRegistrationWithPackageProtectedEventListener() {
		GenericApplicationContext context = new GenericApplicationContext(prepareBeanFactory());
		context.registerBeanDefinition("configuration", BeanDefinitionBuilder.rootBeanDefinition(ProtectedEventListenerConfiguration.class)
				.getBeanDefinition());
		BuildTimeBeanDefinitionsRegistrar registrar = new BuildTimeBeanDefinitionsRegistrar();
		ConfigurableListableBeanFactory beanFactory = registrar.processBeanDefinitions(context);
		assertGeneratedCode(beanFactory, (code, writerContext) -> {
			String protectedAccess = ProtectedEventListenerConfiguration.class.getPackageName() + ".Test.getEventListenersMetadata()";
			assertThat(code).lines().containsExactly("context.registerBean(\"org.springframework.aot.EventListenerRegistrar\", "
					+ "EventListenerRegistrar.class, () -> new EventListenerRegistrar(context, " + protectedAccess + "));");
			BootstrapClass bootstrapClass = writerContext.getBootstrapClass(ProtectedEventListenerConfiguration.class.getPackageName());
			assertGeneratedCode(bootstrapClass).removeIndent(1).lines().containsSequence(
					"public static List<EventListenerMetadata> getEventListenersMetadata() {",
					"  return List.of(",
					"    EventListenerMetadata.forBean(\"protectedEventListener\", ProtectedEventListener.class).annotatedMethod(\"onStartup\", ApplicationStartedEvent.class)",
					"  );",
					"}"
			);
		});
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
		assertGeneratedCode(beanFactory, (code, writerContext) -> {
			assertGeneratedCode(writerContext.getMainBootstrapClass()).removeIndent(1).lines().containsSequence(
					"public static List<EventListenerMetadata> getEventListenersMetadata() {",
					"  return List.of(",
					"    EventListenerMetadata.forBean(\"simple\", SingleEventListener.class).annotatedMethod(\"onStartup\", ApplicationStartedEvent.class),",
					"    EventListenerMetadata.forBean(\"transactional\", SingleTransactionalEventListener.class).eventListenerFactoryBeanName(\"internalTxEventListenerFactory\").annotatedMethod(\"onEvent\", ApplicationEvent.class)",
					"  );",
					"}");
		});
	}

	@Test
	void writeEventListenersRegistrationWithScopedProxy() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(SimpleServiceImpl.class);
		BuildTimeBeanDefinitionsRegistrar registrar = new BuildTimeBeanDefinitionsRegistrar();
		ConfigurableListableBeanFactory beanFactory = registrar.processBeanDefinitions(context);
		assertGeneratedCode(beanFactory, (code, writerContext) -> {
			assertGeneratedCode(writerContext.getMainBootstrapClass()).removeIndent(1).lines().containsSequence(
					"public static List<EventListenerMetadata> getEventListenersMetadata() {",
					"  return List.of(",
					"    EventListenerMetadata.forBean(\"simpleServiceImpl\", SimpleServiceImpl.class).annotatedMethod(\"onContextRefresh\")",
					"  );",
					"}");
		});
	}

	@Test
	void writeEventListenersRegistrationRegisterReflectionMetadata() {
		DefaultListableBeanFactory beanFactory = prepareBeanFactory();
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(SingleEventListener.class)
				.getBeanDefinition());
		beanFactory.registerBeanDefinition("another", BeanDefinitionBuilder.rootBeanDefinition(AnotherEventListener.class)
				.getBeanDefinition());
		assertGeneratedCode(beanFactory, (code, writerContext) -> {
			List<NativeReflectionEntry> entries = writerContext.getNativeConfigurationRegistry().reflection().getEntries();
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
		});
	}

	private DefaultListableBeanFactory prepareBeanFactory() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		AnnotationConfigUtils.registerAnnotationConfigProcessors(beanFactory);
		return beanFactory;
	}

	void assertGeneratedCode(ConfigurableListableBeanFactory beanFactory, BiConsumer<CodeSnippet, BootstrapWriterContext> generatedContent) {
		EventListenerMethodRegistrationGenerator processor = new EventListenerMethodRegistrationGenerator(beanFactory);
		BootstrapWriterContext writerContext = new BootstrapWriterContext("com.example", "Test");
		Builder code = CodeBlock.builder();
		processor.writeEventListenersRegistration(writerContext, code);
		generatedContent.accept(CodeSnippet.of(code.build()), writerContext);
	}

	private TextAssert assertGeneratedCode(BootstrapClass bootstrapClass) {
		try {
			StringWriter out = new StringWriter();
			bootstrapClass.toJavaFile().writeTo(out);
			return new TextAssert(out.toString());
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}

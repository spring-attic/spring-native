package org.springframework.context.bootstrap.generator.event;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.bootstrap.generator.sample.event.AnotherEventListener;
import org.springframework.context.bootstrap.generator.sample.event.SingleEventListener;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EventListenerMetadataGenerator}.
 *
 * @author Stephane Nicoll
 */
class EventListenerMetadataGeneratorTests {

	@Test
	void eventListenerMetadataWithDefaultFactoryAndNoParameter() {
		Method method = ReflectionUtils.findMethod(AnotherEventListener.class, "onRefresh");
		assertThat(generateCode(new EventListenerMetadataGenerator("test", AnotherEventListener.class, method, null))).isEqualTo(
				"EventListenerMetadata.forBean(\"test\", AnotherEventListener.class).annotatedMethod(\"onRefresh\"))");
	}

	@Test
	void eventListenerMetadataWithDefaultFactoryAndParameter() {
		Method method = ReflectionUtils.findMethod(SingleEventListener.class, "onStartup", ApplicationStartedEvent.class);
		assertThat(generateCode(new EventListenerMetadataGenerator("test", SingleEventListener.class, method, null))).isEqualTo(
				"EventListenerMetadata.forBean(\"test\", SingleEventListener.class).annotatedMethod(\"onStartup\", ApplicationStartedEvent.class))");
	}

	private CodeSnippet generateCode(EventListenerMetadataGenerator generator) {
		return CodeSnippet.of(generator::writeEventListenerMetadata);
	}

}

package org.springframework.context.event;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationListener;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link EventListenerRegistrar}.
 *
 * @author Stephane Nicoll
 */
class EventListenerRegistrarTests {

	@Test
	void registerEventListenerWithNoArgument() {
		GenericApplicationContext context = new GenericApplicationContext();
		TestComponent testComponent = mock(TestComponent.class);
		context.registerBean("test", TestComponent.class, () -> testComponent);
		context.registerBean("infrastructure", EventListenerRegistrar.class,
				() -> new EventListenerRegistrar(context, EventListenerMetadata.forBean("test", TestComponent.class).annotatedMethod("onRefresh")));
		context.refresh();
		verify(testComponent).onRefresh();
		context.close();
		verifyNoMoreInteractions(testComponent);
	}

	@Test
	void registerEventListenerWithArgument() {
		GenericApplicationContext context = new GenericApplicationContext();
		TestComponent testComponent = mock(TestComponent.class);
		context.registerBean("test", TestComponent.class, () -> testComponent);
		context.registerBean("infrastructure", EventListenerRegistrar.class,
				() -> new EventListenerRegistrar(context, EventListenerMetadata.forBean("test", TestComponent.class).annotatedMethod("onClosed", ContextClosedEvent.class)));
		context.refresh();
		verifyNoInteractions(testComponent);
		context.close();
		verify(testComponent).onClosed(any(ContextClosedEvent.class));
	}

	@Test
	void registerEventListenerWithCustomFactory() {
		GenericApplicationContext context = new GenericApplicationContext();
		ApplicationListener<?> applicationListener = mock(ApplicationListener.class);
		EventListenerFactory factory = mock(EventListenerFactory.class);
		given(factory.createApplicationListener(eq("test"), eq(TestComponent.class), any(Method.class)))
				.will((invocation) -> applicationListener);
		context.registerBean("testFactory", EventListenerFactory.class, () -> factory);
		context.registerBean("test", TestComponent.class, TestComponent::new);
		context.registerBean("infrastructure", EventListenerRegistrar.class,
				() -> new EventListenerRegistrar(context, EventListenerMetadata.forBean("test", TestComponent.class)
						.eventListenerFactoryBeanName("testFactory").annotatedMethod("onRefresh")));
		assertThat(context.getApplicationListeners()).doesNotContain(applicationListener);
		context.refresh();
		assertThat(context.getApplicationListeners()).contains(applicationListener);
		context.close();
	}

	static class TestComponent {

		@EventListener(classes = ContextRefreshedEvent.class)
		public void onRefresh() {

		}

		@EventListener
		public void onClosed(ContextClosedEvent event) {

		}

	}

}

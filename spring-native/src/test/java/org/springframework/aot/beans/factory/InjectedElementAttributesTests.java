package org.springframework.aot.beans.factory;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for {@link InjectedElementAttributes}.
 *
 * @author Stephane Nicoll
 */
class InjectedElementAttributesTests {

	private static final InjectedElementAttributes unresolved = new InjectedElementAttributes(null);

	private static final InjectedElementAttributes resolved = new InjectedElementAttributes(Collections.singletonList("test"));

	@Test
	void isResolvedWithUnresolvedAttributes() {
		assertThat(unresolved.isResolved()).isFalse();
	}

	@Test
	void isResolvedWithResoledAttributes() {
		assertThat(resolved.isResolved()).isTrue();
	}

	@Test
	void ifResolvedWithUnresolvedAttributesDoesNotInvokeRunnable() {
		Runnable runnable = mock(Runnable.class);
		unresolved.ifResolved(runnable);
		verifyNoInteractions(runnable);
	}

	@Test
	void ifResolvedWithResolvedAttributesInvokesRunnable() {
		Runnable runnable = mock(Runnable.class);
		resolved.ifResolved(runnable);
		verify(runnable).run();
	}

	@Test
	@SuppressWarnings("unchecked")
	void ifResolvedWithUnresolvedAttributesDoesNotInvokeConsumer() {
		ThrowableConsumer<InjectedElementAttributes> consumer = mock(ThrowableConsumer.class);
		unresolved.ifResolved(consumer);
		verifyNoInteractions(consumer);
	}

	@Test
	@SuppressWarnings("unchecked")
	void ifResolvedWithResolvedAttributesInvokesConsumer() {
		ThrowableConsumer<InjectedElementAttributes> consumer = mock(ThrowableConsumer.class);
		resolved.ifResolved(consumer);
		verify(consumer).accept(resolved);
	}

	@Test
	void getWithAvailableAttribute() {
		InjectedElementAttributes attributes = new InjectedElementAttributes(Collections.singletonList("test"));
		assertThat((String) attributes.get(0)).isEqualTo("test");
	}

}

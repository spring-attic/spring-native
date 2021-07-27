package org.springframework.context.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link EventListenerMetadata}.
 *
 * @author Stephane Nicoll
 */
class EventListenerMetadataTests {

	@Test
	void metadataWithInvalidMethod() {
		assertThatIllegalStateException().isThrownBy(() -> EventListenerMetadata.forBean("test", Object.class).annotatedMethod("doesNotExist"))
				.withMessageContaining("'doesNotExist'").withMessageContaining(Object.class.getName());
	}

}

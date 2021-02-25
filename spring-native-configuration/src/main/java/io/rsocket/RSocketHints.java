package io.rsocket;

import io.netty.util.internal.shaded.org.jctools.queues.IndexedQueueSizeUtil;

import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;

@NativeHint(trigger = IndexedQueueSizeUtil.class, types = {
		@TypeHint(
				typeNames = "io.rsocket.internal.jctools.queues.BaseMpscLinkedArrayQueueColdProducerFields",
				fields = @FieldHint(name = "producerLimit", allowUnsafeAccess = true)),
		@TypeHint(
				typeNames = "io.rsocket.internal.jctools.queues.BaseMpscLinkedArrayQueueProducerFields",
				fields = @FieldHint(name = "producerIndex", allowUnsafeAccess = true)),
		@TypeHint(
				typeNames = "io.rsocket.internal.jctools.queues.BaseMpscLinkedArrayQueueConsumerFields",
				fields = @FieldHint(name = "consumerIndex", allowUnsafeAccess = true)),
		})
public class RSocketHints implements NativeConfiguration {
}

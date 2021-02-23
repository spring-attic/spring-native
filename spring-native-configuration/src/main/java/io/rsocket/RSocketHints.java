package io.rsocket;

import io.netty.util.internal.shaded.org.jctools.queues.IndexedQueueSizeUtil;

import org.springframework.nativex.hint.FieldInfo;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeInfo;

@NativeHint(trigger = IndexedQueueSizeUtil.class, types = {
		@TypeInfo(
				typeNames = "io.rsocket.internal.jctools.queues.BaseMpscLinkedArrayQueueColdProducerFields",
				fields = @FieldInfo(name = "producerLimit", allowUnsafeAccess = true)),
		@TypeInfo(
				typeNames = "io.rsocket.internal.jctools.queues.BaseMpscLinkedArrayQueueProducerFields",
				fields = @FieldInfo(name = "producerIndex", allowUnsafeAccess = true)),
		@TypeInfo(
				typeNames = "io.rsocket.internal.jctools.queues.BaseMpscLinkedArrayQueueConsumerFields",
				fields = @FieldInfo(name = "consumerIndex", allowUnsafeAccess = true)),
		})
public class RSocketHints implements NativeConfiguration {
}

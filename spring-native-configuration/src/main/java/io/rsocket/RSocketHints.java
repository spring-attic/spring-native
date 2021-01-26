package io.rsocket;

import io.netty.util.internal.shaded.org.jctools.queues.IndexedQueueSizeUtil;

import org.springframework.nativex.extension.FieldInfo;
import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.TypeInfo;

@NativeImageHint(trigger = IndexedQueueSizeUtil.class, typeInfos = {
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
public class RSocketHints implements NativeImageConfiguration {
}

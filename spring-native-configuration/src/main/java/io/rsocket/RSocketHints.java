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

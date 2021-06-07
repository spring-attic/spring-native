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

package io.lettuce;

import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;

@NativeHint(trigger = io.lettuce.core.RedisClient.class,
		types = {
				@TypeHint(types = {
						io.lettuce.core.AbstractRedisAsyncCommands.class,
						io.lettuce.core.RedisAsyncCommandsImpl.class,
						io.lettuce.core.RedisClient.class,
						io.lettuce.core.api.sync.BaseRedisCommands.class,
						io.lettuce.core.api.sync.RedisCommands.class,
						io.lettuce.core.api.sync.RedisGeoCommands.class,
						io.lettuce.core.api.sync.RedisHLLCommands.class,
						io.lettuce.core.api.sync.RedisHashCommands.class,
						io.lettuce.core.api.sync.RedisKeyCommands.class,
						io.lettuce.core.api.sync.RedisListCommands.class,
						io.lettuce.core.api.sync.RedisScriptingCommands.class,
						io.lettuce.core.api.sync.RedisServerCommands.class,
						io.lettuce.core.api.sync.RedisSetCommands.class,
						io.lettuce.core.api.sync.RedisSortedSetCommands.class,
						io.lettuce.core.api.sync.RedisStreamCommands.class,
						io.lettuce.core.api.sync.RedisStringCommands.class,
						io.lettuce.core.api.sync.RedisTransactionalCommands.class,
						io.lettuce.core.cluster.api.sync.RedisClusterCommands.class,
						io.lettuce.core.protocol.CommandHandler.class,
						io.lettuce.core.protocol.ConnectionWatchdog.class,
						io.lettuce.core.resource.ClientResources.class,
						io.lettuce.core.resource.DefaultClientResources.class,
						io.netty.buffer.AbstractByteBufAllocator.class,
						io.netty.buffer.PooledByteBufAllocator.class,
						io.netty.channel.ChannelDuplexHandler.class,
						io.netty.channel.ChannelHandlerAdapter.class,
						io.netty.channel.ChannelInboundHandlerAdapter.class,
						io.netty.channel.ChannelInitializer.class,
						io.netty.channel.ChannelOutboundHandlerAdapter.class,
						io.netty.channel.socket.nio.NioSocketChannel.class,
						io.netty.handler.codec.MessageToByteEncoder.class,
						io.netty.util.ReferenceCountUtil.class
				}),
				@TypeHint(typeNames = {
						"io.lettuce.core.ChannelGroupListener",
						"io.lettuce.core.ConnectionEventTrigger",
						"io.lettuce.core.PlainChannelInitializer$1",
						"io.netty.channel.DefaultChannelPipeline$HeadContext",
						"io.netty.channel.DefaultChannelPipeline$TailContext"
				}),
				@TypeHint(
						typeNames = "io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueColdProducerFields",
						fields = @FieldHint(name = "producerLimit", allowUnsafeAccess = true)),
				@TypeHint(
						typeNames = "io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueConsumerFields",
						fields = @FieldHint(name = "consumerIndex", allowUnsafeAccess = true)),
				@TypeHint(
						typeNames = "io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueProducerFields",
						fields = @FieldHint(name = "producerIndex", allowUnsafeAccess = true)),
				@TypeHint(
						typeNames = "io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueConsumerIndexField",
						fields = @FieldHint(name = "consumerIndex", allowUnsafeAccess = true)),
				@TypeHint(
						typeNames = "io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueProducerIndexField",
						fields = @FieldHint(name = "producerIndex", allowUnsafeAccess = true)),
				@TypeHint(
						typeNames = "io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueProducerLimitField",
						fields = @FieldHint(name = "producerLimit", allowUnsafeAccess = true))
		},
		jdkProxies = {
				@JdkProxyHint(typeNames = {
						"io.lettuce.core.api.sync.RedisCommands", "io.lettuce.core.cluster.api.sync.RedisClusterCommands"
				})
		}
)
public class LettuceHints implements NativeConfiguration {
}

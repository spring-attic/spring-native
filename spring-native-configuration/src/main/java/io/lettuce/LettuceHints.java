package io.lettuce;

import org.springframework.nativex.hint.FieldInfo;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyInfo;
import org.springframework.nativex.hint.TypeInfo;

@NativeHint(trigger = io.lettuce.core.RedisClient.class,
		typeInfos = {
				@TypeInfo(types = {
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
				@TypeInfo(typeNames = {
						"io.lettuce.core.ChannelGroupListener",
						"io.lettuce.core.ConnectionEventTrigger",
						"io.lettuce.core.PlainChannelInitializer$1",
						"io.netty.channel.DefaultChannelPipeline$HeadContext",
						"io.netty.channel.DefaultChannelPipeline$TailContext"
				}),
				@TypeInfo(
						typeNames = "io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueColdProducerFields",
						fields = @FieldInfo(name = "producerLimit", allowUnsafeAccess = true)),
				@TypeInfo(
						typeNames = "io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueConsumerFields",
						fields = @FieldInfo(name = "consumerIndex", allowUnsafeAccess = true)),
				@TypeInfo(
						typeNames = "io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueProducerFields",
						fields = @FieldInfo(name = "producerIndex", allowUnsafeAccess = true)),
				@TypeInfo(
						typeNames = "io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueConsumerIndexField",
						fields = @FieldInfo(name = "consumerIndex", allowUnsafeAccess = true)),
				@TypeInfo(
						typeNames = "io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueProducerIndexField",
						fields = @FieldInfo(name = "producerIndex", allowUnsafeAccess = true)),
				@TypeInfo(
						typeNames = "io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueProducerLimitField",
						fields = @FieldInfo(name = "producerLimit", allowUnsafeAccess = true))
		},
		proxyInfos = {
				@ProxyInfo(typeNames = {
						"io.lettuce.core.api.sync.RedisCommands", "io.lettuce.core.cluster.api.sync.RedisClusterCommands"
				})
		}
)
public class LettuceHints implements NativeConfiguration {
}

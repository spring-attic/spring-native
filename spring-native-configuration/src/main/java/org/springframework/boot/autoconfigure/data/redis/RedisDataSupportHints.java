/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.boot.autoconfigure.data.redis;

import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.ProxyInfo;
import org.springframework.nativex.extension.TypeInfo;

/**
 * @author Christoph Strobl
 */
@NativeImageHint(trigger = LettuceConnectionConfiguration.class,
		typeInfos = {
				@TypeInfo(typeNames = {
						// lettuce

						// TODO: open pr with config for Lettuce driver

						"io.lettuce.core.AbstractRedisAsyncCommands",
						"io.lettuce.core.ChannelGroupListener",
						"io.lettuce.core.ConnectionEventTrigger",
						"io.lettuce.core.PlainChannelInitializer$1",
						"io.lettuce.core.RedisAsyncCommandsImpl",
						"io.lettuce.core.RedisClient",

						"io.lettuce.core.api.sync.BaseRedisCommands",
						"io.lettuce.core.api.sync.RedisCommands",
						"io.lettuce.core.api.sync.RedisGeoCommands",
						"io.lettuce.core.api.sync.RedisHLLCommands",
						"io.lettuce.core.api.sync.RedisHashCommands",
						"io.lettuce.core.api.sync.RedisKeyCommands",
						"io.lettuce.core.api.sync.RedisListCommands",
						"io.lettuce.core.api.sync.RedisScriptingCommands",
						"io.lettuce.core.api.sync.RedisServerCommands",
						"io.lettuce.core.api.sync.RedisSetCommands",
						"io.lettuce.core.api.sync.RedisSortedSetCommands",
						"io.lettuce.core.api.sync.RedisStreamCommands",
						"io.lettuce.core.api.sync.RedisStringCommands",
						"io.lettuce.core.api.sync.RedisTransactionalCommands",

						"io.lettuce.core.cluster.api.sync.RedisClusterCommands",

						"io.lettuce.core.protocol.CommandHandler",
						"io.lettuce.core.protocol.ConnectionWatchdog",

						"io.lettuce.core.resource.ClientResources",
						"io.lettuce.core.resource.DefaultClientResources",

						// netty
						"io.netty.buffer.AbstractByteBufAllocator",
						"io.netty.buffer.PooledByteBufAllocator",
						"io.netty.channel.ChannelDuplexHandler",
						"io.netty.channel.ChannelHandlerAdapter",
						"io.netty.channel.ChannelInboundHandlerAdapter",
						"io.netty.channel.ChannelInitializer",
						"io.netty.channel.ChannelOutboundHandlerAdapter",
						"io.netty.channel.DefaultChannelPipeline$HeadContext",
						"io.netty.channel.DefaultChannelPipeline$TailContext",
						"io.netty.channel.socket.nio.NioSocketChannel",
						"io.netty.handler.codec.MessageToByteEncoder",
						"io.netty.util.ReferenceCountUtil"

				/*
				   and we need some unsafe methods in the reflect config (listed here so we find a better place)

				   {
 				     "name":"io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueColdProducerFields",
 				     "fields":[{"name":"producerLimit","allowUnsafeAccess" :  true}]
 				   },
 				   {
 				     "name":"io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueConsumerFields",
 				     "fields":[{"name":"consumerIndex","allowUnsafeAccess" :  true}]
 				   },
 				   {
 				     "name":"io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueProducerFields",
 				     "fields":[{"name":"producerIndex", "allowUnsafeAccess" :  true}]
 				   },
 				   {
 				     "name":"io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueConsumerIndexField",
 				     "fields":[{"name":"consumerIndex", "allowUnsafeAccess" :  true}]
 				   },
 				   {
 				     "name":"io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueProducerIndexField",
 				     "fields":[{"name":"producerIndex", "allowUnsafeAccess" :  true}]
 				   },
 				   {
 				     "name":"io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueProducerLimitField",
 				     "fields":[{"name":"producerLimit","allowUnsafeAccess" :  true}]
 				   },
 				   {
 				     "name":"java.nio.Buffer",
 				     "fields":[{"name":"address", "allowUnsafeAccess":true}]
 				   },
 				   {
 				     "name":"java.nio.DirectByteBuffer",
 				     "fields":[{"name":"cleaner", "allowUnsafeAccess":true}],
 				     "methods":[{"name":"<init>","parameterTypes":["long","int"] }]
 				   },
 				   {
 				     "name":"io.netty.buffer.AbstractReferenceCountedByteBuf",
 				     "fields":[{"name":"refCnt", "allowUnsafeAccess":true}]
 				   }
				 */
				})
		},
		proxyInfos = {
				@ProxyInfo(typeNames = {
						"io.lettuce.core.api.sync.RedisCommands", "io.lettuce.core.cluster.api.sync.RedisClusterCommands"
				})
		}
)

@NativeImageHint(trigger = RedisAutoConfiguration.class, //
		typeInfos = {
				@TypeInfo(types = {

						org.springframework.data.redis.connection.RedisConnection.class,
						org.springframework.data.redis.connection.StringRedisConnection.class,
						org.springframework.data.redis.connection.DefaultedRedisConnection.class,
						org.springframework.data.redis.connection.DefaultedRedisClusterConnection.class,
						org.springframework.data.redis.connection.RedisKeyCommands.class,
						org.springframework.data.redis.connection.RedisStringCommands.class,
						org.springframework.data.redis.connection.RedisListCommands.class,
						org.springframework.data.redis.connection.RedisSetCommands.class,
						org.springframework.data.redis.connection.RedisZSetCommands.class,
						org.springframework.data.redis.connection.RedisHashCommands.class,
						org.springframework.data.redis.connection.RedisTxCommands.class,
						org.springframework.data.redis.connection.RedisPubSubCommands.class,
						org.springframework.data.redis.connection.RedisConnectionCommands.class,
						org.springframework.data.redis.connection.RedisServerCommands.class,
						org.springframework.data.redis.connection.RedisStreamCommands.class,
						org.springframework.data.redis.connection.RedisScriptingCommands.class,
						org.springframework.data.redis.connection.RedisGeoCommands.class,
						org.springframework.data.redis.connection.RedisHyperLogLogCommands.class,
						org.springframework.data.redis.connection.RedisClusterCommands.class,
						org.springframework.data.redis.connection.ReactiveRedisConnection.class,

						org.springframework.data.redis.connection.ReactiveKeyCommands.class,
						org.springframework.data.redis.connection.ReactiveStringCommands.class,
						org.springframework.data.redis.connection.ReactiveListCommands.class,
						org.springframework.data.redis.connection.ReactiveSetCommands.class,
						org.springframework.data.redis.connection.ReactiveZSetCommands.class,
						org.springframework.data.redis.connection.ReactiveHashCommands.class,
						org.springframework.data.redis.connection.ReactivePubSubCommands.class,
						org.springframework.data.redis.connection.ReactiveServerCommands.class,
						org.springframework.data.redis.connection.ReactiveStreamCommands.class,
						org.springframework.data.redis.connection.ReactiveScriptingCommands.class,
						org.springframework.data.redis.connection.ReactiveGeoCommands.class,
						org.springframework.data.redis.connection.ReactiveHyperLogLogCommands.class,

						org.springframework.data.redis.connection.ReactiveClusterKeyCommands.class,
						org.springframework.data.redis.connection.ReactiveClusterStringCommands.class,
						org.springframework.data.redis.connection.ReactiveClusterListCommands.class,
						org.springframework.data.redis.connection.ReactiveClusterSetCommands.class,
						org.springframework.data.redis.connection.ReactiveClusterZSetCommands.class,
						org.springframework.data.redis.connection.ReactiveClusterHashCommands.class,
						org.springframework.data.redis.connection.ReactiveClusterServerCommands.class,
						org.springframework.data.redis.connection.ReactiveClusterStreamCommands.class,
						org.springframework.data.redis.connection.ReactiveClusterScriptingCommands.class,
						org.springframework.data.redis.connection.ReactiveClusterGeoCommands.class,
						org.springframework.data.redis.connection.ReactiveClusterHyperLogLogCommands.class,

						org.springframework.data.redis.core.ReactiveRedisOperations.class,
						org.springframework.data.redis.core.ReactiveRedisTemplate.class,
						org.springframework.data.redis.core.RedisOperations.class,
						org.springframework.data.redis.core.RedisTemplate.class,
						org.springframework.data.redis.core.StringRedisTemplate.class,
				})
		},
		proxyInfos = {
				@ProxyInfo(typeNames = {
						"org.springframework.data.redis.connection.DefaultedRedisConnection"
				}),
				@ProxyInfo(typeNames = {
						"org.springframework.data.redis.connection.ReactiveRedisConnection"
				}),
				@ProxyInfo(typeNames = {
						"org.springframework.data.redis.connection.StringRedisConnection", "org.springframework.data.redis.connection.DecoratedRedisConnection"
				})
		}
)

@NativeImageHint(trigger = RedisRepositoriesAutoConfiguration.class, //
		typeInfos = {
				@TypeInfo(types = {

						org.springframework.data.keyvalue.annotation.KeySpace.class,
						org.springframework.data.keyvalue.core.AbstractKeyValueAdapter.class,
						org.springframework.data.keyvalue.core.KeyValueAdapter.class,
						org.springframework.data.keyvalue.core.KeyValueOperations.class,
						org.springframework.data.keyvalue.core.KeyValueTemplate.class,
						org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext.class,
						org.springframework.data.keyvalue.repository.KeyValueRepository.class,
						org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactoryBean.class,
						org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository.class,
						org.springframework.data.keyvalue.repository.config.QueryCreatorType.class,
						org.springframework.data.keyvalue.repository.query.KeyValuePartTreeQuery.class,

						org.springframework.data.redis.core.RedisKeyValueAdapter.class,
						org.springframework.data.redis.core.RedisKeyValueTemplate.class,
						org.springframework.data.redis.core.convert.KeyspaceConfiguration.class,
						org.springframework.data.redis.core.convert.MappingConfiguration.class,
						org.springframework.data.redis.core.convert.MappingRedisConverter.class,
						org.springframework.data.redis.core.convert.RedisConverter.class,
						org.springframework.data.redis.core.convert.RedisCustomConversions.class,
						org.springframework.data.redis.core.convert.ReferenceResolver.class,
						org.springframework.data.redis.core.convert.ReferenceResolverImpl.class,
						org.springframework.data.redis.core.index.IndexConfiguration.class,
						org.springframework.data.redis.core.index.ConfigurableIndexDefinitionProvider.class,
						org.springframework.data.redis.core.mapping.RedisMappingContext.class,
						org.springframework.data.redis.repository.support.RedisRepositoryFactoryBean.class,
						org.springframework.data.redis.repository.query.RedisQueryCreator.class,
				})
		},
		proxyInfos = {
				@ProxyInfo(typeNames = {
						"org.springframework.data.keyvalue.annotation.KeySpace", "org.springframework.core.annotation.SynthesizedAnnotation"
				})
		}
)
public class RedisDataSupportHints implements NativeImageConfiguration {

}

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

package org.springframework.boot.autoconfigure.data.redis;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.repository.support.RedisRepositoryFactoryBean;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;

/**
 * @author Christoph Strobl
 * @author Sebastien Deleuze
 */
@NativeHint(trigger = RedisConnectionFactory.class,
		types = @TypeHint(types = {
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
		,
		jdkProxies = {
				@JdkProxyHint(typeNames = "org.springframework.data.redis.connection.DefaultedRedisConnection"),
				@JdkProxyHint(typeNames = "org.springframework.data.redis.connection.ReactiveRedisConnection"),
				@JdkProxyHint(typeNames = {
						"org.springframework.data.redis.connection.StringRedisConnection",
						"org.springframework.data.redis.connection.DecoratedRedisConnection"
				})
		}
)
@NativeHint(trigger = RedisRepositoryFactoryBean.class,
		types = {
				@TypeHint(types = {
						org.springframework.data.keyvalue.annotation.KeySpace.class,
						org.springframework.data.keyvalue.core.AbstractKeyValueAdapter.class,
						org.springframework.data.keyvalue.core.KeyValueAdapter.class,
						org.springframework.data.keyvalue.core.KeyValueOperations.class,
						org.springframework.data.keyvalue.core.KeyValueTemplate.class,
						org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext.class,
						org.springframework.data.keyvalue.repository.KeyValueRepository.class,
						org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactoryBean.class,
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
				}),
				@TypeHint(types = org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository.class, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS})
		},
		jdkProxies = {
				@JdkProxyHint(typeNames = {
						"org.springframework.data.keyvalue.annotation.KeySpace",
						"org.springframework.core.annotation.SynthesizedAnnotation"
				})
		},
		initialization = @InitializationHint(types = com.rabbitmq.client.SocketChannelConfigurator.class, initTime = InitializationTime.BUILD)
)
public class RedisDataSupportHints implements NativeConfiguration {
}

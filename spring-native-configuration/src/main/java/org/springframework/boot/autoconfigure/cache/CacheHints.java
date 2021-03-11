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

package org.springframework.boot.autoconfigure.cache;

import javax.cache.Caching;

import com.couchbase.client.java.Cluster;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hazelcast.core.HazelcastInstance;
import org.ehcache.impl.serialization.PlainJavaSerializer;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;

import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.CacheConfigurationImportSelector;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = CacheConfigurationImportSelector.class, follow = true,
		types = {
				@TypeHint(types = { GenericCacheConfiguration.class, SimpleCacheConfiguration.class, NoOpCacheConfiguration.class }, access = AccessBits.ALL),
				@TypeHint(types = { EhCacheCacheConfiguration.class, HazelcastCacheConfiguration.class, InfinispanCacheConfiguration.class, JCacheCacheConfiguration.class, RedisCacheConfiguration.class, CaffeineCacheConfiguration.class }, typeNames = "org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration", access = AccessBits.RESOURCE),
				@TypeHint(types = CacheType.class, access = AccessBits.FULL_REFLECTION)
		},
		initialization = @InitializationHint(typeNames = "org.springframework.boot.autoconfigure.cache.CacheConfigurations", initTime = InitializationTime.BUILD))
@NativeHint(trigger = net.sf.ehcache.Cache.class, types = @TypeHint(types = EhCacheCacheConfiguration.class, access = AccessBits.FULL_REFLECTION), resources = @ResourceHint(patterns = "ehcache.xml"))
@NativeHint(trigger = org.ehcache.Cache.class, types = {
		@TypeHint(types = PlainJavaSerializer.class)
}, resources = @ResourceHint(patterns = "ehcache.xml"))
@NativeHint(trigger = HazelcastInstance.class, types = @TypeHint(types = HazelcastCacheConfiguration.class, access = AccessBits.FULL_REFLECTION))
@NativeHint(trigger = SpringEmbeddedCacheManager.class, types = @TypeHint(types = InfinispanCacheConfiguration.class, access = AccessBits.FULL_REFLECTION))
@NativeHint(trigger = Caching.class, follow = true, types = @TypeHint(types = JCacheCacheConfiguration.class, access = AccessBits.FULL_REFLECTION))
@NativeHint(trigger = Cluster.class, types = @TypeHint(typeNames = "org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration", access = AccessBits.FULL_REFLECTION))
@NativeHint(trigger = RedisConnectionFactory.class, types = @TypeHint(types = RedisCacheConfiguration.class, access = AccessBits.FULL_REFLECTION))
@NativeHint(trigger = Caffeine.class, types = @TypeHint(types = CaffeineCacheConfiguration.class, access = AccessBits.FULL_REFLECTION))
public class CacheHints implements NativeConfiguration { }
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

package org.springframework.boot.autoconfigure.data.elasticsearch;

import org.elasticsearch.xcontent.XContentParser;

import org.springframework.boot.autoconfigure.data.SpringDataReactiveHints;
import org.springframework.data.elasticsearch.core.event.AfterConvertCallback;
import org.springframework.data.elasticsearch.core.event.AfterLoadCallback;
import org.springframework.data.elasticsearch.core.event.AfterSaveCallback;
import org.springframework.data.elasticsearch.core.event.BeforeConvertCallback;
import org.springframework.data.elasticsearch.core.event.ReactiveAfterConvertCallback;
import org.springframework.data.elasticsearch.core.event.ReactiveAfterSaveCallback;
import org.springframework.data.elasticsearch.core.event.ReactiveBeforeConvertCallback;
import org.springframework.data.elasticsearch.repository.config.ElasticsearchRepositoryConfigExtension;
import org.springframework.data.elasticsearch.repository.config.ReactiveElasticsearchRepositoryConfigurationExtension;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactoryBean;
import org.springframework.data.elasticsearch.repository.support.ReactiveElasticsearchRepositoryFactoryBean;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.support.SimpleReactiveElasticsearchRepository;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.MethodHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;

/**
 * @author Christoph Strobl
 * @author Sebastien Deleuze
 */
@NativeHint(trigger = ElasticsearchRepositoryFactoryBean.class,
		types = {
				@TypeHint(types = {
						ElasticsearchRepositoryFactoryBean.class,
						ElasticsearchRepositoryConfigExtension.class
				}),
				@TypeHint(types = {
						BeforeConvertCallback.class,
						AfterSaveCallback.class,
						AfterConvertCallback.class,
						AfterLoadCallback.class,
						SimpleElasticsearchRepository.class },
						access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS}),
				@TypeHint(types = {
						org.elasticsearch.Version.class
				}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS})
		},
		resources = @ResourceHint(patterns = "versions.properties"))
@NativeHint(trigger = ReactiveElasticsearchRepositoryFactoryBean.class,
		imports = SpringDataReactiveHints.class,
		types = {
				@TypeHint(
						types = {
								ReactiveElasticsearchRepositoryFactoryBean.class,
								ReactiveElasticsearchRepositoryConfigurationExtension.class,
								XContentParser.class
						},
						typeNames = { // todo check access only on fromXContent method

								"org.elasticsearch.client.core.MainResponse",
								"org.elasticsearch.client.core.AcknowledgedResponse",
								"org.elasticsearch.client.core.CountResponse",
								"org.elasticsearch.client.core.MultiTermVectorsResponse",
								"org.elasticsearch.index.reindex.BulkByScrollResponse",
								"org.elasticsearch.action.admin.indices.refresh.RefreshResponse",

								"org.elasticsearch.action.search.SearchResponse",
								"org.elasticsearch.action.get.GetResponse",
								"org.elasticsearch.action.get.MultiGetResponse",
								"org.elasticsearch.action.get.MultiGetShardResponse",
								"org.elasticsearch.action.bulk.BulkResponse",
								"org.elasticsearch.action.main.MainResponse",
								"org.elasticsearch.action.search.MultiSearchResponse",
								"org.elasticsearch.action.search.ClearScrollResponse",
						}, methods = @MethodHint(name = "fromXContent", parameterTypes = XContentParser.class)),
				@TypeHint(types = {
						org.elasticsearch.Version.class
				}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS}),
				@TypeHint(types = {
						SimpleReactiveElasticsearchRepository.class,
						ReactiveBeforeConvertCallback.class,
						ReactiveAfterSaveCallback.class,
						ReactiveAfterConvertCallback.class
				}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS})
		},
		jdkProxies = {
				@JdkProxyHint(types = XContentParser.class)
		}
)

// org.elasticsearch.client.RestClient - required logging configuration
@NativeHint(trigger = ReactiveElasticsearchRepositoryFactoryBean.class,
		types = {
				@TypeHint(typeNames = {
						// those cause a lot of errors and warnings in logs if not present
						"io.netty.buffer.AbstractByteBufAllocator",
						"io.netty.buffer.PooledByteBufAllocator",
						"io.netty.channel.ChannelDuplexHandler",
						"io.netty.channel.ChannelHandlerAdapter",
						"io.netty.channel.ChannelInboundHandlerAdapter",
						"io.netty.channel.ChannelInitializer",
						"io.netty.channel.ChannelOutboundHandlerAdapter",
						"io.netty.channel.CombinedChannelDuplexHandler",
						"io.netty.channel.DefaultChannelPipeline$HeadContext",
						"io.netty.channel.DefaultChannelPipeline$TailContext",
						"io.netty.channel.embedded.EmbeddedChannel",
						"io.netty.channel.socket.nio.NioSocketChannel",
						"io.netty.handler.codec.ByteToMessageDecoder",
						"io.netty.handler.codec.compression.JdkZlibDecoder",
						"io.netty.handler.codec.dns.DatagramDnsQueryEncoder",
						"io.netty.handler.codec.dns.DnsResponseDecoder",
						"io.netty.handler.codec.dns.TcpDnsResponseDecoder",
						"io.netty.handler.codec.http.HttpClientCodec",
						"io.netty.handler.codec.http.HttpContentDecoder",
						"io.netty.handler.codec.http.HttpContentDecompressor",
						"io.netty.handler.codec.LengthFieldBasedFrameDecoder",
						"io.netty.handler.codec.MessageToByteEncoder",
						"io.netty.handler.codec.MessageToMessageDecoder",
						"io.netty.handler.timeout.IdleStateHandler",
						"io.netty.handler.timeout.ReadTimeoutHandler",
						"io.netty.handler.timeout.WriteTimeoutHandler",
						"io.netty.resolver.dns.DnsNameResolver",
						"io.netty.resolver.dns.DnsNameResolver$1",
						"io.netty.resolver.dns.DnsNameResolver$AddressedEnvelopeAdapter",
						"io.netty.resolver.dns.DnsNameResolver$DnsResponseHandler",
						"io.netty.resolver.InetNameResolver",
						"io.netty.resolver.SimpleNameResolver",
						"io.netty.util.AbstractChannel",
						"io.netty.util.DefaultAttributeMap",
						"io.netty.util.ReferenceCountUtil",
						"reactor.netty.resources.DefaultPooledConnectionProvider",
						"reactor.netty.resources.DefaultPooledConnectionProvider$PooledConnectionAllocator$",
						"reactor.netty.resources.DefaultPooledConnectionProvider$PooledConnectionAllocator$PooledConnectionInitializer",
						"reactor.netty.resources.PooledConnectionProvider",
						"reactor.netty.transport.TransportConfig$TransportChannelInitializer"
				})
		}, initialization = {
			@InitializationHint(typeNames = {
					"org.apache.lucene.util.Constants",
					"org.elasticsearch.common.unit.TimeValue",
					"org.apache.lucene.util.RamUsageEstimator"
			}, initTime = InitializationTime.BUILD)
		}
)
public class ElasticsearchRepositoriesHints implements NativeConfiguration {

}

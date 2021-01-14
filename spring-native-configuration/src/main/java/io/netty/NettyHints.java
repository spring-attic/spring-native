/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.DefaultChannelId;
import io.netty.channel.unix.Errors;
import io.netty.channel.unix.IovArray;
import io.netty.channel.unix.Limits;
import io.netty.channel.unix.Socket;
import io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.resolver.HostsFileEntriesResolver;

import org.springframework.nativex.extension.FieldInfo;
import org.springframework.nativex.extension.InitializationInfo;
import org.springframework.nativex.extension.InitializationTime;
import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

// To be removed when https://github.com/netty/netty/issues/10797 will be fixed
@NativeImageHint(initializationInfos = {
		@InitializationInfo(initTime = InitializationTime.BUILD, packageNames = "io.netty"),
		@InitializationInfo(initTime=InitializationTime.RUN,
		packageNames = "io.netty.channel.epoll",
		types = {
				DefaultChannelId.class,
				Socket.class,Errors.class,Limits.class,IovArray.class,
				Http2ServerUpgradeCodec.class, CleartextHttp2ServerUpgradeHandler.class,
				Http2ConnectionHandler.class, HostsFileEntriesResolver.class
		}, typeNames = {
				"io.netty.handler.codec.http.websocketx.extensions.compression.DeflateDecoder"
		})
})
@NativeImageHint(typeInfos = {
		@TypeInfo(
			typeNames = {"reactor.netty.transport.ServerTransport$AcceptorInitializer",
					"reactor.netty.transport.ServerTransport$Acceptor"
			},
			access=AccessBits.LOAD_AND_CONSTRUCT|AccessBits.PUBLIC_METHODS
		),
		@TypeInfo(types= {ChannelHandlerAdapter.class,
				ChannelHandler.class,ChannelInboundHandler.class},
				typeNames = {
						"io.netty.channel.ChannelInboundInvoker",
						"io.netty.channel.ChannelOutboundInvoker"
				}
		),
		@TypeInfo(
				typeNames = {
						"io.netty.channel.ChannelInitializer",
						"io.netty.channel.DefaultChannelPipeline$HeadContext",
						"io.netty.channel.DefaultChannelPipeline$TailContext",
						"reactor.netty.channel.BootstrapHandlers$BootstrapInitializerHandler",
						"io.netty.channel.ChannelDuplexHandler",
						"io.netty.channel.CombinedChannelDuplexHandler",
						"io.netty.channel.AbstractChannelHandlerContext",
						"io.netty.channel.ChannelHandlerContext",
						"io.netty.handler.codec.http.HttpServerCodec",
						"reactor.netty.channel.ChannelOperationsHandler",
						"reactor.netty.http.server.HttpTrafficHandler",
						"io.netty.channel.ChannelDuplexHandler",
						"io.netty.channel.ChannelFutureListener",
						"io.netty.channel.DefaultChannelPipeline",
						"io.netty.channel.ChannelPipeline",
						"io.netty.util.concurrent.GenericFutureListener",
						"io.netty.bootstrap.ServerBootstrap$1",
						"io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor"},
				access=AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS|AccessBits.PUBLIC_METHODS),
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
				fields = @FieldInfo(name = "producerLimit", allowUnsafeAccess = true)),
		@TypeInfo(
				typeNames = "java.nio.Buffer",
				fields = @FieldInfo(name = "address", allowUnsafeAccess = true)),
		@TypeInfo(
				typeNames = "java.nio.DirectByteBuffer",
				fields = @FieldInfo(name = "cleaner", allowUnsafeAccess = true)
		),
		@TypeInfo(
				typeNames = "io.netty.buffer.AbstractReferenceCountedByteBuf",
				fields = @FieldInfo(name = "refCnt", allowUnsafeAccess = true))
})
// TODO Seems not taken in account for some reason
@NativeImageHint(typeInfos = {
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
public class NettyHints implements NativeImageConfiguration {
}

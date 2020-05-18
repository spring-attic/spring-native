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
package org.springframework.boot.autoconfigure.web.reactive.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

@NativeImageHint(typeInfos = {
		@TypeInfo(types= {ChannelInboundHandlerAdapter.class,ChannelHandlerAdapter.class,
				ChannelHandler.class,ChannelInboundHandler.class},
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
						"io.netty.channel.ChannelInboundInvoker",
						"io.netty.channel.ChannelOutboundInvoker",
						"io.netty.channel.ChannelHandler",
						"io.netty.util.concurrent.GenericFutureListener",
						"io.netty.bootstrap.ServerBootstrap$1",
						"io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor",
						"java.lang.management.ManagementFactory",
						"java.lang.management.RuntimeMXBean"},
				access=AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS|AccessBits.DECLARED_METHODS)
})
public class Hints implements NativeImageConfiguration {
}

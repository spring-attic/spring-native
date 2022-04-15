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

package io.netty;

import io.netty.channel.DefaultChannelId;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.unix.Errors;
import io.netty.channel.unix.IovArray;
import io.netty.channel.unix.Limits;
import io.netty.channel.unix.Socket;
import io.netty.handler.codec.compression.BrotliDecoder;
import io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.ssl.OpenSslAsyncPrivateKeyMethod;
import io.netty.handler.ssl.OpenSslPrivateKeyMethod;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.resolver.HostsFileEntriesResolver;
import io.netty.util.internal.PlatformDependent;

import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = DefaultChannelId.class, initialization = {
		@InitializationHint(initTime=InitializationTime.RUN,
		packageNames = { "io.netty.channel.epoll", "io.netty.internal.tcnative" },
		types = {
				DefaultChannelId.class,
				Socket.class,
				Errors.class,
				Limits.class, IovArray.class,
				Http2ServerUpgradeCodec.class,
				CleartextHttp2ServerUpgradeHandler.class,
				Http2ConnectionHandler.class,
				HostsFileEntriesResolver.class,
				BrotliDecoder.class,
				OpenSslPrivateKeyMethod.class,
				OpenSslAsyncPrivateKeyMethod.class,
				ReferenceCountedOpenSslEngine.class
		}, typeNames = {
				"io.netty.handler.codec.http.websocketx.extensions.compression.DeflateDecoder"
		})
})
@NativeHint(trigger = NioSocketChannel.class, types = {
		@TypeHint(types = NioSocketChannel.class, access = {TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS}),
		@TypeHint(types = NioDatagramChannel.class)
})
// Enable reflective access for PlatformDependent#useDirectBufferNoCleaner - otherwise there's a strange behaviour with
// direct memory buffers
@NativeHint(trigger = PlatformDependent.class, options = "-Dio.netty.tryReflectionSetAccessible=true")
public class NettyHints implements NativeConfiguration {
}

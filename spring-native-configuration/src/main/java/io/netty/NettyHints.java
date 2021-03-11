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
import io.netty.channel.unix.Errors;
import io.netty.channel.unix.IovArray;
import io.netty.channel.unix.Limits;
import io.netty.channel.unix.Socket;
import io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.resolver.HostsFileEntriesResolver;

import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;

@NativeHint(trigger = DefaultChannelId.class, initialization = {
		@InitializationHint(initTime=InitializationTime.RUN,
		packageNames = "io.netty.channel.epoll",
		types = {
				DefaultChannelId.class,
				Socket.class,
				Errors.class,
				Limits.class,IovArray.class,
				Http2ServerUpgradeCodec.class,
				CleartextHttp2ServerUpgradeHandler.class,
				Http2ConnectionHandler.class,
				HostsFileEntriesResolver.class
		}, typeNames = {
				"io.netty.handler.codec.http.websocketx.extensions.compression.DeflateDecoder"
		})
})
public class NettyHints implements NativeConfiguration {
}

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

package org.springframework.web.socket;

import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.broker.AbstractBrokerMessageHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.simp.user.UserDestinationMessageHandler;
import org.springframework.messaging.simp.user.UserDestinationResolver;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.web.socket.config.annotation.DelegatingWebSocketConfiguration;
import org.springframework.web.socket.config.annotation.DelegatingWebSocketMessageBrokerConfiguration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;

@NativeHint(trigger = DelegatingWebSocketMessageBrokerConfiguration.class, types = {
		@TypeHint(types= {
				WebSocketHandlerRegistry.class,
				AbstractSubscribableChannel.class,
				SimpMessagingTemplate.class,
				WebSocketHandler.class,
				WebSocketTransportRegistration.class,
				AbstractBrokerMessageHandler.class,
				TaskExecutor.class,
				ChannelRegistration.class,
				MessageBrokerRegistry.class,
				CompositeMessageConverter.class,
				UserDestinationResolver.class,
				UserDestinationMessageHandler.class,
				MessageHandler.class,
				SimpUserRegistry.class
		}, access = AccessBits.RESOURCE),
		@TypeHint(types = TomcatRequestUpgradeStrategy.class)
})
@NativeHint(trigger = DelegatingWebSocketConfiguration.class, types = {
		@TypeHint(types = TomcatRequestUpgradeStrategy.class)
})
public class WebSocketHints implements NativeConfiguration {
}

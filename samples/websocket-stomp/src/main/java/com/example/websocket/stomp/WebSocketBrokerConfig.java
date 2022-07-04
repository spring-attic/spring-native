package com.example.websocket.stomp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * @author Moritz Halbritter
 */

@Configuration
@EnableWebSocketMessageBroker
class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/stomp");
	}

	@Bean
	public WebSocketStompClient webSocketStompClient(WebSocketClient webSocketClient, TaskScheduler taskScheduler,
			MessageConverter messageConverter) {
		WebSocketStompClient client = new WebSocketStompClient(webSocketClient);
		client.setMessageConverter(messageConverter);
		client.setTaskScheduler(taskScheduler);
		return client;
	}

	@Bean
	public WebSocketClient webSocketClient() {
		return new StandardWebSocketClient();
	}

}

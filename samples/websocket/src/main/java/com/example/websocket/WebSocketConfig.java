package com.example.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final EchoWebSocketHandler echoWebSocketHandler;

	public WebSocketConfig(EchoWebSocketHandler echoWebSocketHandler) {
		this.echoWebSocketHandler = echoWebSocketHandler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(this.echoWebSocketHandler, "/echo");
	}

	@Bean
	public WebSocketClient webSocketClient() {
		return new StandardWebSocketClient();
	}

}

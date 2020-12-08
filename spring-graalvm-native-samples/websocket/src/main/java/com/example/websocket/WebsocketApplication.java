package com.example.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@SpringBootApplication
@EnableWebSocketMessageBroker
public class WebsocketApplication implements WebSocketMessageBrokerConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(WebsocketApplication.class, args);
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/", "/").setAllowedOrigins("*");
	}

}

package com.example.websocket;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * @author Moritz Halbritter
 */
@Component
class CLR implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(CLR.class);

	private final WebSocketClient webSocketClient;

	CLR(WebSocketClient webSocketClient) {
		this.webSocketClient = webSocketClient;
	}

	@Override
	public void run(String... args) throws Exception {
		WebSocketSession session = this.webSocketClient
				.doHandshake(new MyWebsocketHandler(), null, URI.create("ws://localhost:8080/echo"))
				.get(5, TimeUnit.SECONDS);
		TextMessage message = new TextMessage("Hello Websocket");
		session.sendMessage(message);
		LOGGER.info("Client: Sent '{}'", message.getPayload());
	}

	private static class MyWebsocketHandler extends TextWebSocketHandler {

		@Override
		protected void handleTextMessage(WebSocketSession session, TextMessage message) {
			LOGGER.info("Client: Received '{}'", message.getPayload());
		}

	}

}

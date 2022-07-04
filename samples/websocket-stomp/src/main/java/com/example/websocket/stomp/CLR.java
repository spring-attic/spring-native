package com.example.websocket.stomp;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import com.example.websocket.stomp.dto.GreetingMessage;
import com.example.websocket.stomp.dto.HelloMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * @author Moritz Halbritter
 */
@Component
class CLR implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(CLR.class);

	private final WebSocketStompClient webSocketStompClient;

	CLR(WebSocketStompClient webSocketStompClient) {
		this.webSocketStompClient = webSocketStompClient;
	}

	@Override
	public void run(String... args) throws Exception {
		MyStompHandler handler = new MyStompHandler();
		StompSession stompSession = this.webSocketStompClient.connect("ws://localhost:8080/stomp", handler).get(5,
				TimeUnit.SECONDS);

		stompSession.subscribe("/topic/greetings", handler);
		stompSession.send("/app/hello", new HelloMessage("STOMP Client"));
	}

	private static class MyStompHandler extends StompSessionHandlerAdapter {

		@Override
		public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
			LOGGER.info("STOMP Client connected");
		}

		@Override
		public Type getPayloadType(StompHeaders headers) {
			return GreetingMessage.class;
		}

		@Override
		public void handleFrame(StompHeaders headers, Object payload) {
			LOGGER.info("Client: Received '{}'", payload);
		}

	}

}

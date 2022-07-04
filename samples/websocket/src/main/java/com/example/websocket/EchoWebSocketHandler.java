package com.example.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Echo messages by implementing a Spring {@link WebSocketHandler} abstraction.
 */
@Component
public class EchoWebSocketHandler extends TextWebSocketHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(EchoWebSocketHandler.class);

	public EchoWebSocketHandler() {
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		LOGGER.debug("Opened new session in instance {}", this);
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		LOGGER.info("Server: Received '{}' in {}", message.getPayload(), this);
		session.sendMessage(message);
		LOGGER.info("Server: Sent '{}' in {}", message.getPayload(), this);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		LOGGER.error("Closing connection in {}", this, exception);
		session.close(CloseStatus.SERVER_ERROR);
	}

}

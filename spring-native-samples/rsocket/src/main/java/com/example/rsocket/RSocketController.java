package com.example.rsocket;

import reactor.core.publisher.Mono;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class RSocketController {

	@MessageMapping("request-response")
	Message requestResponse(Message request) {
		return new Message("SERVER", "RESPONSE");
	}

	@MessageMapping("mono-request-response")
	Mono<Message> monoRequestResponse(Message request) {
		return Mono.just(new Message("SERVER", "RESPONSE"));
	}
}

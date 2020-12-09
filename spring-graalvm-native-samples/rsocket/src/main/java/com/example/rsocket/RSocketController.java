package com.example.rsocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class RSocketController {

	@MessageMapping("request-response")
	Message requestResponse(Message request) {
		return new Message("SERVER", "RESPONSE");
	}
}

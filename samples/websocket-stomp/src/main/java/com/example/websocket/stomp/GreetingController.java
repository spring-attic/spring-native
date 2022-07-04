package com.example.websocket.stomp;

import com.example.websocket.stomp.dto.GreetingMessage;
import com.example.websocket.stomp.dto.HelloMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class GreetingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(GreetingController.class);

	@MessageMapping("/hello")
	@SendTo("/topic/greetings")
	public GreetingMessage greeting(HelloMessage message) throws Exception {
		LOGGER.info("Server: Received {}", message);
		Thread.sleep(1000); // simulated delay
		return new GreetingMessage("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
	}

}

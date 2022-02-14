package com.example.demo;

import static org.springframework.cloud.function.cloudevent.CloudEventMessageUtils.ID;
import static org.springframework.cloud.function.cloudevent.CloudEventMessageUtils.SOURCE;
import static org.springframework.cloud.function.cloudevent.CloudEventMessageUtils.SPECVERSION;
import static org.springframework.cloud.function.cloudevent.CloudEventMessageUtils.SUBJECT;

import java.net.URI;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.cloudevent.CloudEventMessageBuilder;
import org.springframework.cloud.function.web.util.HeaderUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;

@SpringBootApplication
public class DemoApplication {

	private static final Logger LOGGER = Logger.getLogger(DemoApplication.class.getName());

	public static void main(String[] args) throws Exception {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public Function<Message<KeyPressed>, Message<KeyPressed>> echoCloudEvent() {
		return m -> {
			HttpHeaders httpHeaders = HeaderUtils.fromMessage(m.getHeaders());

			LOGGER.log(Level.INFO, "Input CE Id:{0}", httpHeaders.getFirst(ID));
			LOGGER.log(Level.INFO, "Input CE Spec Version:{0}", httpHeaders.getFirst(SPECVERSION));
			LOGGER.log(Level.INFO, "Input CE Source:{0}", httpHeaders.getFirst(SOURCE));
			LOGGER.log(Level.INFO, "Input CE Subject:{0}", httpHeaders.getFirst(SUBJECT));
			
			KeyPressed incoming = m.getPayload();
			
			KeyPressed kp = new KeyPressed(incoming.getKey(), incoming.getPosition(), incoming.getTimestamp());

			return CloudEventMessageBuilder.withData(kp)
					.setType("KeyPressed-Echo").setId(UUID.randomUUID().toString())
					.setSource(URI.create("https://key.pressed")).build();

		};
	}
}

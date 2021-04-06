package com.example.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.SendTo;

@SpringBootApplication
public class RabbitApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(RabbitApplication.class, args).close();
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

	@RabbitListener(id = "graal", queues = "graal")
	@SendTo
	public String upperCaseIt(String in) {
		return in.toUpperCase();
	}

	@Bean
	public Queue queue() {
		return new Queue("graal");
	}

	@Bean
	public ApplicationRunner runner(RabbitTemplate template) {
		return args -> {
			System.out.println("++++++Received:"
					+ template.convertSendAndReceive("", "graal", "foo"));
		};
	}

}

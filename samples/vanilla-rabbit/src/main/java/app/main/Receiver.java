package app.main;

import app.main.model.Foo;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

	private RabbitTemplate template;

	public Receiver(RabbitTemplate template) {
		this.template = template;
	}

	/**
	 * Send a message with empty routing key, JSON content and
	 * <code>content_type=application/json</code> in the properties.
	 */
	@RabbitListener(queues = "#{queue.name}")
	public void receive(Foo message) {
		System.out.println("Received <" + message + ">");
		template.convertAndSend(new Foo(message.getValue().toUpperCase()));
	}

}
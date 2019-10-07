package app.main;

import app.main.model.Foo;

import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(proxyBeanMethods = false)
public class SampleApplication {

	@Bean
	Queue queue() {
		return new AnonymousQueue();
	}

	@Bean
	TopicExchange input() {
		return new TopicExchange("input");
	}

	@Bean
	TopicExchange output() {
		return new TopicExchange("output");
	}

	@Bean
	Binding binding(Queue queue, @Qualifier("input") TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with("#");
	}


	@Bean
	Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
		Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
		converter.setClassMapper(new ClassMapper() {
			
			@Override
			public Class<?> toClass(MessageProperties properties) {
				return Foo.class;
			}
			
			@Override
			public void fromClass(Class<?> clazz, MessageProperties properties) {
			}
		});
		return converter;
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

}

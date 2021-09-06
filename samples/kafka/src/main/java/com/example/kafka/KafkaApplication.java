package com.example.kafka;

import org.apache.kafka.clients.admin.NewTopic;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
public class KafkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaApplication.class, args).close();
	}

	@KafkaListener(id = "graal", topics = "graal")
	public void listen(String in) {
		System.out.println("++++++Received:" + in);
	}

	@Bean
	public NewTopic topic() {
		return TopicBuilder.name("graal").partitions(1).replicas(1).build();
	}

	@Bean
	public ApplicationRunner runner(KafkaTemplate<String, String> template,
				ConsumerFactory<String, String> cf) {

		cf.addListener(new ConsumerFactory.Listener() { });
		return args -> {
			template.send("graal", "foo");
			System.out.println("++++++Sent:foo");
			Thread.sleep(5000);
		};
	}

}

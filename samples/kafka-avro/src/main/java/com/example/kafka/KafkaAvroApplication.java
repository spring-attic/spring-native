package com.example.kafka;

import org.apache.kafka.clients.admin.NewTopic;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.nativex.hint.TypeHint;

@SpringBootApplication
@TypeHint(types = Thing.class)
public class KafkaAvroApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaAvroApplication.class, args).close();
	}

	@KafkaListener(id = "graal", topics = "graal")
	public void listen(Thing in) {
		System.out.println("++++++Received:" + in);
	}

	@Bean
	public NewTopic topic() {
		return TopicBuilder.name("graal").partitions(1).replicas(1).build();
	}

	@Bean
	public ApplicationRunner runner(KafkaTemplate<String, Thing> template) {
		return args -> {
			Thing thing = Thing.newBuilder().setStringField("someValue").setIntField(42).build();
			template.send("graal", thing);
			System.out.println("++++++Sent:" + thing);
			Thread.sleep(5000);
		};
	}

}

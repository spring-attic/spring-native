package com.example.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.message.CreateTopicsRequestData;
import org.apache.kafka.common.message.CreateTopicsRequestData.CreatableTopic;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
public class GraalKafka1Application {

	public static void main(String[] args) {
		// CreatableTopic creatableTopic = new CreateTopicsRequestData.CreatableTopic();
		// System.out.println(creatableTopic);
		SpringApplication.run(GraalKafka1Application.class, args).close();
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
	public ApplicationRunner runner(KafkaTemplate<String, String> template) {
		return args -> {
			template.send("graal", "foo");
			System.out.println("++++++Sent:foo");
			Thread.sleep(5000);
		};
	}

}

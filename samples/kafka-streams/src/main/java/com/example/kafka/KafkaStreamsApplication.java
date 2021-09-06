package com.example.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
@EnableKafkaStreams
public class KafkaStreamsApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsApplication.class, args).close();
	}

	@Bean
	KStream<String, String> stream(StreamsBuilder builder) {
		KStream<String, String> stream = builder.stream("graalStreamsIn");
		stream.map((k, v) -> new KeyValue<>(k, v.toUpperCase()))
				.to("graalStreamsOut");
		return stream;
	}

	@KafkaListener(id = "graal", topics = "graalStreamsOut")
	public void listen(String in) {
		System.out.println("++++++Received:" + in);
	}

	@Bean
	public NewTopic topic1() {
		return TopicBuilder.name("graalStreamsIn").partitions(1).replicas(1).build();
	}

	@Bean
	public NewTopic topic2() {
		return TopicBuilder.name("graalStreamsOut").partitions(1).replicas(1).build();
	}

	@Bean
	public ApplicationRunner runner(KafkaTemplate<String, String> template) {
		return args -> {
			template.send("graalStreamsIn", "foo");
			System.out.println("++++++Sent:foo");
			Thread.sleep(5000);
		};
	}

}

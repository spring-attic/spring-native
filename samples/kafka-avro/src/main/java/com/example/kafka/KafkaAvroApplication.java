package com.example.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class KafkaAvroApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaAvroApplication.class, args).close();
	}

	@KafkaListener(id = "graal", topics = "graal")
	public void listen(Thing in) {
		System.out.println("++++++Received Thing:" + in);
	}

	@Bean
	public NewTopic topic() {
		return TopicBuilder.name("graal").partitions(1).replicas(1).build();
	}

	@Bean
	public ConcurrentMessageListenerContainer<String, Thing3> manualListenerContainer(MyMessageListener listener,
			ConcurrentKafkaListenerContainerFactory<String, Thing3> factory) {

		ConcurrentMessageListenerContainer<String, Thing3> container = factory.createContainer("graal");
		container.getContainerProperties().setGroupId("graal3");
		container.getContainerProperties().setMessageListener(listener);
		return container;
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

@Component
class RecordListener {

	@KafkaListener(id = "graal2", topics = "graal")
	void listen(ConsumerRecord<String, Thing2> record) {
		System.out.println("++++++Received Thing2:" + record);
	}

}

@Component
class MyMessageListener implements MessageListener<String, Thing3> {

	@Override
	public void onMessage(ConsumerRecord<String, Thing3> data) {
		System.out.println("++++++Received Thing3:" + data);
	}


}

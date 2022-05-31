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
import org.springframework.kafka.listener.CommonContainerStoppingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

@SpringBootApplication
// TODO no support for custom options @NativeHint(options = "--enable-url-protocols=http")
public class KafkaAvroApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaAvroApplication.class, args);
	}

	@Bean
	public NewTopic topic1() {
		return TopicBuilder.name("graal1").partitions(1).replicas(1).build();
	}

	@Bean
	public NewTopic topic2() {
		return TopicBuilder.name("graal2").partitions(1).replicas(1).build();
	}

	@Bean
	public NewTopic topic3() {
		return TopicBuilder.name("graal3").partitions(1).replicas(1).build();
	}

	@Bean
	public ConcurrentMessageListenerContainer<Object, Object> manualListenerContainer(
			NotAComponentMessageListener listener,
			ConcurrentKafkaListenerContainerFactory<Object, Object> factory) {

		factory.setCommonErrorHandler(new CommonContainerStoppingErrorHandler());
		ConcurrentMessageListenerContainer<Object, Object> container = factory.createContainer("graal3");
		container.getContainerProperties().setGroupId("graal3");
		container.getContainerProperties().setMessageListener(listener);
		return container;
	}

	@Bean
	NotAComponentMessageListener otherListner() {
		return new NotAComponentMessageListener();
	}

	@Bean
	public ApplicationRunner runner(KafkaTemplate<Object, Object> template) {
		return args -> {
			Thing thing = Thing.newBuilder().setStringField("thing1Value").setIntField(42).build();
			template.send("graal1", thing);
			Thing2 thing2 = Thing2.newBuilder().setStringField("thing2Value").setIntField(42).build();
			template.send("graal2", thing2);
			Thing3 thing3 = Thing3.newBuilder().setStringField("thing3Value").setIntField(42).build();
			template.send("graal3", thing3);
			System.out.println("++++++Sent:" + thing + thing2 + thing3);
		};
	}

}

@Component
class RecordListener {

	@KafkaListener(id = "graal", topics = "graal1")
	void listen(Thing in) {
		System.out.println("++++++Received "
				+ in.getClass().getSimpleName() + ":" + in);
	}

	@KafkaListener(id = "graal2", topics = "graal2")
	void listen(ConsumerRecord<String, Thing2> record) {
		System.out.println("++++++Received " + record.value().getClass().getSimpleName() + ":" + record);
	}

}

class NotAComponentMessageListener implements MessageListener<String, Thing3> {

	@Override
	public void onMessage(ConsumerRecord<String, Thing3> record) {
		System.out.println("++++++Received " + record.value().getClass().getSimpleName() + ":" + record);
	}


}

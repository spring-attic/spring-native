/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.kafka.annotation;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.RangeAssignor;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.message.CreateTopicsRequestData.CreatableTopic;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.AppInfoParser.AppInfo;

import org.springframework.nativex.extension.NativeConfiguration;
import org.springframework.nativex.extension.NativeHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;
import org.springframework.kafka.config.AbstractKafkaListenerContainerFactory;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaResourceFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConsumerProperties;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.LoggingProducerListener;
import org.springframework.kafka.support.ProducerListener;

@NativeHint(trigger=KafkaListenerConfigurationSelector.class,
	typeInfos = {
		@TypeInfo(types= {
			PartitionOffset.class, TopicPartition.class,
			ConsumerProperties.class,ContainerProperties.class,
			ProducerListener.class,KafkaListener.class,
			EnableKafka.class
		},access=AccessBits.CLASS|AccessBits.DECLARED_METHODS),
		@TypeInfo(types= {
			org.apache.kafka.common.protocol.Message.class,
			org.apache.kafka.common.utils.ImplicitLinkedHashCollection.Element.class,
			KafkaListener.class,org.springframework.messaging.handler.annotation.MessageMapping.class,KafkaListeners.class,
		},access=AccessBits.ALL),
		@TypeInfo(types= {
			KafkaListenerAnnotationBeanPostProcessor.class,
		},access=AccessBits.LOAD_AND_CONSTRUCT|AccessBits.DECLARED_METHODS|AccessBits.DECLARED_FIELDS),
		@TypeInfo(types= {
			KafkaBootstrapConfiguration.class,
			CreatableTopic.class,
			KafkaListenerEndpointRegistry.class
		},access=AccessBits.LOAD_AND_CONSTRUCT|AccessBits.RESOURCE),
		@TypeInfo(types = {
				NewTopic.class,
				AbstractKafkaListenerContainerFactory.class,ConcurrentKafkaListenerContainerFactory.class,
				KafkaListenerContainerFactory.class, KafkaListenerEndpointRegistry.class,
				DefaultKafkaConsumerFactory.class,DefaultKafkaProducerFactory.class,
				KafkaAdmin.class,KafkaOperations.class, KafkaResourceFactory.class,
				KafkaTemplate.class,ProducerFactory.class, KafkaOperations.class,ConsumerFactory.class,
				LoggingProducerListener.class
			}, access=AccessBits.LOAD_AND_CONSTRUCT|AccessBits.DECLARED_FIELDS|AccessBits.DECLARED_METHODS),
		@TypeInfo(types = {
				AppInfo.class,
				RangeAssignor.class,DefaultPartitioner.class,StringDeserializer.class,StringSerializer.class
			}, typeNames = "java.util.zip.CRC32C")
	}
)
public class KafkaHints implements NativeConfiguration { }
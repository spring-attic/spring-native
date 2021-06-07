/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.kafka.annotation;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.RangeAssignor;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.message.CreateTopicsRequestData.CreatableTopic;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.AppInfoParser.AppInfo;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.errors.DefaultProductionExceptionHandler;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;
import org.apache.kafka.streams.processor.DefaultPartitionGrouper;
import org.apache.kafka.streams.processor.FailOnInvalidTimestamp;
import org.apache.kafka.streams.processor.internals.StreamsPartitionAssignor;
import org.apache.kafka.streams.processor.internals.assignment.FallbackPriorTaskAssignor;
import org.apache.kafka.streams.processor.internals.assignment.HighAvailabilityTaskAssignor;
import org.apache.kafka.streams.processor.internals.assignment.StickyTaskAssignor;
import org.glassfish.jersey.internal.RuntimeDelegateImpl;
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
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

import io.confluent.kafka.schemaregistry.client.rest.entities.ErrorMessage;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaString;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaTypeConverter;
import io.confluent.kafka.schemaregistry.client.rest.entities.requests.RegisterSchemaRequest;
import io.confluent.kafka.schemaregistry.client.rest.entities.requests.RegisterSchemaResponse;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.subject.TopicNameStrategy;

@NativeHint(trigger=KafkaListenerConfigurationSelector.class,
	types = {
		@TypeHint(types= {
				PartitionOffset.class,
				TopicPartition.class,
				ConsumerProperties.class,
				ContainerProperties.class,
				ProducerListener.class,
				KafkaListener.class,
				EnableKafka.class
		}, access = AccessBits.CLASS | AccessBits.DECLARED_METHODS),
		@TypeHint(types= {
				org.apache.kafka.common.protocol.Message.class,
				org.apache.kafka.common.utils.ImplicitLinkedHashCollection.Element.class,
				KafkaListener.class,
				org.springframework.messaging.handler.annotation.MessageMapping.class,
				KafkaListeners.class,
		}, access = AccessBits.ALL),
		@TypeHint(types= {
			KafkaListenerAnnotationBeanPostProcessor.class,
		},access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_FIELDS),
		@TypeHint(types= {
			KafkaBootstrapConfiguration.class,
			CreatableTopic.class,
			KafkaListenerEndpointRegistry.class
		}, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.RESOURCE),
		@TypeHint(types = {
				NewTopic.class,
				AbstractKafkaListenerContainerFactory.class,
				ConcurrentKafkaListenerContainerFactory.class,
				KafkaListenerContainerFactory.class,
				KafkaListenerEndpointRegistry.class,
				DefaultKafkaConsumerFactory.class,
				DefaultKafkaProducerFactory.class,
				KafkaAdmin.class,
				KafkaOperations.class,
				KafkaResourceFactory.class,
				KafkaTemplate.class,
				ProducerFactory.class,
				KafkaOperations.class,
				ConsumerFactory.class,
				LoggingProducerListener.class
			}, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.DECLARED_FIELDS | AccessBits.DECLARED_METHODS),
		@TypeHint(types = {
				AppInfo.class,
				RangeAssignor.class,
				DefaultPartitioner.class,
				StringDeserializer.class,
				StringSerializer.class,
				JsonSerializer.class,
				ByteArrayDeserializer.class,
				ByteArraySerializer.class
			}, typeNames = "java.util.zip.CRC32C")
	},
	jdkProxies = @JdkProxyHint(types = {
			Consumer.class,
			org.springframework.aop.SpringProxy.class,
			org.springframework.aop.framework.Advised.class,
			org.springframework.core.DecoratingProxy.class
	})
)

@NativeHint(trigger = StreamsBuilder.class,
	types = {
		@TypeHint(types = {
				DefaultPartitionGrouper.class,
				StreamsPartitionAssignor.class,
				DefaultProductionExceptionHandler.class,
				FailOnInvalidTimestamp.class,
				HighAvailabilityTaskAssignor.class,
				StickyTaskAssignor.class,
				FallbackPriorTaskAssignor.class,
				LogAndFailExceptionHandler.class
		}),
		@TypeHint(types = {
				Serdes.class,
				Serdes.ByteArraySerde.class,
				Serdes.BytesSerde.class,
				Serdes.ByteBufferSerde.class,
				Serdes.DoubleSerde.class,
				Serdes.FloatSerde.class,
				Serdes.IntegerSerde.class,
				Serdes.LongSerde.class,
				Serdes.ShortSerde.class,
				Serdes.StringSerde.class,
				Serdes.UUIDSerde.class,
				Serdes.VoidSerde.class
		})
})
@NativeHint(trigger = KafkaAvroSerializer.class,
	types = {
		@TypeHint(types = {
				ErrorMessage.class,
				KafkaAvroSerializer.class,
				KafkaAvroDeserializer.class,
				RegisterSchemaRequest.class,
				RegisterSchemaResponse.class,
				RuntimeDelegateImpl.class,
				SchemaString.class,
				SchemaTypeConverter.class,
				TopicNameStrategy.class
		})
	}
)
public class KafkaHints implements NativeConfiguration {
}

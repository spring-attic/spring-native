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

import io.confluent.kafka.schemaregistry.client.rest.entities.Config;
import io.confluent.kafka.schemaregistry.client.rest.entities.ErrorMessage;
import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaString;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaTypeConverter;
import io.confluent.kafka.schemaregistry.client.rest.entities.ServerClusterId;
import io.confluent.kafka.schemaregistry.client.rest.entities.SubjectVersion;
import io.confluent.kafka.schemaregistry.client.rest.entities.requests.CompatibilityCheckResponse;
import io.confluent.kafka.schemaregistry.client.rest.entities.requests.ConfigUpdateRequest;
import io.confluent.kafka.schemaregistry.client.rest.entities.requests.ModeGetResponse;
import io.confluent.kafka.schemaregistry.client.rest.entities.requests.ModeUpdateRequest;
import io.confluent.kafka.schemaregistry.client.rest.entities.requests.RegisterSchemaRequest;
import io.confluent.kafka.schemaregistry.client.rest.entities.requests.RegisterSchemaResponse;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.clients.consumer.RangeAssignor;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.clients.consumer.StickyAssignor;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.clients.producer.UniformStickyPartitioner;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.message.CreateTopicsRequestData.CreatableTopic;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.ByteBufferDeserializer;
import org.apache.kafka.common.serialization.ByteBufferSerializer;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.DoubleSerializer;
import org.apache.kafka.common.serialization.FloatDeserializer;
import org.apache.kafka.common.serialization.FloatSerializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.ListDeserializer;
import org.apache.kafka.common.serialization.ListSerializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.AppInfoParser.AppInfo;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.errors.DefaultProductionExceptionHandler;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;
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
import org.springframework.kafka.support.serializer.DelegatingByTopicDeserializer;
import org.springframework.kafka.support.serializer.DelegatingDeserializer;
import org.springframework.kafka.support.serializer.DelegatingSerializer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.support.serializer.ParseStringDeserializer;
import org.springframework.kafka.support.serializer.StringOrBytesSerializer;
import org.springframework.kafka.support.serializer.ToStringSerializer;
import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = KafkaListenerAnnotationBeanPostProcessor.class,
	types = {
		@TypeHint(types= {
				PartitionOffset.class,
				TopicPartition.class,
				ConsumerProperties.class,
				ContainerProperties.class,
				ProducerListener.class,
				KafkaListener.class,
				EnableKafka.class
		}, access = Flag.allDeclaredMethods),
		@TypeHint(types= {
				org.apache.kafka.common.protocol.Message.class,
				org.apache.kafka.common.utils.ImplicitLinkedHashCollection.Element.class,
				KafkaListener.class,
				org.springframework.messaging.handler.annotation.MessageMapping.class,
				KafkaListeners.class,
		}, access = { Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allPublicMethods, Flag.resource }),
		@TypeHint(types= {
			KafkaListenerAnnotationBeanPostProcessor.class,
		}, access = { Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allDeclaredFields }),
		@TypeHint(types= {
			KafkaBootstrapConfiguration.class,
			CreatableTopic.class,
			KafkaListenerEndpointRegistry.class
		}, access = { Flag.allDeclaredConstructors, Flag.resource }),
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
			}, access = { Flag.allDeclaredConstructors, Flag.allDeclaredFields, Flag.allDeclaredMethods }),
		@TypeHint(types = {
				AppInfo.class,
				// standard assignors
				CooperativeStickyAssignor.class,
				RangeAssignor.class,
				RoundRobinAssignor.class,
				StickyAssignor.class,
				// standard partitioners
				DefaultPartitioner.class,
				RoundRobinPartitioner.class,
				UniformStickyPartitioner.class,
				// standard serialization
				ByteArrayDeserializer.class,
				ByteArraySerializer.class,
				ByteBufferDeserializer.class,
				ByteBufferSerializer.class,
				BytesDeserializer.class,
				BytesSerializer.class,
				DoubleSerializer.class,
				DoubleDeserializer.class,
				FloatSerializer.class,
				FloatDeserializer.class,
				IntegerSerializer.class,
				IntegerDeserializer.class,
				ListDeserializer.class,
				ListSerializer.class,
				LongSerializer.class,
				LongDeserializer.class,
				StringDeserializer.class,
				StringSerializer.class,
				// Spring serialization
				DelegatingByTopicDeserializer.class,
				DelegatingDeserializer.class,
				ErrorHandlingDeserializer.class,
				DelegatingSerializer.class,
				JsonDeserializer.class,
				JsonSerializer.class,
				ParseStringDeserializer.class,
				StringOrBytesSerializer.class,
				ToStringSerializer.class
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
@NativeHint(trigger = AbstractKafkaListenerContainerFactory.class,
	types = {
		@TypeHint(types = {
				KafkaAvroSerializer.class,
				KafkaAvroDeserializer.class,
				RuntimeDelegateImpl.class,
				TopicNameStrategy.class
		}),
			@TypeHint(types = {
					ErrorMessage.class,
					SchemaTypeConverter.class,
					RegisterSchemaRequest.class,
					RegisterSchemaResponse.class,
					Config.class,
					ModeGetResponse.class,
					Schema.class,
					SchemaString.class,
					SubjectVersion.class,
					CompatibilityCheckResponse.class,
					ConfigUpdateRequest.class,
					ModeUpdateRequest.class,
					ServerClusterId.class
			}, access = { Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allPublicMethods })
	}
)
@NativeHint(trigger = org.springframework.kafka.support.KafkaUtils.class,
		initialization =
		@InitializationHint(initTime = InitializationTime.BUILD,
				types = {
				org.springframework.kafka.support.JacksonUtils.class,
						org.springframework.kafka.support.KafkaUtils.class,
						org.springframework.kafka.support.JacksonPresent.class
		}))
public class KafkaHints implements NativeConfiguration {
}

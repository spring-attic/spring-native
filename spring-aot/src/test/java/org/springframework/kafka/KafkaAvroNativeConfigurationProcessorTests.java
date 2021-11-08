/*
 * Copyright 2021 the original author or authors.
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

package org.springframework.kafka;

import java.util.List;

import org.apache.avro.specific.AvroGenerated;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.nativex.hint.Flag;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gary Russell
 */
public class KafkaAvroNativeConfigurationProcessorTests {

    @Test
    void genericMessageListener() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class)
                .getBeanDefinition());
        beanFactory.registerBeanDefinition("myListener", BeanDefinitionBuilder
                .rootBeanDefinition(MyListener.class)
                .getBeanDefinition());
        NativeConfigurationRegistry registry = process(beanFactory);
        List<NativeReflectionEntry> entries = registry.reflection().getEntries();
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getType()).isSameAs(AvroType1.class);
    }

    @Test
    void kafkaListenerSimple() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class)
                .getBeanDefinition());
        beanFactory.registerBeanDefinition("myListener", BeanDefinitionBuilder
                .rootBeanDefinition(Listener1.class)
                .getBeanDefinition());
        NativeConfigurationRegistry registry = process(beanFactory);
        List<NativeReflectionEntry> entries = registry.reflection().getEntries();
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getType()).isSameAs(AvroType1.class);
    }

    @Test
    void kafkaListenerListOfAvros() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class)
                .getBeanDefinition());
        beanFactory.registerBeanDefinition("myListener", BeanDefinitionBuilder
                .rootBeanDefinition(Listener2.class)
                .getBeanDefinition());
        NativeConfigurationRegistry registry = process(beanFactory);
        List<NativeReflectionEntry> entries = registry.reflection().getEntries();
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getType()).isSameAs(AvroType1.class);
    }

    @Test
    void kafkaListenerConsumerRecordTwoAvros() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class)
                .getBeanDefinition());
        beanFactory.registerBeanDefinition("myListener", BeanDefinitionBuilder
                .rootBeanDefinition(Listener3.class)
                .getBeanDefinition());
        NativeConfigurationRegistry registry = process(beanFactory);
        List<NativeReflectionEntry> entries = registry.reflection().getEntries();
        assertThat(entries).hasSize(2);
        if (entries.get(0).getType().equals(AvroType1.class)) {
            assertThat(entries.get(1).getType()).isSameAs(AvroType2.class);
        } else if (entries.get(0).getType().equals(AvroType2.class)) {
            assertThat(entries.get(1).getType()).isSameAs(AvroType1.class);
        }
        assertThat(entries.get(0).getFlags()).contains(Flag.allDeclaredConstructors);
        assertThat(entries.get(1).getFlags()).contains(Flag.allDeclaredConstructors);
    }

    @Test
    void kafkaListenerConsumerRecordsTwoAvros() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class)
                .getBeanDefinition());
        beanFactory.registerBeanDefinition("myListener", BeanDefinitionBuilder
                .rootBeanDefinition(Listener4.class)
                .getBeanDefinition());
        NativeConfigurationRegistry registry = process(beanFactory);
        List<NativeReflectionEntry> entries = registry.reflection().getEntries();
        assertThat(entries).hasSize(2);
        if (entries.get(0).getType().equals(AvroType1.class)) {
            assertThat(entries.get(1).getType()).isSameAs(AvroType2.class);
        } else if (entries.get(0).getType().equals(AvroType2.class)) {
            assertThat(entries.get(1).getType()).isSameAs(AvroType1.class);
        }
    }

    private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
        NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
        new KafkaAvroNativeConfigurationProcessor().process(beanFactory, registry);
        return registry;
    }

    @Component
    static class MyListener implements MessageListener<String, AvroType1> {

        @Override
        public void onMessage(ConsumerRecord<String, AvroType1> record) {
        }

    }

    @Component
    static class Listener1 {

        @KafkaListener
        void listen(AvroType1 avro) {
        }

    }

    @Component
    static class Listener2 {

        @KafkaListener
        void listen(List<AvroType1> avro) {
        }

    }

    @Component
    static class Listener3 {

        @KafkaListener
        void listen(ConsumerRecord<AvroType1, AvroType2> record) {
        }

    }

    @Component
    static class Listener4 {

        @KafkaListener
        void listen(ConsumerRecords<AvroType1, AvroType2> record) {
        }

    }

    @AvroGenerated
    private static final class AvroType1 {
    }

    @AvroGenerated
    private static final class AvroType2 {
    }

}

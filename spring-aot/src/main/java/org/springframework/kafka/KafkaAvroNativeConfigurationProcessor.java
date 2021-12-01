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

package org.springframework.kafka;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.support.BeanFactoryProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.kafka.listener.GenericMessageListener;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Detect and register Avro types for Apache Kafka listeners.
 *
 * @author Gary Russell
 */
public class KafkaAvroNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

    private static final String KAFKA_LISTENER_CLASS_NAME = "org.springframework.kafka.annotation.KafkaListener";

    private static final String KAFKA_HANDLER_CLASS_NAME = "org.springframework.kafka.annotation.KafkaHandler";

    private static final String CONSUMER_RECORD_CLASS_NAME = "org.apache.kafka.clients.consumer.ConsumerRecord";

    private static final String CONSUMER_RECORDS_CLASS_NAME = "org.apache.kafka.clients.consumer.ConsumerRecords";

    private static final String AVRO_GENERATED_CLASS_NAME = "org.apache.avro.specific.AvroGenerated";

    @Override
    public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
        if (ClassUtils.isPresent(KAFKA_LISTENER_CLASS_NAME, beanFactory.getBeanClassLoader())
                && ClassUtils.isPresent(AVRO_GENERATED_CLASS_NAME, beanFactory.getBeanClassLoader())) {
            new Processor().process(beanFactory, registry);
        }
    }

    public static boolean isListener(Class<?> beanType) {
        return GenericMessageListener.class.isAssignableFrom(beanType)
                || MergedAnnotations.from(beanType).get(KAFKA_LISTENER_CLASS_NAME).isPresent();
    }

    public static boolean hasListenerMethods(Class<?> beanType) {
        for (Method method : beanType.getDeclaredMethods()) {
            MergedAnnotations methodAnnotations = MergedAnnotations.from(method);
            if (methodAnnotations.get(KAFKA_LISTENER_CLASS_NAME).isPresent()
                || methodAnnotations.get(KAFKA_HANDLER_CLASS_NAME).isPresent()) {
                return true;
            }
        }
        return false;
    }

    public static void processMethods(Class<?> beanType, Set<Class<?>> avroTypes) {
        for (Method method : beanType.getDeclaredMethods()) {
            processMethod(method, avroTypes);
        }
    }

    public static void processMethod(Method method, Set<Class<?>> avroTypes) {
        MergedAnnotations methodAnnotations = MergedAnnotations.from(method);
        if (methodAnnotations.get(KAFKA_LISTENER_CLASS_NAME).isPresent()
                || methodAnnotations.get(KAFKA_HANDLER_CLASS_NAME).isPresent()) {
            Type[] paramTypes = method.getGenericParameterTypes();
            for (Type paramType : paramTypes) {
                checkType(paramType, avroTypes);
            }
        }
    }

    public static void checkType(Type paramType, Set<Class<?>> avroTypes) {
        if (paramType == null) {
            return;
        }
        boolean container = isContainer(paramType);
        if (!container && paramType instanceof Class) {
            MergedAnnotations mergedAnnotations = MergedAnnotations.from((Class<?>) paramType);
            if (mergedAnnotations.get(AVRO_GENERATED_CLASS_NAME).isPresent()) {
                avroTypes.add((Class<?>) paramType);
            }
        }
        else if (container) {
            if (paramType instanceof ParameterizedType) {
                Type[] generics = ((ParameterizedType) paramType).getActualTypeArguments();
                if (generics.length > 0) {
                    checkAvro(generics[0], avroTypes);
                }
                if (generics.length == 2) {
                    checkAvro(generics[1], avroTypes);
                }
            }
        }
    }

    public static void checkAvro(Type generic, Set<Class<?>> avroTypes) {
        if (generic instanceof Class) {
            MergedAnnotations methodAnnotations = MergedAnnotations.from((Class<?>) generic);
            if (methodAnnotations.get(AVRO_GENERATED_CLASS_NAME).isPresent()) {
                avroTypes.add((Class<?>) generic);
            }
        }
    }

    public static boolean isContainer(Type paramType) {
        if (paramType instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) paramType).getRawType();
            return (rawType.equals(List.class))
                    || rawType.getTypeName().equals(CONSUMER_RECORD_CLASS_NAME)
                    || rawType.getTypeName().equals(CONSUMER_RECORDS_CLASS_NAME);
        }
        return false;
    }

    private static final class Processor {

        /*
        Avoid synthesized ctor.
         */
        Processor() {
        }

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			Set<Class<?>> avroTypes = new HashSet<>();
			new BeanFactoryProcessor(beanFactory).processBeans(this::isCandidate, (beanName, beanType) -> {
				if (GenericMessageListener.class.isAssignableFrom(beanType)) {
					ReflectionUtils.doWithMethods(beanType, method -> {
						Type[] types = method.getGenericParameterTypes();
						if (types.length > 0) {
							ResolvableType resolvableType = ResolvableType.forType(types[0]);
							Class<?> keyType = resolvableType.resolveGeneric(0);
							Class<?> valueType = resolvableType.resolveGeneric(1);
							checkType(keyType, avroTypes);
							checkType(valueType, avroTypes);
						}
					}, method -> method.getName().equals("onMessage"));
				}
				else {
					processMethods(beanType, avroTypes);
				}
			});
            avroTypes.forEach(avroType -> registry
                    .reflection()
                    .forType(avroType)
                    .withAccess(TypeAccess.DECLARED_CONSTRUCTORS));
        }

		private boolean isCandidate(Class<?> type) {
			return isListener(type) || hasListenerMethods(type);
		}

	}

}

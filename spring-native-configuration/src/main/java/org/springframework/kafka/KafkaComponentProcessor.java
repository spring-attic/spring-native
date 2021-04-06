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

import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.Method;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Type;

import java.util.*;

/**
 * Detect and register Avro types for Apache Kafka listeners.
 *
 * @author Gary Russell
 */
public class KafkaComponentProcessor implements ComponentProcessor {

    private final Set<String> added = new HashSet<>();

    @Override
    public boolean handle(NativeContext imageContext, String componentType, List<String> classifiers) {
        Type type = imageContext.getTypeSystem().resolveName(componentType);
        if (type == null) {
            return false;
        }
        List<Method> methods =
                type.getMethodsWithAnnotationName("org.springframework.kafka.annotation.KafkaListener", true);
        return methods.size() > 0
                || type.implementsInterface("org/springframework/kafka/listener/MessageListener");
    }

    @Override
    public void process(NativeContext imageContext, String componentType, List<String> classifiers) {
        Type type = imageContext.getTypeSystem().resolveName(componentType);
        Set<Method> listeners = new LinkedHashSet<>();
        listeners.addAll(type.getMethodsWithAnnotationName("org.springframework.kafka.annotation.KafkaListener", true));
        if (type.implementsInterface("org/springframework/kafka/listener/MessageListener")) {
            listeners.addAll(type.getMethod("onMessage"));
        }
        Set<String> types = new HashSet<>();
        for (Method method : listeners) {
            for (Type param : method.getParameterTypes()) {
                if (param.hasAnnotation("Lorg/apache/avro/specific/AvroGenerated;", false)) {
                    String name = param.getDottedName();
                    if (added.add(name)) {
                        types.add(name);
                    }
                } else if (isContainer(param)) {
                    Set<String> params = method.getTypesInSignature();
                    params.forEach(p -> {
                        Type paramType = imageContext.getTypeSystem().resolveName(p);
                        if (paramType != null && paramType.hasAnnotation("Lorg/apache/avro/specific/AvroGenerated;", false)) {
                            String name = paramType.getDottedName();
                            if (added.add(name)) {
                                types.add(name);
                            }
                        }
                    });
                }
            }
        }
        for (String toRegister : types) {
            imageContext.addReflectiveAccess(toRegister, AccessBits.LOAD_AND_CONSTRUCT);
        }
    }

    private boolean isContainer(Type param) {
        return param.getName().startsWith("org/apache/kafka/clients/consumer/ConsumerRecord") ||
                param.getName().equals("java.util.List");
    }

}

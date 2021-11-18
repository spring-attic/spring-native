///*
// * Copyright 2019-2021 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.springframework.transaction.event;
//
//import java.lang.annotation.Annotation;
//import java.lang.reflect.Method;
//import java.util.LinkedHashSet;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
//import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
//import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
//import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
//import org.springframework.boot.context.AotProxyNativeConfigurationProcessor;
//import org.springframework.nativex.hint.Flag;
//import org.springframework.util.ClassUtils;
//
///**
// * Recognize components that are using TransactionalEventListener annotated methods.
// *
// * @author Andy Clement
// */
//public class TransactionalEventNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {
//
//	private static Log logger = LogFactory.getLog(TransactionalEventNativeConfigurationProcessor.class);
//
//	protected final static String TRANSACTIONAL_EVENT_LISTENER_CLASS_NAME = "org.springframework.transaction.event.TransactionalEventListener";
//	
//	protected final static String EVENT_LISTENER_CLASS_NAME = "org.springframework.context.event.EventListener";
//
//	protected final static String TRANSACTION_PHASE_CLASS_NAME = "org.springframework.transaction.event.TransactionPhase";
//	
//	protected final static String SYNTHESIZED_CLASS_NAME = "org.springframework.core.annotation.SynthesizedAnnotation";
//
//	@Override
//	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
//		if (ClassUtils.isPresent(TRANSACTIONAL_EVENT_LISTENER_CLASS_NAME, beanFactory.getBeanClassLoader())) {
//			new Processor().process(beanFactory, registry);
//		}
//	}
//
//	private static class Processor {
//
////	@Override
////	public void process(NativeContext imageContext, String componentType, List<String> classifiers) {
////		imageContext.addProxy("org.springframework.transaction.event.TransactionalEventListener", "org.springframework.core.annotation.SynthesizedAnnotation");
////		imageContext.addReflectiveAccess("org.springframework.transaction.event.TransactionalEventListener", AccessBits.ANNOTATION);
////		imageContext.addReflectiveAccess("org.springframework.context.event.EventListener", AccessBits.ANNOTATION);
////		imageContext.addReflectiveAccess("org.springframework.transaction.event.TransactionPhase", AccessBits.CLASS | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_FIELDS);
//		
//		@SuppressWarnings("unchecked")
//		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
//			final Class<? extends Annotation> transactionalEventListenerType;
//			try {
//				transactionalEventListenerType = 
//					(Class<? extends Annotation>) ClassUtils.forName(TRANSACTIONAL_EVENT_LISTENER_CLASS_NAME, beanFactory.getBeanClassLoader());
//			} catch (ClassNotFoundException | LinkageError e) {
//				// Assume problems with the annotation class mean it cannot be annotated with it
//				return;
//			}
//			AotProxyNativeConfigurationProcessor.doWithComponents(beanFactory,
//				(beanName, beanType) -> {
//					logger.debug("creating reflective configuration to support @TransactionalEventListener annotation on component type "+beanType.getName());
//					LinkedHashSet<String> interfaces = new LinkedHashSet<>();
//					interfaces.add(TRANSACTIONAL_EVENT_LISTENER_CLASS_NAME);
//					interfaces.add(SYNTHESIZED_CLASS_NAME);
//					registry.proxy().add(NativeProxyEntry.ofInterfaceNames(interfaces.toArray(new String[0])));
//					registry.reflection().forType(transactionalEventListenerType).withFlags(Flag.allPublicMethods);
//					try {
//						Class<?> clazz = ClassUtils.forName(EVENT_LISTENER_CLASS_NAME, beanFactory.getBeanClassLoader());
//						registry.reflection().forType(clazz).withFlags(Flag.allPublicMethods);
//					} catch (ClassNotFoundException | LinkageError e) {
//						logger.error("unexpectedly failed to load class "+EVENT_LISTENER_CLASS_NAME, e);
//					}
//					try {
//						Class<?> clazz = ClassUtils.forName(TRANSACTION_PHASE_CLASS_NAME, beanFactory.getBeanClassLoader());
//						registry.reflection().forType(clazz).withFlags(Flag.allDeclaredMethods, Flag.allDeclaredFields);
//					} catch (ClassNotFoundException | LinkageError e) {
//						logger.error("unexpectedly failed to load class "+TRANSACTION_PHASE_CLASS_NAME, e);
//					}
//				},
//				(beanName, beanType) -> {
//					for (Method method: beanType.getDeclaredMethods()) {
//						if (method.getAnnotation(transactionalEventListenerType) != null) {
//							return true;
//						}
//					}
//					return false;
//				});
//		}
//	}
//	
//}

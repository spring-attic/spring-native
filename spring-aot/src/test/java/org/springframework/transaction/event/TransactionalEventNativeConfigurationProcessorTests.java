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
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.util.List;
//import java.util.Set;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
//import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
//import org.springframework.beans.factory.support.BeanDefinitionBuilder;
//import org.springframework.beans.factory.support.DefaultListableBeanFactory;
//import org.springframework.context.event.EventListener;
//import org.springframework.core.annotation.SynthesizedAnnotation;
//import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
//import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
//import org.springframework.nativex.domain.reflect.ClassDescriptor;
//import org.springframework.nativex.hint.Flag;
//import org.springframework.stereotype.Component;
//
///**
// * Tests for {@link TransactionalEventNativeConfigurationProcessor}.
// *
// * @author Andy Clement
// */
//class TransactionalEventNativeConfigurationProcessorTests {
//
//	@Test
//	void transactionalEventListener() {
//		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
//		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
//		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(HelloEventListener.class).getBeanDefinition());
//		NativeConfigurationRegistry registry = process(beanFactory);
//		Set<NativeProxyEntry> proxyEntries = registry.proxy().getEntries();
//		assertThat(proxyEntries).hasSize(1);
//		ProxiesDescriptor proxiesDescriptor = registry.proxy().toProxiesDescriptor();
//		Set<JdkProxyDescriptor> proxyDescriptors = proxiesDescriptor.getProxyDescriptors();
//		assertThat(proxyDescriptors).hasSize(1);
//		JdkProxyDescriptor proxyDescriptor = proxyDescriptors.iterator().next();
//		assertThat(proxyDescriptor.getTypes()).containsExactly(TransactionalEventListener.class.getName(), SynthesizedAnnotation.class.getName());
//		List<ClassDescriptor> classDescriptors = registry.reflection().toClassDescriptors();
//		ClassDescriptor eventListener = fetchClassDescriptor(classDescriptors, EventListener.class);
//		assertThat(eventListener).isNotNull();
//		assertThat(eventListener.getFlags()).containsOnly(Flag.allPublicMethods);
//		ClassDescriptor transactionalPhase = fetchClassDescriptor(classDescriptors, TransactionPhase.class);
//		assertThat(transactionalPhase).isNotNull();
//		System.out.println(transactionalPhase);
//		assertThat(transactionalPhase.getFlags()).containsOnly(Flag.allDeclaredMethods, Flag.allDeclaredFields);
//	}
//	
//	/** 
//	 * Check nothing is computed when there is no annotation.
//	 */
//	@Test
//	void transactionalEventListenerMissing() {
//		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
//		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
//		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(MissingEventListener.class).getBeanDefinition());
//		NativeConfigurationRegistry registry = process(beanFactory);
//		assertThat(registry.reflection().toClassDescriptors()).isEmpty();
//		assertThat(registry.proxy().toProxiesDescriptor().getProxyDescriptors()).isEmpty();
//	}
//
//	private ClassDescriptor fetchClassDescriptor(List<ClassDescriptor> classDescriptors, Class<?> clazz) {
//		for (ClassDescriptor classDescriptor: classDescriptors) {
//			if (classDescriptor.getName().equals(clazz.getName())) {
//				return classDescriptor;
//			}
//		}
//		return null;
//	}
//
//	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
//		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
//		new TransactionalEventNativeConfigurationProcessor().process(beanFactory, registry);
//		return registry;
//	}
//
//	@Component
//	static class HelloEventListener {
//	
//	    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
//	    public void processHelloEvent(Object event) {
//	    }
//	    
//	}
//
//	@Component
//	static class MissingEventListener {
//	
////	    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
//	    public void processHelloEvent(Object event) {
//	    }
//	    
//	}
//
//}

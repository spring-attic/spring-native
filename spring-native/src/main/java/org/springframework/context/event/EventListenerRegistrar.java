/*
 * Copyright 2012-2021 the original author or authors.
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

package org.springframework.context.event;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.GenericApplicationContext;

/**
 * A {@link SmartInitializingSingleton} that registers event listeners detected at build
 * time.
 *
 * @author Stephane Nicoll
 */
public class EventListenerRegistrar implements SmartInitializingSingleton {

	private static final EventListenerFactory DEFAULT_EVENT_LISTENER_FACTORY = new DefaultEventListenerFactory();

	private final EventExpressionEvaluator evaluator = new EventExpressionEvaluator();

	private final GenericApplicationContext context;

	private final Collection<EventListenerMetadata> eventListenersMetadata;

	public EventListenerRegistrar(GenericApplicationContext context,
			EventListenerMetadata... eventListenersMetadata) {
		this.context = context;
		this.eventListenersMetadata = Arrays.asList(eventListenersMetadata);
	}

	@Override
	public void afterSingletonsInstantiated() {
		for (EventListenerMetadata eventListenerMetadata : this.eventListenersMetadata) {
			register(eventListenerMetadata);
		}
	}

	void register(EventListenerMetadata metadata) {
		EventListenerFactory factory = determineEventListenerFactory(this.context,
				metadata.getEventListenerFactoryBeanName());
		ApplicationListener<?> applicationListener = factory.createApplicationListener(metadata.getBeanName(),
				metadata.getBeanType(), metadata.getMethod());
		if (applicationListener instanceof ApplicationListenerMethodAdapter) {
			((ApplicationListenerMethodAdapter) applicationListener).init(this.context, this.evaluator);
		}
		this.context.addApplicationListener(applicationListener);
	}

	private EventListenerFactory determineEventListenerFactory(GenericApplicationContext context,
			String factoryBeanName) {
		return (factoryBeanName != null) ? context.getBean(factoryBeanName, EventListenerFactory.class)
				: DEFAULT_EVENT_LISTENER_FACTORY;

	}
}

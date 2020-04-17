/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.samples.petclinic.system;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Dave Syer
 *
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ShutdownApplicationListener
		implements ApplicationListener<ApplicationReadyEvent>, DisposableBean,
		ApplicationContextAware {

	private static final String SHUTDOWN_LISTENER = "SHUTDOWN_LISTENER";
	public static final String MARKER = "Benchmark app stopped";

	private ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		if (!event.getApplicationContext().equals(this.context)) {
			return;
		}
		if (isSpringBootApplication(sources(event))) {
			((DefaultListableBeanFactory) event.getApplicationContext().getBeanFactory())
					.registerDisposableBean(SHUTDOWN_LISTENER, this);
		}
	}

	@Override
	public void destroy() throws Exception {
		try {
			System.out.println(MARKER);
		}
		catch (Exception e) {
		}
	}

	private boolean isSpringBootApplication(Set<Class<?>> sources) {
		for (Class<?> source : sources) {
			if (AnnotatedElementUtils.hasAnnotation(source,
					SpringBootConfiguration.class)) {
				return true;
			}
		}
		return false;
	}

	private Set<Class<?>> sources(ApplicationReadyEvent event) {
		Method method = ReflectionUtils.findMethod(SpringApplication.class,
				"getAllSources");
		if (method == null) {
			method = ReflectionUtils.findMethod(SpringApplication.class, "getSources");
		}
		ReflectionUtils.makeAccessible(method);
		@SuppressWarnings("unchecked")
		Set<Object> objects = (Set<Object>) ReflectionUtils.invokeMethod(method,
				event.getSpringApplication());
		Set<Class<?>> result = new LinkedHashSet<>();
		for (Object object : objects) {
			if (object instanceof String) {
				object = ClassUtils.resolveClassName((String) object, null);
			}
			result.add((Class<?>) object);
		}
		return result;
	}
}

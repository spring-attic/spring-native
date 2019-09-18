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

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Dave Syer
 *
 */
public class BeanCountingApplicationListener
		implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {

	private static Log logger = LogFactory.getLog(BeanCountingApplicationListener.class);
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
		ConfigurableApplicationContext context = event.getApplicationContext();
		log(context);
	}

	public void log(ConfigurableApplicationContext context) {
		int count = 0;
		String id = context.getId();
		List<String> names = new ArrayList<>();
		while (context != null) {
			count += context.getBeanDefinitionCount();
			names.addAll(Arrays.asList(context.getBeanDefinitionNames()));
			context = (ConfigurableApplicationContext) context.getParent();
		}
		logger.info("Bean count: " + id + "=" + count);
		logger.debug("Bean names: " + id + "=" + names);
		try {
			logger.info("Class count: " + id + "=" + ManagementFactory
					.getClassLoadingMXBean().getTotalLoadedClassCount());
		}
		catch (Exception e) {
		}
	}

}

package com.example.commandlinerunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class InitComponent implements ApplicationContextAware {

	private static final Log logger = LogFactory.getLog(InitComponent.class);

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		Assert.notNull(applicationContext, "Application context should not be null in ApplicationContextAware callback");
		logger.info("ApplicationContextAware callback invoked");
	}
}

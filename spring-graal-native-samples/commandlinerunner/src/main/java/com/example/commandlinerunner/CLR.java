package com.example.commandlinerunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

@Component
public class CLR implements CommandLineRunner {

	private static final Log logger = LogFactory.getLog(SpringApplication.class);

	@Override
	public void run(String... args) throws Exception {
		System.out.println("commandlinerunner running!");
		logger.trace("WARNING log message");
		logger.debug("DEBUG log message");
		logger.info("INFO log message");
		logger.warn("WARNING log message");
		logger.error("ERROR log message");
	}
	
}

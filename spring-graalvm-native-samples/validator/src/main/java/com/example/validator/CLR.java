package com.example.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

@Component
public class CLR implements CommandLineRunner {

	private static final Log logger = LogFactory.getLog(SpringApplication.class);
	private Foo foo;

	public CLR(Foo foo) {
		this.foo = foo;
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("commandlinerunner running! Foo: " + foo);
		logger.trace("WARNING log message");
		logger.debug("DEBUG log message");
		logger.info("INFO log message");
		logger.warn("WARNING log message");
		logger.error("ERROR log message");
	}
	
}

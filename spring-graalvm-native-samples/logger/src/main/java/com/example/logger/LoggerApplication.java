package com.example.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggerApplication {

	public static void main(String[] args) throws InterruptedException {
		Log LOGGER = LogFactory.getLog(LoggerApplication.class);
		LOGGER.info("info");
		LOGGER.error("ouch", new RuntimeException());
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

}

package com.example.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggerApplication {

	public static void main(String[] args) {
		Log LOGGER = LogFactory.getLog(LoggerApplication.class);
		LOGGER.info("info");
		LOGGER.error("ouch", new RuntimeException());
	}

}

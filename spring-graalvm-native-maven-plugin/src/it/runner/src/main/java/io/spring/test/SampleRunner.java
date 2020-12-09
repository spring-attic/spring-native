package io.spring.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SampleRunner implements ApplicationRunner {

	private static Log logger = LogFactory.getLog(SampleRunner.class);

	@Override
	public void run(ApplicationArguments args) throws Exception {
		this.logger.info("ApplicationRunner is executed.");
	}
	
}

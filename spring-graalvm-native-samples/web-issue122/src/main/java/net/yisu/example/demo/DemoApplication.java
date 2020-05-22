package net.yisu.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(proxyBeanMethods = false)
public class DemoApplication {

	private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		
		logger.info("-- Application Started (Spring Boot v2) --");

		String javaVersion = System.getProperty("java.version");
		logger.info("-- Java Version: " + javaVersion + " --");
	}

}

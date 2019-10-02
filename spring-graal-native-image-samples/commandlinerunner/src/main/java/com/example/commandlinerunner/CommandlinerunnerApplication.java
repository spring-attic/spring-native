package com.example.commandlinerunner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(proxyBeanMethods=false)
public class CommandlinerunnerApplication {

//	  private static final Logger LOGGER;
//	  static {
//	    try {
//			LogManager.getLogManager().readConfiguration(CommandlinerunnerApplication.class.getResourceAsStream("logging.properties"));
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	    LOGGER = Logger.getLogger(CommandlinerunnerApplication.class.getName());
//	  }
	  
	public static void main(String[] args) {
//		LOGGER.fine("foo");
		SpringApplication.run(CommandlinerunnerApplication.class, args);
	}
	
//	@Bean
//	public CommandLineRunner clr1() {
//		return new CLR();
//	}

}

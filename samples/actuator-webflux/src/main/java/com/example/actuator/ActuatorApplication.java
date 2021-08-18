package com.example.actuator;

import org.springframework.aot.SpringAotApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ActuatorApplication {

	public static void main(String[] args) {
		SpringAotApplication.run(ActuatorApplication.class, args);
	}

}

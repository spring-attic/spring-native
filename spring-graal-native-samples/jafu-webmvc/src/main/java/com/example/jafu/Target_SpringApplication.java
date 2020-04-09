package com.example.jafu;


import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ServletWebServerApplicationContextWithoutSpel;

@TargetClass(className = "org.springframework.boot.SpringApplication")
public final class Target_SpringApplication {

	// Use the application context without SpEL and avoid reflection
	@Substitute
	protected ConfigurableApplicationContext createApplicationContext() {
		return new ServletWebServerApplicationContextWithoutSpel();
	}
}

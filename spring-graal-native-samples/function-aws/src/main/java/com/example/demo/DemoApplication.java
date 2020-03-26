package com.example.demo;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.FunctionRegistration;
import org.springframework.cloud.function.context.FunctionType;
import org.springframework.cloud.function.context.FunctionalSpringApplication;
import org.springframework.cloud.function.web.source.ExporterProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

@SpringBootApplication(proxyBeanMethods = false)
public class DemoApplication implements ApplicationContextInitializer<GenericApplicationContext> {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = FunctionalSpringApplication.run(DemoApplication.class, args);
		System.err.println(
			"Beans: " + Arrays.asList(context.getBeanDefinitionNames())
		);
		System.err.println(
			"URL: " + context.getBean(ExporterProperties.class).getSource().getUrl()
		);
	}

	@Override
	public void initialize(GenericApplicationContext context) {
		context.registerBean("foobar", FunctionRegistration.class,
				() -> new FunctionRegistration<>(new Foobar()).type(
						FunctionType.from(String.class).to(String.class)));
	}

}

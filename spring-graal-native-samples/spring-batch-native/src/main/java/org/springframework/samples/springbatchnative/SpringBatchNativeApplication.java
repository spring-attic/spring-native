package org.springframework.samples.springbatchnative;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(proxyBeanMethods=false)
public class SpringBatchNativeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchNativeApplication.class, new String[] {"inputFile=person.csv"});
	}
}

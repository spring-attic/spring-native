package com.example.graalvmdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.ClassProxyHint;
import org.springframework.nativex.hint.ProxyBits;

@ClassProxyHint(targetClass=com.example.graalvmdemo.rest.PersonController.class,proxyFeatures = ProxyBits.IS_STATIC)
@SpringBootApplication
public class GraalvmDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraalvmDemoApplication.class, args);
	}

}

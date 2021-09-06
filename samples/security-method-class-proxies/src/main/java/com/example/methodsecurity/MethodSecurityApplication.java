package com.example.methodsecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.AotProxyHint;
import org.springframework.nativex.hint.ProxyBits;

@AotProxyHint(targetClass=com.example.methodsecurity.GreetingServiceImpl.class, proxyFeatures = ProxyBits.IS_STATIC)
@SpringBootApplication
public class MethodSecurityApplication {

	public static void main(String[] args) throws Throwable {
		SpringApplication.run(MethodSecurityApplication.class, args);
	}

}

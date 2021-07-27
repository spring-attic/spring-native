package com.example.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.AotProxyHint;
import org.springframework.nativex.hint.ProxyBits;

@SpringBootApplication
@AotProxyHint(targetClass = com.example.webflux.MainController.class, proxyFeatures = ProxyBits.IS_STATIC)
public class SecurityWebfluxApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityWebfluxApplication.class, args);
	}

}

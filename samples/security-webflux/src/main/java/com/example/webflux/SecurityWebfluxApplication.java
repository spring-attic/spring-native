package com.example.webflux;

import org.springframework.aot.thirdpartyhints.NettyRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(NettyRuntimeHints.class)
public class SecurityWebfluxApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityWebfluxApplication.class, args);
	}

}

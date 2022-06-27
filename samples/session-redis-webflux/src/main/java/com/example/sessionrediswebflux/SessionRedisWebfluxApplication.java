package com.example.sessionrediswebflux;

import org.springframework.aot.thirdpartyhints.NettyRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(NettyRuntimeHints.class)
public class SessionRedisWebfluxApplication {

	public static void main(String[] args) throws Throwable {
		SpringApplication.run(SessionRedisWebfluxApplication.class, args);
	}

}

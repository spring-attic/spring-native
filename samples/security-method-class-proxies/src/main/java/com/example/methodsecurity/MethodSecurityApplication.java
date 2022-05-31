package com.example.methodsecurity;

import com.example.methodsecurity.MethodSecurityApplication.Registrar;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(Registrar.class)
public class MethodSecurityApplication {

	public static void main(String[] args) throws Throwable {
		SpringApplication.run(MethodSecurityApplication.class, args);
	}

	static class Registrar implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			hints.proxies().registerClassProxy(GreetingServiceImpl.class, hint -> {});
		}
	}

}

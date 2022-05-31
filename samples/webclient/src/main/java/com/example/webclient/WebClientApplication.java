package com.example.webclient;

import com.example.webclient.WebClientApplication.Registrar;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(Registrar.class)
public class WebClientApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(WebClientApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

	static class Registrar implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			hints.reflection().registerType(Data.class, hint -> hint.withMembers(
					MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS));
		}
	}
	
}

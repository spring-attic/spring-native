package com.example.graalvmdemo;

import com.example.graalvmdemo.GraalvmDemoApplication.Registrar;
import com.example.graalvmdemo.rest.PersonController;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(Registrar.class)
public class GraalvmDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraalvmDemoApplication.class, args);
	}

	static class Registrar implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			hints.proxies().registerClassProxy(PersonController.class, hint -> {});
		}
	}

}

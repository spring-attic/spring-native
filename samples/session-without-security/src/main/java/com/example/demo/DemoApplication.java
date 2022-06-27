package com.example.demo;

import com.example.demo.DemoApplication.Registrar;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.thirdpartyhints.NettyRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints({ Registrar.class, NettyRuntimeHints.class })
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	static class Registrar implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			// For Thymeleaf
			hints.reflection().registerType(TypeReference.of(
					"org.springframework.web.server.session.InMemoryWebSessionStore$InMemoryWebSession"),
					hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
		}
	}

}

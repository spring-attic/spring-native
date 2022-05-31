package com.example.batch;

import com.example.batch.BatchApplication.Registrar;
import com.example.batch.domain.Person;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@EnableBatchProcessing
@SpringBootApplication
@ImportRuntimeHints(Registrar.class)
public class BatchApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(BatchApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

	// TODO Infer those hints
	static class Registrar implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			hints.reflection().registerType(Person.class, hint -> hint.withMembers(
					MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS));
		}
	}

}

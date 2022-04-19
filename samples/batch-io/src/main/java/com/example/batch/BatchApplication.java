package com.example.batch;

import com.example.batch.domain.Person;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;

// TODO Infer those hints
@TypeHint(types = Person.class, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS })
@EnableBatchProcessing
@SpringBootApplication
public class BatchApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(BatchApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

}

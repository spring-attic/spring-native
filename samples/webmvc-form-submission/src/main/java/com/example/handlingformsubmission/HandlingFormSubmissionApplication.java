package com.example.handlingformsubmission;

import org.springframework.aot.thirdpartyhints.ThymeleafRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(ThymeleafRuntimeHints.class)
public class HandlingFormSubmissionApplication {

	public static void main(String[] args) {
		SpringApplication.run(HandlingFormSubmissionApplication.class, args);
	}

}

package com.example.commandlinerunner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CLR implements CommandLineRunner {

	@Override
	public void run(String... args) throws Exception {
		System.out.println("commandlinerunner running!");
	}
	
}

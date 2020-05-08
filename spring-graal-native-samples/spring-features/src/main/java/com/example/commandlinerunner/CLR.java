package com.example.commandlinerunner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CLR implements CommandLineRunner {

	@Autowired
	public FooBean foo;

	@Override
	public void run(String... args) throws Exception {
		if (foo == null) {
			throw new IllegalStateException("foo is not set");
		}
		System.out.println("commandlinerunner running!");
	}

}

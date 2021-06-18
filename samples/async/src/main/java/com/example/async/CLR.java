package com.example.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CLR implements CommandLineRunner {
	
	public static String prefix = "missing:";
	
	@Autowired
	Runner runner;
	
	@Override
	public void run(String... args) throws Exception {
		System.out.println("event application running!");
		runner.run();
		prefix = "set:";
	}
	
}

package com.example.jafu;

import org.springframework.boot.CommandLineRunner;
import org.springframework.fu.jafu.JafuApplication;

import static org.springframework.fu.jafu.Jafu.application;

public class DemoApplication {

	public static JafuApplication app = application(a -> {
		a.beans(b -> b.bean(CommandLineRunner.class, () -> arguments -> System.out.println("jafu running!")));
	});

	public static void main(String[] args) throws InterruptedException {
		app.run(args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}

package com.example.bootfeatures;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CLR implements CommandLineRunner {
	
	@Autowired
	Creds credentials;

	@Override
	public void run(String... args) throws Exception {
		System.out.println("bootfeatures running "+
			credentials.getUsername()+":"+credentials.getPassword());
	}
	
}

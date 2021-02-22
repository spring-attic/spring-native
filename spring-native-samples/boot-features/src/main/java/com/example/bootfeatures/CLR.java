package com.example.bootfeatures;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.bootfeatures.nestedcfg.School;
import com.example.bootfeatures.separatecfg.*;

@Component
public class CLR implements CommandLineRunner {
	
	@Autowired
	Creds credentials;
	
	@Autowired
	AcmeProperties acme;
	
	@Autowired
	University university;
	
	@Autowired
	School school;
	
	@Autowired
	Props2 props2;

	@Override
	public void run(String... args) throws Exception {
		System.out.println("bootfeatures running "+
			credentials.getUsername()+":"+credentials.getPassword());
		System.out.println("school is "+school);
		System.out.println("uni is "+university);
		System.out.println("acme is "+acme);
		System.out.println("props is "+props2);
	}
	
}

package com.example.graalvmdemo.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersonController {

	@GetMapping(value="/with")
	public String demo() {
		return "hello1";
	}
	
}

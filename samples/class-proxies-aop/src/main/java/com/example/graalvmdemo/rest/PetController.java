package com.example.graalvmdemo.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class PetController {

	@GetMapping(value="/without")
	String wemo() {
		return "hello2";
	}
	
	
}

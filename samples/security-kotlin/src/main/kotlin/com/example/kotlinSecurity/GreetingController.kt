package com.example.kotlinSecurity

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class GreetingController {

	@GetMapping("/")
	fun greet(): Bar {
		return Bar("hi!")
	}
}

data class Bar(val message: String)

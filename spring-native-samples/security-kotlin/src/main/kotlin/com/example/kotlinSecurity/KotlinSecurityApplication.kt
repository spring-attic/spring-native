package com.example.kotlinSecurity

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestHeader

@SpringBootApplication
class KotlinSecurityApplication

fun main(args: Array<String>) {
	runApplication<KotlinSecurityApplication>(*args)
}

data class Bar(val message: String)

@RestController
class GreetingController {

	@GetMapping("/")
	fun greet(): Bar {
		return Bar("hi!")
	}
}

package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication(proxyBeanMethods = false)
class KotlinCoroutinesApplication

fun main(args: Array<String>) {
	runApplication<KotlinCoroutinesApplication>(*args)
}

data class Bar(val message: String)

@RestController
class Foo {

	@GetMapping("/")
	fun greet(): Bar {
		return Bar("hi!")
	}
}

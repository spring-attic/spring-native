package com.example.webflux

import kotlinx.coroutines.delay
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class WebfluxApplication

fun main(args: Array<String>) {
	runApplication<WebfluxApplication>(*args)
}

data class Bar(val message: String)

@RestController
class WebfluxController {

	@GetMapping("/")
	suspend fun greet(): Bar {
		delay(10)
		return Bar("hi!")
	}
}

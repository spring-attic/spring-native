package com.example.webmvc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class WebmvcApplication

fun main(args: Array<String>) {
	runApplication<WebmvcApplication>(*args)
}

data class Bar(val message: String)

@RestController
class WebmvcController {

	@GetMapping("/")
	fun greet(): Bar {
		return Bar("hi!")
	}
}

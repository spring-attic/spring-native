package com.example.webflux

import kotlinx.coroutines.delay
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class WebfluxController {

    @GetMapping("/")
    suspend fun greet(): Bar {
        delay(10)
        return Bar("hi!")
    }

    @GetMapping("/header")
    suspend fun header(@RequestHeader("x-header") xHeader: String): Bar {
        delay(10)
        return Bar(xHeader)
    }
}
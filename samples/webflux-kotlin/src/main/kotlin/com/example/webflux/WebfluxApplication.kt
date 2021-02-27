package com.example.webflux

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.nativex.hint.AccessBits
import org.springframework.nativex.hint.NativeHint
import org.springframework.nativex.hint.TypeHint

// Reflection entry required due to how Coroutines generates bytecode with an Object return type, see https://github.com/spring-projects/spring-framework/issues/21546 related issue
@TypeHint(types = [Bar::class], access = AccessBits.FULL_REFLECTION)
@SpringBootApplication
class WebfluxApplication

fun main(args: Array<String>) {
	runApplication<WebfluxApplication>(*args)
}
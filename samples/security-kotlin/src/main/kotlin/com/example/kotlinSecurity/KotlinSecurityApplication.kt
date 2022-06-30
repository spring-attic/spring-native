package com.example.kotlinSecurity

import org.springframework.aot.thirdpartyhints.KotlinRuntimeHints
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ImportRuntimeHints

@SpringBootApplication
@ImportRuntimeHints(KotlinRuntimeHints::class)
class KotlinSecurityApplication

fun main(args: Array<String>) {
	runApplication<KotlinSecurityApplication>(*args)
}

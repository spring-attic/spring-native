/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.webflux

import org.springframework.aot.thirdpartyhints.NettyRuntimeHints
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.data.annotation.Id
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router

data class Reservation(@Id var name: String, var id: Int? = null)
interface ReservationRepository : CoroutineCrudRepository<Reservation, Int>

@SpringBootApplication
@ImportRuntimeHints(NettyRuntimeHints::class)
class WebfluxApplication {

	@Bean
	fun routes(reservationRepository: ReservationRepository) = router {
		GET("/reservations") {
			ServerResponse.ok().body<Reservation>(reservationRepository.findAll())
		}
	}

	@Bean
	fun runner(dbc: DatabaseClient?, reservationRepository: ReservationRepository): ApplicationRunner {
		return ApplicationRunner { println(reservationRepository.findAll().toString()) }
	}
}

fun main(args: Array<String>) {
	runApplication<WebfluxApplication>(*args)
}

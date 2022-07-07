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
package com.example.webflux;

import org.springframework.aot.thirdpartyhints.NettyRuntimeHints;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableR2dbcAuditing(auditorAwareRef = "fixedAuditor")
@ImportRuntimeHints({NettyRuntimeHints.class, RuntimeHints.class }) //, ReactiveAuditingRuntimeHints.class})
public class WebfluxApplication {

	@Bean // TODO: enable once web support is back
	RouterFunction<ServerResponse> routes(ReservationRepository reservationRepository) {
		return route().GET("/reservations", r -> ok().body(reservationRepository.findAll(), Reservation.class)).build();
	}

	@Bean
	ApplicationRunner runner(DatabaseClient dbc, ReservationRepository reservationRepository) {
		return args -> {
			reservationRepository.findAll().doOnNext(System.out::println).then().subscribe();
			System.out.println("And... DONE :)");
		};
	}

	@Bean
	ReactiveAuditorAware<String> fixedAuditor() {
		return () -> Mono.just("Douglas Adams");
	}

	public static void main(String[] args) {
		SpringApplication.run(WebfluxApplication.class, args);
	}
}

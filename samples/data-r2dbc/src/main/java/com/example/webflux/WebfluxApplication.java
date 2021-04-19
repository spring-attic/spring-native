package com.example.webflux;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class WebfluxApplication {

	@Bean
	RouterFunction<ServerResponse> routes(ReservationRepository reservationRepository) {
		return route().GET("/reservations", r -> ok().body(reservationRepository.findAll(), Reservation.class)).build();
	}

	@Bean
	ApplicationRunner runner(DatabaseClient dbc, ReservationRepository reservationRepository) {
		return args -> {
			reservationRepository.findAll().doOnNext(System.out::println).then().subscribe();
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(WebfluxApplication.class, args);
	}
}

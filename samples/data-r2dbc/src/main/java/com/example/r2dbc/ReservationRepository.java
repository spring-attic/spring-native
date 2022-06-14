package com.example.r2dbc;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

interface ReservationRepository extends ReactiveCrudRepository<Reservation, Integer> {
}

package com.example.traditional;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

interface ReservationRepository extends ReactiveCrudRepository<Reservation, Integer> {
}
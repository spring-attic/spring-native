/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.data.cassandra;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * A repository interface assembling CRUD functionality as well as the API to invoke the methods implemented manually.
 *
 * @author Mark Paluch
 */
public interface OrderRepository extends ReactiveCrudRepository<Order, String>, OrderRepositoryCustom {

	Flux<Order> findByCustomerId(String customerId);

	Flux<OrderProjection> findProjectionByCustomerId(String customerId);

	Mono<Slice<Order>> findSliceByCustomerId(String customerId, Pageable pageable);

	Flux<Order> findById(String id, Sort sort);
}

/*
 * Copyright 2020 the original author or authors.
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
package com.example.data.mongo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A repository interface assembling CRUD functionality as well as the API to invoke the methods implemented manually.
 */
public interface OrderRepository extends ReactiveCrudRepository<Order, String>, OrderRepositoryCustom, ReactiveQueryByExampleExecutor<Order> {

	Flux<Order> findByCustomerId(String customerId);

	Flux<Order> findBy(Sort sort);

	Flux<Order> findByCustomerId(String customerId, Pageable pageable);

	Flux<Order> findSliceByCustomerId(String customerId, Pageable pageable);

	@Query("{ 'customerId' : '?0'}")
	Flux<Order> findByCustomerViaAnnotation(String customer);

	Flux<OrderProjection> findOrderProjectionByCustomerId(String customerId);

	@Aggregation("{ $group : { _id : $customerId, total : { $sum : 1 } } }")
	Flux<OrdersPerCustomer> totalOrdersPerCustomer(Sort sort);

	@Aggregation(pipeline = {"{ $match : { customerId : ?0 } }", "{ $count : total }"})
	Mono<Long> totalOrdersForCustomer(String customerId);
}

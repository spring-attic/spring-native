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

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

/**
 * A repository interface assembling CRUD functionality as well as the API to invoke the methods implemented manually.
 *
 * @author Mark Paluch
 */
public interface OrderRepository extends CrudRepository<Order, String>, OrderRepositoryCustom {

	List<Order> findByCustomerId(String customerId);

	List<OrderProjection> findProjectionByCustomerId(String customerId);

	Slice<Order> findSliceByCustomerId(String customerId, Pageable pageable);

	List<Order> findById(String id, Sort sort);
}

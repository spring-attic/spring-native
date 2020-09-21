/*
 * Copyright 2013-2019 the original author or authors.
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
package com.example.data.neo4j;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

/**
 * A repository interface assembling CRUD functionality as well as the API to invoke the methods implemented manually.
 *
 * @author Thomas Darimont
 * @author Oliver Gierke
 * @author Christoph Strobl
 * @author Michael J. Simons
 */
public interface OrderRepository
	extends CrudRepository<Order, String>, OrderRepositoryCustom, QueryByExampleExecutor<Order> {

	List<Order> findByCustomerId(String customerId);

	List<Order> findBy(Sort sort);

	Page<Order> findByCustomerId(String customerId, Pageable pageable);

	Slice<Order> findSliceByCustomerId(String customerId, Pageable pageable);

	@Query("MATCH (n:Order) - [:HAS] -> (l:LineItem) WHERE n.customerId = $customer RETURN n, collect(l)")
	List<Order> findByCustomerViaAnnotation(String customer);

	List<OrderProjection> findOrderProjectionByCustomerId(String customerId);
}

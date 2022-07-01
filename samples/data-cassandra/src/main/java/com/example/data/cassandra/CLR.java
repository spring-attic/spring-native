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
package com.example.data.cassandra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class CLR implements CommandLineRunner {

	@Autowired
	private CassandraTemplate template;

	@Autowired
	private OrderRepository orderRepository;

	@Override
	public void run(String... args) {

		LineItem product1 = new LineItem("p1", 1.23);
		LineItem product2 = new LineItem("p2", 0.87, 2);
		LineItem product3 = new LineItem("p3", 5.33);

		log("\n\n\n---- INT REPO ----");
		log("-----------------\n\n\n");

		runSaveThenFindAll(product1, product2, product3);
		runPagingAndSorting(product1, product2, product3);
		runPartTreeQuery(product1, product2, product3);
		runResultProjection(product1, product2, product3);
	}

	// Basic save and findAll with Repository
	private void runSaveThenFindAll(LineItem product1, LineItem product2, LineItem product3) {

		log("---- FIND ALL ----");
		orderRepository.deleteAll();

		Order order = newOrder("o1", "c42", product1, product2, product3);
		orderRepository.save(order);

		Iterable<Order> all = orderRepository.findAll();
		all.forEach(this::log);

		log("-----------------\n\n\n");
	}

	// Paging and Sorting with Repository
	private void runPagingAndSorting(LineItem product1, LineItem product2, LineItem product3) {

		log("---- Paging / Sorting ----");
		orderRepository.deleteAll();

		orderRepository.save(newOrder("o2", "c42", product1));
		orderRepository.save(newOrder("o3", "c42", product2));
		orderRepository.save(newOrder("o4", "c42", product3));
		orderRepository.save(newOrder("o5", "b12", product1));
		orderRepository.save(newOrder("o6", "b12", product1));

		// sort
		List<Order> sortedByCustomer = orderRepository.findById("o1", Sort.by("customerId"));
		log("sortedByCustomer: %s", sortedByCustomer);

		// page

		// slice
		Slice<Order> c42_slice0 = orderRepository.findSliceByCustomerId("c42", PageRequest.of(0, 2));
		log("c42_slice0: %s", c42_slice0);

		Slice<Order> c42_slice1 = orderRepository.findSliceByCustomerId("c42", c42_slice0.nextPageable());
		log("c42_slice1: %s", c42_slice1);

		log("-----------------\n\n\n");
	}

	// Part Tree Query
	private void runPartTreeQuery(LineItem product1, LineItem product2, LineItem product3) {

		log("---- PART TREE QUERY ----");
		orderRepository.deleteAll();

		Order order = newOrder("o7", "c42", product1, product2, product3);
		orderRepository.save(order);

		List<Order> byCustomerId = orderRepository.findByCustomerId(order.getCustomerId());
		byCustomerId.forEach(this::log);

		byCustomerId.forEach(this::log);

		log("-----------------\n\n\n");
	}


	// Query with Result Projection
	private void runResultProjection(LineItem product1, LineItem product2, LineItem product3) {

		log("---- RESULT PROJECTION ----");
		orderRepository.deleteAll();

		Order order = newOrder("o9", "c42", product1, product2, product3);
		orderRepository.save(order);

		List<OrderProjection> result = orderRepository.findProjectionByCustomerId(order.getCustomerId());
		result.forEach(it -> log("OrderProjection(%s){id=%s, customerId=%s}",
			it.getClass().getSimpleName(), it.getId(), it.getCustomerId()));

		log("-----------------\n\n\n");
	}

	private Order newOrder(String id, String customerId, LineItem... items) {
		return newOrder(id, customerId, new Date(), items);
	}

	private Order newOrder(String id, String customerId, Date date, LineItem... items) {

		Order order = new Order(id, customerId, date, new ArrayList<>());

		Arrays.stream(items).forEach(order::addItem);

		return order;
	}

	private void log(Object value) {
		log(String.valueOf(value), new Object[0]);
	}

	private void log(String message, Object... arguments) {
		String messageNewline = String.valueOf(message)
				.endsWith("\n") ? message : message + "\n";
		System.out.printf(messageNewline, arguments);
		System.out.flush();
	}

}

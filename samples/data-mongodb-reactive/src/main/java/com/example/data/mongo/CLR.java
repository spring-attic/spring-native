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

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class CLR implements CommandLineRunner {

	@Autowired
	private ReactiveMongoTemplate template;

	@Autowired
	private OrderRepository repository;

	@Override
	public void run(String... args) throws Exception {

		LineItem product1 = new LineItem("p1", 1.23);
		LineItem product2 = new LineItem("p2", 0.87, 2);
		LineItem product3 = new LineItem("p3", 5.33);

		System.out.println("\n\n\n---- INT REPO ----");

		System.out.println("-----------------\n\n\n");

		// Basic save find via repository
		{
			System.out.println("---- FIND ALL ----");

			repository.deleteAll().block();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			repository.save(order).block();

			Iterable<Order> all = repository.findAll().toIterable();
			all.forEach(System.out::println);

			System.out.println("-----------------\n\n\n");
		}

		{
			System.out.println("---- Paging / Sorting ----");
			repository.deleteAll().block();

			repository.save(new Order("c42", new Date()).addItem(product1)).block();
			repository.save(new Order("c42", new Date()).addItem(product2)).block();
			repository.save(new Order("c42", new Date()).addItem(product3)).block();

			repository.save(new Order("b12", new Date()).addItem(product1)).block();
			repository.save(new Order("b12", new Date()).addItem(product1)).block();

			// sort
			List<Order> sortedByCustomer = repository.findBy(Sort.by("customerId")).collectList().block();
			System.out.println("sortedByCustomer: " + sortedByCustomer);

			// page
			List<Order> c42_page0 = repository.findByCustomerId("c42", PageRequest.of(0, 2)).collectList().block();
			System.out.println("c42_page0: " + c42_page0);
			List<Order> c42_page1 = repository.findByCustomerId("c42", PageRequest.of(1, 2)).collectList().block();
			System.out.println("c42_page1: " + c42_page1);

			// slice
			List<Order> c42_slice0 = repository.findSliceByCustomerId("c42", PageRequest.of(0, 2)).collectList().block();
			System.out.println("c42_slice0: " + c42_slice0);

			System.out.println("-----------------\n\n\n");
		}

		// Part Tree Query
		{
			System.out.println("---- PART TREE QUERY ----");
			repository.deleteAll().block();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			repository.save(order).block();

			List<Order> byCustomerId = repository.findByCustomerId(order.getCustomerId()).collectList().block();
			byCustomerId.forEach(System.out::println);

			System.out.println("-----------------\n\n\n");
		}

		// Annotated Query
		{
			System.out.println("---- ANNOTATED QUERY ----");
			repository.deleteAll().block();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			repository.save(order).block();

			List<Order> byCustomerId = repository.findByCustomerViaAnnotation(order.getCustomerId()).collectList().block();
			byCustomerId.forEach(System.out::println);

			System.out.println("-----------------\n\n\n");
		}

		// Annotated Aggregations
		{
			System.out.println("---- ANNOTATED AGGREGATIONS ----");
			repository.deleteAll().block();

			repository.save(new Order("c42", new Date()).addItem(product1)).block();
			repository.save(new Order("c42", new Date()).addItem(product2)).block();
			repository.save(new Order("c42", new Date()).addItem(product3)).block();

			repository.save(new Order("b12", new Date()).addItem(product1)).block();
			repository.save(new Order("b12", new Date()).addItem(product1)).block();

			List<OrdersPerCustomer> result = repository.totalOrdersPerCustomer(Sort.by(Sort.Order.desc("total"))).collectList().block();
			System.out.println("result: " + result);

//			assertThat(result).containsExactly(new OrdersPerCustomer("c42", 3L), new OrdersPerCustomer("b12", 2L));
			System.out.println("-----------------\n\n\n");
		}

		// Custom Implementation
		{
			System.out.println("---- CUSTOM IMPLEMENTATION ----");
			repository.deleteAll().block();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			order = repository.save(order).block();

			Invoice invoice = repository.getInvoiceFor(order).block();
			System.out.println("invoice: " + invoice);

			System.out.println("-----------------\n\n\n");
		}

		// Result Projection
		{
			System.out.println("---- RESULT PROJECTION ----");
			repository.deleteAll().block();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			repository.save(order).block();

			List<OrderProjection> result = repository.findOrderProjectionByCustomerId(order.getCustomerId()).collectList().block();
			result.forEach(it -> System.out.println(String.format("OrderProjection(%s){id=%s, customerId=%s}", it.getClass().getSimpleName(), it.getId(), it.getCustomerId())));
			System.out.println("-----------------\n\n\n");
		}

		{

			System.out.println("---- QUERY BY EXAMPLE ----");
			repository.deleteAll().block();

			Order order1 = repository.save(new Order("c42", new Date()).addItem(product1)).block();
			Order order2 = repository.save(new Order("b12", new Date()).addItem(product1)).block();

			Example<Order> example = Example.of(new Order(order1.getCustomerId()), ExampleMatcher.matching().withIgnorePaths("items"));
			Iterable<Order> result = repository.findAll(example).toIterable();

			System.out.print("result: ");
			result.forEach(System.out::print);
			System.out.println();

			System.out.println("-----------------\n\n\n");
		}
	}
}

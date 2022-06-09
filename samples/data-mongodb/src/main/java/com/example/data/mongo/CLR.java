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
package com.example.data.mongo;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class CLR implements CommandLineRunner {

	@Autowired
	private MongoTemplate template;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Override
	public void run(String... args) {

		initializeDatabase(args);

		LineItem product1 = new LineItem("p1", 1.23);
		LineItem product2 = new LineItem("p2", 0.87, 2);
		LineItem product3 = new LineItem("p3", 5.33);

		log("\n\n\n---- INT REPO ----");
		log("-----------------\n\n\n");

		runSaveThenFindAll(product1, product2, product3);
		runPagingAndSorting(product1, product2, product3);
		runPartTreeQuery(product1, product2, product3);
		runAnnotatedQuery(product1, product2, product3);
		runAnnotatedAggregations(product1, product2, product3);
		runCustomImplementation(product1, product2, product3);
		runResultProjection(product1, product2, product3);
		runCustomConversions();
		runWithDbRefs(product1, product2, product3);
		runWithDocumentReferences(product1, product2, product3);
		runQueryByExample(product1);
	}

	// Prepare Collections to avoid timeouts on slow ci/docker/...
	private void initializeDatabase(String... args) {

		template.execute(db -> {

			Set<String> collectionNames = db.listCollectionNames().into(new HashSet<>());

			if (!collectionNames.contains("order")) {
				db.createCollection("order");
			}
			if (collectionNames.contains("coupon")) {
				template.execute(Coupon.class, mongoCollection ->
						mongoCollection.deleteMany(new Document()));
			}
			else {
				db.createCollection("coupon");
			}

			return "ok";
		});
	}

	// Basic save and findAll with Repository
	private void runSaveThenFindAll(LineItem product1, LineItem product2, LineItem product3) {

		log("---- FIND ALL ----");
		orderRepository.deleteAll();

		Order order = newOrder("c42", product1, product2, product3);
		orderRepository.save(order);

		Iterable<Order> all = orderRepository.findAll();
		all.forEach(this::log);

		log("-----------------\n\n\n");
	}

	// Paging and Sorting with Repository
	private void runPagingAndSorting(LineItem product1, LineItem product2, LineItem product3) {

		log("---- Paging / Sorting ----");
		orderRepository.deleteAll();

		orderRepository.save(newOrder("c42", product1));
		orderRepository.save(newOrder("c42", product2));
		orderRepository.save(newOrder("c42", product3));
		orderRepository.save(newOrder("b12", product1));
		orderRepository.save(newOrder("b12", product1));

		// sort
		List<Order> sortedByCustomer = orderRepository.findBy(Sort.by("customerId"));
		log("sortedByCustomer: %s", sortedByCustomer);

		// page
		Page<Order> c42_page0 = orderRepository.findByCustomerId("c42", PageRequest.of(0, 2));
		log("c42_page0: %s", c42_page0);
		Page<Order> c42_page1 = orderRepository.findByCustomerId("c42", c42_page0.nextPageable());
		log("c42_page1: %s", c42_page1);

		// slice
		Slice<Order> c42_slice0 = orderRepository.findSliceByCustomerId("c42", PageRequest.of(0, 2));
		log("c42_slice0: %s", c42_slice0);

		log("-----------------\n\n\n");
	}

	// Part Tree Query
	private void runPartTreeQuery(LineItem product1, LineItem product2, LineItem product3) {

		log("---- PART TREE QUERY ----");
		orderRepository.deleteAll();

		Order order = newOrder("c42", product1, product2, product3);
		orderRepository.save(order);

		List<Order> byCustomerId = orderRepository.findByCustomerId(order.getCustomerId());
		byCustomerId.forEach(this::log);

		log("-----------------\n\n\n");
	}

	// Query using Annotation (@Query(..))
	private void runAnnotatedQuery(LineItem product1, LineItem product2, LineItem product3) {

		log("---- ANNOTATED QUERY ----");
		orderRepository.deleteAll();

		Order order = newOrder("c42", product1, product2, product3);
		orderRepository.save(order);

		List<Order> byCustomerId = orderRepository.findByCustomerViaAnnotation(order.getCustomerId());
		byCustomerId.forEach(this::log);

		log("-----------------\n\n\n");
	}

	// Query with Aggregations (e.g. sum, total)
	private void runAnnotatedAggregations(LineItem product1, LineItem product2, LineItem product3) {

		log("---- ANNOTATED AGGREGATIONS ----");
		orderRepository.deleteAll();

		orderRepository.save(newOrder("c42", product1));
		orderRepository.save(newOrder("c42", product2));
		orderRepository.save(newOrder("c42", product3));
		orderRepository.save(newOrder("b12", product1));
		orderRepository.save(newOrder("b12", product1));

		List<OrdersPerCustomer> result = orderRepository.totalOrdersPerCustomer(Sort.by(Sort.Order.desc("total")));
		log("result: %s", result);

		//assertThat(result).containsExactly(new OrdersPerCustomer("c42", 3L), new OrdersPerCustomer("b12", 2L));
		log("-----------------\n\n\n");
	}

	// Query as a Custom Implementation
	private void runCustomImplementation(LineItem product1, LineItem product2, LineItem product3) {

		log("---- CUSTOM IMPLEMENTATION ----");
		orderRepository.deleteAll();

		Order order = newOrder("c42", product1, product2, product3);
		order = orderRepository.save(order);

		Invoice invoice = orderRepository.getInvoiceFor(order);
		log("invoice: %s", invoice);

		log("-----------------\n\n\n");
	}

	// Query with Result Projection
	private void runResultProjection(LineItem product1, LineItem product2, LineItem product3) {

		log("---- RESULT PROJECTION ----");
		orderRepository.deleteAll();

		Order order = newOrder("c42", product1, product2, product3);
		orderRepository.save(order);

		List<OrderProjection> result = orderRepository.findOrderProjectionByCustomerId(order.getCustomerId());
		result.forEach(it -> log("OrderProjection(%s){id=%s, customerId=%s}",
			it.getClass().getSimpleName(), it.getId(), it.getCustomerId()));

		log("-----------------\n\n\n");
	}

	// Query using Custom Conversions
	private void runCustomConversions() {

		log("---- CUSTOM CONVERSION ----");
		customerRepository.deleteAll();

		customerRepository.save(new Customer("c-1", "c", "42"));

		Document saved = template.execute(Customer.class, collection -> {
			return collection.find(new Document("_id", "c-1")).first();
		});

		log("Raw Document: %s", saved);

		if(!saved.get("name").equals("c; 42")) {
			throw new RuntimeException("Custom Conversion is broken");
		}

		Optional<Customer> byId = customerRepository.findById("c-1");
		log("Domain Object: %s", byId.get());

		log("-----------------\n\n\n");
	}

	// Query for entity with DBRefs
	private void runWithDbRefs(LineItem product1, LineItem product2, LineItem product3) {

		log("---- DBREFs ----");
		orderRepository.deleteAll();

		Coupon coupon = new Coupon("X3R");
		template.insert(coupon);

		Order order = newOrder("c42", product1, product2, product3);
		order.setCoupon(coupon);
		order.setReduction(coupon);
		order.setSimpleRef(coupon);
		orderRepository.save(order);

		Optional<Order> loaded = orderRepository.findById(order.getId());
		log("simple ref (no proxy): %s", loaded.get().getSimpleRef().getCode());
		log("lazyLoading (aot): %s", loaded.get().getCoupon().getCode());
		log("lazyLoading (jdk): %s", loaded.get().getReduction().getId());

		log("-----------------\n\n\n");
	}

	// Query for entity with Document References
	private void runWithDocumentReferences(LineItem product1, LineItem product2, LineItem product3) {

		log("---- Document References ----");
		orderRepository.deleteAll();

		Discount discount = new Discount(30F);
		template.insert(discount);

		Order order = newOrder("c42", product1, product2, product3);
		order.setDocumentRef(discount);
		order.setLazyDocumentRef(discount);
		orderRepository.save(order);

		Optional<Order> loaded = orderRepository.findById(order.getId());
		log("document ref (no proxy): %s", loaded.get().getDocumentRef().getPercentage());
		log("lazy document ref (aot): %s", loaded.get().lazyDocumentRef);

		log("-----------------\n\n\n");
	}

	private void runQueryByExample(LineItem product1) {

		log("---- QUERY BY EXAMPLE ----");
		orderRepository.deleteAll();

		Order order1 = orderRepository.save(newOrder("c42", product1));
		Order order2 = orderRepository.save(newOrder("b12", product1));

		Example<Order> example = Example.of(newOrder(order1.getCustomerId(), order1.getOrderDate()),
			ExampleMatcher.matching().withIgnorePaths("items"));

		Iterable<Order> result = orderRepository.findAll(example);
		log("result: %s", result);

		log("-----------------\n\n\n");
	}

	private Order newOrder(String customerId, LineItem... items) {
		return newOrder(customerId, new Date(), items);
	}

	private Order newOrder(String customerId, Date date, LineItem... items) {

		Order order = new Order(customerId, date);

		Arrays.stream(items).forEach(order::addItem);

		return order;
	}

	private void log(Object value) {
		log(String.valueOf(value), new Object[0]);
	}

	private void log(String message, Object... arguments) {
		String messageNewline = String.valueOf(message).endsWith("\n") ? message : message + "\n";
		System.out.printf(messageNewline, arguments);
		System.out.flush();
	}

}

package com.example.data.mongo;

import java.util.Date;
import java.util.List;

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

			repository.deleteAll();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			repository.save(order);

			Iterable<Order> all = repository.findAll();
			all.forEach(System.out::println);

			System.out.println("-----------------\n\n\n");
		}

		{
			System.out.println("---- Paging / Sorting ----");
			repository.deleteAll();

			repository.save(new Order("c42", new Date()).addItem(product1));
			repository.save(new Order("c42", new Date()).addItem(product2));
			repository.save(new Order("c42", new Date()).addItem(product3));

			repository.save(new Order("b12", new Date()).addItem(product1));
			repository.save(new Order("b12", new Date()).addItem(product1));

			// sort
			List<Order> sortedByCustomer = repository.findBy(Sort.by("customerId"));
			System.out.println("sortedByCustomer: " + sortedByCustomer);

			// page
			Page<Order> c42_page0 = repository.findByCustomerId("c42", PageRequest.of(0, 2));
			System.out.println("c42_page0: " + c42_page0);
			Page<Order> c42_page1 = repository.findByCustomerId("c42", c42_page0.nextPageable());
			System.out.println("c42_page1: " + c42_page1);

			// slice
			Slice<Order> c42_slice0 = repository.findSliceByCustomerId("c42", PageRequest.of(0, 2));
			System.out.println("c42_slice0: " + c42_slice0);

			System.out.println("-----------------\n\n\n");
		}

		// Part Tree Query
		{
			System.out.println("---- PART TREE QUERY ----");
			repository.deleteAll();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			repository.save(order);

			List<Order> byCustomerId = repository.findByCustomerId(order.getCustomerId());
			byCustomerId.forEach(System.out::println);

			System.out.println("-----------------\n\n\n");
		}

		// Annotated Query
		{
			System.out.println("---- ANNOTATED QUERY ----");
			repository.deleteAll();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			repository.save(order);

			List<Order> byCustomerId = repository.findByCustomerViaAnnotation(order.getCustomerId());
			byCustomerId.forEach(System.out::println);

			System.out.println("-----------------\n\n\n");
		}

		// Annotated Aggregations
		{
			System.out.println("---- ANNOTATED AGGREGATIONS ----");
			repository.deleteAll();

			repository.save(new Order("c42", new Date()).addItem(product1));
			repository.save(new Order("c42", new Date()).addItem(product2));
			repository.save(new Order("c42", new Date()).addItem(product3));

			repository.save(new Order("b12", new Date()).addItem(product1));
			repository.save(new Order("b12", new Date()).addItem(product1));

			List<OrdersPerCustomer> result = repository.totalOrdersPerCustomer(Sort.by(Sort.Order.desc("total")));
			System.out.println("result: " + result);

//			assertThat(result).containsExactly(new OrdersPerCustomer("c42", 3L), new OrdersPerCustomer("b12", 2L));
			System.out.println("-----------------\n\n\n");
		}

		// Custom Implementation
		{
			System.out.println("---- CUSTOM IMPLEMENTATION ----");
			repository.deleteAll();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			order = repository.save(order);

			Invoice invoice = repository.getInvoiceFor(order);
			System.out.println("invoice: " + invoice);

			System.out.println("-----------------\n\n\n");
		}

		// Result Projection
		{
			System.out.println("---- RESULT PROJECTION ----");
			repository.deleteAll();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			repository.save(order);

			List<OrderProjection> result = repository.findOrderProjectionByCustomerId(order.getCustomerId());
			result.forEach(it -> System.out.println(String.format("OrderProjection(%s){id=%s, customerId=%s}", it.getClass().getSimpleName(), it.getId(), it.getCustomerId())));
			System.out.println("-----------------\n\n\n");
		}

		{

			System.out.println("---- QUERY BY EXAMPLE ----");
			repository.deleteAll();

			Order order1 = repository.save(new Order("c42", new Date()).addItem(product1));
			Order order2 = repository.save(new Order("b12", new Date()).addItem(product1));

			Example<Order> example = Example.of(new Order(order1.getCustomerId()), ExampleMatcher.matching().withIgnorePaths("items"));
			Iterable<Order> result = repository.findAll(example);

			System.out.println("result: " + result);

			System.out.println("-----------------\n\n\n");
		}
	}
}

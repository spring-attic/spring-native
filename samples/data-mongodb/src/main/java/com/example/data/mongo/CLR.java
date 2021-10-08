package com.example.data.mongo;

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
	public void run(String... args) throws Exception {

		{
			// prepare collections to avoid timeouts on slow ci/docker/...
			template.execute(db -> {
				Set<String> collections = db.listCollectionNames().into(new HashSet<>());
				if(!collections.contains("order")) {
					db.createCollection("order");
				}
				if(collections.contains("coupon")) {
					template.execute(Coupon.class, mongoCollection ->
							mongoCollection.deleteMany(new Document()));
				}
				else {
					db.createCollection("coupon");
				}
				return "ok";
			});
		}

		LineItem product1 = new LineItem("p1", 1.23);
		LineItem product2 = new LineItem("p2", 0.87, 2);
		LineItem product3 = new LineItem("p3", 5.33);

		System.out.println("\n\n\n---- INT REPO ----");

		System.out.println("-----------------\n\n\n");

		// Basic save find via repository
		{
			System.out.println("---- FIND ALL ----");

			orderRepository.deleteAll();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			orderRepository.save(order);

			Iterable<Order> all = orderRepository.findAll();
			all.forEach(System.out::println);

			System.out.println("-----------------\n\n\n");
		}

		{
			System.out.println("---- Paging / Sorting ----");
			orderRepository.deleteAll();

			orderRepository.save(new Order("c42", new Date()).addItem(product1));
			orderRepository.save(new Order("c42", new Date()).addItem(product2));
			orderRepository.save(new Order("c42", new Date()).addItem(product3));

			orderRepository.save(new Order("b12", new Date()).addItem(product1));
			orderRepository.save(new Order("b12", new Date()).addItem(product1));

			// sort
			List<Order> sortedByCustomer = orderRepository.findBy(Sort.by("customerId"));
			System.out.println("sortedByCustomer: " + sortedByCustomer);

			// page
			Page<Order> c42_page0 = orderRepository.findByCustomerId("c42", PageRequest.of(0, 2));
			System.out.println("c42_page0: " + c42_page0);
			Page<Order> c42_page1 = orderRepository.findByCustomerId("c42", c42_page0.nextPageable());
			System.out.println("c42_page1: " + c42_page1);

			// slice
			Slice<Order> c42_slice0 = orderRepository.findSliceByCustomerId("c42", PageRequest.of(0, 2));
			System.out.println("c42_slice0: " + c42_slice0);

			System.out.println("-----------------\n\n\n");
		}

		// Part Tree Query
		{
			System.out.println("---- PART TREE QUERY ----");
			orderRepository.deleteAll();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			orderRepository.save(order);

			List<Order> byCustomerId = orderRepository.findByCustomerId(order.getCustomerId());
			byCustomerId.forEach(System.out::println);

			System.out.println("-----------------\n\n\n");
		}

		// Annotated Query
		{
			System.out.println("---- ANNOTATED QUERY ----");
			orderRepository.deleteAll();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			orderRepository.save(order);

			List<Order> byCustomerId = orderRepository.findByCustomerViaAnnotation(order.getCustomerId());
			byCustomerId.forEach(System.out::println);

			System.out.println("-----------------\n\n\n");
		}

		// Annotated Aggregations
		{
			System.out.println("---- ANNOTATED AGGREGATIONS ----");
			orderRepository.deleteAll();

			orderRepository.save(new Order("c42", new Date()).addItem(product1));
			orderRepository.save(new Order("c42", new Date()).addItem(product2));
			orderRepository.save(new Order("c42", new Date()).addItem(product3));

			orderRepository.save(new Order("b12", new Date()).addItem(product1));
			orderRepository.save(new Order("b12", new Date()).addItem(product1));

			List<OrdersPerCustomer> result = orderRepository.totalOrdersPerCustomer(Sort.by(Sort.Order.desc("total")));
			System.out.println("result: " + result);

//			assertThat(result).containsExactly(new OrdersPerCustomer("c42", 3L), new OrdersPerCustomer("b12", 2L));
			System.out.println("-----------------\n\n\n");
		}

		// Custom Implementation
		{
			System.out.println("---- CUSTOM IMPLEMENTATION ----");
			orderRepository.deleteAll();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			order = orderRepository.save(order);

			Invoice invoice = orderRepository.getInvoiceFor(order);
			System.out.println("invoice: " + invoice);

			System.out.println("-----------------\n\n\n");
		}

		// Result Projection
		{
			System.out.println("---- RESULT PROJECTION ----");
			orderRepository.deleteAll();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			orderRepository.save(order);

			List<OrderProjection> result = orderRepository.findOrderProjectionByCustomerId(order.getCustomerId());
			result.forEach(it -> System.out.println(String.format("OrderProjection(%s){id=%s, customerId=%s}", it.getClass().getSimpleName(), it.getId(), it.getCustomerId())));
			System.out.println("-----------------\n\n\n");
		}

		// Custom Conversions
		{
			System.out.println("---- CUSTOM CONVERSION ----");
			customerRepository.deleteAll();

			customerRepository.save(new Customer("c-1", "c", "42"));

			Document saved = template.execute(Customer.class, collection -> {
				return collection.find(new Document("_id", "c-1")).first();
			});

			System.out.println("Raw Document: " + saved);
			if(!saved.get("name").equals("c; 42")) {
				throw new RuntimeException("Custom Conversion is broken");
			}
			Optional<Customer> byId = customerRepository.findById("c-1");

			System.out.println("Domain Object: " + byId.get());
			System.out.println("-----------------\n\n\n");
		}

		// DBRefs
		{
			System.out.println("---- DBREFs ----");
			orderRepository.deleteAll();
			Coupon coupon = new Coupon("X3R");
			template.insert(coupon);

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			order.setCoupon(coupon);
			order.setReduction(coupon);
			order.setSimpleRef(coupon);

			orderRepository.save(order);

			Optional<Order> loaded = orderRepository.findById(order.getId());
			System.out.println("simple ref (no proxy): " + loaded.get().getSimpleRef().getCode());
			System.out.println("lazyLoading (aot): " + loaded.get().getCoupon().getCode());
			System.out.println("lazyLoading (jdk): " + loaded.get().getReduction().getId());
			System.out.println("-----------------\n\n\n");
		}

		{

			System.out.println("---- QUERY BY EXAMPLE ----");
			orderRepository.deleteAll();

			Order order1 = orderRepository.save(new Order("c42", new Date()).addItem(product1));
			Order order2 = orderRepository.save(new Order("b12", new Date()).addItem(product1));

			Example<Order> example = Example.of(new Order(order1.getCustomerId()), ExampleMatcher.matching().withIgnorePaths("items"));
			Iterable<Order> result = orderRepository.findAll(example);

			System.out.println("result: " + result);

			System.out.println("-----------------\n\n\n");
		}
	}
}

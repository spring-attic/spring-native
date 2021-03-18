package com.example.webflux;

import org.springframework.data.annotation.Id;

public class Customer {

	@Id
	private Integer id;
	private String name;

	public Customer() {
	}

	public Customer(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}

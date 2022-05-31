/*
 * Copyright 2017-2021 the original author or authors.
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
package org.springframework.hateoas.examples;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Greg Turnquist
 */
@Entity
class Employee {

	@Id @GeneratedValue private Long id;
	private String name;
	private String role;

	public Employee() {
	}

	/**
	 * To break the recursive, bi-directional relationship, don't serialize {@literal manager}.
	 */
	@JsonIgnore @OneToOne private Manager manager;

	Employee(String name, String role, Manager manager) {

		this.name = name;
		this.role = role;
		this.manager = manager;
	}

	public Optional<Long> getId() {
		return Optional.ofNullable(this.id);
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Manager getManager() {
		return manager;
	}

	public void setManager(Manager manager) {
		this.manager = manager;
	}
}

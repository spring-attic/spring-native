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

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Legacy representation. Contains older format of data. Fewer links because hypermedia at the time was an after
 * thought.
 *
 * @author Greg Turnquist
 */
@JsonPropertyOrder({"id", "name", "employees"})
class Supervisor {

	@JsonIgnore private final Manager manager;

	public Supervisor(Manager manager) {
		this.manager = manager;
	}

	public Long getId() {

		return this.manager.getId() //
				.orElseThrow(() -> new RuntimeException("Couldn't find anything"));
	}

	public String getName() {
		return this.manager.getName();
	}

	public List<String> getEmployees() {

		return manager.getEmployees().stream() //
				.map(employee -> employee.getName() + "::" + employee.getRole()) //
				.collect(Collectors.toList());
	}
}

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class defined purely for hosting an "detailed" point of view for REST.
 *
 * @author Greg Turnquist
 */
@JsonPropertyOrder({"id", "name", "role", "manager"})
public class EmployeeWithManager {

	@JsonIgnore private final Employee employee;

	public EmployeeWithManager(Employee employee) {
		this.employee = employee;
	}

	public Long getId() {

		return this.employee.getId() //
				.orElseThrow(() -> new RuntimeException("Couldn't find anything."));
	}

	public String getName() {
		return this.employee.getName();
	}

	public String getRole() {
		return this.employee.getRole();
	}

	public String getManager() {
		return this.employee.getManager().getName();
	}
}

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

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author Greg Turnquist
 */
@Component
class DatabaseLoader {

	@Bean
	CommandLineRunner initDatabase(EmployeeRepository employeeRepository, ManagerRepository managerRepository) {
		return args -> {

			/*
			 * Gather Gandalf's team
			 */
			Manager gandalf = managerRepository.save(new Manager("Gandalf"));

			Employee frodo = employeeRepository.save(new Employee("Frodo", "ring bearer", gandalf));
			Employee bilbo = employeeRepository.save(new Employee("Bilbo", "burglar", gandalf));

			gandalf.setEmployees(Arrays.asList(frodo, bilbo));
			managerRepository.save(gandalf);

			/*
			 * Put together Saruman's team
			 */
			Manager saruman = managerRepository.save(new Manager("Saruman"));

			Employee sam = employeeRepository.save(new Employee("Sam", "gardener", saruman));

			saruman.setEmployees(Arrays.asList(sam));

			managerRepository.save(saruman);
		};
	}
}

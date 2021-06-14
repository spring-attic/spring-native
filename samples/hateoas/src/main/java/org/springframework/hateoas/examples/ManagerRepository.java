/*
 * Copyright 2017-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.examples;

import org.springframework.data.repository.CrudRepository;

/**
 * @author Greg Turnquist
 */
interface ManagerRepository extends CrudRepository<Manager, Long> {

	/**
	 * Navigate through the JPA relationship to find a {@link Manager} based on an {@link Employee}'s {@literal id}.
	 * 
	 * @param id
	 * @return
	 */
	Manager findByEmployeesId(Long id);
}

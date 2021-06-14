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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

/**
 * @author Greg Turnquist
 */
@Component
class EmployeeRepresentationModelAssembler extends SimpleIdentifiableRepresentationModelAssembler<Employee> {

	EmployeeRepresentationModelAssembler() {
		super(EmployeeController.class);
	}

	/**
	 * Define links to add to every {@link EntityModel}.
	 *
	 * @param resource
	 */
	@Override
	public void addLinks(EntityModel<Employee> resource) {

		/**
		 * Add some custom links to the default ones provided. NOTE: To replace default links, don't invoke
		 * {@literal super.addLinks()}.
		 */
		super.addLinks(resource);

		resource.getContent().getId() //
				.ifPresent(id -> { //
					// Add additional links
					resource.add(linkTo(methodOn(ManagerController.class).findManager(id)).withRel("manager"));
					resource.add(linkTo(methodOn(EmployeeController.class).findDetailedEmployee(id)).withRel("detailed"));

					// Maintain a legacy link to support older clients not yet adjusted to the switch from "supervisor" to
					// "manager".
					resource.add(linkTo(methodOn(SupervisorController.class).findOne(id)).withRel("supervisor"));
				});
	}

	/**
	 * Define links to add to {@link CollectionModel} collection.
	 *
	 * @param resources
	 */
	@Override
	public void addLinks(CollectionModel<EntityModel<Employee>> resources) {

		super.addLinks(resources);

		resources.add(linkTo(methodOn(EmployeeController.class).findAllDetailedEmployees()).withRel("detailedEmployees"));
		resources.add(linkTo(methodOn(ManagerController.class).findAll()).withRel("managers"));
		resources.add(linkTo(methodOn(RootController.class).root()).withRel("root"));
	}
}

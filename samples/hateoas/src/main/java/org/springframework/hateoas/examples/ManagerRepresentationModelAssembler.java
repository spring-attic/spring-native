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
class ManagerRepresentationModelAssembler extends SimpleIdentifiableRepresentationModelAssembler<Manager> {

	ManagerRepresentationModelAssembler() {
		super(ManagerController.class);
	}

	/**
	 * Retain default links provided by {@link SimpleIdentifiableRepresentationModelAssembler}, but add extra ones to each
	 * {@link Manager}.
	 *
	 * @param resource
	 */
	@Override
	public void addLinks(EntityModel<Manager> resource) {
		/**
		 * Retain default links.
		 */
		super.addLinks(resource);

		resource.getContent().getId() //
				.ifPresent(id -> { //
					// Add custom link to find all managed employees
					resource.add(linkTo(methodOn(EmployeeController.class).findEmployees(id)).withRel("employees"));
				});
	}

	/**
	 * Retain default links for the entire collection, but add extra custom links for the {@link Manager} collection.
	 *
	 * @param resources
	 */
	@Override
	public void addLinks(CollectionModel<EntityModel<Manager>> resources) {

		super.addLinks(resources);

		resources.add(linkTo(methodOn(EmployeeController.class).findAll()).withRel("employees"));
		resources.add(linkTo(methodOn(EmployeeController.class).findAllDetailedEmployees()).withRel("detailedEmployees"));
		resources.add(linkTo(methodOn(RootController.class).root()).withRel("root"));
	}
}

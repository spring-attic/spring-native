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

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Greg Turnquist
 */
@RestController
class ManagerController {

	private final ManagerRepository repository;
	private final ManagerRepresentationModelAssembler assembler;

	ManagerController(ManagerRepository repository, ManagerRepresentationModelAssembler assembler) {

		this.repository = repository;
		this.assembler = assembler;
	}

	/**
	 * Look up all managers, and transform them into a REST collection resource using
	 * {@link ManagerRepresentationModelAssembler#toCollectionModel(Iterable)}. Then return them through Spring Web's
	 * {@link ResponseEntity} fluent API.
	 */
	@GetMapping("/managers")
	ResponseEntity<CollectionModel<EntityModel<Manager>>> findAll() {

		return ResponseEntity.ok( //
				assembler.toCollectionModel(repository.findAll()));

	}

	/**
	 * Look up a single {@link Manager} and transform it into a REST resource using
	 * {@link ManagerRepresentationModelAssembler#toModel(Object)}. Then return it through Spring Web's
	 * {@link ResponseEntity} fluent API.
	 *
	 * @param id
	 */
	@GetMapping("/managers/{id}")
	ResponseEntity<EntityModel<Manager>> findOne(@PathVariable long id) {

		return repository.findById(id) //
				.map(assembler::toModel) //
				.map(ResponseEntity::ok) //
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Find an {@link Employee}'s {@link Manager} based upon employee id. Turn it into a context-based link.
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/employees/{id}/manager")
	ResponseEntity<EntityModel<Manager>> findManager(@PathVariable long id) {

		return ResponseEntity.ok( //
				assembler.toModel(repository.findByEmployeesId(id)));
	}
}

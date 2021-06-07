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

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Represent an older controller that has since been replaced with {@link ManagerController}. This controller is used to
 * provide legacy routes, i.e. backwards compatibility.
 *
 * @author Greg Turnquist
 */
@RestController
public class SupervisorController {

	private final ManagerController controller;

	public SupervisorController(ManagerController controller) {
		this.controller = controller;
	}

	@GetMapping("/supervisors/{id}")
	public ResponseEntity<EntityModel<Supervisor>> findOne(@PathVariable Long id) {

		EntityModel<Manager> managerResource = controller.findOne(id).getBody();

		EntityModel<Supervisor> supervisorResource = EntityModel.of( //
				new Supervisor(managerResource.getContent()), //
				managerResource.getLinks());

		return ResponseEntity.ok(supervisorResource);
	}
}

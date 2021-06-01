/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.samples.petclinic.owner;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link VisitController}
 *
 * @author Colin But
 */
@WebMvcTest(VisitController.class)
class VisitControllerTests {

	private static final int TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private VisitRepository visits;

	@Autowired
	private PetRepository pets;

	@Test
	void testInitNewVisitForm() throws Exception {
		mockMvc.perform(get("/owners/*/pets/{petId}/visits/new", TEST_PET_ID)).andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdateVisitForm"));
	}

	@Test
	void testProcessNewVisitFormSuccess() throws Exception {
		mockMvc.perform(post("/owners/*/pets/{petId}/visits/new", TEST_PET_ID).param("name", "George")
				.param("description", "Visit Description")).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessNewVisitFormHasErrors() throws Exception {
		mockMvc.perform(post("/owners/*/pets/{petId}/visits/new", TEST_PET_ID).param("name", "George"))
				.andExpect(model().attributeHasErrors("visit")).andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdateVisitForm"));
	}


	@TestConfiguration
	static class VisitControllerTestConfiguration {

		@Bean
		VisitRepository visits() {
			return new VisitRepository() {
				@Override
				public void save(Visit visit) throws DataAccessException {
				}

				@Override
				public List<Visit> findByPetId(Integer petId) {
					return Lists.emptyList();
				}
			};
		}

		@Bean
		PetRepository pets() {
			return new PetRepository() {
				@Override
				public List<PetType> findPetTypes() {
					return null;
				}

				@Override
				public Pet findById(Integer id) {
					if (id.equals(TEST_PET_ID)) {
						return new Pet();
					}
					return null;
				}

				@Override
				public void save(Pet pet) {
				}
			};
		}
	}

}

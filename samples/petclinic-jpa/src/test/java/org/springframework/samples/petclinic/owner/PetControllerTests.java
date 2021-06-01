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

import java.util.Collection;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for the {@link PetController}
 *
 * @author Colin But
 */
@WebMvcTest(value = PetController.class,
		includeFilters = @ComponentScan.Filter(value = PetTypeFormatter.class, type = FilterType.ASSIGNABLE_TYPE))
class PetControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PetRepository pets;

	@Autowired
	private OwnerRepository owners;

	@Test
	void testInitCreationForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/new", TEST_OWNER_ID)).andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm")).andExpect(model().attributeExists("pet"));
	}

	@Test
	void testProcessCreationFormSuccess() throws Exception {
		mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Betty")
				.param("type", "hamster").param("birthDate", "2015-02-12")).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessCreationFormHasErrors() throws Exception {
		mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Betty").param("birthDate",
				"2015-02-12")).andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet")).andExpect(model().attributeHasFieldErrors("pet", "type"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "type", "required")).andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	@Test
	void testInitUpdateForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID))
				.andExpect(status().isOk()).andExpect(model().attributeExists("pet"))
				.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	@Test
	void testProcessUpdateFormSuccess() throws Exception {
		mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).param("name", "Betty")
				.param("type", "hamster").param("birthDate", "2015-02-12")).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessUpdateFormHasErrors() throws Exception {
		mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).param("name", "Betty")
				.param("birthDate", "2015/02/12")).andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet")).andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
	}


	@TestConfiguration
	static class PetControllerTestConfiguration {

		@Bean
		PetRepository pets() {
			PetType cat = new PetType();
			cat.setId(3);
			cat.setName("hamster");
			return new PetRepository() {
				@Override
				public List<PetType> findPetTypes() {
					return Lists.newArrayList(cat);
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

		@Bean
		OwnerRepository owners() {
			return new OwnerRepository() {
				@Override
				public Collection<Owner> findByLastName(String lastName) {
					return Lists.emptyList();
				}

				@Override
				public Owner findById(Integer id) {
					if (id.equals(TEST_OWNER_ID)) {
						return new Owner();
					}
					return null;
				}

				@Override
				public void save(Owner owner) {
				}
			};
		}
	}

}

package org.springframework.samples.petclinic.owner;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

/**
 * Test class for {@link VisitController}
 *
 * @author Colin But
 */
@WebFluxTest(VisitController.class)
@ImportAutoConfiguration(ThymeleafAutoConfiguration.class)
public class VisitControllerTests {

	private static final int TEST_PET_ID = 1;

	@Autowired
	private WebTestClient client;

	@Autowired
	private VisitRepository visits;

	@Autowired
	private PetRepository pets;

	@Test
	public void testInitNewVisitForm() throws Exception {
		client.get().uri("/owners/*/pets/{petId}/visits/new", TEST_PET_ID).exchange().expectStatus().isOk();
	}

	@Test
	public void testProcessNewVisitFormSuccess() throws Exception {
		client.post().uri("/owners/*/pets/{petId}/visits/new", TEST_PET_ID)
				.body(fromFormData("name", "George").with("description", "Visit Description")).exchange().expectStatus()
				.is3xxRedirection();
	}

	@Test
	public void testProcessNewVisitFormHasErrors() throws Exception {
		client.post().uri("/owners/*/pets/{petId}/visits/new", TEST_PET_ID).body(fromFormData("name", "George"))
				.exchange().expectStatus().isOk();
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
				public Pet findById(int id) {
					if (id == TEST_PET_ID) {
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

package org.springframework.samples.petclinic.owner;

import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Hooks;

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

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Test class for {@link OwnerController}
 *
 * @author Colin But
 */
@WebFluxTest(OwnerController.class)
@ImportAutoConfiguration(ThymeleafAutoConfiguration.class)
public class OwnerControllerTests {

	private static final int TEST_OWNER_ID = 1;

	@Autowired
	private WebTestClient client;

	@Autowired
	private OwnerRepository owners;

	@Autowired
    private VisitRepository visits;

	private Owner george;

	@Test
	public void testInitCreationForm() throws Exception {
		client.get().uri("/owners/new").exchange().expectStatus().isOk();
	}

	@Test
	public void testProcessCreationFormSuccess() throws Exception {
		client.post().uri("/owners/new")
				.body(fromFormData("firstName", "Joe").with("lastName", "Bloggs")
						.with("address", "123 Caramel Street").with("city", "London")
						.with("telephone", "01316761638"))
				.exchange().expectStatus().is3xxRedirection();
	}

	@Test
	public void testProcessCreationFormHasErrors() throws Exception {
		client.post().uri("/owners/new").body(fromFormData("firstName", "Joe")
				.with("lastName", "Bloggs").with("city", "London")).exchange()
				.expectStatus().isOk();
	}

	@Test
	public void testInitFindForm() throws Exception {
		client.get().uri("/owners/find").exchange().expectStatus().isOk();
	}

	@Test
	public void testProcessFindFormSuccess() throws Exception {
		client.get().uri("/owners").exchange().expectStatus().isOk();
	}

	@Test
	public void testProcessFindFormByLastName() throws Exception {
		client.get().uri("/owners?lastName=Franklin").exchange().expectStatus()
				.is3xxRedirection();
	}

	@Test
	public void testProcessFindFormNoOwnersFound() throws Exception {
		client.get().uri("/owners?lastName=Unknown Surname").exchange().expectStatus()
				.isOk();
	}

	@Test
	public void testInitUpdateOwnerForm() throws Exception {
		Hooks.onOperatorDebug();
		client.get().uri("/owners/{ownerId}/edit", TEST_OWNER_ID).exchange()
				.expectStatus().isOk();
	}

	@Test
	public void testProcessUpdateOwnerFormSuccess() throws Exception {
		client.post().uri("/owners/{ownerId}/edit", TEST_OWNER_ID)
				.body(fromFormData("firstName", "Joe").with("lastName", "Bloggs")
						.with("address", "123 Caramel Street").with("city", "London")
						.with("telephone", "01616291589"))
				.exchange().expectStatus().is3xxRedirection();
	}

	@Test
	public void testProcessUpdateOwnerFormHasErrors() throws Exception {
		client.post().uri("/owners/{ownerId}/edit", TEST_OWNER_ID)
				.body(fromFormData("firstName", "Joe").with("lastName", "Bloggs")
						.with("city", "London"))
				.exchange().expectStatus().isOk();
	}

	// TODO Add Hamcrest reflection config, required for org.hamcrest.internal.ReflectiveTypeFinder
	//@Test
	public void testShowOwner() throws Exception {
		client.get().uri("/owners/{ownerId}", TEST_OWNER_ID).exchange().expectStatus()
				.isOk().expectBody(String.class)
				.value(Matchers.containsString("Address"))
				.value(Matchers.containsString("6085551023"))
				.value(Matchers.containsString("Madison"))
				.value(Matchers.containsString("Max"));
	}

	@TestConfiguration
	static class OwnerControllerTestConfiguration {

		Pet max;

		Owner george;

		public OwnerControllerTestConfiguration() {
			max = new Pet();
			PetType dog = new PetType();
			dog.setName("dog");
			max.setId(1);
			max.setType(dog);
			max.setName("Max");
			max.setBirthDate(LocalDate.now());

			george = new Owner();
			george.setId(TEST_OWNER_ID);
			george.setFirstName("George");
			george.setLastName("Franklin");
			george.setAddress("110 W. Liberty St.");
			george.setCity("Madison");
			george.setTelephone("6085551023");
			george.setPetsInternal(Collections.singleton(max));
		}

		@Bean
		VisitRepository visits() {
			Visit visit = new Visit();
			visit.setDate(LocalDate.now());
			return new VisitRepository() {
				@Override
				public void save(Visit visit) throws DataAccessException {
				}
				@Override
				public List<Visit> findByPetId(Integer petId) {
					if (petId.equals(max.getId())) {
						return Collections.singletonList(visit);
					}
					return null;
				}
			};
		}

		@Bean
		OwnerRepository owners() {
			return new OwnerRepository() {
				@Override
				public Collection<Owner> findByLastName(String lastName) {
					if (lastName.equals("")) {
						return Lists.newArrayList(george, new Owner());
					}
					else if (lastName.equals(george.getLastName())) {
						return Lists.newArrayList(george);
					}
					return Lists.emptyList();
				}
				@Override
				public Owner findById(int id) {
					if (id == TEST_OWNER_ID) {
						return george;
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

package org.springframework.samples.petclinic.owner;

import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.BDDMockito.given;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

import java.time.LocalDate;
import java.util.Collections;

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

	@MockBean
	private OwnerRepository owners;

    @MockBean
    private VisitRepository visits;

	private Owner george;

	@BeforeEach
	public void setup() {
		george = new Owner();
		george.setId(TEST_OWNER_ID);
		george.setFirstName("George");
		george.setLastName("Franklin");
		george.setAddress("110 W. Liberty St.");
		george.setCity("Madison");
		george.setTelephone("6085551023");
        Pet max = new Pet();
        PetType dog = new PetType();
        dog.setName("dog");
        max.setId(1);
        max.setType(dog);
        max.setName("Max");
        max.setBirthDate(LocalDate.now());
        george.setPetsInternal(Collections.singleton(max));
        given(this.owners.findById(TEST_OWNER_ID)).willReturn(george);
        Visit visit = new Visit();
        visit.setDate(LocalDate.now());
        given(this.visits.findByPetId(max.getId())).willReturn(Collections.singletonList(visit));
	}

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
		given(this.owners.findByLastName(""))
				.willReturn(Lists.newArrayList(george, new Owner()));
		client.get().uri("/owners").exchange().expectStatus().isOk();
	}

	@Test
	public void testProcessFindFormByLastName() throws Exception {
		given(this.owners.findByLastName(george.getLastName()))
				.willReturn(Lists.newArrayList(george));
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

	@Test
	public void testShowOwner() throws Exception {
		client.get().uri("/owners/{ownerId}", TEST_OWNER_ID).exchange().expectStatus()
				.isOk().expectBody(String.class)
				.value(Matchers.containsString("Address"))
				.value(Matchers.containsString("6085551023"))
				.value(Matchers.containsString("Madison"))
				.value(Matchers.containsString("Max"))
				;
	}

}

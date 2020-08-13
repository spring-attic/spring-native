package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.BDDMockito.given;
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

	@MockBean
	private VisitRepository visits;

	@MockBean
	private PetRepository pets;

	@BeforeEach
	public void init() {
		given(this.pets.findById(TEST_PET_ID)).willReturn(new Pet());
	}

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

}

package org.springframework.samples.petclinic.vet;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.BDDMockito.given;

/**
 * Test class for the {@link VetController}
 */
@WebFluxTest(controllers = VetController.class)
@ImportAutoConfiguration(ThymeleafAutoConfiguration.class)
public class VetControllerTests {

	@Autowired
	private WebTestClient client;

	@MockBean
	private VetRepository vets;

	@BeforeEach
	public void setup() {
		Vet james = new Vet();
		james.setFirstName("James");
		james.setLastName("Carter");
		james.setId(1);
		Vet helen = new Vet();
		helen.setFirstName("Helen");
		helen.setLastName("Leary");
		helen.setId(2);
		Specialty radiology = new Specialty();
		radiology.setId(1);
		radiology.setName("radiology");
		helen.addSpecialty(radiology);
		given(this.vets.findAll()).willReturn(Lists.newArrayList(james, helen));
	}

	@Test
	public void testShowVetListHtml() throws Exception {
		client.get().uri("/vets.html").exchange().expectStatus().isOk();
	}

	@Test
	public void testShowResourcesVetList() throws Exception {
		client.get().uri("/vets").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$.vetList[0].id", 1);
	}

}

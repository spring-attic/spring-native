package org.springframework.samples.petclinic.owner;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.BDDMockito.given;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

/**
 * Test class for the {@link PetController}
 *
 * @author Colin But
 */
@WebFluxTest(value = PetController.class, includeFilters = @ComponentScan.Filter(value = PetTypeFormatter.class, type = FilterType.ASSIGNABLE_TYPE))
@ImportAutoConfiguration(ThymeleafAutoConfiguration.class)
public class PetControllerTests {

    private static final int TEST_OWNER_ID = 1;
    private static final int TEST_PET_ID = 1;

    @Autowired
    private WebTestClient client;

    @MockBean
    private PetRepository pets;

    @MockBean
    private OwnerRepository owners;

    @BeforeEach
    public void setup() {
        PetType cat = new PetType();
        cat.setId(3);
        cat.setName("hamster");
        given(this.pets.findPetTypes()).willReturn(Lists.newArrayList(cat));
        given(this.owners.findById(TEST_OWNER_ID)).willReturn(new Owner());
        given(this.pets.findById(TEST_PET_ID)).willReturn(new Pet());

    }

    @Test
    public void testInitCreationForm() throws Exception {
        client.get().uri("/owners/{ownerId}/pets/new", TEST_OWNER_ID).exchange()
                .expectStatus().isOk();
    }

    @Test
    public void testProcessCreationFormSuccess() throws Exception {
        client.post().uri("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
                .body(fromFormData("name", "Betty").with("type", "hamster")
                        .with("birthDate", "2015-02-12"))
                .exchange().expectStatus().is3xxRedirection();
    }

    @Test
    public void testProcessCreationFormHasErrors() throws Exception {
        client.post().uri("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
                .body(fromFormData("name", "Betty").with("birthDate", "2015-02-12"))
                .exchange().expectStatus().isOk();
    }

    @Test
    public void testInitUpdateForm() throws Exception {
        client.get()
                .uri("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
                .exchange().expectStatus().isOk();
    }

    @Test
    public void testProcessUpdateFormSuccess() throws Exception {
        client.post()
                .uri("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
                .body(fromFormData("name", "Betty").with("type", "hamster")
                        .with("birthDate", "2015-02-12"))
                .exchange().expectStatus().is3xxRedirection();
    }

    @Test
    public void testProcessUpdateFormHasErrors() throws Exception {
        client.post()
                .uri("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
                .body(fromFormData("name", "Betty").with("birthDate", "2015/02/12"))
                .exchange().expectStatus().isOk();
    }

}

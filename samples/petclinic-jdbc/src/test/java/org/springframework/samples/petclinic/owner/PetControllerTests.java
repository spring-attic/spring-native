package org.springframework.samples.petclinic.owner;

import java.util.Collection;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.reactive.server.WebTestClient;

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

    @Autowired
    private PetRepository pets;

    @Autowired
    private OwnerRepository owners;

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

        @Bean
        OwnerRepository owners() {
            return new OwnerRepository() {
                @Override
                public Collection<Owner> findByLastName(String lastName) {
                    return Lists.emptyList();
                }

                @Override
                public Owner findById(int id) {
                    if (id == TEST_OWNER_ID) {
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

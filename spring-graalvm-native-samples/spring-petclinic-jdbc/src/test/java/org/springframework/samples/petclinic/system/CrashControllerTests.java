package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Test class for {@link CrashController}
 *
 * @author Colin But
 */
// Waiting https://github.com/spring-projects/spring-boot/issues/5574
@Disabled
@WebFluxTest(controllers = CrashController.class)
@ImportAutoConfiguration(ThymeleafAutoConfiguration.class)
public class CrashControllerTests {

    @Autowired
    private WebTestClient client;

    @Test
    public void testTriggerException() throws Exception {
        client.get().uri("/oups").exchange().expectStatus().isOk();
    }
}

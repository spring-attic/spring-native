package com.example.webflux.test;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SecuringWebfluxApplicationTests {

    private WebTestClient rest;

    @Autowired
    public void setup(ApplicationContext context) {
        this.rest = WebTestClient
                .bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @Test
    public void anonymousWorksWithoutLogin() throws Exception {
        this.rest
                .get()
                .uri("/rest/anonymous")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void authorizedDoesntWorkWithoutLogin() throws Exception {
        this.rest
                .get()
                .uri("/rest/authorized")
                .exchange()
                .expectStatus().isEqualTo(401);
    }

    @Test
    @WithMockUser(username = "user", password = "password")
    public void authorizedWorksWithLogin() throws Exception {
        this.rest
                .get()
                .uri("/rest/authorized")
                .exchange()
                .expectStatus().isEqualTo(200);
    }

    @Test
    public void adminDoesntWorkWithoutLogin() throws Exception {
        this.rest
                .get()
                .uri("/rest/admin")
                .exchange()
                .expectStatus().isEqualTo(401);
    }

    @Test
    @WithMockUser(username = "user", password = "password")
    public void adminDoesntWorkWithWrongLogin() throws Exception {
        this.rest
                .get()
                .uri("/rest/admin")
                .exchange()
                .expectStatus().isEqualTo(403);
    }

    @Test
    @WithMockUser(username = "admin", password = "password", roles = "ADMIN")
    public void adminWorksWithLogin() throws Exception {
        this.rest
                .get()
                .uri("/rest/admin")
                .exchange()
                .expectStatus().isEqualTo(200);
    }
}

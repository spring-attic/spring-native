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
    public void accessUnsecuredResourceThenOk() throws Exception {
        this.rest
                .get()
                .uri("/")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void accessSecuredResourceUnauthenticatedThenRedirectsToLogin() throws Exception {
        this.rest
                .get()
                .uri("/hello")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    public void accessSecuredResourceAuthenticatedThenOk() throws Exception {
        this.rest
                .get()
                .uri("/hello")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser
    public void accessAdminPageAsUserThenForbidden() throws Exception {
        this.rest
                .get()
                .uri("/admin")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void accessAdminPageAsAdminThenOk() throws Exception {
        this.rest
                .get()
                .uri("/admin")
                .exchange()
                .expectStatus().isOk();
    }

}

package com.example.webflux.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
class WebfluxSliceTests {

    @Autowired
    private WebTestClient client;

    @Test
    void check() {
        this.client.get().uri("/").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("hi!");
    }

}

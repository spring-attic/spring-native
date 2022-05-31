package com.example.webflux.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class RandomPortTests {

    @LocalServerPort
    int port;

    @Test
    void check(@Autowired WebTestClient client) {
        assertThat(this.port).isNotZero();

        client.get().uri("/").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("hi!");
    }

}

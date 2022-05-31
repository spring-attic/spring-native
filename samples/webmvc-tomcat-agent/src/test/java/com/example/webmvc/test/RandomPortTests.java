package com.example.webmvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"foo.name=FOO", "bar.name=BAR"})
class RandomPortTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;

    @Value("${foo.name}")
    private String fooName;

    @Test
    void check(@Autowired Environment env) {
        assertThat(this.port).isNotZero();

        ResponseEntity<String> response = this.template.getForEntity("/", String.class);
        assertThat(response.getBody()).isEqualTo("Hello from Spring MVC and Tomcat");

        assertThat(this.fooName).isEqualTo("FOO");
        assertThat(env.getProperty("bar.name")).isEqualTo("BAR");
    }

}

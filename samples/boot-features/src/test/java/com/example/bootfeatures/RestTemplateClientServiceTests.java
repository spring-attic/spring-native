package com.example.bootfeatures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

@RestClientTest(RestTemplateClientService.class)
class RestTemplateClientServiceTests {

    @Autowired
    RestTemplateClientService client;

    @Autowired
    MockRestServiceServer server;

    @Test
    void get() {
        this.server.expect(requestTo("/foo")).andRespond(withSuccess("FOO", MediaType.TEXT_PLAIN));
        String result = this.client.get("/foo");
        this.server.verify();
        assertThat(result).isEqualTo("FOO");
    }

}

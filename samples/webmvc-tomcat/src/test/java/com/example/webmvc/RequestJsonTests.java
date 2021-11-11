package com.example.webmvc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@Disabled("Failed in JVM AOT mode")
@JsonTest
class RequestJsonTests {

    @Autowired
    JacksonTester<Request> tester;

    @Test
    void parse() throws Exception {
        this.tester.parse("{\"message\":\"Hi\"}")
                .assertThat().extracting(Request::getMessage).isEqualTo("Hi");
    }

    @Test
    void write() throws Exception {
        Request request = new Request();
        request.setMessage("Hello");
        assertThat(this.tester.write(request))
                .extractingJsonPathValue("message").isEqualTo("Hello");
    }

}

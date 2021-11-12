package com.example.graalvmdemo.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class RestControllerIntegrationTests {

	@Autowired
	TestRestTemplate template;

	/**
	 * See {@code verify.sh} in root of project.
	 */
	@ParameterizedTest
	@CsvSource({"/with, hello1", "/without, hello2"})
	void restControllers(String path, String expectedResponse) {
		ResponseEntity<String> response = this.template.getForEntity(path, String.class);
		assertThat(response.getBody()).isEqualTo(expectedResponse);
	}

}

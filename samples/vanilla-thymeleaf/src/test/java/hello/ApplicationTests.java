/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hello;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = GreetingController.class)
public class ApplicationTests {

	@Autowired
	private WebTestClient client;

	@Test
	public void homePage() throws Exception {
		// N.B. jsoup can be useful for asserting HTML content
		client.get().uri("/index.html").exchange().expectBody(String.class).consumeWith(
				content -> content.getResponseBody().contains("Get your greeting"));
	}

	@Test
	public void greeting() throws Exception {
		client.get().uri("/greeting").exchange().expectBody(String.class).consumeWith(
				content -> content.getResponseBody().contains("Hello, World!"));
	}

	@Test
	public void greetingWithUser() throws Exception {
		client.get().uri("/greeting?name={name}", "Greg").exchange()
				.expectBody(String.class).consumeWith(
						content -> content.getResponseBody().contains("Hello, Greg!"));
	}

}

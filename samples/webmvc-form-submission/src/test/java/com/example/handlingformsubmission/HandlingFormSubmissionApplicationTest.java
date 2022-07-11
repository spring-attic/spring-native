/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.handlingformsubmission;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GreetingController.class)
@TestPropertySource(properties = "logging.level.org.springframework.web=DEBUG")
public class HandlingFormSubmissionApplicationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void rendersForm() throws Exception {
		mockMvc.perform(get("/greeting"))
				.andExpect(content().string(containsString("Form")));
	}

	@Test
	public void submitsForm() throws Exception {
		mockMvc.perform(post("/greeting").param("id", "12345").param("content", "Hello"))
				.andExpect(content().string(containsString("Result")))
				.andExpect(content().string(containsString("id: 12345")));
	}

}

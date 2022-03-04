package com.example.methodsecurity;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MethodSecurityApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	public void accessSecuredResourceUnauthenticatedThenUnauthorized() throws Exception {
		mockMvc.perform(get("/hello"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser
	public void accessSecuredResourceAuthenticatedThenOk() throws Exception {
		mockMvc.perform(get("/hello"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void accessAdminPageAsUserThenForbidden() throws Exception {
		mockMvc.perform(get("/admin/hello"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void accessAdminPageAsAdminThenOk() throws Exception {
		mockMvc.perform(get("/admin/hello"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void accessHelloFilterThenOk() throws Exception {
		mockMvc.perform(get("/filter/hello"))
				.andExpect(status().isOk())
				.andExpect(content().string("Hello"));
	}
}

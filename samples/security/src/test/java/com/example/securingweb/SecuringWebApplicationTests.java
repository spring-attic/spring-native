package com.example.securingweb;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecuringWebApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
    public void anonymousWorksWithoutLogin() throws Exception {
        mockMvc.perform(get("/rest/anonymous"))
                .andExpect(status().isOk());
    }

    @Test
    public void authorizedDoesntWorkWithoutLogin() throws Exception {
        mockMvc.perform(get("/rest/authorized"))
                .andExpect(status().is(401));
    }

    @Test
    @WithMockUser(username = "user", password = "password")
    public void authorizedWorksWithLogin() throws Exception {
        mockMvc.perform(get("/rest/authorized"))
                .andExpect(status().is(200));
    }

    @Test
    public void adminDoesntWorkWithoutLogin() throws Exception {
        mockMvc.perform(get("/rest/admin"))
                .andExpect(status().is(401));
    }

    @Test
    @WithMockUser(username = "user", password = "password")
    public void adminDoesntWorkWithWrongLogin() throws Exception {
        mockMvc.perform(get("/rest/admin"))
                .andExpect(status().is(403));
    }

    @Test
    @WithMockUser(username = "admin", password = "password", roles = "ADMIN")
    public void adminWorksWithLogin() throws Exception {
        mockMvc.perform(get("/rest/admin"))
                .andExpect(status().is(200));
    }

}

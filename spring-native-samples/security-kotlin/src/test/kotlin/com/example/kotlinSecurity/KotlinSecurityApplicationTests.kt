package com.example.kotlinSecurity

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
class KotlinSecurityApplicationTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `protected page returns 401`() {
        this.mockMvc.get("/")
                .andExpect {
                    status { isUnauthorized() }
                }
    }

    @Test
    fun `valid user permitted to log in`() {
        this.mockMvc.perform(formLogin("/login").user("user").password("password"))
                .andExpect(authenticated())
    }

    @Test
    fun `invalid user not permitted to log in`() {
        this.mockMvc.perform(formLogin("/login").user("invalid").password("invalid"))
                .andExpect(unauthenticated())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection)
    }

    @Test
    fun `logged in user can access protected page`() {
        val mvcResult = this.mockMvc.perform(formLogin("/login").user("user").password("password"))
                .andExpect(authenticated()).andReturn()

        val httpSession = mvcResult.request.getSession(false) as MockHttpSession

        this.mockMvc.get("/") {
            session = httpSession
        }.andExpect {
            status { isOk() }
        }
    }
}

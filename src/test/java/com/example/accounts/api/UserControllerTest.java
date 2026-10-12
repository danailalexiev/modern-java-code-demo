package com.example.accounts.api;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Happy path only. The Act 0 bug (upgrading a user with no payment method
 * returns 500) deliberately has no test covering it yet.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getReturnsExistingUser() throws Exception {
        mockMvc.perform(get("/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email", is("alice@example.com")));
    }

    @Test
    void upgradeSucceedsForActiveUserWithPaymentMethod() throws Exception {
        mockMvc.perform(post("/users/1/upgrade")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"plan\":\"PRO\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.plan", is("PRO")));
    }
}

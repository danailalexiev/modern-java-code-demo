package com.example.accounts.api;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getReturnsExistingAccount() throws Exception {
        mockMvc.perform(get("/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email", is("alice@example.com")));
    }

    @Test
    void getOnAMissingUserIsNotFound() throws Exception {
        mockMvc.perform(get("/users/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void upgradeSucceedsForActiveUserWithPaymentMethod() throws Exception {
        mockMvc.perform(post("/users/1/upgrade")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"plan\":\"PRO\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.plan", is("PRO")));
    }

    @Test
    void upgradeIsRejectedWithoutAPaymentMethod() throws Exception {
        mockMvc.perform(post("/users/2/upgrade")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"plan\":\"PRO\"}"))
            .andExpect(status().isPaymentRequired());
    }

    @Test
    void upgradeIsRejectedForASuspendedUser() throws Exception {
        mockMvc.perform(post("/users/3/upgrade")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"plan\":\"PRO\"}"))
            .andExpect(status().isConflict());
    }
}

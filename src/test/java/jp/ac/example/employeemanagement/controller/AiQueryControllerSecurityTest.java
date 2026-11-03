package jp.ac.example.employeemanagement.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiQueryControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedApiCallReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/ai/query").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"社員数を教えてください\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void disabledAiReturnsServiceUnavailableWithoutAnApiKey() throws Exception {
        mockMvc.perform(post("/api/ai/query").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"在籍社員数を教えてください\"}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void oversizedQuestionIsRejectedByTheApi() throws Exception {
        String question = "a".repeat(501);
        mockMvc.perform(post("/api/ai/query").with(csrf()).with(user("viewer").roles("VIEWER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"" + question + "\"}"))
                .andExpect(status().isBadRequest());
    }
}

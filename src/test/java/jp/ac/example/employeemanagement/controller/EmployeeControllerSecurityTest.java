package jp.ac.example.employeemanagement.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedVisitorsAreRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern("**/login"));
    }

    @Test
    void viewerCanReadButCannotOpenWriteFormsOrPostChanges() throws Exception {
        mockMvc.perform(get("/employees").with(user("viewer").roles("VIEWER")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/departments").with(user("viewer").roles("VIEWER")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/employees/new").with(user("viewer").roles("VIEWER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/employees").with(user("viewer").roles("VIEWER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/departments").with(user("viewer").roles("VIEWER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanOpenEmployeeForm() throws Exception {
        mockMvc.perform(get("/employees/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminStillNeedsCsrfForWriteRequests() throws Exception {
        mockMvc.perform(post("/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginPageIsAvailableToAnonymousVisitors() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
}

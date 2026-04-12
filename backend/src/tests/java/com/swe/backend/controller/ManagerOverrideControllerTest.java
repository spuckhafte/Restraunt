package com.swe.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.swe.backend.auth.AuthRequestContext;
import com.swe.backend.auth.AuthRole;
import com.swe.backend.auth.SessionPrincipal;
import com.swe.backend.model.SimpleMessage;
import com.swe.backend.service.ManagerOverrideService;

@WebMvcTest(controllers = ManagerOverrideController.class)
class ManagerOverrideControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerOverrideService managerOverrideService;

    @Test
    void assumeRole_returnsMessage() throws Exception {
        SessionPrincipal principal = new SessionPrincipal(1L, "manager", AuthRole.MANAGER, AuthRole.MANAGER, "token123");
        when(managerOverrideService.assumeRole(principal, "SALES"))
            .thenReturn(new SimpleMessage("Role switched to SALES"));

        mockMvc.perform(post("/api/manager/override/assume/SALES")
                .requestAttr(AuthRequestContext.ATTR_PRINCIPAL, principal))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Role switched to SALES"));
    }

    @Test
    void restoreRole_returnsMessage() throws Exception {
        SessionPrincipal principal = new SessionPrincipal(1L, "manager", AuthRole.SALES, AuthRole.MANAGER, "token123");
        when(managerOverrideService.restoreRole(principal))
            .thenReturn(new SimpleMessage("Role restored to MANAGER"));

        mockMvc.perform(post("/api/manager/override/restore")
                .requestAttr(AuthRequestContext.ATTR_PRINCIPAL, principal))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Role restored to MANAGER"));
    }
}

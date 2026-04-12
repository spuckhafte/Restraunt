package com.swe.backend.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.swe.backend.auth.AuthRequestContext;
import com.swe.backend.auth.AuthRole;
import com.swe.backend.auth.SessionPrincipal;
import com.swe.backend.model.AuthUserDto;
import com.swe.backend.model.LoginResponse;
import com.swe.backend.service.AuthService;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void login_returnsTokenAndUser() throws Exception {
        when(authService.login("manager", "manager123"))
            .thenReturn(new LoginResponse("token123", new AuthUserDto(1L, "manager", "MANAGER")));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "manager",
                      "password": "manager123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionToken").value("token123"))
            .andExpect(jsonPath("$.user.role").value("MANAGER"));
    }

    @Test
    void logout_returnsOkMessage() throws Exception {
        doNothing().when(authService).logout("token123");

        mockMvc.perform(post("/api/auth/logout")
                .header("X-Session-Token", "token123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out"));
    }

    @Test
    void me_returnsCurrentUser() throws Exception {
        mockMvc.perform(get("/api/auth/me")
            .requestAttr(AuthRequestContext.ATTR_PRINCIPAL, new SessionPrincipal(1L, "manager", AuthRole.MANAGER, AuthRole.MANAGER, "token123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("manager"))
            .andExpect(jsonPath("$.role").value("MANAGER"));
    }
}

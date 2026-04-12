package com.swe.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.swe.backend.auth.AuthRequestContext;
import com.swe.backend.auth.AuthRole;
import com.swe.backend.auth.SessionPrincipal;
import com.swe.backend.model.CheckPaymentDto;
import com.swe.backend.service.PaymentsService;

@WebMvcTest(controllers = PaymentsController.class)
class PaymentsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentsService paymentsService;

    @Test
    void generateCheck_returnsCreated() throws Exception {
        SessionPrincipal principal = new SessionPrincipal(1L, "manager", AuthRole.MANAGER, AuthRole.MANAGER, "token123");
        when(paymentsService.generateSupplierCheck(10L, 1L))
            .thenReturn(new CheckPaymentDto(10L, "CHK-1", 700.0, Instant.now(), "abc", 49300.0));

        mockMvc.perform(post("/api/payments/checks/generate")
                .requestAttr(AuthRequestContext.ATTR_PRINCIPAL, principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"invoiceId\":10}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.checkNumber").value("CHK-1"));
    }
}

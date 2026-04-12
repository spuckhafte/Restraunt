package com.swe.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.swe.backend.auth.AuthRequestContext;
import com.swe.backend.auth.SessionPrincipal;
import com.swe.backend.model.CheckPaymentDto;
import com.swe.backend.model.request.GenerateCheckRequest;
import com.swe.backend.service.PaymentsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {
    private final PaymentsService paymentsService;

    public PaymentsController(PaymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }

    @PostMapping("/checks/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public CheckPaymentDto generateCheck(
        @Valid @RequestBody GenerateCheckRequest request,
        HttpServletRequest servletRequest
    ) {
        SessionPrincipal principal = (SessionPrincipal) servletRequest.getAttribute(AuthRequestContext.ATTR_PRINCIPAL);
        long generatedBy = principal == null ? 0L : principal.userId();
        return paymentsService.generateSupplierCheck(request.invoiceId(), generatedBy);
    }
}

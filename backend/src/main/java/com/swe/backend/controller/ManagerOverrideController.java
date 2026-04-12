package com.swe.backend.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swe.backend.auth.AuthRequestContext;
import com.swe.backend.auth.SessionPrincipal;
import com.swe.backend.model.SimpleMessage;
import com.swe.backend.service.ManagerOverrideService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/manager/override")
public class ManagerOverrideController {
    private final ManagerOverrideService managerOverrideService;

    public ManagerOverrideController(ManagerOverrideService managerOverrideService) {
        this.managerOverrideService = managerOverrideService;
    }

    @PostMapping("/assume/{role}")
    public SimpleMessage assumeRole(@PathVariable String role, HttpServletRequest request) {
        SessionPrincipal principal = (SessionPrincipal) request.getAttribute(AuthRequestContext.ATTR_PRINCIPAL);
        return managerOverrideService.assumeRole(principal, role);
    }

    @PostMapping("/restore")
    public SimpleMessage restoreRole(HttpServletRequest request) {
        SessionPrincipal principal = (SessionPrincipal) request.getAttribute(AuthRequestContext.ATTR_PRINCIPAL);
        return managerOverrideService.restoreRole(principal);
    }
}

package com.swe.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.swe.backend.auth.AuthRequestContext;
import com.swe.backend.auth.SessionPrincipal;
import com.swe.backend.model.AuthUserDto;
import com.swe.backend.model.LoginResponse;
import com.swe.backend.model.SimpleMessage;
import com.swe.backend.model.request.LoginRequest;
import com.swe.backend.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.username(), request.password());
    }

    @PostMapping("/logout")
    public SimpleMessage logout(@RequestHeader(AuthRequestContext.HEADER_SESSION_TOKEN) String token) {
        authService.logout(token);
        return new SimpleMessage("Logged out");
    }

    @GetMapping("/me")
    public AuthUserDto me(HttpServletRequest request) {
        SessionPrincipal principal = (SessionPrincipal) request.getAttribute(AuthRequestContext.ATTR_PRINCIPAL);
        if (principal == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Unauthorized");
        }
        return new AuthUserDto(principal.userId(), principal.username(), principal.role().name());
    }
}

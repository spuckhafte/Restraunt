package com.swe.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.swe.backend.auth.AuthRole;
import com.swe.backend.auth.SessionPrincipal;
import com.swe.backend.model.SimpleMessage;
import com.swe.backend.repository.AuthRepository;

@Service
public class ManagerOverrideService {
    private final AuthRepository authRepository;

    public ManagerOverrideService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public SimpleMessage assumeRole(SessionPrincipal principal, String role) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (principal.baseRole() != AuthRole.MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only manager can switch operational role");
        }

        AuthRole targetRole = AuthRole.fromDb(role);
        authRepository.updateSessionRole(principal.token(), targetRole.name());
        authRepository.logManagerOverride(
            principal.userId(),
            principal.token(),
            principal.role().name(),
            targetRole.name(),
            "ASSUME_ROLE"
        );

        return new SimpleMessage("Role switched to " + targetRole.name());
    }

    public SimpleMessage restoreRole(SessionPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (principal.baseRole() != AuthRole.MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only manager can restore role");
        }

        authRepository.updateSessionRole(principal.token(), principal.baseRole().name());
        authRepository.logManagerOverride(
            principal.userId(),
            principal.token(),
            principal.role().name(),
            principal.baseRole().name(),
            "RESTORE_ROLE"
        );

        return new SimpleMessage("Role restored to " + principal.baseRole().name());
    }
}

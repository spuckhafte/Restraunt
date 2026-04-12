package com.swe.backend.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.swe.backend.auth.AuthRole;
import com.swe.backend.auth.SessionPrincipal;
import com.swe.backend.model.AuthSessionDto;
import com.swe.backend.model.AuthUserDto;
import com.swe.backend.model.LoginResponse;
import com.swe.backend.repository.AuthRepository;

@Service
public class AuthService {
    private final AuthRepository authRepository;

    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LoginResponse login(String username, String password) {
        AuthUserDto user = authRepository.findUserByUsernameAndPassword(username.trim(), password)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        authRepository.createSession(user.id(), token, user.role());
        return new LoginResponse(token, user);
    }

    public void logout(String token) {
        boolean invalidated = authRepository.invalidateSession(token);
        if (!invalidated) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session token is invalid or already expired");
        }
    }

    public SessionPrincipal validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing session token");
        }

        AuthSessionDto session = authRepository.findActiveSession(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session token is invalid"));

        AuthRole baseRole = AuthRole.fromDb(session.user().role());
        String effectiveRoleRaw = session.effectiveRole() == null || session.effectiveRole().isBlank()
            ? session.user().role()
            : session.effectiveRole();
        AuthRole effectiveRole = AuthRole.fromDb(effectiveRoleRaw);

        return new SessionPrincipal(
            session.user().id(),
            session.user().username(),
            effectiveRole,
            baseRole,
            session.token()
        );
    }

    public void ensureRole(SessionPrincipal principal, Set<AuthRole> allowed) {
        if (!allowed.contains(principal.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for role: " + principal.role());
        }
    }
}

package com.swe.backend.auth;

import java.util.Set;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import com.swe.backend.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService authService;
    private final AuthPolicyService authPolicyService;

    public AuthInterceptor(AuthService authService, AuthPolicyService authPolicyService) {
        this.authService = authService;
        this.authPolicyService = authPolicyService;
    }

    @Override
    public boolean preHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler
    ) {
        String token = request.getHeader(AuthRequestContext.HEADER_SESSION_TOKEN);
        SessionPrincipal principal = authService.validateToken(token);

        String path = request.getRequestURI();
        String method = request.getMethod();

        Set<AuthRole> allowed = authPolicyService.resolveAllowedRoles(path, method);
        if (allowed != null) {
            boolean isManagerEndpoint = path.startsWith("/api/manager");
            if (isManagerEndpoint && principal.baseRole() == AuthRole.MANAGER) {
                request.setAttribute(AuthRequestContext.ATTR_PRINCIPAL, principal);
                return true;
            }
            authService.ensureRole(principal, allowed);
        }

        request.setAttribute(AuthRequestContext.ATTR_PRINCIPAL, principal);
        return true;
    }
}

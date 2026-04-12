package com.swe.backend.auth;

import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
public class AuthPolicyService {
    private static final Set<AuthRole> MENU_READ = Set.of(AuthRole.MANAGER, AuthRole.SALES, AuthRole.INVENTORY);
    private static final Set<AuthRole> MENU_WRITE = Set.of(AuthRole.MANAGER);
    private static final Set<AuthRole> INVENTORY = Set.of(AuthRole.MANAGER, AuthRole.INVENTORY);
    private static final Set<AuthRole> SALES = Set.of(AuthRole.MANAGER, AuthRole.SALES);
    private static final Set<AuthRole> MANAGER_ONLY = Set.of(AuthRole.MANAGER);

    public Set<AuthRole> resolveAllowedRoles(String path, String method) {
        if (path.startsWith("/api/menu")) {
            return HttpMethod.GET.matches(method) ? MENU_READ : MENU_WRITE;
        }

        if (path.startsWith("/api/inventory")) {
            return INVENTORY;
        }

        if (path.startsWith("/api/sales")) {
            return SALES;
        }

        if (path.startsWith("/api/reports") || path.startsWith("/api/manager") || path.startsWith("/api/payments")) {
            return MANAGER_ONLY;
        }

        return null;
    }
}

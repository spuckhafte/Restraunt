package com.swe.backend.auth;

public record SessionPrincipal(
    long userId,
    String username,
    AuthRole role,
    AuthRole baseRole,
    String token
) {
}

package com.swe.backend.model;

import java.time.Instant;

public record AuthSessionDto(
    String token,
    AuthUserDto user,
    String effectiveRole,
    Instant createdAt,
    boolean active
) {
}

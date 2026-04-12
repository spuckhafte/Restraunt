package com.swe.backend.model;

public record LoginResponse(
    String sessionToken,
    AuthUserDto user
) {
}

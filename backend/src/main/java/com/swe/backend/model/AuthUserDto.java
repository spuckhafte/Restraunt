package com.swe.backend.model;

public record AuthUserDto(
    long id,
    String username,
    String role
) {
}

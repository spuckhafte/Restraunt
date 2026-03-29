package com.swe.backend.model;

public record MenuItemDto(
    String code,
    String name,
    double basePrice,
    boolean active
) {
}

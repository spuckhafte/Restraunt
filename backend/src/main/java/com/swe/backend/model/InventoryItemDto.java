package com.swe.backend.model;

public record InventoryItemDto(
    String code,
    String name,
    String unit,
    double quantityOnHand,
    double reorderThreshold
) {
}

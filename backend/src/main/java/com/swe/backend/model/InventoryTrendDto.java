package com.swe.backend.model;

public record InventoryTrendDto(
    String itemCode,
    String itemName,
    double issuedToday,
    double threeDayAverage,
    boolean flaggedUnusual
) {
}

package com.swe.backend.model;

public record ItemPerformanceDto(
    String itemCode,
    String itemName,
    long quantitySold,
    double revenue
) {
}

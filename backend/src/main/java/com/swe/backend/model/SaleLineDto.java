package com.swe.backend.model;

public record SaleLineDto(
    String itemCode,
    String itemName,
    double unitPrice,
    int quantity,
    double lineTotal
) {
}

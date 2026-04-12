package com.swe.backend.model;

public record SalesSummaryDto(
    String month,
    long billsCount,
    double totalSales
) {
}

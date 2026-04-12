package com.swe.backend.model;

public record LiquidityStatusDto(
    double cashBalance,
    long checksIssued,
    double totalCheckPayments
) {
}

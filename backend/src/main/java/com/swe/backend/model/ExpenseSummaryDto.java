package com.swe.backend.model;

public record ExpenseSummaryDto(
    long invoiceCount,
    double totalExpenses,
    double paidExpenses,
    double unpaidExpenses
) {
}

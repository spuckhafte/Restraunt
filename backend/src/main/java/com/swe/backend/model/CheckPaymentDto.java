package com.swe.backend.model;

import java.time.Instant;

public record CheckPaymentDto(
    long invoiceId,
    String checkNumber,
    double amount,
    Instant generatedAt,
    String pdfBase64,
    double cashBalanceAfterPayment
) {
}

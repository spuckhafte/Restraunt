package com.swe.backend.model;

import java.time.LocalDate;

public record SupplierInvoiceDto(
    long id,
    String supplierName,
    String itemCode,
    double quantity,
    double unitPrice,
    double totalAmount,
    LocalDate invoiceDate,
    boolean approved,
    boolean paid,
    boolean flaggedForReview
) {
}

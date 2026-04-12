package com.swe.backend.model.request;

import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSupplierInvoiceRequest(
    @NotBlank String supplierName,
    @NotBlank String itemCode,
    @DecimalMin(value = "0.0", inclusive = false) double quantity,
    @DecimalMin(value = "0.0", inclusive = false) double unitPrice,
    @NotNull LocalDate invoiceDate,
    boolean approved
) {
}

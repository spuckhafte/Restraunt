package com.swe.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SaleEntryRequest(
    @NotBlank String itemCode,
    @Positive int quantity
) {
}

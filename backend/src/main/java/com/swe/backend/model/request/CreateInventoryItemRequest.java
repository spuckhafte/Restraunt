package com.swe.backend.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record CreateInventoryItemRequest(
    @NotBlank String code,
    @NotBlank String name,
    @NotBlank String unit,
    @DecimalMin(value = "0.0", inclusive = true) double quantityOnHand
) {
}

package com.swe.backend.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record CreateMenuItemRequest(
    @NotBlank String code,
    @NotBlank String name,
    @DecimalMin(value = "0.0", inclusive = true) double basePrice
) {
}

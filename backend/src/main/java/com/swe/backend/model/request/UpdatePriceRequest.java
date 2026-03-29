package com.swe.backend.model.request;

import jakarta.validation.constraints.DecimalMin;

public record UpdatePriceRequest(
    @DecimalMin(value = "0.0", inclusive = true) double newPrice
) {
}

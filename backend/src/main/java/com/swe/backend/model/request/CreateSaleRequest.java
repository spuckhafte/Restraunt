package com.swe.backend.model.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CreateSaleRequest(
    @NotEmpty List<@Valid SaleEntryRequest> entries
) {
}

package com.swe.backend.model.request;

import jakarta.validation.constraints.Positive;

public record GenerateCheckRequest(
    @Positive long invoiceId
) {
}

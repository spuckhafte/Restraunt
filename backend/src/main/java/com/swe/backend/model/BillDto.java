package com.swe.backend.model;

import java.time.Instant;
import java.util.List;

public record BillDto(
    long id,
    List<SaleLineDto> lines,
    double subtotal,
    Instant createdAt,
    boolean voided
) {
}

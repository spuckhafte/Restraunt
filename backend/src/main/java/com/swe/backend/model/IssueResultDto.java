package com.swe.backend.model;

public record IssueResultDto(
    InventoryItemDto item,
    boolean flaggedUnusualConsumption
) {
}

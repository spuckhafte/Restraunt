package com.swe.core.modules.sales;

public class SaleEntry {
    public String itemCode;
    public int quantity;

    public SaleEntry(String itemCode, int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
        this.itemCode = itemCode;
        this.quantity = quantity;
    }
}

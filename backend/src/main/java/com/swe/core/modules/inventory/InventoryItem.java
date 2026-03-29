package com.swe.core.modules.inventory;

import java.util.ArrayList;

public class InventoryItem {
    public String code;
    public String name;
    public String unit;
    public double quantityOnHand;
    public double reorderThreshold;
    public ArrayList<Double> usageHistory;

    public InventoryItem(String code, String name, String unit, double quantityOnHand) {
        if (quantityOnHand < 0)
            throw new IllegalArgumentException("Quantity cannot be negative");
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.quantityOnHand = quantityOnHand;
        this.reorderThreshold = 0;
        this.usageHistory = new ArrayList<>();
    }

    public void receive(double quantity) {
        if (quantity < 0)
            throw new IllegalArgumentException("Quantity cannot be negative");
        this.quantityOnHand += quantity;
    }

    public void issue(double quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
        if (quantity > this.quantityOnHand)
            throw new IllegalArgumentException("Insufficient stock");
        this.quantityOnHand -= quantity;
    }

    public void logUsage(double quantity) {
        if (quantity < 0)
            throw new IllegalArgumentException("Quantity cannot be negative");
        this.usageHistory.add(quantity);
        if (this.usageHistory.size() > 3)
            this.usageHistory.remove(0);
        this.recalculateThreshold();
    }

    public double averageUsage() {
        if (this.usageHistory.isEmpty())
            return 0;
        double sum = 0;
        for (double val : this.usageHistory)
            sum += val;
        return sum / this.usageHistory.size();
    }

    private void recalculateThreshold() {
        double avg = this.averageUsage();
        this.reorderThreshold = avg * 2;
    }
}

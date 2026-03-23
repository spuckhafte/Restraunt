package com.swe.core.modules.menu;

import java.time.Instant;

public class MenuItem {
    String code;
    String name;
    double basePrice;
    TaxGroup taxGroup;
    boolean active;
    Instant lastUpdate;

    public MenuItem(String code, String name, double basePrice, TaxGroup taxGroup) {
        if (basePrice < 0)
            throw new IllegalArgumentException("Base price cannot be negative");
        this.code = code;
        this.name = name;
        this.basePrice = basePrice;
        this.taxGroup = taxGroup;
        this.active = true;
        this.lastUpdate = Instant.now();
    }

    public void updatePrice(double newPrice) {
        if (newPrice < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        this.basePrice = newPrice;
        this.lastUpdate = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.lastUpdate = Instant.now();
    }

    public String getName() {
        return this.name;
    }

    public double getBasePrice() {
        return this.basePrice;
    }

    public TaxGroup getTaxGroup() {
        return this.taxGroup;
    }

    public boolean isActive() {
        return this.active;
    }

    public String getCode() {
        return this.code;
    }
}
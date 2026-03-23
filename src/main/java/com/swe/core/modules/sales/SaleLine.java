package com.swe.core.modules.sales;

import com.swe.core.modules.menu.MenuItem;

public class SaleLine {
    public MenuItem item;
    public int quantity;
    public double lineTotal; // total amt of this bill line

    public SaleLine(MenuItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
        this.lineTotal = item.getBasePrice() * quantity;
    }
}

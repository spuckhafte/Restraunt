package com.swe.core.modules.sales;

import java.time.Instant;
import java.util.ArrayList;

public class Bill {
    public ArrayList<SaleLine> lines;
    public double subtotal;
    public Instant createdAt;
    public boolean voided;

    public Bill(ArrayList<SaleLine> lines, double subtotal) {
        this.lines = lines;
        this.subtotal = subtotal;
        this.createdAt = Instant.now();
        this.voided = false;
    }

    public void voidBill() {
        this.voided = true;
    }
}

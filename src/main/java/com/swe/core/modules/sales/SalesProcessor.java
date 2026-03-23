package com.swe.core.modules.sales;

import java.util.ArrayList;
import java.util.Optional;

import com.swe.core.modules.menu.Menu;
import com.swe.core.modules.menu.MenuItem;

public class SalesProcessor {
    public Bill processSale(Menu menu, ArrayList<SaleEntry> entries) {
        ArrayList<SaleLine> lines = new ArrayList<>();
        double subtotal = 0;

        for (SaleEntry entry : entries) {
            if (entry.quantity <= 0)
                throw new IllegalArgumentException("Quantity must be positive");

            Optional<MenuItem> item = menu.viewMenu()
                .stream()
                .filter(i -> i.getCode().equals(entry.itemCode))
                .findFirst();

            if (!item.isPresent())
                throw new IllegalArgumentException("Invalid item code");

            SaleLine line = new SaleLine(item.get(), entry.quantity);
            lines.add(line);
            subtotal += line.lineTotal;
        }

        return new Bill(lines, subtotal);
    }

    public void voidSale(Bill bill) {
        bill.voidBill();
    }
}

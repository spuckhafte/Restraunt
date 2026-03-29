package com.swe.core.ui.helpers;

import java.util.ArrayList;

import com.swe.core.modules.inventory.InventoryItem;
import com.swe.core.modules.menu.MenuItem;
import com.swe.core.modules.sales.Bill;

public class UiPrinter {
    private RestaurantContext context;

    public UiPrinter(RestaurantContext context) {
        this.context = context;
    }

    public void printMenu() {
        ArrayList<MenuItem> list = this.context.menu.viewMenu();
        if (list.isEmpty()) {
            System.out.println("No active items");
            return;
        }
        for (MenuItem item : list) {
            System.out.println(
                String.format("%s | %s | %.2f", item.getCode(), item.getName(), item.getBasePrice())
            );
        }
    }

    public void printBill(Bill bill) {
        System.out.println("--- Bill ---");
        for (int i = 0; i < bill.lines.size(); i++) {
            System.out.println(String.format("%s x%d = %.2f", bill.lines.get(i).item.getName(), bill.lines.get(i).quantity, bill.lines.get(i).lineTotal));
        }
        System.out.println(String.format("Subtotal: %.2f", bill.subtotal));
        System.out.println(String.format("Status: %s", bill.voided ? "VOID" : "ACTIVE"));
    }

    public void printInventory() {
        ArrayList<InventoryItem> list = this.context.inventoryManager.list();
        if (list.isEmpty()) {
            System.out.println("No inventory");
            return;
        }
        for (InventoryItem item : list) {
            System.out.println(String.format("%s | %s | %.2f %s | threshold %.2f", item.code, item.name, item.quantityOnHand, item.unit, item.reorderThreshold));
        }
    }

    public void printReorderAlerts() {
        ArrayList<InventoryItem> list = this.context.inventoryManager.list();
        boolean any = false;
        for (InventoryItem item : list) {
            if (item.quantityOnHand <= item.reorderThreshold) {
                any = true;
                System.out.println(String.format("Reorder: %s (%s) qty %.2f <= threshold %.2f", item.code, item.name, item.quantityOnHand, item.reorderThreshold));
            }
        }
        if (!any)
            System.out.println("No alerts");
    }
}

package com.swe.core.ui;

import java.io.BufferedReader;
import java.io.IOException;

import com.swe.core.modules.inventory.InventoryItem;
import com.swe.core.ui.helpers.RestaurantContext;
import com.swe.core.ui.helpers.UiPrinter;

public class InventoryUI implements RoleUI {
    private BufferedReader reader;
    private RestaurantContext context;
    private UiPrinter printer;

    public InventoryUI(BufferedReader reader, RestaurantContext context, UiPrinter printer) {
        this.reader = reader;
        this.context = context;
        this.printer = printer;
    }

    public void show() throws IOException {
        while (true) {
            System.out.println("Inventory Staff: 1) Add Item 2) Receive Shipment 3) Issue Ingredient 4) List Inventory 0) Back");
            String choice = this.reader.readLine();
            if (choice == null || choice.trim().equals("0"))
                return;
            try {
                if (choice.trim().equals("1"))
                    this.addInventoryItem();
                else if (choice.trim().equals("2"))
                    this.receiveInventory();
                else if (choice.trim().equals("3"))
                    this.issueInventory();
                else if (choice.trim().equals("4"))
                    this.printer.printInventory();
                else
                    System.out.println("Invalid choice");
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private void addInventoryItem() throws IOException {
        System.out.print("Code: ");
        String code = this.reader.readLine();
        System.out.print("Name: ");
        String name = this.reader.readLine();
        System.out.print("Unit: ");
        String unit = this.reader.readLine();
        System.out.print("Qty on hand: ");
        double qty = Double.parseDouble(this.reader.readLine());
        InventoryItem item = new InventoryItem(code, name, unit, qty);
        this.context.inventoryManager.addItem(item);
        System.out.println("Inventory item added");
    }

    private void receiveInventory() throws IOException {
        System.out.print("Code: ");
        String code = this.reader.readLine();
        System.out.print("Qty received: ");
        double qty = Double.parseDouble(this.reader.readLine());
        this.context.inventoryManager.receiveShipment(code, qty);
        System.out.println("Received");
    }

    private void issueInventory() throws IOException {
        System.out.print("Code: ");
        String code = this.reader.readLine();
        System.out.print("Qty issued: ");
        double qty = Double.parseDouble(this.reader.readLine());
        boolean flagged = this.context.inventoryManager.issueIngredient(code, qty);
        if (flagged)
            System.out.println("Unusual consumption flagged");
        else
            System.out.println("Issued");
    }
}

package com.swe.core.ui;

import java.io.BufferedReader;
import java.io.IOException;

import com.swe.core.modules.menu.MenuItem;
import com.swe.core.ui.helpers.RestaurantContext;
import com.swe.core.ui.helpers.UiPrinter;

public class ManagerUI implements RoleUI {
    private BufferedReader reader;
    private RestaurantContext context;
    private UiPrinter printer;

    public ManagerUI(BufferedReader reader, RestaurantContext context, UiPrinter printer) {
        this.reader = reader;
        this.context = context;
        this.printer = printer;
    }

    public void show() throws IOException {
        while (true) {
            System.out.println("Manager: 1) View Menu 2) Add Item 3) Update Price 4) Delete Item 5) Void Last Bill 0) Back");
            String choice = this.reader.readLine();
            if (choice == null || choice.trim().equals("0"))
                return;
            try {
                if (choice.trim().equals("1"))
                    this.printer.printMenu();
                else if (choice.trim().equals("2"))
                    this.addMenuItem();
                else if (choice.trim().equals("3"))
                    this.updateMenuPrice();
                else if (choice.trim().equals("4"))
                    this.deleteMenuItem();
                else if (choice.trim().equals("5"))
                    this.voidLastBill();
                else
                    System.out.println("Invalid choice");
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private void addMenuItem() throws IOException {
        System.out.print("Code: ");
        String code = this.reader.readLine();
        System.out.print("Name: ");
        String name = this.reader.readLine();
        System.out.print("Price: ");
        double price = Double.parseDouble(this.reader.readLine());
        MenuItem item = new MenuItem(code, name, price);
        this.context.menu.addItem(item);
        System.out.println("Added");
    }

    private void updateMenuPrice() throws IOException {
        System.out.print("Code: ");
        String code = this.reader.readLine();
        System.out.print("New Price: ");
        double price = Double.parseDouble(this.reader.readLine());
        this.context.menu.updatePrice(code, price);
        System.out.println("Updated");
    }

    private void deleteMenuItem() throws IOException {
        System.out.print("Code: ");
        String code = this.reader.readLine();
        this.context.menu.deleteItem(code);
        System.out.println("Deactivated");
    }

    private void voidLastBill() {
        if (this.context.lastBill == null) {
            System.out.println("No bill to void");
            return;
        }

        if (!this.context.lastBill.voided) {
            this.context.salesProcessor.voidSale(this.context.lastBill);
            System.out.println("Last bill voided");
        } else {
            System.out.println("Last bill was already voided");
        }
    }
}

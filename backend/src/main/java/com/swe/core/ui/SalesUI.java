package com.swe.core.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import com.swe.core.modules.sales.Bill;
import com.swe.core.modules.sales.SaleEntry;
import com.swe.core.ui.helpers.RestaurantContext;
import com.swe.core.ui.helpers.UiPrinter;

public class SalesUI implements RoleUI {
    private BufferedReader reader;
    private RestaurantContext context;
    private UiPrinter printer;

    public SalesUI(BufferedReader reader, RestaurantContext context, UiPrinter printer) {
        this.reader = reader;
        this.context = context;
        this.printer = printer;
    }

    public void show() throws IOException {
        while (true) {
            System.out.println("Salesclerk: 1) New Sale 2) View Menu 0) Back");
            String choice = this.reader.readLine();
            if (choice == null || choice.trim().equals("0"))
                return;
            try {
                if (choice.trim().equals("1"))
                    this.processSale();
                else if (choice.trim().equals("2"))
                    this.printer.printMenu();
                else
                    System.out.println("Invalid choice");
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private void processSale() throws IOException {
        ArrayList<SaleEntry> entries = new ArrayList<>();
        while (true) {
            System.out.print("Item code (blank to finish): ");
            String code = this.reader.readLine();
            if (code == null || code.trim().isEmpty())
                break;
            System.out.print("Quantity: ");
            int qty = Integer.parseInt(this.reader.readLine());
            entries.add(new SaleEntry(code.trim(), qty));
        }
        if (entries.isEmpty()) {
            System.out.println("No entries");
            return;
        }
        Bill bill = this.context.salesProcessor.processSale(this.context.menu, entries);
        this.context.lastBill = bill;
        this.printer.printBill(bill);
    }
}

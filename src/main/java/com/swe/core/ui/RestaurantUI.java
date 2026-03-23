package com.swe.core.ui;

import java.io.BufferedReader;
import java.io.IOException;

import com.swe.core.modules.menu.Menu;
import com.swe.core.modules.menu.MenuItem;
import com.swe.core.ui.helpers.RestaurantContext;
import com.swe.core.ui.helpers.UiPrinter;

public class RestaurantUI {
    private BufferedReader reader;
    private RestaurantContext context;
    private UiPrinter printer;
    private ManagerUI managerUI;
    private SalesUI salesUI;
    private InventoryUI inventoryUI;

    public RestaurantUI(BufferedReader reader) {
        this.reader = reader;
        this.context = new RestaurantContext(this.seedMenu());
        this.printer = new UiPrinter(this.context);
        this.managerUI = new ManagerUI(reader, this.context, this.printer);
        this.salesUI = new SalesUI(reader, this.context, this.printer);
        this.inventoryUI = new InventoryUI(reader, this.context, this.printer);
    }

    public void run() throws IOException {
        while (true) {
            System.out.println("Select role: 1) Manager 2) Salesclerk 3) Inventory Staff 0) Exit");
            String choice = this.reader.readLine();
            if (choice == null)
                break;
            if (choice.trim().equals("1"))
                this.managerUI.show();
            else if (choice.trim().equals("2"))
                this.salesUI.show();
            else if (choice.trim().equals("3"))
                this.inventoryUI.show();
            else if (choice.trim().equals("0"))
                return;
            else
                System.out.println("Invalid choice");
        }
    }

    private Menu seedMenu() {
        return new Menu(new MenuItem[] {
            new MenuItem("FD1", "Fried Dumplings", 60.0),
            new MenuItem("NV1", "Noodles Veg", 75.0),
            new MenuItem("DS1", "Dessert Special", 40.0)
        });
    }
}

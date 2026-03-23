package com.swe.core.ui.helpers;

import com.swe.core.modules.inventory.InventoryManager;
import com.swe.core.modules.menu.Menu;
import com.swe.core.modules.sales.Bill;
import com.swe.core.modules.sales.SalesProcessor;

public class RestaurantContext {
    public Menu menu;
    public SalesProcessor salesProcessor;
    public InventoryManager inventoryManager;
    public Bill lastBill;

    public RestaurantContext(Menu menu) {
        this.menu = menu;
        this.salesProcessor = new SalesProcessor();
        this.inventoryManager = new InventoryManager();
        this.lastBill = null;
    }
}

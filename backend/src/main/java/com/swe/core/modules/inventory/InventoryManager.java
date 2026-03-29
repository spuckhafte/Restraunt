package com.swe.core.modules.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InventoryManager {
    private Map<String, InventoryItem> items;

    public InventoryManager() {
        this.items = new HashMap<>();
    }

    public void addItem(InventoryItem item) {
        if (this.items.containsKey(item.code))
            throw new IllegalArgumentException("Duplicate inventory code");
        this.items.put(item.code, item);
    }

    public void receiveShipment(String code, double quantity) {
        InventoryItem item = this.find(code);
        item.receive(quantity);
    }

    public boolean issueIngredient(String code, double quantity) {
        InventoryItem item = this.find(code);
        item.issue(quantity);
        item.logUsage(quantity);
        double avg = item.averageUsage();
        return quantity > avg && avg > 0;
    }

    public ArrayList<InventoryItem> list() {
        return new ArrayList<>(this.items.values());
    }

    private InventoryItem find(String code) {
        Optional<InventoryItem> item = Optional.ofNullable(this.items.get(code));
        if (!item.isPresent())
            throw new IllegalArgumentException("Inventory item not found");
        return item.get();
    }
}

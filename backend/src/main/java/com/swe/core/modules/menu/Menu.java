package com.swe.core.modules.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import com.swe.core.utils.database.DBConnect;

public class Menu {
    private ArrayList<MenuItem> items;

    public Menu(MenuItem[] items) {
        this.items = new ArrayList<>(Arrays.asList(items));
    }

    public Menu(DBConnect db) {
        this.items = new ArrayList<>();
    }

    public void addItem(MenuItem item) {
        Optional<MenuItem> existing = this.items
            .stream()
            .filter(i -> i.code.equals(item.code))
            .findFirst();

        if (existing.isPresent())
            throw new IllegalArgumentException("Duplicate item code");

        this.items.add(item);
    }

    public void updatePrice(String code, double newPrice) {
        MenuItem item = this.findActive(code);
        item.updatePrice(newPrice);
    }

    public ArrayList<MenuItem> viewMenu() {
        return new ArrayList<>(this.items
            .stream()
            .filter(i -> i.active)
            .collect(Collectors.toList()));
    }

    public void deleteItem(String code) {
        MenuItem item = this.findActive(code);
        item.deactivate();
    }

    private MenuItem findActive(String code) {
        Optional<MenuItem> item = this.items
            .stream()
            .filter(i -> i.code.equals(code) && i.active)
            .findFirst();

        if (!item.isPresent())
            throw new IllegalArgumentException("Active item not found");

        return item.get();
    }
}
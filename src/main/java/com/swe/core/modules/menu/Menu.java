package com.swe.core.modules.menu;

import java.util.ArrayList;
import java.util.Arrays;

import com.swe.core.utils.database.DBConnect;

public class Menu {
    private ArrayList<MenuItem> items;

    public Menu(MenuItem[] items) {
        this.items = new ArrayList<>(Arrays.asList(items));
    }

    public Menu(DBConnect db) {
        // unimplemented
    }
}
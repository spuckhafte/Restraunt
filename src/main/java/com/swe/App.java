package com.swe;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.swe.core.ui.RestaurantUI;

public class App {
    public static void main( String[] args ) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        RestaurantUI ui = new RestaurantUI(reader);
        ui.run();
    }
}

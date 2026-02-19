package com.swe.core.modules.menu;

import java.time.Instant;

public class MenuItem {
    String code;
    String name;
    double basePrice;
    TaxGroup taxGroup;
    Instant lastUpdate;
}

enum TaxGroup {
    VAT,
    ServiceCharge
}
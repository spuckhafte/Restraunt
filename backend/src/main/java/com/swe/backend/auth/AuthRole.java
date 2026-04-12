package com.swe.backend.auth;

public enum AuthRole {
    MANAGER,
    SALES,
    INVENTORY;

    public static AuthRole fromDb(String rawRole) {
        return AuthRole.valueOf(rawRole.trim().toUpperCase());
    }
}

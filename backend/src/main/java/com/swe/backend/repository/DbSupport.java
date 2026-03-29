package com.swe.backend.repository;

import java.sql.Connection;

import com.swe.core.utils.database.DBConnect;

abstract class DbSupport {
    protected Connection connection() {
        try {
            return DBConnect.getInstance().conn;
        } catch (Exception ex) {
            throw new IllegalStateException("Database connection is not available", ex);
        }
    }
}

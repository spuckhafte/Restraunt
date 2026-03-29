package com.swe.backend.config;

import java.sql.Connection;
import java.sql.Statement;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.swe.core.utils.database.DBConnect;

@Component
public class SchemaInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Connection conn = DBConnect.getInstance().conn;

        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS menu_items (
                    code VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    base_price DOUBLE NOT NULL,
                    active BOOLEAN NOT NULL DEFAULT TRUE,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS inventory_items (
                    code VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    unit VARCHAR(50) NOT NULL,
                    quantity_on_hand DOUBLE NOT NULL,
                    reorder_threshold DOUBLE NOT NULL DEFAULT 0,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS inventory_usage (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    item_code VARCHAR(50) NOT NULL,
                    quantity DOUBLE NOT NULL,
                    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (item_code) REFERENCES inventory_items(code)
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS bills (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    subtotal DOUBLE NOT NULL,
                    voided BOOLEAN NOT NULL DEFAULT FALSE,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS bill_lines (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    bill_id BIGINT NOT NULL,
                    item_code VARCHAR(50) NOT NULL,
                    item_name VARCHAR(255) NOT NULL,
                    unit_price DOUBLE NOT NULL,
                    quantity INT NOT NULL,
                    line_total DOUBLE NOT NULL,
                    FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE
                )
                """);
        }
    }
}

package com.swe.backend.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.swe.core.utils.database.DBConnect;

@Component
public class SchemaInitializer implements CommandLineRunner {
    private static final String USER_SESSIONS_EFFECTIVE_ROLE = "effective_role";

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

            st.execute("""
                CREATE TABLE IF NOT EXISTS app_users (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(100) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(30) NOT NULL,
                    active BOOLEAN NOT NULL DEFAULT TRUE,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS user_sessions (
                    token VARCHAR(128) PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    effective_role VARCHAR(30) NULL,
                    active BOOLEAN NOT NULL DEFAULT TRUE,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS supplier_invoices (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    supplier_name VARCHAR(255) NOT NULL,
                    item_code VARCHAR(50) NOT NULL,
                    quantity DOUBLE NOT NULL,
                    unit_price DOUBLE NOT NULL,
                    total_amount DOUBLE NOT NULL,
                    invoice_date DATE NOT NULL,
                    approved BOOLEAN NOT NULL DEFAULT TRUE,
                    paid BOOLEAN NOT NULL DEFAULT FALSE,
                    flagged_for_review BOOLEAN NOT NULL DEFAULT FALSE,
                    created_by_user_id BIGINT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (item_code) REFERENCES inventory_items(code),
                    FOREIGN KEY (created_by_user_id) REFERENCES app_users(id)
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS supplier_checks (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    invoice_id BIGINT NOT NULL UNIQUE,
                    check_number VARCHAR(50) NOT NULL UNIQUE,
                    amount DOUBLE NOT NULL,
                    pdf_data LONGBLOB NOT NULL,
                    generated_by_user_id BIGINT NULL,
                    printed BOOLEAN NOT NULL DEFAULT TRUE,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (invoice_id) REFERENCES supplier_invoices(id),
                    FOREIGN KEY (generated_by_user_id) REFERENCES app_users(id)
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS cash_ledger (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    entry_type VARCHAR(20) NOT NULL,
                    amount DOUBLE NOT NULL,
                    reference_type VARCHAR(50) NULL,
                    reference_id BIGINT NULL,
                    note VARCHAR(255) NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS manager_override_audit (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    manager_user_id BIGINT NOT NULL,
                    token VARCHAR(128) NOT NULL,
                    from_role VARCHAR(30) NOT NULL,
                    to_role VARCHAR(30) NOT NULL,
                    action_name VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (manager_user_id) REFERENCES app_users(id)
                )
                """);

            ensureUserSessionsEffectiveRoleColumn(conn, st);

            st.execute("""
                INSERT INTO app_users(username, password, role, active)
                VALUES
                    ('manager', 'manager123', 'MANAGER', TRUE),
                    ('sales', 'sales123', 'SALES', TRUE),
                    ('inventory', 'inventory123', 'INVENTORY', TRUE)
                ON DUPLICATE KEY UPDATE username = VALUES(username)
                """);

            st.execute("""
                INSERT INTO cash_ledger(entry_type, amount, reference_type, note)
                SELECT 'CREDIT', 50000, 'SYSTEM', 'Opening balance'
                WHERE NOT EXISTS (SELECT 1 FROM cash_ledger)
                """);
        }
    }

    private void ensureUserSessionsEffectiveRoleColumn(Connection conn, Statement st) throws Exception {
        String columnExistsSql = """
            SELECT COUNT(*) AS column_count
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'user_sessions'
              AND COLUMN_NAME = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(columnExistsSql)) {
            ps.setString(1, USER_SESSIONS_EFFECTIVE_ROLE);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt("column_count") == 0) {
                    st.execute("ALTER TABLE user_sessions ADD COLUMN effective_role VARCHAR(30) NULL");
                }
            }
        }
    }
}

package com.swe.backend.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.swe.backend.model.BillDto;
import com.swe.backend.model.SaleLineDto;

@Repository
public class SalesRepository extends DbSupport {
    public long createBill(double subtotal, List<SaleLineDto> lines) {
        Connection conn = connection();
        try {
            conn.setAutoCommit(false);

            long billId;
            String insertBillSql = "INSERT INTO bills(subtotal, voided) VALUES (?, FALSE)";
            try (PreparedStatement ps = conn.prepareStatement(insertBillSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDouble(1, subtotal);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new IllegalStateException("Failed to create bill");
                    }
                    billId = keys.getLong(1);
                }
            }

            String insertLineSql = "INSERT INTO bill_lines(bill_id, item_code, item_name, unit_price, quantity, line_total) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertLineSql)) {
                for (SaleLineDto line : lines) {
                    ps.setLong(1, billId);
                    ps.setString(2, line.itemCode());
                    ps.setString(3, line.itemName());
                    ps.setDouble(4, line.unitPrice());
                    ps.setInt(5, line.quantity());
                    ps.setDouble(6, line.lineTotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return billId;
        } catch (Exception ex) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            throw new IllegalStateException("Failed to create bill", ex);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public boolean voidBill(long billId) {
        String sql = "UPDATE bills SET voided = TRUE WHERE id = ? AND voided = FALSE";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to void bill", ex);
        }
    }

    public Optional<BillDto> findBill(long billId) {
        String billSql = "SELECT id, subtotal, created_at, voided FROM bills WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(billSql)) {
            ps.setLong(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                long id = rs.getLong("id");
                double subtotal = rs.getDouble("subtotal");
                Timestamp createdAtTs = rs.getTimestamp("created_at");
                Instant createdAt = createdAtTs == null ? Instant.now() : createdAtTs.toInstant();
                boolean voided = rs.getBoolean("voided");

                List<SaleLineDto> lines = findLines(id);
                return Optional.of(new BillDto(id, lines, subtotal, createdAt, voided));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load bill", ex);
        }
    }

    private List<SaleLineDto> findLines(long billId) throws SQLException {
        String lineSql = "SELECT item_code, item_name, unit_price, quantity, line_total FROM bill_lines WHERE bill_id = ? ORDER BY id";
        List<SaleLineDto> lines = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(lineSql)) {
            ps.setLong(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lines.add(new SaleLineDto(
                        rs.getString("item_code"),
                        rs.getString("item_name"),
                        rs.getDouble("unit_price"),
                        rs.getInt("quantity"),
                        rs.getDouble("line_total")
                    ));
                }
            }
        }
        return lines;
    }
}

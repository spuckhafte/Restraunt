package com.swe.backend.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.swe.backend.model.InventoryItemDto;
import com.swe.backend.model.IssueResultDto;

@Repository
public class InventoryRepository extends DbSupport {
    public List<InventoryItemDto> list() {
        String sql = "SELECT code, name, unit, quantity_on_hand, reorder_threshold FROM inventory_items ORDER BY code";
        List<InventoryItemDto> items = new ArrayList<>();

        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(map(rs));
            }
            return items;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load inventory", ex);
        }
    }

    public Optional<InventoryItemDto> findByCode(String code) {
        String sql = "SELECT code, name, unit, quantity_on_hand, reorder_threshold FROM inventory_items WHERE code = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load inventory item", ex);
        }
    }

    public void insert(InventoryItemDto item) {
        String sql = "INSERT INTO inventory_items(code, name, unit, quantity_on_hand, reorder_threshold) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, item.code());
            ps.setString(2, item.name());
            ps.setString(3, item.unit());
            ps.setDouble(4, item.quantityOnHand());
            ps.setDouble(5, item.reorderThreshold());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create inventory item", ex);
        }
    }

    public InventoryItemDto receive(String code, double quantity) {
        String updateSql = "UPDATE inventory_items SET quantity_on_hand = quantity_on_hand + ? WHERE code = ?";
        try (PreparedStatement ps = connection().prepareStatement(updateSql)) {
            ps.setDouble(1, quantity);
            ps.setString(2, code);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Inventory item not found");
            }
            return findByCode(code).orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to receive inventory", ex);
        }
    }

    public IssueResultDto issue(String code, double quantity) {
        Connection conn = connection();
        try {
            conn.setAutoCommit(false);

            InventoryItemDto item = findByCode(code).orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));
            if (quantity > item.quantityOnHand()) {
                throw new IllegalArgumentException("Insufficient stock");
            }

            double historicalAverage = averageUsageFromLastThree(code);
            boolean flagged = historicalAverage > 0 && quantity > historicalAverage;

            insertUsage(code, quantity);
            double recalculatedAverage = averageUsageFromLastThree(code);
            double newThreshold = recalculatedAverage * 2;

            String updateSql = "UPDATE inventory_items SET quantity_on_hand = quantity_on_hand - ?, reorder_threshold = ? WHERE code = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setDouble(1, quantity);
                ps.setDouble(2, newThreshold);
                ps.setString(3, code);
                ps.executeUpdate();
            }

            conn.commit();
            return new IssueResultDto(
                findByCode(code).orElseThrow(() -> new IllegalArgumentException("Inventory item not found")),
                flagged
            );
        } catch (Exception ex) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            if (ex instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) ex;
            }
            throw new IllegalStateException("Failed to issue inventory", ex);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    private void insertUsage(String code, double quantity) throws SQLException {
        String sql = "INSERT INTO inventory_usage(item_code, quantity) VALUES (?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setDouble(2, quantity);
            ps.executeUpdate();
        }
    }

    private double averageUsageFromLastThree(String code) throws SQLException {
        String sql = "SELECT COALESCE(AVG(quantity), 0) AS avg_qty FROM (SELECT quantity FROM inventory_usage WHERE item_code = ? ORDER BY used_at DESC LIMIT 3) recent";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_qty");
                }
                return 0;
            }
        }
    }

    private InventoryItemDto map(ResultSet rs) throws SQLException {
        return new InventoryItemDto(
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("unit"),
            rs.getDouble("quantity_on_hand"),
            rs.getDouble("reorder_threshold")
        );
    }
}

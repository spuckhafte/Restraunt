package com.swe.backend.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.swe.backend.model.MenuItemDto;

@Repository
public class MenuRepository extends DbSupport {
    
    // Returns all active menu items
    public List<MenuItemDto> findActive() {
        String sql = "SELECT code, name, base_price, active FROM menu_items WHERE active = TRUE ORDER BY code";
        List<MenuItemDto> items = new ArrayList<>();

        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(map(rs));
            }
            return items;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load menu", ex);
        }
    }

    public Optional<MenuItemDto> findActiveByCode(String code) {
        String sql = "SELECT code, name, base_price, active FROM menu_items WHERE code = ? AND active = TRUE";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load menu item", ex);
        }
    }

    public void insert(MenuItemDto item) {
        String sql = "INSERT INTO menu_items(code, name, base_price, active) VALUES (?, ?, ?, TRUE)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, item.code());
            ps.setString(2, item.name());
            ps.setDouble(3, item.basePrice());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create menu item", ex);
        }
    }

    // Updates base price for an active item
    public boolean updatePrice(String code, double newPrice) {
        String sql = "UPDATE menu_items SET base_price = ? WHERE code = ? AND active = TRUE";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setDouble(1, newPrice);
            ps.setString(2, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update item price", ex);
        }
    }

    // Soft-deletes an item by setting active=false
    public boolean deactivate(String code) {
        String sql = "UPDATE menu_items SET active = FALSE WHERE code = ? AND active = TRUE";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to deactivate menu item", ex);
        }
    }

    private MenuItemDto map(ResultSet rs) throws SQLException {
        return new MenuItemDto(
            rs.getString("code"),
            rs.getString("name"),
            rs.getDouble("base_price"),
            rs.getBoolean("active")
        );
    }
}

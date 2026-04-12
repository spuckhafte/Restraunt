package com.swe.backend.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.swe.backend.model.ExpenseSummaryDto;
import com.swe.backend.model.InventoryTrendDto;
import com.swe.backend.model.ItemPerformanceDto;
import com.swe.backend.model.LiquidityStatusDto;
import com.swe.backend.model.SalesSummaryDto;

@Repository
public class ReportsRepository extends DbSupport {
    public List<SalesSummaryDto> salesSummaries(LocalDate from, LocalDate to) {
        String sql = """
            SELECT DATE_FORMAT(created_at, '%Y-%m') AS month_label,
                   COUNT(*) AS bills_count,
                   COALESCE(SUM(subtotal), 0) AS total_sales
            FROM bills
            WHERE voided = FALSE
              AND (? IS NULL OR created_at >= ?)
              AND (? IS NULL OR created_at < DATE_ADD(?, INTERVAL 1 DAY))
            GROUP BY DATE_FORMAT(created_at, '%Y-%m')
            ORDER BY month_label DESC
            """;

        List<SalesSummaryDto> results = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            setDateRange(ps, from, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new SalesSummaryDto(
                        rs.getString("month_label"),
                        rs.getLong("bills_count"),
                        rs.getDouble("total_sales")
                    ));
                }
            }
            return results;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load sales summaries", ex);
        }
    }

    public List<ItemPerformanceDto> itemPerformance(LocalDate from, LocalDate to) {
        String sql = """
            SELECT bl.item_code,
                   bl.item_name,
                   COALESCE(SUM(bl.quantity), 0) AS qty_sold,
                   COALESCE(SUM(bl.line_total), 0) AS revenue
            FROM bill_lines bl
            JOIN bills b ON b.id = bl.bill_id
            WHERE b.voided = FALSE
              AND (? IS NULL OR b.created_at >= ?)
              AND (? IS NULL OR b.created_at < DATE_ADD(?, INTERVAL 1 DAY))
            GROUP BY bl.item_code, bl.item_name
            ORDER BY revenue DESC, qty_sold DESC
            """;

        List<ItemPerformanceDto> results = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            setDateRange(ps, from, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new ItemPerformanceDto(
                        rs.getString("item_code"),
                        rs.getString("item_name"),
                        rs.getLong("qty_sold"),
                        rs.getDouble("revenue")
                    ));
                }
            }
            return results;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load item performance", ex);
        }
    }

    public ExpenseSummaryDto expenses(LocalDate from, LocalDate to) {
        String sql = """
            SELECT COUNT(*) AS invoice_count,
                   COALESCE(SUM(total_amount), 0) AS total_expenses,
                   COALESCE(SUM(CASE WHEN paid = TRUE THEN total_amount ELSE 0 END), 0) AS paid_expenses,
                   COALESCE(SUM(CASE WHEN paid = FALSE THEN total_amount ELSE 0 END), 0) AS unpaid_expenses
            FROM supplier_invoices
            WHERE (? IS NULL OR invoice_date >= ?)
              AND (? IS NULL OR invoice_date <= ?)
            """;

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            setDateRange(ps, from, to);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return new ExpenseSummaryDto(0, 0, 0, 0);
                }

                return new ExpenseSummaryDto(
                    rs.getLong("invoice_count"),
                    rs.getDouble("total_expenses"),
                    rs.getDouble("paid_expenses"),
                    rs.getDouble("unpaid_expenses")
                );
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load expense summary", ex);
        }
    }

    public List<InventoryTrendDto> inventoryTrends() {
        String sql = """
            SELECT
                ii.code,
                ii.name,
                COALESCE((SELECT SUM(u.quantity)
                    FROM inventory_usage u
                    WHERE u.item_code = ii.code AND DATE(u.used_at) = CURRENT_DATE), 0) AS issued_today,
                COALESCE((SELECT AVG(recent.quantity)
                    FROM (
                        SELECT u2.quantity
                        FROM inventory_usage u2
                        WHERE u2.item_code = ii.code
                        ORDER BY u2.used_at DESC
                        LIMIT 3
                    ) recent
                ), 0) AS avg_last_three
            FROM inventory_items ii
            ORDER BY ii.code
            """;

        List<InventoryTrendDto> trends = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double issuedToday = rs.getDouble("issued_today");
                double avg = rs.getDouble("avg_last_three");
                boolean flagged = avg > 0 && issuedToday > avg;
                trends.add(new InventoryTrendDto(
                    rs.getString("code"),
                    rs.getString("name"),
                    issuedToday,
                    avg,
                    flagged
                ));
            }
            return trends;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load inventory trends", ex);
        }
    }

    public LiquidityStatusDto liquidity() {
        String sql = """
            SELECT
                COALESCE((SELECT SUM(CASE
                    WHEN entry_type = 'CREDIT' THEN amount
                    WHEN entry_type = 'DEBIT' THEN -amount
                    ELSE 0
                END) FROM cash_ledger), 0) AS cash_balance,
                COALESCE((SELECT COUNT(*) FROM supplier_checks), 0) AS checks_issued,
                COALESCE((SELECT SUM(amount) FROM supplier_checks), 0) AS total_check_payments
            """;

        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return new LiquidityStatusDto(0, 0, 0);
            }

            return new LiquidityStatusDto(
                rs.getDouble("cash_balance"),
                rs.getLong("checks_issued"),
                rs.getDouble("total_check_payments")
            );
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load liquidity report", ex);
        }
    }

    private void setDateRange(PreparedStatement ps, LocalDate from, LocalDate to) throws SQLException {
        if (from == null) {
            ps.setDate(1, null);
            ps.setDate(2, null);
        } else {
            java.sql.Date fromDate = java.sql.Date.valueOf(from);
            ps.setDate(1, fromDate);
            ps.setDate(2, fromDate);
        }

        if (to == null) {
            ps.setDate(3, null);
            ps.setDate(4, null);
        } else {
            java.sql.Date toDate = java.sql.Date.valueOf(to);
            ps.setDate(3, toDate);
            ps.setDate(4, toDate);
        }
    }
}

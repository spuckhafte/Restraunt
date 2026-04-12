package com.swe.backend.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.swe.backend.model.SupplierInvoiceDto;

@Repository
public class PaymentsRepository extends DbSupport {
    public Optional<SupplierInvoiceDto> findApprovedUnpaidInvoice(long invoiceId) {
        String sql = """
            SELECT id, supplier_name, item_code, quantity, unit_price, total_amount,
                   invoice_date, approved, paid, flagged_for_review
            FROM supplier_invoices
            WHERE id = ? AND approved = TRUE AND paid = FALSE
            """;

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new SupplierInvoiceDto(
                    rs.getLong("id"),
                    rs.getString("supplier_name"),
                    rs.getString("item_code"),
                    rs.getDouble("quantity"),
                    rs.getDouble("unit_price"),
                    rs.getDouble("total_amount"),
                    rs.getDate("invoice_date").toLocalDate(),
                    rs.getBoolean("approved"),
                    rs.getBoolean("paid"),
                    rs.getBoolean("flagged_for_review")
                ));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load invoice for payment", ex);
        }
    }

    public double currentCashBalance() {
        String sql = """
            SELECT COALESCE(SUM(CASE
                WHEN entry_type = 'CREDIT' THEN amount
                WHEN entry_type = 'DEBIT' THEN -amount
                ELSE 0
            END), 0) AS balance
            FROM cash_ledger
            """;
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("balance");
            }
            return 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read cash balance", ex);
        }
    }

    public PaymentPersistenceResult persistCheckPayment(
        SupplierInvoiceDto invoice,
        String checkNumber,
        byte[] pdfData,
        long generatedByUserId
    ) {
        Connection conn = connection();
        try {
            conn.setAutoCommit(false);

            String insertCheckSql = """
                INSERT INTO supplier_checks(invoice_id, check_number, amount, pdf_data, generated_by_user_id, printed)
                VALUES (?, ?, ?, ?, ?, TRUE)
                """;
            Timestamp createdAt;
            try (PreparedStatement ps = conn.prepareStatement(insertCheckSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, invoice.id());
                ps.setString(2, checkNumber);
                ps.setDouble(3, invoice.totalAmount());
                ps.setBytes(4, pdfData);
                if (generatedByUserId <= 0) {
                    ps.setNull(5, java.sql.Types.BIGINT);
                } else {
                    ps.setLong(5, generatedByUserId);
                }
                ps.executeUpdate();
            }

            String markPaidSql = "UPDATE supplier_invoices SET paid = TRUE WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(markPaidSql)) {
                ps.setLong(1, invoice.id());
                ps.executeUpdate();
            }

            String ledgerSql = """
                INSERT INTO cash_ledger(entry_type, amount, reference_type, reference_id, note)
                VALUES ('DEBIT', ?, 'SUPPLIER_CHECK', ?, ?)
                """;
            try (PreparedStatement ps = conn.prepareStatement(ledgerSql)) {
                ps.setDouble(1, invoice.totalAmount());
                ps.setLong(2, invoice.id());
                ps.setString(3, "Check " + checkNumber + " for invoice " + invoice.id());
                ps.executeUpdate();
            }

            String readCreatedAtSql = "SELECT created_at FROM supplier_checks WHERE invoice_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(readCreatedAtSql)) {
                ps.setLong(1, invoice.id());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        createdAt = rs.getTimestamp("created_at");
                    } else {
                        createdAt = Timestamp.from(Instant.now());
                    }
                }
            }

            conn.commit();
            return new PaymentPersistenceResult(
                checkNumber,
                createdAt == null ? Instant.now() : createdAt.toInstant(),
                currentCashBalance()
            );
        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            throw new IllegalStateException("Failed to persist check payment", ex);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public record PaymentPersistenceResult(
        String checkNumber,
        Instant createdAt,
        double cashBalanceAfterPayment
    ) {
    }
}
